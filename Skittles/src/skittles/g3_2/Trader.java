package skittles.g3_2;

import java.util.Random;

import skittles.sim.Offer;

public class Trader {
	public Info info;

	public Trader(Info info) {
		this.info = info;
	}

	public void setOffer(Offer off) {
		// see, for each color and every other color, what the value of the
		// trade would be
		int[] empty = new int[info.hand.length];
		int[] giving = Util.copy(empty);
		int[] taking = Util.copy(empty);
		int[] index = Util.index(info.preference); 
		Random r = new Random();

		for (int i = 0; i < info.hand.length; i++)
			if (info.preference[i] < 0)
				giving[i] = info.hand[i];

		int sum = Util.sum(giving);

		for (int i = 0; i < taking.length; i++) {
			int toAdd = index[i];
			if(sum > 0)
				taking[toAdd] += r.nextInt(sum);
			if(i == taking.length - 1)
				taking[toAdd] = sum;
			sum -= taking[toAdd];
		}
		off.setOffer(giving, taking);
	}

	public Offer pickOffer() {
		for (Offer off : info.currentOffers) {
			if (off.getOfferLive() && off.getOfferedByIndex() != info.id) {
				if (info.evaluate(off.getOffer(), off.getDesire(), true) > 0)
					return off;
			}
		}
		return null;
	}
}
