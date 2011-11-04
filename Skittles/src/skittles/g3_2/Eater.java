package skittles.g3_2;

public class Eater {
	public Info info;

	public Eater(Info info) {
		this.info = info;
	}

	public void decideToEat(int[] hand) {
		int[] ranks = Util.index(info.preference);
		int toEatIndex = ranks.length - 1;
		while (toEatIndex > 0 && info.hand[ranks[toEatIndex]] == 0)
			toEatIndex--;
		int toEat = ranks[toEatIndex];
		if (info.hand[toEat] > 0
				&& (info.preference[toEat] < 0 || !info.tasted[toEat]))
			hand[toEat] = 1;
		else
			hand[toEat] = info.hand[toEat];
	}

}
