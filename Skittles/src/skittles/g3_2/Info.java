package skittles.g3_2;

import java.util.Vector;

import skittles.sim.Offer;

public class Info {
	public int numPlayers;
	public int id;
	public String name;
	public int[] hand;
	public double[] preference;
	public boolean[] tasted;
	public Offer[] currentOffers;
	public Vector<Offer[]> pastOffers;
	public int[] eating;

	public Info(int players, int intPlayerIndex, String strClassName,
			int[] aintInHand) {

		this.numPlayers = players;
		this.id = intPlayerIndex;
		this.name = strClassName;
		this.hand = Util.copy(aintInHand);
		this.pastOffers = new Vector<Offer[]>();
		this.preference = new double[hand.length];
		for (int i = 0; i < hand.length; i++)
			preference[i] = 0.1;
		this.eating = new int[hand.length];
		this.tasted = new boolean[hand.length];
	}

	public void setEating(int[] eating) {
		this.eating = Util.copy(eating);
		for (int i = 0; i < eating.length; i++)
			hand[i] -= eating[i];
	}

	public String toString() {
		return this.id + " " + this.name + "\nHand: "
				+ Util.toString(this.hand) + ",\nPreference:"
				+ Util.toString(this.preference);
	}

	public void update(double happiness) {
		for (int i = 0; i < eating.length; i++) {
			if (eating[i] != 0) {
				preference[i] = happiness / (eating[i] * eating[i]);
				tasted[i] = true;
			}
		}
	}

	public void recordOffers(Offer[] offers) {
		currentOffers = offers;
	}

	public void recordExecuted(Offer[] offers) {
		for (Offer offer : offers) {
			if (offer.getPickedByIndex() == id)
				for (int i = 0; i < hand.length; i++)
					hand[i] += offer.getOffer()[i] - offer.getDesire()[i];
			pastOffers.add(offers);
		}
	}

	public double evaluate(int[] offer, int[] desire, boolean usingProfile) {
		double value = 0;
		for (int i = 0; i < hand.length; i++) {
			double tmp = (usingProfile ? hand[i] : 0) + offer[i] - desire[i];
			if (tmp < 0) // invalid offer (gives negative something)
				return -1;
			else if (preference[i] > 0)
				tmp *= tmp;
			value += tmp * preference[i];
		}
		return value;
	}

	public void recordPicked(Offer offer) {
		// we picked an offer.
		int[] taking = offer.getDesire();
		int[] giving = offer.getOffer();
		for (int i = 0; i < hand.length; i++)
			hand[i] += taking[i] - giving[i];
	}
}
