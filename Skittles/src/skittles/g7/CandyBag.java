package skittles.g7;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandyBag {
	private Map<Integer, Candy> bag = new HashMap<Integer, Candy>();
	
	public CandyBag(int[] candies){
		for(int i=0; i < candies.length; i++){
			Candy candy = new Candy(i,candies[i]);
			bag.put(i, candy);
		}
	}
	
	public void addCandy(int color, int candies){
		if(bag.containsKey(color)){
			bag.get(color).addCandy(candies);
		}else{
			Candy candy = new Candy(color, candies);
			bag.put(color, candy);
		}
	}
	
	public void removeCandy(int color, int candies) throws IllegalArgumentException{
		if(!bag.containsKey(color)){
			throw new IllegalArgumentException("Tried to remove candy from a color that we don't have");
		}
		bag.get(color).consume(candies);
	}
	
	public Candy getCandy(int color){
		return bag.get(color);
	}
	
	public int getNumColors(){
		return bag.size();
	}
	

	public static List<Candy> sortByPreference(Collection<Candy> collection){
		List<Candy> sortedCandies = new ArrayList<Candy>();
		sortedCandies.addAll(collection);
		Collections.sort(sortedCandies, new Comparator<Candy>(){

			@Override
			public int compare(Candy c1, Candy c2) {
				return c1.getPref().compareTo(c2.getPref());
			}
			
		});
		
		return sortedCandies;
		
	}
	
	public List<Candy> sortByPreference(){
		return sortByPreference(bag.values());
	}
	
	/**
	 *  
	 * @param collection of candy
	 * @return
	 * Return a list of candies sorted by descending order of gain
	 * 
	 */
	public static List<Candy> sortByGain(Collection<Candy> collection){
		List<Candy> sortedCandies = new ArrayList<Candy>();
		sortedCandies.addAll(collection);
		Collections.sort(sortedCandies, new Comparator<Candy>(){
				
				public int compare(Candy c1, Candy c2) {
					if (c1.value() < c2.value())
						return 1;
					if (c1.value() == c2.value())
						return 0;
					return -1;
				}
			});
		return sortedCandies;
	}
	
	public List<Candy> sortByGain(){
		return sortByGain(bag.values());
	}
	
	/**
	 *  
	 * @return
	 * 
	 * Return the Candy that has a negative preference that's closest to zero 
	 * or has a preference of zero
	 * Return null if no such candy exists
	 * 
	 */
	
	public Candy getLeastNegative(){

		Candy[] candies = sortByPreference(bag.values()).toArray(new Candy[getNumColors()]);
		if(candies.length == 1){
			if(candies[0].getRemaining()>0) return candies[0];
			else return null;
		}
		
		int ii = 0;
		if(candies[ii].getPref() > 0) return null;
		
		for(int i = 0; i < candies.length && candies[i].getPref() <= 0; i++){
			if(candies[i].getRemaining() > 0) ii = i;
		}
		
		if(candies[ii].getPref() <= 0 && candies[ii].getRemaining() > 0) return candies[ii];
		
		return null;
	}
	
	
	/**
	 * Return the Candy that will give you the least positive gain if you ate 
	 * all of that candy
	 * 
	 * returns null if we dont have a candy with a positive gain
	 */
	public Candy getLeastPositive(){
		Candy[] candies = sortByGain(bag.values()).toArray(new Candy[getNumColors()]);
		System.out.println("Sorted by order of descending gain:");
		for(int i=0; i<candies.length; i++)		System.out.println(candies[i]);
		
		if(candies.length == 1){
			if(candies[0].value() > 0) return candies[0];
			else return null;
		}
		
		int ii = 0;
		if(candies[ii].value() <= 0) return null;
		
		for(int i=0; i < candies.length && candies[i].value() >= 0; i++){
			if(candies[i].getRemaining() > 0) ii = i;
		}
		
		if(candies[ii].value() > 0 && candies[ii].getRemaining() > 0) return candies[ii];
		
		return null;
	}
	
}
