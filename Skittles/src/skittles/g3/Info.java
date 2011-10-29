package skittles.g3;

import java.util.Arrays;
import java.util.Vector;

import skittles.sim.Offer;

public class Info {

	private int[][] profile;

	private Vector <Offer[]> offers;

	public final int colors;
	public final int players;

	private int myId;

	private Vector <int[][]> give;
	private Vector <int[][]> take;

	public Info(int players, int myId, int[] hand)
	{
		colors = hand.length;
		this.players = players;
		this.myId = myId;
		profile = new int [players][colors + 1];
		/* Total number of skittles per player */
		int skittles = sum(hand);
		/* Everyone else's profiles */
		for (int i = 0 ; i != players ; ++i) {
			for (int j = 0 ; j != colors ; ++j)
				profile[i][j] = 0;
			profile[i][colors] = skittles;
		}
		/* Your own hand */
		for (int i = 0 ; i != players ; ++i)
			profile[myId][i] = hand[i];
		profile[myId][colors] = 0;
		/* Initialize love and hate */
		give = new Vector <int[][]> ();
		take = new Vector <int[][]> ();
	}

	public void updateOffers(Offer[] offers)
	{
		/* Update the profiles */
		for (Offer o : offers) {
			refineProfile(o.getOfferedByIndex(), o.getOffer());
			if (!o.getOfferLive()) {
				refineProfile(o.getPickedByIndex(), o.getDesire());
				transferBetweenProfiles(o);
			}
		}
		/* Update the offer bookkeeping */
		Offer[] offersThisTurn = new Offer [players];
		for (Offer o : offers)
			offersThisTurn[o.getOfferedByIndex()] = copy(o);
		this.offers.add(offersThisTurn);
		/* Update the preference arrays */
		int[][] giveNow = new int [players][colors];
		int[][] takeNow = new int [players][colors];
		for (int i = 0 ; i != players ; ++i)
			for (int j = 0 ; j != colors ; ++j)
				giveNow[i][j] = takeNow[i][j] = 0;
		for (Offer o : offers) {
			int from = o.getOfferedByIndex();
			int[] offer = o.getOffer();
			int[] desire = o.getDesire();
			for (int i = 0 ; i != colors ; ++i) {
				giveNow[from][i] = offer[i];
				takeNow[from][i] = desire[i];
			}
			if (o.getOfferLive())
				continue;
			int to = o.getPickedByIndex();
			for (int i = 0 ; i != colors ; ++i) {
				giveNow[to][i] += desire[i];
				takeNow[to][i] += offer[i];
			}
		}
		give.add(giveNow);
		take.add(takeNow);
	}

	public double[] preferences(int player)
	{
		int giveSum[] = new int [colors];
		int takeSum[] = new int [colors];
		for (int i = 0 ; i != colors ; ++i)
			giveSum[i] = takeSum[i] = 0;
		for (int turn = 0 ; turn != give.size() ; ++turn) {
			int[] giveNow = give.get(turn)[player];
			int[] takeNow = give.get(turn)[player];
			for (int i = 0 ; i != colors ; ++i) {
				giveSum[i] += giveNow[i];
				takeSum[i] += takeNow[i];
			}
		}
		double[] pref = new double [colors];
		double giveMax = (double) max(giveSum);
		double takeMax = (double) max(takeSum);
		for (int i = 0 ; i != colors ; ++i)
			pref[i] = giveSum[i] / giveMax - takeSum[i] / takeMax;
		return pref;
	}

	public static int[] rank(int[] value)
	{
		double[] dvalue = new double [value.length];
		for (int i = 0 ; i != value.length ; ++i)
			dvalue[i] = value[i];
		return rank(dvalue);
	}

	public static int[] rank(double[] value)
	{
		int colors = value.length;
		int[] index = new int [colors];
		for (int i = 0 ; i != colors ; ++i)
			index[i] = i;
		for (int i = 0 ; i != colors ; ++i) {
			int max = i;
			for (int j = i + 1 ; j != colors ; ++j)
				if (value[index[j]] > value[index[max]])
					max = j;
			swap(index, max, i);
		}
		return index;
	}

	public Offer getOffer(int turn, int player)
	{
		return copy(offers.get(turn)[player]);
	}

	public int[] hand()
	{
		return copy(profile[myId], colors);
	}

	public int[] profile(int player)
	{
		return copy(profile[player], colors);
	}

	public void updateEaten(int color, int howMany)
	{
		profile[myId][color] -= howMany;
	}

	public int turns()
	{
		return offers.size();
	}

	/* Helper function */

	/* Update the profile by offer */
	private void refineProfile(int id, int[] vector)
	{
		for (int i = 0 ; i != colors ; ++i)
			if (vector[i] > profile[id][i]) {
				profile[id][colors] -= vector[i] - profile[id][i];
				profile[id][i] = vector[i];
			}
	}

	/* Tranfer skittles */
	private void transferBetweenProfiles(Offer o)
	{
		int from = o.getOfferedByIndex();
		int[] offered = o.getOffer();
		int to = o.getPickedByIndex();
		int[] desire = o.getDesire();
		for (int i = 0 ; i != colors ; ++i) {
			profile[from][i] += offered[i] - desire[i];
			profile[to][i] += desire[i] - offered[i];
		}
	}

	/* Get the max value */
	private static int max(int ... v)
	{
		int max = 0;
		for (int i = 1 ; i != v.length ; ++i)
			if (v[i] > v[max])
				max  = i;
		return v[max];
	}

	/* Sum of a list of integers */
	private static int sum(int ... v)
	{
		int sum = 0;
		for (int i = 0 ; i != v.length ; ++i)
			sum += v[i];
		return sum;
	}

	/* Copy the offer object */
	private static Offer copy(Offer o)
	{
		int colors = o.getOffer().length;
		Offer copy = new Offer(o.getOfferedByIndex(), colors);
		copy.setOffer(copy(o.getOffer()), copy(o.getDesire()));
		if (!o.getOfferLive())
			copy.setPickedByIndex(o.getPickedByIndex());
		return copy;
	}

	/* Copy a part array */
	private static int[] copy(int[] arr, int len)
	{
		return Arrays.copyOf(arr, len);
	}

	/* Copy the whole array */
	private static int[] copy(int[] arr)
	{
		return Arrays.copyOf(arr, arr.length);
	}

	/* Swap elements of array */
	private static void swap(int[] a, int i, int j)
	{
		int t = a[i];
		a[i] = a[j];
		a[j] = t;
	}
}
