package skittles.g6.strategy;

import skittles.g6.CompulsiveEater;
import skittles.sim.Offer;

public interface PreferenceEvaluator {
	
	//The current player
	public void setPlayer(CompulsiveEater player);
	
	//This method only worries about people who posted offers
	public void examineIncomeOffers(Offer[] offers);
	
	//This method should only worry about people who took offers
	public void examineAcceptedOffers(Offer[] offers);
	
	//return negative number if 2 is prefered over 1, positive if 1 prefered 2, 0 if no preference or not enough info
	public int queryPreference(int playerId, int color1, int color2);
	
	//return negative if 2 prefers more than 1, positive if 1 prefers more than 2, 0 if same or not enough info
	public int queryPlayerWithStrongerPreference(int color, int player1, int player2);
	
	public int[] getPlayersWhoLikeColor(int c);
	
	//sorted in descending order of preference
	public int[] getColorsSortedFromPlayer(int playerId);
	
	//sorted in descending order of preference
	public int[] getPlayersSortedFromColor(int color);
}
