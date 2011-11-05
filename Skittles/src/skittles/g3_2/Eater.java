package skittles.g3_2;

public class Eater {
	public Info info;

	public Eater(Info info) {
		this.info = info;
	}

	private int whichColor = -1;
	private int howMany = -1;

	private void decideToEat() {
		whichColor = -1;
		howMany = 1;
		int colors = info.hand.length;
		// get all unknown colors
		int[] unknown = new int [colors];
		int unknownCount = 0;
		for (int i = 0 ; i != colors ; ++i)
			if (info.hand[i] > 0 && !info.tasted[i])
				unknown[unknownCount++] = i;
		// if there are unknown
		if (unknownCount != 0) {
			// return one of the biggest pile
			whichColor = unknown[0];
			for (int i = 1 ; i != unknownCount ; ++i)
				if (info.hand[unknown[i]] > info.hand[whichColor])
					whichColor = unknown[i];
			return;
		}
		// number of piles we are hoarding
		// TODO
		// update using the number of piled
		int hoardingCount = 2;
		// sort the array of tastes
		int[] sortedPreferences = Util.index(info.preference);
		// search between the non-stacked piles
		for (int i = hoardingCount ; i != colors ; ++i) {
			int color = sortedPreferences[i];
			if (info.hand[color] > 0)
				// pick the biggest pile
				if (whichColor < 0 || info.hand[color] > info.hand[whichColor])
					whichColor = color;
		}
		// if there was a skittle in a non-stacked pile
		if (whichColor >= 0)
			return;
		// pick from stacked piles
		for (int i = 0 ; i != hoardingCount ; ++i) {
			int color = sortedPreferences[i];
			if (info.hand[color] > 0)
				// pick the smallest pile
				if (whichColor < 0 || info.hand[color] < info.hand[whichColor])
					whichColor = color;
		}
		howMany = info.hand[whichColor];
	}

	public void decideToEat(int[] eating) {
		decideToEat();
		int colors = info.hand.length;
		for (int i = 0 ; i != colors ; ++i)
			eating[i] = 0;
		eating[whichColor] = howMany;
		Util.print(info.preference);
		Util.print(eating);
		Util.print(info.hand);
		System.out.println(whichColor + " " + howMany);
	}
}
