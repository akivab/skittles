package skittles.g6.strategy;

import java.util.ArrayList;
import java.util.Collections;

import skittles.g6.CompulsiveEater;
import skittles.sim.Offer;

public class PreferenceEvaluatorImpl implements PreferenceEvaluator {

	private CompulsiveEater player;
	
	private double[][] rawScores = null;
	
	private int c;
	
	public PreferenceEvaluatorImpl(int c) {
		this.c = c;
	}
	
	public void setPlayer(CompulsiveEater player) {
		this.player = player;
	}

	public void examineIncomeOffers(Offer[] offers) {
		
		int turn = player.getTurnCounter();
		
		if (rawScores == null) {
			int maxId = -1;
			for (Offer o : offers) {
				if (o.getOfferedByIndex() > maxId) {
					maxId = o.getOfferedByIndex();
				}
			}
			
			rawScores = new double[maxId+1][c];
			
			for (int i = 0; i < rawScores.length; i++) {
				for (int j = 0; j < rawScores[i].length; j++) {
					rawScores[i][j] = Integer.MIN_VALUE;
				}
			}
		}
		
		for (Offer o : offers) {
			//Index is offset by 1
			int curPlayer = o.getOfferedByIndex();
			
			int[] bad = o.getOffer();
			int[] good = o.getDesire();
			
			int sum = 0;
			
			for (int i = 0; i < bad.length; i++) {
				if (bad[i]>=1) sum+=bad[i];
			}
			
			for (int i = 0; i < bad.length; i++) {
				
				if (!(bad[i]>=1)) continue;
				
				if (rawScores[curPlayer][i]==Integer.MIN_VALUE) rawScores[curPlayer][i] = 0;
				rawScores[curPlayer][i] -= relativeScore(bad[i] / sum, turn);
			}
			
			for (int i = 0; i < good.length; i++) {
				if (!(good[i]>=1)) continue;
				
				if (rawScores[curPlayer][i]==Integer.MIN_VALUE) rawScores[curPlayer][i] = 0;
				rawScores[curPlayer][i] += relativeScore(good[i] / sum, turn);
			}
		}
	}

	public void examineAcceptedOffers(Offer[] offers) {
		
		int turn = player.getTurnCounter();
		
		for (Offer o : offers) {
			//Index is offset by 1
			int curPlayer = o.getPickedByIndex();
			
			if (curPlayer<0) continue;
			
			int[] good = o.getOffer();
			int[] bad = o.getDesire();
			
			int sum = 0;
			
			for (int i = 0; i < bad.length; i++) {
				if (bad[i]>=1) sum+=bad[i];
			}
			
			for (int i = 0; i < bad.length; i++) {
				
				if (!(bad[i]>=1)) continue;
				
				if (rawScores[curPlayer][i]==Integer.MIN_VALUE) rawScores[curPlayer][i] = 0;
				rawScores[curPlayer][i] -= relativeScore(bad[i] / sum, turn);
			}
			
			for (int i = 0; i < good.length; i++) {
				if (!(good[i]>=1)) continue;
				
				if (rawScores[curPlayer][i]==Integer.MIN_VALUE) rawScores[curPlayer][i] = 0;
				rawScores[curPlayer][i] += relativeScore(good[i] / sum, turn);
			}
		}
	}
	
	//TODO tune this
	private double relativeScore(double ratio, double turn) {
		return ratio * (0.5 + 0.1 * turn);
	}

	public int queryPreference(int playerId, int color1, int color2) {
		if (rawScores[playerId][color1]==Integer.MIN_VALUE || rawScores[playerId][color2]==Integer.MIN_VALUE
				|| rawScores[playerId][color1]==rawScores[playerId][color2]) 
			return 0;
		
		return (rawScores[playerId][color1]>rawScores[playerId][color2] ? 1 : -1);
	}

	public int queryPlayerWithStrongerPreference(int color, int player1,
			int player2) {
		if (rawScores[player1][color]==Integer.MIN_VALUE || rawScores[player2][color]==Integer.MIN_VALUE
				|| rawScores[player1][color]==rawScores[player2][color]) 
			return 0;
		
		return (rawScores[player1][color]>rawScores[player2][color] ? 1 : -1);
	}

	public int[] getPlayersWhoLikeColor(int c) {
		ArrayList<Integer> players = new ArrayList<Integer>();
		for (int i = 0; i < rawScores.length; i++) {
			if (rawScores[i][c] > 0) {
				players.add(i);
			}
		}
		
		int[] toReturn = new int[players.size()];
		int counter = 0;
		for (int i : players) {
			toReturn[counter++] = i;
		}
		
		return toReturn;
	}

	//TODO: BUGGY!!!
	public int[] getColorsSortedFromPlayer(int playerId) {
		ArrayList<Pair<Double, Integer>> temp = new ArrayList<Pair<Double, Integer>>();
		for (int i = 0; i < rawScores[playerId].length; i++) {
			double score = (rawScores[playerId][i]==Integer.MIN_VALUE ? 0 : rawScores[playerId][i]);
			
			temp.add(new Pair<Double, Integer>(score, i));
		}
		Collections.sort(temp);
		
		int[] toReturn = new int[temp.size()];
		int counter=0;
		for (Pair<Double, Integer> pair : temp) {
			toReturn[counter++] = pair.getBack();
		}
		
		return toReturn;
	}

	public int[] getPlayersSortedFromColor(int color) {
		ArrayList<Pair<Double, Integer>> temp = new ArrayList<Pair<Double, Integer>>();
		for (int i = 0; i < rawScores.length; i++) {
			double score = (rawScores[i][color]==Integer.MIN_VALUE ? 0 : rawScores[i][color]);
			
			temp.add(new Pair<Double, Integer>(score, i));
		}
		Collections.sort(temp);
		
		int[] toReturn = new int[temp.size()];
		int counter=0;
		for (Pair<Double, Integer> pair : temp) {
			toReturn[counter++] = pair.getBack();
		}
		
		return toReturn;
	}
}
