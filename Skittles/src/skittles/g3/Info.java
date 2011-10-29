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
	}

	public void updateOffers(Offer[] offers)
	{
		/* Update the profiles */
		for (Offer o : offers) {
			mustHave(o.getOfferedByIndex(), o.getOffer());
			if (!o.getOfferLive()) {
				mustHave(o.getPickedByIndex(), o.getDesire());
				transfer(o);
			}
		}
		Offer[] offersThisTurn = new Offer [players];
		for (Offer o : offers)
			offersThisTurn[o.getOfferedByIndex()] = copy(o);
		this.offers.add(offersThisTurn);
	}

	public Offer getOffer(int turn, int player)
	{
		return copy(offers.get(turn)[player]);
	}

	public int[] profile(int player)
	{
		return copy(profile[player], colors);
	}

	public void eaten(int color, int howMany)
	{
		profile[myId][color] -= howMany;
	}

	public int turns()
	{
		return offers.size();
	}

	/* Helper function */

	/* Update the profile by offer */
	private void mustHave(int id, int[] vector)
	{
		for (int i = 0 ; i != colors ; ++i)
			if (vector[i] > profile[id][i]) {
				profile[id][colors] -= vector[i] - profile[id][i];
				profile[id][i] = vector[i];
			}
	}

	/* Tranfer skittles */
	private void transfer(Offer o)
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
		return Arrays.copyOf(arr, len)
	}

	/* Copy the whole array */
	private static int[] copy(int[] arr)
	{
		return Arrays.copyOf(arr, arr.length);
	}
}
