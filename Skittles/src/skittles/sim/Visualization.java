package skittles.sim;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;

import skittles.g3.Info;

import com.googlecode.charts4j.AxisLabels;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.AxisStyle;
import com.googlecode.charts4j.AxisTextAlignment;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.DataUtil;
import com.googlecode.charts4j.Fills;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Line;
import com.googlecode.charts4j.LineChart;
import com.googlecode.charts4j.LineStyle;
import com.googlecode.charts4j.LinearGradientFill;
import com.googlecode.charts4j.Plots;
import com.googlecode.charts4j.Shape;

public class Visualization {
	public JFrame offers;
	public JFrame graphOfProgress;
	public int numPlayers = -1;
	public int maxTime = 0;
	public double maxScore = 0;
	public double minScore = 0;
	public String outputHTML;
	public HashMap<Integer, HashMap<Integer, UpdateObject>> updates;
	public ArrayList<String> URLs = new ArrayList<String>();
	private Player[] players;
	public Visualization(){
		this.offers = new JFrame("Offers");
		this.graphOfProgress = new JFrame("Graph of Progress");
		this.updates = new HashMap<Integer, HashMap<Integer, UpdateObject>>();
	}
	public void setPlayers(Player[] players){
		this.players = players;
	}
	public double calculatePotential(int[] hand, double[] pref){
		double potential = 0;
		for(int i = 0; i < hand.length; i++)
			if(pref[i] < 0)
				potential += hand[i] * pref[i];
			else
				potential += hand[i] * hand[i] * pref[i];
		return potential;
	}
	public void updateStatuses(int time, PlayerStatus[] statuses){
		if(numPlayers < 0) numPlayers = statuses.length;
		this.maxTime = time;
		updates.put(time, new HashMap<Integer, UpdateObject>());
		for(PlayerStatus status : statuses){
			double[] pref = status.getPreferences();
			UpdateObject update = new UpdateObject();
			update.pref = pref;
			update.playerIndex = status.getPlayerIndex();
			update.happiness = status.getHappiness();
			update.hand = Info.copy(status.getInHand());
			update.potential = this.calculatePotential(update.hand, pref) + update.happiness;
			this.maxScore = Math.max(Math.max(this.maxScore, update.potential), update.happiness);
			this.minScore = Math.min(Math.min(this.minScore, update.potential), update.happiness);
			updates.get(time).put(update.playerIndex, update);
		}
	}
	public void updateOffers(int time, Offer[] offers){
		for(Offer offer : offers){
			int offeredBy = offer.getOfferedByIndex();
			int pickedBy = offer.getPickedByIndex();
			int[] desiring = offer.getDesire();
			int[] offering = offer.getOffer();
			updates.get(time).get(offeredBy).setOffer(desiring, offering, pickedBy, offeredBy);
			if(pickedBy != -1)
				updates.get(time).get(pickedBy).setOffer(desiring, offering, pickedBy, offeredBy);
		}
	}
	
	public String getRandomColor(){
		String chars = "0123456789ABCDEF";
		String toReturn = "";
		Random r = new Random();
		for(int i = 0; i < 6; i++)
			toReturn += chars.charAt(r.nextInt(chars.length()));
		return toReturn;
	}
	
	public void generateGraph2(){
		// for each player, look at delta in certain colors.
		// colors are based on preference
		double maxValue = 1;
		for(int i= 0 ; i <this.numPlayers; i++)
			for(int j = 0; j <= this.maxTime; j++){
				int[] hand = this.updates.get(j).get(i).hand;
				for(int k = 0; k < hand.length; k++)
					maxValue = Math.max(maxValue, 
							this.updates.get(j).get(i).hand[k]);
			}
		maxValue += 1;
		for(int i = 0; i < this.numPlayers; i++){
			ArrayList<Line> lines = new ArrayList<Line>();
			double[] pref = this.updates.get(0).get(i).pref;
			int[] index = Info.index(pref);
			for(int k = 0; k < index.length; k++){
				double[] amountOfK = new double[this.maxTime+1];
				for(int j = 0; j <= this.maxTime; j++){
					amountOfK[j] = this.updates.get(j).get(i).hand[index[k]];
				}
				Color color = Color.newColor(this.getRandomColor());
				if(k==0)
					color = Color.newColor("FFFF00");
				else if(k==0)
					color = Color.newColor("FF0080");

				String str = ("" + pref[index[k]]).substring(0,5);
				Line l1 =Plots.newLine(DataUtil.scaleWithinRange(0, maxValue, amountOfK), 
						color, "# "+k+" for " + i + " ("+str+")");
				lines.add(l1);
			}
			for(Line line : lines){
				line.addShapeMarkers(Shape.DIAMOND, Color.WHITE, this.maxTime);
				line.setLineStyle(LineStyle.THICK_DOTTED_LINE);
			}
			
			double[] scores = new double[this.maxTime+1];
			for(int j = 0; j <= this.maxTime; j++)
				scores[j] = this.updates.get(j).get(i).happiness;
			Line l1 = Plots.newLine(DataUtil.scaleWithinRange(this.minScore, this.maxScore, scores), 
					Color.newColor(this.getRandomColor()), "Scores for " +i);
			l1.addShapeMarkers(Shape.DIAMOND, Color.WHITE, this.maxTime);
			l1.setLineStyle(LineStyle.MEDIUM_LINE);
			lines.add(l1);
			
			LineChart chart = GCharts.newLineChart(lines);
	        AxisStyle axisStyle = AxisStyle.newAxisStyle(Color.WHITE, 12, AxisTextAlignment.CENTER);
	        AxisLabels yAxis = AxisLabelsFactory.newNumericRangeAxisLabels(0, maxValue);
	        yAxis.setAxisStyle(axisStyle);
	        chart.addYAxisLabels(yAxis);
	        
	        chart.setSize(600, 450);
	        chart.setMargins(10, 0, 0,10);
	        chart.setTitle("In hand for player "+i, Color.WHITE, 14);
	        
	        chart.setBackgroundFill(Fills.newSolidFill(Color.newColor("1F1D1D")));
	        LinearGradientFill fill = Fills.newLinearGradientFill(0, Color.newColor("363433"), 100);
	        fill.addColorAndOffset(Color.newColor("2E2B2A"), 0);
	        chart.setAreaFill(fill);
	        
	        URLs.add(chart.toURLString());
		}
	}
	public void generateGraph1(){
		// for each player, at each time (>1) let's graph
		// the difference in potential AND the difference in
		// score at each turn.
		this.minScore *= 1.1;
		ArrayList<Line> lines = new ArrayList<Line>();
		for(int i = 0; i < this.numPlayers; i++){
			double[] scores = new double[this.maxTime+1];
			double[] potentials = new double[this.maxTime+1];
			for(int j = 0; j <= this.maxTime; j++){
				scores[j] = this.updates.get(j).get(i).happiness;
				potentials[j] = this.updates.get(j).get(i).potential;
			}
			String str = getRandomColor();
			Line l1 = Plots.newLine(DataUtil.scaleWithinRange(this.minScore, this.maxScore, scores), Color.newColor(str), "Scores for " +i);
			Line l2 = Plots.newLine(DataUtil.scaleWithinRange(this.minScore, this.maxScore, potentials), Color.newColor(str), "Potentials for "+i);
			l1.addShapeMarkers(Shape.DIAMOND, Color.WHITE, this.maxTime);
			l2.addShapeMarkers(Shape.DIAMOND, Color.WHITE, this.maxTime);
			l1.setLineStyle(LineStyle.MEDIUM_LINE);
			l2.setLineStyle(LineStyle.THICK_DOTTED_LINE);
			lines.add(l1);
			lines.add(l2);
		}
        // Defining chart.
        LineChart chart = GCharts.newLineChart(lines);
        AxisStyle axisStyle = AxisStyle.newAxisStyle(Color.WHITE, 12, AxisTextAlignment.CENTER);
        AxisLabels yAxis = AxisLabelsFactory.newNumericRangeAxisLabels(this.minScore, this.maxScore);
        yAxis.setAxisStyle(axisStyle);
        chart.addYAxisLabels(yAxis);

        chart.setSize(600, 450);
        chart.setMargins(10, 0, 0,10);
        chart.setTitle("Scores versus Potentials", Color.WHITE, 14);
        
        // Defining background and chart fills.
        chart.setBackgroundFill(Fills.newSolidFill(Color.newColor("1F1D1D")));
        LinearGradientFill fill = Fills.newLinearGradientFill(0, Color.newColor("363433"), 100);
        fill.addColorAndOffset(Color.newColor("2E2B2A"), 0);
        chart.setAreaFill(fill);
        String url = chart.toURLString();
        URLs.add(url);
	}
	
	public void generateGraphs(){
		generateGraph1();
		generateGraph2();
	}
	
	public void generateHTML(String htmlFile) throws IOException{
		FileWriter outFile = new FileWriter(htmlFile);
		PrintWriter out = new PrintWriter(outFile);
		out.println("<html>");
		out.println("<head><style>div{ width: 900px; margin: auto}"+
							"table{ border: 1px solid black; margin-bottom: 20px; }" +
							"td{ margin: 0px; background-color: gray; color: white; padding: 5px; }</style></head>");
		out.println("<div><h1>Analysis for Game</h1><table><h3>Players</h3><tr><td>name</td><td>index</td><td>score</td></tr>");
		for(Player i : players){
			out.println("<tr><td>"+i.getClassName()+"</td><td>"+i.getPlayerIndex()+"</td><td>"+this.updates.get(this.maxTime).get(i.getPlayerIndex()).happiness+"</td><tr>");
		}
		out.println("</table>");
		for(String i: URLs){
			out.println("<img src='"+i+"' />");
		}
		out.println("</div></html>");
		out.close();
	}
}

class UpdateObject{
	int time;
	int playerIndex;
	double happiness;
	double potential;
	int[] hand;
	double[] pref;
	Offer offering = null;
	Offer taking = null;
	public void setOffer(int[] desiring, int[] offering, int pickedBy, int offeredBy){
		Offer offer = new Offer(offering, desiring, offeredBy, pickedBy);
		if(offeredBy == playerIndex)
			this.offering = offer; 
		else if(pickedBy == playerIndex)
			this.taking = offer;
	}
}
