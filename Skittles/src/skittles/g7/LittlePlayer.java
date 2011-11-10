package skittles.g7;

import skittles.sim.Offer;
import skittles.sim.Player;

public class LittlePlayer extends Player {
	
	private int myId;
	private String myClassName;
	private Strategy strategy;

	@Override
	public void eat(int[] aintTempEat) {
		strategy.getNextSnack(aintTempEat);
	}

	@Override
	public void offer(Offer offTemp) {
		strategy.getNextTradeOffer(offTemp);
	}

	@Override
	public void syncInHand(int[] aintInHand) {
		// TODO Auto-generated method stub

	}

	@Override
	public void happier(double dblHappinessUp) {
		strategy.updateHappiness(dblHappinessUp);
	}

	
	@Override
	public Offer pickOffer(Offer[] aoffCurrentOffers) {
		return strategy.pickOffer(this, aoffCurrentOffers);
	}

	@Override
	public void offerExecuted(Offer offPicked) {
		strategy.offerExecuted(offPicked);

	}

	@Override
	public void updateOfferExe(Offer[] aoffCurrentOffers) {
		strategy.updateOfferExecutions(aoffCurrentOffers);
	}

	@Override
	public void initialize(int numPlayers, double dblTasteMean, int myId, String myClassName, int[] skittlesInHand) {
		this.myId = myId;
		this.myClassName = myClassName;
		strategy = new Strategy(numPlayers, new CandyBag(skittlesInHand));
	}

	@Override
	public String getClassName() {
		return myClassName;
	}

	@Override
	public int getPlayerIndex() {
		return myId;
	}

}
