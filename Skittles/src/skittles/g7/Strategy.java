package skittles.g7;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import skittles.sim.Offer;
import skittles.sim.Player;

public class Strategy {

	private boolean DEBUG = false;
	private Random random = new Random();

	private int numPlayers;
	private Map<Integer, Friend> friends = new HashMap<Integer, Friend>();
	private MarketKnowledge[] market;
	private CandyBag bag;
	private TradeHistory tradeHistory;

	private int numCandiesEatenOnLastTurn;
	private int colorEatenOnLastTurn;
	private int numColorsToHoard;

	private double currentHappiness = 0;

	private boolean tasting = false;

	public Strategy(int numPlayers, CandyBag bag) {
		this.numPlayers = numPlayers;
		// friends = new ArrayList<Friend>(numPlayers);
		this.bag = bag;
		tradeHistory = new TradeHistory();
		market = new MarketKnowledge[numPlayers];
		for (int i = 0; i < numPlayers; i++) {
			market[i] = new MarketKnowledge(bag.getNumColors());
		}

		// TODO: heuristic here!
		numColorsToHoard = (int) Math.ceil(bag.getNumColors()
				/ (1.0 * numPlayers));
	}

	public void updateHappiness(double happiness) {

		currentHappiness += happiness;

		double happinessPerCandy = happiness
				/ Math.pow(numCandiesEatenOnLastTurn, 2);
		Candy lastCandyEaten = bag.getCandy(colorEatenOnLastTurn);
		if (!lastCandyEaten.isTasted()) {
			if (DEBUG) {
				System.out.println("Setting pref for color: "
						+ colorEatenOnLastTurn);
			}
			lastCandyEaten.setPref(happinessPerCandy);
			lastCandyEaten.setTasted(true);
		} else {
			if (lastCandyEaten.getPref() != happinessPerCandy) {
				System.out.println("Error: Inconsistent color happiness!");
			}
		}

	}

	public void getNextSnack(int[] snack) {
		int tempIndex = 0;
		int inHand = 0;
		int indexToTaste = -1;
		// Taste one of each skittle that we have in our hand, prioritizing to
		// the skittle with the highest stack size
		while (tempIndex < bag.getNumColors()) {
			Candy tempCandy = bag.getCandy(tempIndex);
			if (!tempCandy.isTasted() && tempCandy.getRemaining() > inHand) {
				inHand = tempCandy.getRemaining();
				indexToTaste = tempIndex;
			}
			++tempIndex;
		}
		if (DEBUG) {
			System.out.println("Index to taste: " + indexToTaste);
		}
		tempIndex = indexToTaste;
		if (tempIndex < bag.getNumColors() && tempIndex >= 0) {
			tasting = true;
			colorEatenOnLastTurn = tempIndex;
			numCandiesEatenOnLastTurn = 1;
			snack[colorEatenOnLastTurn] = numCandiesEatenOnLastTurn;
			bag.removeCandy(tempIndex, 1);
			return;
		}
		// After this point, we've tasted everything that we're going to taste

		// tasting means that our last skittle that we ate was due to us tasting
		// a skittle, not because we were just eating one
		if (tasting) {
			tasting = false;
			colorEatenOnLastTurn = -1;
		}

		// If we just ate some skittles, reset colorEatenOnLastTurn to -1 if we
		// finished off the pile. Otherwise, we probably want to eat more

		if (colorEatenOnLastTurn >= 0
				&& bag.getCandy(colorEatenOnLastTurn).getRemaining() == 0) {
			colorEatenOnLastTurn = -1;
		}

		// If there is still more of the last thing we tasted, lets taste some
		// more if its negative, or consult the oracle if its positive

		if (colorEatenOnLastTurn >= 0) {
			if (bag.getCandy(colorEatenOnLastTurn).getPref() <= 0) {
				numCandiesEatenOnLastTurn = 1;
			} else {
				// Use the oracle here to determine whether or not to eat one or
				// eat all. If the oracle returns true, we eat one. Else we eat
				// all.
				boolean oracle = false;
				if (oracle) {
					numCandiesEatenOnLastTurn = 1;
				} else {
					numCandiesEatenOnLastTurn = bag.getCandy(
							colorEatenOnLastTurn).getRemaining();
				}
			}
		}

		// Find the skittle that will give us the least negative score (but
		// still negative) and eat one of those.
		// This will give us trading time, and also cause us to not eat a lot of
		// negative skittles in one go.
		if (colorEatenOnLastTurn < 0) {
			if (DEBUG) {
				System.out.println("Retrieving least negative");
			}
			Candy candy = bag.getLeastNegative();
			if (DEBUG) {
				for (int i = 0; i < bag.getNumColors(); ++i) {
					System.out.println("Candy Index: " + bag.getCandy(i));
				}
				System.out.println(candy);
				System.out
						.println("+++++++++++++++++#####################@@@@@@@@@@@@@@@@@@@@@@@@@");
			}
			if (candy != null) {
				colorEatenOnLastTurn = candy.getColor();
				numCandiesEatenOnLastTurn = 1;
			}

		}
		// After this point, if colorEatenOnLastTurn == -1, then we have no more
		// negative valued skittles.

		// Now find our smallest positive valued skittle that isnt one of our
		// indicies to hoard
		if (colorEatenOnLastTurn < 0) {
			if (DEBUG) {
				System.out.println("Retrieving least positive");
			}
			Candy candy = bag.getLeastPositive();
			if (candy == null) {
				colorEatenOnLastTurn += 0;

				System.out.println();
			}
			colorEatenOnLastTurn = bag.getLeastPositive().getColor();
			boolean highValue = false;
			for (int i = 0; i < numColorsToHoard; ++i) {
				if (bag.getCandy(i).getColor() == colorEatenOnLastTurn) {
					highValue = true;
				}
			}
			if (highValue) {
				numCandiesEatenOnLastTurn = bag.getCandy(colorEatenOnLastTurn)
						.getRemaining();
			} else {
				numCandiesEatenOnLastTurn = 1;
			}
		}

		// Update the aintInHand array
		snack[colorEatenOnLastTurn] = numCandiesEatenOnLastTurn;
		if (DEBUG) {
			System.out.println("Color to be eaten: " + colorEatenOnLastTurn);
			System.out.println("Num candies to be eaten: "
					+ numCandiesEatenOnLastTurn);
		}
		bag.removeCandy(colorEatenOnLastTurn, numCandiesEatenOnLastTurn);
	}

	public void getNextTradeOffer(Offer temp) {

		int numExchanged = random.nextInt(5) + 1;
		int numColors = bag.getNumColors();
		List<Candy> candies = bag.sortByGain();
		int[] bid = new int[numColors];
		int[] ask = new int[numColors];

		int fav = candies.get(0).getColor();

		double highestFavColorValue = -1;
		int highestFavColorIndex = -1;
		for (MarketKnowledge mk : market) {
			for (int i = 0; i < numColors; i++) {
				// retrieves the highest color value and makes sure its not our
				// favorite color
				if (mk.getColorInfo(i) > highestFavColorValue && i != fav) {
					highestFavColorValue = mk.getColorInfo(i);
					highestFavColorIndex = i;
				}
			}

			if (DEBUG) {
				System.out.println("highest color value: "
						+ highestFavColorValue + " with index:"
						+ highestFavColorIndex);
			}
		}

		if (highestFavColorValue != -1) {
			while (bag.getCandy(highestFavColorIndex).getRemaining() < numExchanged)
				numExchanged--;
			bid[highestFavColorIndex] = numExchanged;
			ask[fav] = numExchanged;

		}

		temp.setOffer(bid, ask);
	}

	public void offerExecuted(Offer offPicked) {
		int[] aintOffer = offPicked.getOffer();
		int[] aintDesire = offPicked.getDesire();
		for (int color = 0; color < bag.getNumColors(); color++) {
			bag.addCandy(color, aintDesire[color]);
			bag.removeCandy(color, aintOffer[color]);
		}
	}

	public void updateOfferExecutions(Offer[] aoffCurrentOffers) {

		int numColors = bag.getNumColors();
		for (MarketKnowledge mk : market) {
			mk.decay();
		}
		for (Offer off : aoffCurrentOffers) {
			int giverIndex = off.getOfferedByIndex();
			int[] givingUp = off.getOffer();
			int[] wants = off.getDesire();

			for (int i = 0; i < numColors; ++i) {
				market[giverIndex].addColorInfo(i, wants[i] - givingUp[i]);
			}
			if (off.getPickedByIndex() != -1) {
				givingUp = off.getDesire();
				wants = off.getOffer();
				for (int i = 0; i < numColors; ++i) {
					market[off.getPickedByIndex()].addColorInfo(i, givingUp[i]
							- wants[i]);
				}
			}
		}

	}

	private boolean checkEnoughInHand(int[] aintTryToUse) {
		int numColors = bag.getNumColors();
		for (int color = 0; color < numColors; color++) {
			if (aintTryToUse[color] > bag.getCandy(color).getRemaining()) {
				return false;
			}
		}
		return true;
	}

	public Offer pickOffer(Player me, Offer[] aoffCurrentOffers) {

		Offer offReturn = null;
		double highestPotentialScore = 0; // can change this in future phases
		for (Offer offTemp : aoffCurrentOffers) {
			if (offTemp.getOfferedByIndex() == me.getPlayerIndex()
					|| offTemp.getOfferLive() == false)
				continue;
			int[] currentDesire = offTemp.getDesire();
			// int[] currentOffer = offTemp.getOffer();
			double currentPotentialScore = checkOffer(offTemp);
			// Check to see if we have enough to even go through with this trade
			// before we deliberate
			if (checkEnoughInHand(currentDesire)) {
				// check if offer is worth accepting. right now if its greater
				// than 0
				if (currentPotentialScore > highestPotentialScore) {
					// if here, than its the current best offer for us
					offReturn = offTemp;
					highestPotentialScore = currentPotentialScore;
				}
			}
		}

		if (highestPotentialScore > 0) {
			int[] tempDesire = offReturn.getDesire();
			tempDesire = offReturn.getDesire();
			int[] tempOffer = offReturn.getOffer();
			int numColors = bag.getNumColors();

			for (int color = 0; color < bag.getNumColors(); color++) {
				bag.addCandy(color, tempOffer[color]);
				bag.removeCandy(color, tempDesire[color]);
			}

		}
		return offReturn;
	}

	public double checkOffer(Offer offer) {

		double differenceInScore = 0;
		int[] tempDesire = offer.getDesire();
		int[] tempOffer = offer.getOffer();
		int numColors = bag.getNumColors();
		for (int color = 0; color < numColors; color++) {
			// if its a color we like, update potential score
			Candy candy = bag.getCandy(color);
			Double pref = candy.getPref();
			if (pref > 0) {
				differenceInScore += (pref
						* Math.pow((tempOffer[color] + candy.getRemaining()), 2) - candy
						.value());
				differenceInScore -= candy.value()
						- (pref * Math.pow(
								(candy.getRemaining() - tempDesire[color]), 2));
			} else {

				// Is this right?
				differenceInScore += (pref * tempOffer[color]);
				differenceInScore -= (pref * tempDesire[color]);
			}

		}

		return differenceInScore;
	}

}
