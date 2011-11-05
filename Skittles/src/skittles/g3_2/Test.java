package skittles.g3_2;

public class Test {
	public static void main(String[] args){
		Test test = new Test();
		test.testInfo();
		System.out.println("Testing OK");
		test.testInfoEvaluate();
		System.out.println("Evaluation OK");
		test.testInfoHoardingCount();
		System.out.println("Hoarding OK");
	}
	
	public int numPlayers;
	public int playerIndex;
	public int[] smallHand;
	public int[] bigHand;
	public Info smallInfo;
	public Info bigInfo;
	
	public Test(){
		numPlayers = 5;
		playerIndex = 1;
		smallHand = new int[]{10,10,10,10,10};
		bigHand = new int[]{10,10,10,10,10,10,10,10,10,10};
		
	}
	
	public void testInfo(){
		smallInfo = new Info(numPlayers, playerIndex, "Test", smallHand);
		bigInfo = new Info(numPlayers, playerIndex, "Test", bigHand);
		assert smallInfo.hoardingCount() == 1;
		assert bigInfo.hoardingCount() == 2;
		assert smallInfo.pile.trading.size() == 0 && smallInfo.pile.hoarding.size() == 0;
	}
	
	public void testInfoHoardingCount(){
		smallInfo.setEating(new int[]{10, 0, 0, 0, 0});
		bigInfo.setEating(new int[]{10, 0, 0, 0, 0, 0, 0, 0, 0, 0});
		assert smallInfo.hand[0] == 0;
		assert bigInfo.hand[0] == 0;
		smallInfo.update(0);
		bigInfo.update(0);
		assert smallInfo.pile.trading.size() == 1;
		assert bigInfo.pile.trading.size() == 1;
		assert Math.round(smallInfo.preference[0]) == 0;
		assert Math.round(bigInfo.preference[0]) == 0;
		
		smallInfo.setEating(new int[]{0, 10, 0, 0, 0});
		bigInfo.setEating(new int[]{0, 10, 0, 0, 0, 0, 0, 0, 0, 0});
		smallInfo.update(100);
		bigInfo.update(-100);
		assert smallInfo.pile.hoarding.size() == 1 && smallInfo.pile.trading.size() == 1;
		assert bigInfo.pile.trading.size() == 2 && bigInfo.pile.hoarding.size() == 0;
		Util.print(smallInfo.preference);
		assert Math.round(smallInfo.preference[1]) == 1;
		assert Math.round(bigInfo.preference[1]) == -1;
		
		int[] trading = bigInfo.pile.getTradingColorsByPreference();
		assert trading[0] == 0;
		assert trading[1] == 1;
		
		for(int i = 2; i < 5; i++){
			int[] eat = new int[5];
			eat[i] = 10;
			smallInfo.setEating(eat);
			smallInfo.update(100);
		}
		
		trading = smallInfo.pile.getTradingColorsByPreference();
		int[] hoarding = smallInfo.pile.getHoardingColorsByPreference();
		Util.print(hoarding);
		assert hoarding.length == smallInfo.hoardingCount();
		assert trading.length == smallInfo.hand.length - smallInfo.hoardingCount();
		assert hoarding[0] == 1;
		assert trading[trading.length-1] == 0;
	}
	
	public void testInfoEvaluate(){
		smallInfo.preference = new double[]{1.0, 0.5, 0.2, -0.1, 0};
		double eval = smallInfo.evaluate(new int[]{2,0,0,2,0}, new int[5], false);
		assert Math.round(10*(eval - 3.8)) == 0;
	}	
}
