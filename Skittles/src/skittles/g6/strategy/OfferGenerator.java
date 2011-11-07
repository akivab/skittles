package skittles.g6.strategy;

import skittles.g6.CompulsiveEater;
import skittles.sim.Offer;

public interface OfferGenerator {
	public void setPlayer(CompulsiveEater player);
	public void setCurrentOffers(Offer[] offers);
	public Offer getOffer();
}
