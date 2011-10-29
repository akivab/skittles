package skittles.g3;

import java.util.Vector;

import skittles.sim.Offer;

public class G3Player extends skittles.sim.Player 
{
	private int[] hand;
	private int colors;
	private int players;
	private String name;
	private int id;
	private boolean[] tasted;
	private double[] taste;
	private int last_eaten;
	private double real_factor = 0.0;

	private Vector <Offer[]> offers;

	public void initialize(int intPlayerIndex, String strClassName,	int[] aintInHand) 
	{
		id = intPlayerIndex;
		name = strClassName;
		hand = aintInHand;
		colors = aintInHand.length;
		taste = new double[colors];
		tasted = new boolean [colors];
		for (int i = 0 ; i != colors ; ++i) {
			taste[i] = 0.0;
			tasted[i] = false;
		}
		offers = new Vector <Offer[]> ();
		profiles = new int [players][colors + 1];
		for (int i = 0 ; i != players ; ++i)
	}

	/* What to eat */
	public void eat(int[] eat)
	{
		for (int i = 0 ; i != colors ; ++i)
			eat[i] = 0;
		for (int i = 0 ; i != colors ; ++i)
			if (!tasted[i]) {
				eat[i] = 1;
				return;
			}
		int color = last(order());
		eat[color] = !tasted[color] || taste[color] < 0 ? 1 : hand[color];
		last_eaten = color;
	}

	/* Get colors ordered by taste value
	 * Unknown are considered as 0
	 */
	private int[] order()
	{
		int[] res = new int [colors];
		for (int i = 0 ; i != colors ; ++i)
			res[i] = i;
		for (int i = 0 ; i != colors ; ++i) {
			int max = i;
			for (int j = i + 1 ; j != res.length ; ++j)
				if (exp_taste(j) > exp_taste(max))
					max = j;
			swap(res, i, max);
		}
		return res;
	}

	/* Get the expected taste of a color
	 * Zero for colors not tasted yet
	 */
	private double exp_taste(int c)
	{
		return tasted[c] ? taste[c] : 0.0;
	}

	/* Get preferences based on desire table
	 * More appearances mean more valuable
	 */
	private static int[] order(int[] hand)
	{
		int tasted_num = 0;
		for (int i = 0 ; i != hand.length ; ++i)
			if (hand[i] > 0)
				tasted_num++;
		int[] res = new int [tasted_num];
		for (int i = 0, j = 0 ; i != hand.length ; ++i)
			if (hand[i] > 0)
				res[j++] = i;
		for (int i = 0 ; i != res.length ; ++i) {
			int max = i;
			for (int j = i + 1 ; j != res.length ; ++j)
				if (hand[j] > hand[max])
					max = j;
			swap(res, i, max);
		}
		return res;
	}

	/* Median of all offer sizes
	 * Only takes into account
	 * accepted offers
	 */
	public int medianOfferSize()
	{
		int acc = 0;
		int turns = out.size();
		if (turns == 0)
			return 1;
		int[] sizes = new int [turns * players];
		for (int i = 0 ; i != turns ; ++i)
			for (int j = 0 ; j != players ; ++j)
				if (off_acc.get(i)[j])
					sizes[acc++] = off_out.get(i)[j].length;
		java.util.Arrays.sort(sizes, 0, acc);
		return sizes[acc / 2];
	}

	/* Simple version of offer function
	 * Offer as many as you can from
	 * the worst of your colors and
	 * ask for the best of your colors
	 */
	public void offer(Offer o)
	{
		int[] order = order();
		int best = order[0];
		int worst = last(order);
		int[] offer = new int [colors];
		int[] desire = new int [colors];
		for (int i = 0 ; i != colors ; ++i)
			offer[i] = desire[i] = 0;
		int size = min(hand[worst], medianOfferSize());
		offer[worst] = size;
		desire[best] = size;
		o.setOffer(offer, desire);
	}

	public Offer pickOffer(Offer[] offers) 
	{
		real_factor += 0.01;
		Offer best = null;
		double max_gain = 0.0;
		for (Offer o : offers) {
			if (o.getOfferedByIndex() == id || !o.getOfferLive() || !inHand(o.getDesire()))
				continue;
			int[] new_hand = add(hand, o.getOffer(), vectorNeg(o.getDesire()));
			double new_real = realHappiness(new_hand);
			double real = new_real - realHappiness(hand);
			double linear_out = linearHappiness(o.getDesire());
			double linear_in = linearHappiness(o.getOffer());
			double linear = linear_in - linear_out;
			double gain = real * real_factor + linear * (1.0 - real_factor);
			if (gain > 0.0)
				if (best == null || gain > max_gain) {
					best = o;
					max_gain = gain;
				}
		}
		return best;
	}

	/* Update hand */
	public void offerExecuted(Offer offPicked) 
	{
		int[] offer = offPicked.getOffer();
		int[] desire = offPicked.getDesire();
		for (int i = 0 ; i != colors ; ++i)
			hand[i] += desire[i] - offer[i];
	}

	public void updateOfferExe(Offer[] offers)
	{
		int[][] _in = new int [players][];
		int[][] _out = new int [players][];
		int[][] _off_out = new int [players][];
		int[][] _off_in = new int [players][];
		boolean[] _off_acc = new boolean [players];
		for (Offer o : offers) {
			int from = o.getOfferedByIndex();
			_off_out[from] = vectorAdd(o.getOffer());
			_off_in[from] = vectorAdd(o.getDesire());
			_off_acc[from] = !o.getOfferLive();
			if (!_off_acc[from]) {
				_out[from] = vectorAdd(_out[from], o.getOffer());
				_in[from] = vectorAdd(_in[from], o.getDesire());
				int to = o.getPickedByIndex();
				_out[to] = vectorAdd(_out[to], o.getDesire());
				_in[to] = vectorAdd(_in[to], o.getOffer());
			}
		}
		out.add(_out);
		in.add(_in);
		off_out.add(_off_out);
		off_in.add(_off_in);
	}

	private boolean inHand(int[] comb)
	{
		for (int i = 0 ; i != colors ; ++i)
			if (hand[i] < comb[i])
				return false;
		return true;
	}

	private double realHappiness(int[] hand)
	{
		double res = 0.0;
		for (int i = 0 ; i != colors ; ++i)
			if (tasted[i])
				if (taste[i] > 0.0)
					res += hand[i] * hand[i] * taste[i];
				else
					res -= hand[i] * taste[i];
		return res;
	}

	private double linearHappiness(int[] hand)
	{
		double res = 0.0;
		for (int i = 0 ; i != colors ; ++i)
			if (tasted[i])
				res += taste[i] * hand[i];
		return res;
	}

	private static int last(int[] arr)
	{
		return arr[arr.length - 1];
	}

	private static int[] vectorAdd(int[] ... vectors)
	{
		int size = -1;
		for (int[] v : vectors)
			if (v != null)
				if (size < 0)
					size = v.length;
				else if (size != v.length)
					return null;
		if (size < 0)
			return null;
		int[] res = new int [size];
		for (int i = 0 ; i != size ; ++i)
			res[i] = 0;
		for (int[] v : vectors)
			if (v != null)
				for (int i = 0 ; i != size ; ++i)
					res[i] += v[i];
		return res;
	}

	private static int[] vectorNeg(int[] arr)
	{
		int[] res = new int [arr.length];
		for (int i = 0 ; i != arr.length ; ++i)
			res[i] = -arr[i];
		return res;
	}

	private static void swap(int[] a, int i, int j)
	{
		int t = a[i];
		a[i] = a[j];
		a[j] = t;
	}

	public static int min(int ... a)
	{
		int min = a[0];
		for (int i = 0 ; i != a.length ; ++i)
			if (a[i] < min)
				min = a[i];
		return min;
	}

	@Override
	public String getClassName() 
	{
		return name;
	}

	@Override
	public int getPlayerIndex() 
	{
		return id;
	}

	@Override
	public void happier(double up)
	{
		if (!tasted[last_eaten]) {
			tasted[last_eaten] = true;
			taste[last_eaten] = up;
		}
	}

	public void syncInHand(int[] hand) {}
}
