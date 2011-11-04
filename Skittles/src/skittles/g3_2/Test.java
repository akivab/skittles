package skittles.g3_2;

public class Test {
	public static void main(String[] args){
		try {
			new Test();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void do_assert(boolean f) throws Exception{
		if(!f)
			throw new Exception("Not gonna happen, bubs.");
	}
	public Test() throws Exception{
		Info info = new Info(2, 1, "Test", new int[]{2,2,2,2,2});
		for(int i : info.hand)
			do_assert(i == 2);
		System.out.println("Initialization OK");
		info.setEating(new int[]{2,0,0,0,0});
		do_assert(info.hand[0] == 0);
		for(int i = 1; i < info.hand.length; i++)
			do_assert(info.hand[i] == 2);
		System.out.println("Eating OK");
		info.update(4);
		do_assert(Math.round(info.preference[0]) == 1);
		for(int i = 1; i < info.preference.length; i++)
			do_assert(info.preference[i] == 0.1);
		System.out.println("Preferences OK");
		// we have 0, 2, 2, 2, 2 in hand.
		// we like 1 with certainty 1
		int[] k1 = new int[info.hand.length];
		int[] k2 = Util.copy(k1);
		double b1 = info.evaluate(k1, k1, true);
		k1[0] = 2;
		double b2 = info.evaluate(k1, k2, true);
		System.out.println(b1 + " " + b2);
		do_assert(Math.round(b2 - b1) == 4);
		System.out.println("Evaluate works");
		Eater eat = new Eater(info);
		int[] eaten = new int[5];
		eat.decideToEat(eaten);
		do_assert(eaten[0] == 0);
		System.out.println("Eating OK");
	}
}
