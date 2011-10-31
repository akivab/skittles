package skittles.g3;

//import java.util.Arrays;

import java.util.Random;

import skittles.sim.Offer;

public class G3Player extends skittles.sim.Player 
{
	/* Information and ranking of
	 * profiles and preferences
	 */
	private Info info;

	/* Offer generation class */
	private Generation generation;

	/* Offer evaluation class */
	private Evaluation evaluation;

	/* Name of the player */
	private String name;

	/* Random generator
	 * Used to pick from
	 * color not tasted yet
	 */
	private Random random;

	/* Timeout for offer generation */
	private int timeout;

	/* Stop offer generation after
	 * generating this number of
	 * different offers
	 */
	private int maxGenerations;

	/* Color last eaten */
	private int colorEaten;

	/* How many of the color last eaten */
	private int howManyEaten;

	/* Initialize stuff */
	public void initialize(int players, int myId, String name, int[] hand)
	{
		info = new Info(players, myId, hand);
		this.name = name;
		timeout = 100;
		maxGenerations = 100;
		colorEaten = -1;
		howManyEaten = -1;
		random = new Random();
		generation = new Generation(info, random);
		evaluation = new Evaluation(info);
	}

	/* Generate offers randomly
	 * Then pick the one with
	 * the best evaluation
	 */
	public void offer(Offer returnedOffer)
	{
		Offer bestOffer = null;
		double bestValue = 0.0;
		long startingTime = System.currentTimeMillis();
		int generationCount = 0;
//		System.out.println("Hand: " + Arrays.toString(info.hand()));
		do {
			Offer generatedOffer = generation.generateOffer();
//			System.out.println("Generated offer take: " + Arrays.toString(generatedOffer.getDesire()));
//			System.out.println("Generated offer give: " + Arrays.toString(generatedOffer.getOffer()));
			int[] toTake = generatedOffer.getDesire();
			int[] toGive = generatedOffer.getOffer();
			double marketValue = evaluation.marketEvaluation(toTake, toGive);
			double selfValue = evaluation.selfEvaluation(toTake, toGive);
			double value = marketValue * selfValue;
			if (bestOffer == null || value > bestValue) {
				bestOffer = generatedOffer;
				bestValue = value;
			}
		} while (++generationCount != maxGenerations &&
		         System.currentTimeMillis() - startingTime < timeout);
//		System.out.println("Best: " + bestOffer);
		returnedOffer.setOffer(bestOffer.getOffer(), bestOffer.getDesire());
//		System.out.println("Returned: " + returnedOffer);
	}

	/* Pick the best out of available offers */
	public Offer pickOffer(Offer[] offers)
	{
		Offer bestOffer = null;
		double bestValue = 0.0;
		for (Offer offer : offers) {
			int from = offer.getOfferedByIndex();
			if (from == info.myId)
				continue;
			if (!offer.getOfferLive())
				continue;
			int[] toGive = offer.getDesire();
			if (!info.canTrade(toGive, info.myId))
				continue;
			int[] toTake = offer.getOffer();
			double marketValue = evaluation.marketEvaluation(toTake, toGive);
			double selfValue = evaluation.selfEvaluation(toTake, toGive);
			double value = marketValue * selfValue;
			if (bestOffer == null || value > bestValue) {
				bestOffer = offer;
				bestValue = value;
			}
		}
//		System.out.println("Picked: " + bestOffer);
		return bestOffer;
	}

	/* Update information
	 * Also update about offers
	 * that you accepted or "sold"
	 */
	public void updateOfferExe(Offer[] offers)
	{
		info.updateOffers(offers);
	}

	/* Update tastes */
	public void happier(double happyChange)
	{
		info.updateEaten(colorEaten, howManyEaten, happyChange);
	}

	/* Return what to eat */
	public void eat(int[] vector)
	{
		/* Set variables */
		eat();
		/* Return vector to the simulator */
		for (int i = 0 ; i != info.colors ; ++i)
			vector[i] = 0;
		vector[colorEaten] = howManyEaten;
//		System.out.println(info.myId + " eating " + howManyEaten + " of " + colorEaten);
	}

	/* Deside what to eat and set
	 * the class variables
	 */
	private void eat()
	{
		int[] hand = info.hand();
		/* Try something you haven't tasted */
		int nonTastedCount = 0;
		int[] nonTasted = new int [info.colors];
		for (int i = 0 ; i != info.colors ; ++i)
			if (!info.tasted(i) && hand[i] > 0)
				nonTasted[nonTastedCount++] = i;
		if (nonTastedCount != 0) {
			howManyEaten = 1;
			colorEaten = nonTasted[random.nextInt(nonTastedCount)];
			return;
		}
		/* Find worst taste */
		colorEaten = -1;
		for (int i = 0 ; i != info.colors ; ++i)
			if (info.tasted(i) && hand[i] > 0)
				if (colorEaten < 0 || info.taste(i) < info.taste(colorEaten))
					colorEaten = i;
		howManyEaten = 1;
		/* If worst taste is positive eat all */
		if (info.taste(colorEaten) > 0.0)
			howManyEaten = hand[colorEaten];
	}

	/* Return name of player */
	public String getClassName() 
	{
		return name;
	}

	/* Return id of player */
	public int getPlayerIndex() 
	{
		return info.myId;
	}

	/* Useless
	 * We sync hand during the
	 * broadcast of all offers
	 */
	public void syncInHand(int[] hand) {}

	/* Useless
	 * We see if our own offer
	 * was picked during
	 * broadcast of all offers
	 */
	public void offerExecuted(Offer offPicked) {}
}
