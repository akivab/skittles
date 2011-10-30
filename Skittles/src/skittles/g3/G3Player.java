package skittles.g3;

import skittles.sim.Offer;

public class G3Player extends skittles.sim.Player 
{
	private Info info;

	private Generation generation;

	private Evaluation evaluation;

	private String name;

	private int timeout;

	private int colorEaten = 0;

	private int howManyEaten = 0;

	public void initialize(int players, int myId, String name, int[] hand)
	{
		info = new Info(players, myId, hand);
		this.name = name;
		timeout = 100;
		generation = new Generation(info);
		evaluation = new Evaluation(info);
	}

	/* Simple version of offer function
	 * Offer as many as you can from
	 * the worst of your colors and
	 * ask for the best of your colors
	 */
	public void offer(Offer bestOffer)
	{
		double bestValue = Double.MIN_VALUE;
		long startingTime = System.currentTimeMillis();
		do {
			Offer generatedOffer = generation.generateOffer();
			int[] toTake = generatedOffer.getDesire();
			int[] toGive = generatedOffer.getOffer();
			double marketValue = evaluation.marketEvaluation(toTake, toGive);
			double selfValue = evaluation.selfEvaluation(toTake, toGive);
			double value = marketValue * selfValue;
			if (value > bestValue) {
				bestOffer.setOffer(toGive, toTake);
				bestValue = value;
			}
		} while (System.currentTimeMillis() - startingTime < timeout);
	}

	/* Pick the best out of available offers */
	public Offer pickOffer(Offer[] offers)
	{
		Offer bestOffer = null;
		double bestValue = Double.MIN_VALUE;
		for (Offer offer : offers) {
			int[] toTake = offer.getOffer();
			int[] toGive = offer.getDesire();
			double marketValue = evaluation.marketEvaluation(toTake, toGive);
			double selfValue = evaluation.selfEvaluation(toTake, toGive);
			double value = marketValue * selfValue;
			if (value > bestValue) {
				bestOffer = offer;
				bestValue = value;
			}
		}
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
	}

	/* Deside what to eat and set
	 * the class variables
	 */
	private void eat()
	{
		/* Try something you haven't tasted */
		for (int i = 0 ; i != info.colors ; ++i)
			if (!info.tasted(i)) {
				colorEaten = i;
				howManyEaten = 1;
				return;
			}
		/* Find worst taste */
		int[] hand = info.hand();
		colorEaten = -1;
		for (int i = 0 ; i != info.colors ; ++i)
			if (info.tasted(i) && hand[i] != 0)
				if (colorEaten < 0 || info.taste(i) < info.taste(colorEaten))
					colorEaten = i;
		howManyEaten = 1;
		/* If worst taste is positive eat all */
		if (info.taste(colorEaten) > 0.0)
			howManyEaten = hand[colorEaten];
	}

	public String getClassName() 
	{
		return name;
	}

	public int getPlayerIndex() 
	{
		return info.myId;
	}

	public void syncInHand(int[] hand) {}

	public void offerExecuted(Offer offPicked) {}
}
