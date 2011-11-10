package skittles.g7;

public class Candy implements Comparable<Candy>{
	private Integer color;
	private int inHand;
	private Double pref;
	private boolean isTasted = false;
	
	public Candy(int color, int inHand){
		this.color = color;
		this.inHand = inHand;
		this.pref = 0.0;
	}
	
	public Candy(Integer color, int inHand, Double pref) {
		this.color = color;
		this.inHand = inHand;
		this.pref = pref;
	}

	public void consume(int numConsumed) throws IllegalArgumentException{
		if(inHand < numConsumed){
			throw new IllegalArgumentException("Tried to remove/consume more candy than what we have");
		}
		inHand -= numConsumed;
	}
	
	public void addCandy(int candies){
		inHand += candies;
	}
	
	public int getRemaining(){
		return inHand;
	}
	
	public Integer getColor(){
		return color;
	}

	@Override
	public int hashCode() {
		return color;
	}

	public Double getPref() {
		return pref;
	}

	public void setPref(Double pref) {
		this.pref = pref;
	}

	@Override
	public int compareTo(Candy c) {
		if (this.value() < c.value())
			return 1;
		if (this.value() == c.value())
			return 0;
		return -1;
	}
	
	public Double value(){
		return pref * Math.pow(inHand, 2);
	}

	public boolean isTasted() {
		return isTasted;
	}

	public void setTasted(boolean isTasted) {
		this.isTasted = isTasted;
	}
	
	public int compareByPreference(Candy c){
		return 0;
	}

	@Override
	public String toString() {
		return "{color="+color+" pref="+pref+" num="+inHand+" isTasted="+isTasted+"}";
	}
	
	
		
}
