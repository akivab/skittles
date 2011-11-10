package skittles.g7;

import java.util.ArrayList;

public class MarketKnowledge {
	ArrayList<Double> colorKnowledge = new ArrayList<Double>();

	public MarketKnowledge(int numColors) {
		for (int i = 0; i < numColors; ++i) {
			colorKnowledge.add(0.0);
		}
	}

	public double getColorInfo(int index) {
		return colorKnowledge.get(index);
	}

	public void addColorInfo(int index, int delta) {
		colorKnowledge.set(index, colorKnowledge.get(index) + delta);
	}

	public void decay() {
		for (int i = 0; i < colorKnowledge.size(); ++i) {
			if (colorKnowledge.get(i) > 0) {
				colorKnowledge.set(i, colorKnowledge.get(i) * 0.8);
			}
		}
	}

	public int getMaxColorIndex() {
		double max = -2.0;
		int maxIndex = -1;
		for (int i = 0; i < colorKnowledge.size(); ++i) {
			if (colorKnowledge.get(i) > max) {
				max = colorKnowledge.get(i);
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	public int getMinColorIndex() {
		double min = 2.0;
		int minIndex = -1;
		for (int i = 0; i < colorKnowledge.size(); ++i) {
			if (colorKnowledge.get(i) < min) {
				min = colorKnowledge.get(i);
				minIndex = i;
			}
		}
		return minIndex;
	}
}
