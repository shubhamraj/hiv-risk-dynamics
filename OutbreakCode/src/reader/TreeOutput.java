package reader;




import interfaces.AgentInterface;
import interfaces.ParametersInterface;

import java.util.ArrayList;
import java.util.EnumMap;


import cluster.Edge;

import edu.uci.ics.jung.graph.DelegateTree;

/**
 * 
 * @author shah
 *
 */
public class TreeOutput implements ParametersInterface {
	protected EnumMap<Output, double[]> outputsMap;

	public TreeOutput() {
		outputsMap = new EnumMap<OutbreakStatsReader.Output, double[]>(Output.class);
	}
	
	protected void initializeOutputMap(int numData) {
		outputsMap.clear();
		for (Output output : Output.values()) {
			double[] data = new double[numData];
			for (int i=0; i<data.length; i++) {
				data[i] = 0;
			}
			outputsMap.put(output, data);
		}
	}

	protected void setOutputMapDataArray(Output output, ArrayList<Double> dataArrayList) {
		outputsMap.remove(output);
		double[] data = returnDoubleArray(dataArrayList);
		outputsMap.put(output, data);
	}

	protected ArrayList<Double> calculateStats(Output output) {
		double[] data = outputsMap.get(output);
		ArrayList<Double> statistics = new ArrayList<Double>();
		
		double val = Double.NaN;
		for (Statistics statistic : Statistics.values()) {
			try {
				switch (statistic) {
				case Avg: 
					val = Stats.returnMean(data);
					break;
				case Max:
					val = Stats.returnMaximum(data); 
					break;
				case Var:
					val = Stats.returnVariance(data);
					break;
				case IQR:
					val = Stats.returnInterQuartileMean(data);
					break;
				case p25:
					val = Stats.returnPercentile(data, 25);
					break;
				case p50:
					val = Stats.returnPercentile(data, 50);
					break;
				case p75: 
					val = Stats.returnPercentile(data, 75);
					break;
				case p90: 
					val = Stats.returnPercentile(data, 90);
					break;
				case p99: 
					val = Stats.returnPercentile(data, 99);
					break;
				default:
					System.err.println("Error in the switch function. Entered in default case.");
					break;
				}				 
			} 
			catch (Exception e) {
				System.err.println("Error in calculateStats function. Returning NaN.");
				e.printStackTrace();
			}
			statistics.add(val);
		}

		int[] ranges = null;
		if (output.equals(Output.Size)) {
			ranges = SizeRanges;			
		}
		else if (output.equals(Output.Duration)) {
			ranges = durationRanges;
		}
		if (ranges != null) {
			double[] cumfrac = Stats.returnCumulativeFractions(data, ranges);
			for (double cf : cumfrac) {
				statistics.add(cf);
			}
		}
		return statistics;
	}

	public static double returnDuration(DelegateTree<AgentInterface, Edge> tree) {
		double min = Double.MAX_VALUE;
		double max = 0;
		for (Edge edge : tree.getEdges()) {
			double time = edge.getTransmission().getTime();
			if (time < min) {
				min = time;
			}
			if (time > max) {
				max = time;
			}
		}
		double duration = (max - min) + 1;
		return duration;
	}
	
	public double[] returnDoubleArray (ArrayList<Double> arrayList) {
		if (arrayList == null) {
			System.err.println("Arraylist is null.");
			return null;
		}
		double[] array = new double[arrayList.size()];
		int arrayIndex = 0;
		for (Double dbl : arrayList) {
			array[arrayIndex] = dbl;
			arrayIndex++;
		}
		return array;
	}
}