package skittles.g3;

public class Evaluation {

	private Info info;

	public Evaluation(Info info)
	{
		this.info = info;
	}

	/* Evaluate an offer deciding how
	 * useful it is for other players
	 * How you evaluate an offer is by
	 * using the ranking based on
	 * preferences of each player
	 * You square the absolute value
	 * of each color even if is negative
	 * and they are assigned values
	 * between -1 and 1
	 */
	public double marketEvaluation(int[] toTake, int[] toGive)
	{
		/* The first one is 1.0 the last one is -1.0
		 * For 5 colors this array would be
		 * [1, 0.5, 0, -0.5, -1]
		 */
		double[] value = new double [info.colors];
		for (int i = 0 ; i != info.colors ; ++i)
			value[i] = 1.0 - (i / (double) (info.colors - 1)) * 2.0;
		/* Go through every player */
		double res = 0.0;
		for (int player = 0 ; player != info.players ; ++player) {
			/* Skip yourself */
			if (player == info.myId)
				continue;
			/* Check the guy has the stuff */
			if (!info.canTrade(toTake, player))
				continue;
			/* Get ranking of colors for that player */
			int[] rank = Info.rank(info.preferences(player));
			for (int color = 0 ; color != info.colors ; ++color) {
				double val = value[rank[color]];
				/* If the color is beneficial to the player */
				if (val > 0.0)
					/* Taking it from him hurts him
					 * Giving it to him helps him
					 */
					res += (toGive[color] - toTake[color]) * val * val;
				/* If the color is useless to the player */
				else
					/* Taking it from him helps him
					 * Giving it to him hurts him
					 */
					res += (toTake[color] - toGive[color]) * val * val;
			}
		}
//		System.out.println("Market value: " + res);
		return res;
	}

	/* Evaluate an offer for yourself
	 * Gives high values if the colors
	 * that it gives bring higher
	 * happiness to self player
	 */
	public double selfEvaluation(int[] toTake, int[] toGive)
	{
		double val = 0.5 * selfGreedyEvaluation(toTake, toGive) +
		             0.5 * selfLinearEvaluation(toTake, toGive);
//		System.out.println("Self value: " + val);
		return val;
	}

	/* Evaluate an offer for yourself
	 * in a greedy manner
	 * If that offer
	 */
	public double selfGreedyEvaluation(int[] toTake, int[] toGive)
	{
		int[] hand = info.profile(info.myId);
		int[] newHand = new int [hand.length];
		for (int i = 0 ; i != info.colors ; ++i)
			newHand[i] = hand[i] + toTake[i] - toGive[i];
		return happiness(newHand) - happiness(hand);
	}

	/* Self value of current offer
	 * by taking into account only
	 * the value of each color
	 */
	public double selfLinearEvaluation(int[] toTake, int[] toGive)
	{
		double res = 0.0;
		for (int i = 0 ; i != info.colors ; ++i)
			if (info.tasted(i))
				res += info.taste(i) * (toTake[i] - toGive[i]);
		return res;
	}

	/* Happiness gained if consumed
	 * the whole hand at once
	 */
	public double happiness(int[] hand)
	{
		double res = 0.0;
		for (int i = 0 ; i != info.colors ; ++i)
			if (info.tasted(i))
				if (info.taste(i) > 0.0)
					res += info.taste(i) * hand[i] * hand[i];
				else
					res -= info.taste(i) * hand[i];
		return res;
	}
}
