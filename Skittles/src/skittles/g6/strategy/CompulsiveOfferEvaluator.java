package skittles.g6.strategy;

import java.util.Arrays;

import skittles.g6.CompulsiveEater;
import skittles.sim.Offer;

public class CompulsiveOfferEvaluator implements OfferEvaluator {
	
	private CompulsiveEater player;
	private int playerIndex;
	private int[] inHand;
	private double[] tastes;

	public void setPlayer(CompulsiveEater player) {
		this.player = player;
		playerIndex = player.getPlayerIndex();
		inHand = player.getAIntInHand(); 
	}

	public Offer getBestOffer(Offer[] offers) {
		double maxScore = 0;
		Offer bestOffer = null;
		tastes = player.getPreferences();
		for (Offer currOffer : offers) {
			int[] aintDesire = currOffer.getDesire();

			if(!checkEnoughInHand(aintDesire) || currOffer.getOfferedByIndex() == playerIndex 
					|| !currOffer.getOfferLive())
				continue;
			double currentScore = 0;
			
			for (int i = 0; i < aintDesire.length; i++) {
				
				if (i == tastes.length) break;
				//squares of negatives are positive
				if(tastes[i] == Parameters.UNKNOWN_TASTE)
					currentScore += tastes[i] * (Math.pow(inHand[i] + aintDesire[i], 2) - Math.pow(inHand[i], 2));
				else if(tastes[i] > 0)
					currentScore += -1 * tastes[i] * (Math.pow(inHand[i] + aintDesire[i], 2) - Math.pow(inHand[i], 2));
				else
					currentScore += -1 * tastes[i] * aintDesire[i];
			}
			int[] aintOffer = currOffer.getOffer();
			for (int i = 0; i < aintOffer.length; i++) {
				
				if (i==tastes.length) break;
				
				//TODO: possibly tweak offer eval receiving threshold
				if (tastes[i] == Parameters.UNKNOWN_TASTE)
					currentScore += -1 * tastes[i] * (Math.pow(inHand[i] + aintOffer[i], 2) - Math.pow(inHand[i], 2));
				else if(tastes[i] > 0)
					currentScore += tastes[i] * (Math.pow(inHand[i] + aintOffer[i], 2) - Math.pow(inHand[i], 2));
				else
					currentScore += tastes[i] * aintOffer[i];
			}
			
			if(currentScore > maxScore){
				bestOffer = currOffer;
				maxScore = currentScore;
			}
		}
		return bestOffer;
	}

	private boolean checkEnoughInHand( int[] aintToUse ) 
	{
		for ( int intColorIndex = 0; intColorIndex < inHand.length; intColorIndex ++ )
		{
			if ( aintToUse[ intColorIndex ] > inHand[ intColorIndex ] || aintToUse[ intColorIndex ] < 0 )
			{
				return false;
			}
		}
		return true;
	}
}
