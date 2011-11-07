package skittles.g3;

import java.util.Arrays;
import java.util.Vector;

import skittles.sim.Offer;

public class Info {

	private int[][] profile;

	private Vector <Offer[]> offers;

	public final int colors;
	public final int players;
	public final int myId;

	private Vector <int[][]> give;
	private Vector <int[][]> take;

	private double[] taste;
	private boolean[] tasted;

	public Info(int players, int myId, int[] hand)
	{
		colors = hand.length;
		this.players = players;
		this.myId = myId;
		profile = new int [players][colors + 1];
		offers = new Vector <Offer[]> ();
		/* Total number of skittles per player */
		int skittles = sum(hand);
		/* Everyone else's profiles */
		for (int i = 0 ; i != players ; ++i) {
			for (int j = 0 ; j != colors ; ++j)
				profile[i][j] = 0;
			profile[i][colors] = skittles;
		}
		/* Your own hand */
		for (int i = 0 ; i != colors ; ++i)
			profile[myId][i] = hand[i];
		profile[myId][colors] = 0;
		/* Initialize love and hate */
		give = new Vector <int[][]> ();
		take = new Vector <int[][]> ();
		/* Initialize taste and tasted */
		taste = new double [colors];
		tasted = new boolean [colors];
		for (int i = 0 ; i != colors ; ++i)
			tasted[i] = false;
//		System.out.println("Hand of " + myId + ": " + Arrays.toString(hand));
	}

	public void updateOffers(Offer[] offers)
	{
		/* Update the profiles */
		for (Offer offer : offers) {
			int from = offer.getOfferedByIndex();
			int to = offer.getPickedByIndex();
//			System.out.println(from + " --> " + to);
			if (!offer.getOfferLive() && from != to && to >= 0 && to < players) {
				refineProfile(from, offer.getOffer());
//				System.out.println("Transfered!");
				refineProfile(to, offer.getDesire());
				transferBetweenProfiles(offer);
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
			int to = o.getPickedByIndex();
			if (!o.getOfferLive() && to != from && to >= 0 && to < players)
				for (int i = 0 ; i != colors ; ++i) {
					giveNow[to][i] += desire[i];
					takeNow[to][i] += offer[i];
				}
		}
		give.add(giveNow);
		take.add(takeNow);
//		System.out.println("Profiles by " + myId);
//		for (int i = 0 ; i != players ; ++i)
//			System.out.println("Profile " + i + ": " + Arrays.toString(profile[i]) + " " + (i == myId ? "[me]" : ""));
	}

	public double[] preferences(int player)
	{
		int giveSum[] = new int [colors];
		int takeSum[] = new int [colors];
		for (int i = 0 ; i != colors ; ++i)
			giveSum[i] = takeSum[i] = 0;
		int turns = give.size();
		for (int turn = 0 ; turn != turns ; ++turn) {
			int[] giveNow = give.get(turn)[player];
			int[] takeNow = take.get(turn)[player];
			for (int i = 0 ; i != colors ; ++i) {
				giveSum[i] += giveNow[i];
				takeSum[i] += takeNow[i];
			}
		}
		double[] pref = new double [colors];
		double giveMax = turns > 0 ? (double) max(giveSum) : 1.0;
		double takeMax = turns > 0 ? (double) max(takeSum) : 1.0;
		for (int i = 0 ; i != colors ; ++i)
			pref[i] = giveSum[i] / giveMax - takeSum[i] / takeMax;
//		System.out.println("Preferences of " + player + ": " + Arrays.toString(pref));
		return pref;
	}

	public static int[] rank(int[] value)
	{
		double[] dvalue = new double [value.length];
		for (int i = 0 ; i != value.length ; ++i)
			dvalue[i] = value[i];
		return rank(dvalue);
	}

	public static int[] rank(double[] value){
		int[] index = index(value);
		int[] rank = new int [index.length];

		for (int i = 0 ; i != index.length ; ++i)
			rank[index[i]] = i;
		return rank;
	}
	public static int[] index(double[] value)
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

	public boolean canTrade(int[] wanted, int player)
	{
		int unknown = 0;
		int[] hand = profile[player];
		for (int i = 0 ; i != colors ; ++i)
			if (wanted[i] > hand[i])
				unknown += wanted[i] - hand[i];
		return unknown <= profile[player][colors];
	}

	public Offer getOffer(int turn, int player)
	{
		Offer offer = offers.get(turn)[player];
		return offer == null ? null : copy(offer);
	}

	public int[] hand()
	{
		return copy(profile[myId], colors);
	}

	public int[] profile(int player)
	{
		return copy(profile[player], colors);
	}

	public void updateEaten(int color, int howMany, double happyChange)
	{
		/* Update your hand */
		profile[myId][color] -= howMany;
		/* Update tastes array */
		if (!tasted[color]) {
			/* If negative it is linear */
			if (happyChange < 0.0)
				taste[color] = happyChange / howMany;
			/* If positive it is squared */
			else
				taste[color] = happyChange / (howMany * howMany);
			tasted[color] = true;
		}
//		System.out.print("Taste: [" + (!tasted[0] ? "?" : taste[0]));
//		for (int i = 1 ; i != colors ; ++i)
//			System.out.print(", " + (!tasted[i] ? "?" : taste[i]));
//		System.out.println("]");
	}

	public int turns()
	{
		return offers.size();
	}

	public boolean tasted(int color)
	{
		return tasted[color];
	}

	public double taste(int color)
	{
		if(color < 0) return 0;
		return !tasted[color] ? 0.0 : taste[color];
	}

	/* Helper function */

	/* Update the profile by offer */
	private void refineProfile(int id, int[] vector)
	{
//		System.out.println("Profile: " + Arrays.toString(profile[id]));
//		System.out.println("Vector: " + Arrays.toString(vector));
		for (int i = 0 ; i != colors ; ++i)
			if (vector[i] > profile[id][i]) {
				profile[id][colors] -= vector[i] - profile[id][i];
				profile[id][i] = vector[i];
			}
//		System.out.println("Profile: " + Arrays.toString(profile[id]));
	}

	/* Tranfer skittles */
	private void transferBetweenProfiles(Offer offer)
	{
		int from = offer.getOfferedByIndex();
		int[] offered = offer.getOffer();
		int to = offer.getPickedByIndex();
		int[] desire = offer.getDesire();
		for (int i = 0 ; i != colors ; ++i) {
			profile[from][i] += desire[i] - offered[i];
			profile[to][i] += offered[i] - desire[i];
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
	public static int sum(int ... v)
	{
		int sum = 0;
		for (int i = 0 ; i != v.length ; ++i)
			sum += v[i];
		return sum;
	}

	/* Copy the offer and override who sent */
	private static class CopyOffer extends Offer {

		public CopyOffer(int from, int colors)
		{
			super(from, colors);
		}

		public void setPickedByIndex(int id)
		{
			super.setPickedByIndex(id);
		}
	}

	/* Copy the offer object */
	private static Offer copy(Offer offer)
	{
		int colors = offer.getOffer().length;
		CopyOffer copy = new CopyOffer(offer.getOfferedByIndex(), colors);
		copy.setOffer(copy(offer.getOffer()), copy(offer.getDesire()));
		copy.setPickedByIndex(offer.getPickedByIndex());
		return copy;
	}

	/* Copy a part array */
	private static int[] copy(int[] arr, int len)
	{
		return Arrays.copyOf(arr, len);
	}

	/* Copy the whole array */
	public static int[] copy(int[] arr)
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
