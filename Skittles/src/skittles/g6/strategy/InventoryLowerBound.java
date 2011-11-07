package skittles.g6.strategy;

import skittles.g6.CompulsiveEater;
import skittles.sim.Offer;

public interface InventoryLowerBound {
	public void setPlayer(CompulsiveEater player);
	public void setCurrentOffers(Offer[] offers);
	public void queryLowerBound(int playerId, int color);
}
