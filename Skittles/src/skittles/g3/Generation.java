package skittles.g3;

import java.util.Arrays;
import java.util.Random;

import skittles.sim.Offer;

public class Generation {

	private Info info;

	private Random random;

	private int previousTurns;

	private int medianOfferSize;

	private int inHand;

	public Generation(Info info, Random random)
	{
		this.info = info;
		this.random = random;
		this.previousTurns = -1;
	}

	/* This is the only function of this class
	 * Generate a new offer and return it
	 */
	public Offer generateOffer()
	{
		/* Re-initialize generator */
		if (info.turns() != previousTurns) {
			previousTurns = info.turns();
			inHand = sum(info.hand());
			if (inHand != 0)
				medianOfferSize = medianOfferSize();
		}
		if (inHand == 0)
			return new Offer(info.myId, info.colors);
		int[] toGive, toTake;
		do {
			/* Find out the offer size using */
			int offerSize = generateOfferSize();
			/* Create the desire array */
			toTake = generateOfferTake(offerSize);
			toGive = generateOfferGive(toTake);
		} while (toGive == null);
		/* Create the offer and return it */
		Offer offer = new Offer(info.myId, info.colors);
		offer.setOffer(toGive, toTake);
		return offer;
	}

	/* Get a randomly chosen desire
	 * array for the next offer
	 * Every turn in order to pick a new
	 * color to add to the offer you
	 * use an array of weights for each
	 * color which starts in 1/n
	 * Every turn a color is picked you
	 * add 0.5 at its value so if you had
	 * 4 colors at start you can pick any
	 * of the three with prob = 25%
	 * but after you pick one you have
	 * [0.25, 0.25, 0.25, 0.75] and now
	 * it is 1/2 to re-pick the same
	 * and 1/6 to pick any of the other 3
	 */
	private int[] generateOfferTake(int size)
	{
		/* Initialize the desire array */
		int[] toTake = new int [info.colors];
		for (int i = 0 ; i != toTake.length ; ++i)
			toTake[i] = 0;
		/* Initialize the weights of picking a desire */
		double[] weight = new double [info.colors];
		for (int i = 0 ; i != info.colors ; ++i)
			weight[i] = 1.0 / info.colors;
		/* Start the procedure of offer creation */
		for (int i = 0 ; i != size ; ++i) {
			/* Pick a color according to the weights */
			int picked = randomPickUsingWeights(weight);
			/* Update the weights */
			weight[picked] += 0.5;
			/* Update the result */
			toTake[picked]++;
		}
		return toTake;
	}

	/* Get the matching offer for the randomly
	 * created desire given by above method
	 * Does not pick from colors that are
	 * in the desire array and always checks
	 * that the player has the colors in hand
	 */
	private int[] generateOfferGive(int[] toTake)
	{
		int[] hand = info.profile(info.myId);
		/* Initialize the ask array */
		int[] toGive = new int [info.colors];
		for (int i = 0 ; i != info.colors ; ++i)
			toGive[i] = 0;
		/* Count colors that are not in the
		 * desire array and you still have
		 * skittles of them
		 */
		int nonUsed = 0;
		for (int i = 0 ; i != info.colors ; ++i)
			if (hand[i] > 0 && toTake[i] == 0)
				nonUsed++;
		/* Create the initial array of weights */
		double[] weight = new double [info.colors];
		for (int i = 0 ; i != info.colors ; ++i)
			if (hand[i] > 0 && toTake[i] == 0)
				weight[i] = 1.0 / nonUsed;
			else
				weight[i] = 0.0;
		/* Fill out the offer array and each time
		 * you pick a color you make it more
		 * probable that it will be chosen again
		 */
		int desireSize = sum(toTake);
		for (int i = 0 ; i != desireSize ; ++i) {
			int picked = randomPickUsingWeights(weight);
			if (picked == -1)
				return null;
			/* If you have no more in hand */
			if (++toGive[picked] == hand[picked])
				/* This color cannot be picked
				 * any more for this offer
				 */
				weight[picked] = 0.0;
			/* If you have more of the color */
			else
				/* Make it probable it will re-appear */
				weight[picked] += 0.5;
		}
		return toGive;
	}

	/* Get median of offer sizes so far
	 * Returns 1 if no offers made
	 */
	private int medianOfferSize()
	{
		int[] offerSizes = new int [info.turns() * info.players];
		int offerCount = 0;
		for (int turn = 0 ; turn != info.turns() ; ++turn)
			for (int player = 0 ; player != info.players ; ++player) {
				Offer offer = info.getOffer(turn, player);
				if (offer != null)
					offerSizes[offerCount++] = offer.getOffer().length;
			}
		/* Sort offer sizes and get median */
		Arrays.sort(offerSizes, 0, offerCount);
		return offerCount == 0 ? 1 : offerSizes[offerCount / 2];
	}

	/* Get an offer size around the median
	 * set size using a Gaussian distribution
	 * with a standard deviation of 3
	 */
	private int generateOfferSize()
	{
		int size;
		int medianUsed = medianOfferSize;
		if (medianUsed > inHand)
			medianUsed = inHand;
		do {
			size = (int) (random.nextDouble() * 3.0 + medianUsed);
		} while (size <= 0 || size > inHand);
		return size;
	}
	

	/* Pick one index in the array using
	 * the values of the array
	 * The values do not need to be normalized
	 * For example if the array is [0.3, 0.2, 0.5] then
	 * it's 30% to pick 0, 20% to pick 1, 50% to pick 2
	 * You return the index of the one picked
	 * If someone has 0.0 value he cannot be picked
	 * If everyone is 0.0 returns -1
	 */
	private int randomPickUsingWeights(double[] weight)
	{
		double value = random.nextDouble() * sum(weight);
		for (int i = 0 ; i != info.colors ; ++i) {
			value -= weight[i];
			if (value < 0.0)
				return i;
		}
		return -1;
	}

	/* Return the sum of all doubles */
	private static double sum(double ... vs)
	{
		double sum = 0.0;
		for (double v : vs)
			sum += v;
		return sum;
	}

	/* Sum integers */
	private static int sum(int ... vs)
	{
		int sum = 0;
		for (int v : vs)
			sum += v;
		return sum;
	}
}
