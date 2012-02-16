package reader;

import interfaces.ParametersInterface;
import cern.jet.random.Normal;

/**
 * Contains static functions for various statistics used for outbreak distributions.
 *  Uses the MathCommons, Colt and Flanagan Java libraries.
 *  
 *  @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 *
 */
public class Stats implements ParametersInterface {	
	public static double returnMean(double[] data) {
		if (dataArrayNull(data)) {
			return -1;
		}
		try {
			return flanagan.analysis.Stat.mean(data);
		} catch (Exception e) {
			System.err.println("Error in Mean. Returning -1.");
			e.printStackTrace();
			return -1; 
		}
	}
	
	public static double returnMedian(double[] data) {
		if (dataArrayNull(data)) {
			return -1;
		}
		try {
			return flanagan.analysis.Stat.median(data);
		} catch (Exception e) {
			System.err.println("Error in Median. Returning -1.");
			e.printStackTrace();
			return -1; 
		}
	}

	public static double returnMaximum(double[] data) {
		if (dataArrayNull(data)) {
			return -1;
		}
		try {			
			return flanagan.math.Fmath.maximum(data);
		} catch (Exception e) {
			System.err.println("Error in Maximum. Returning -1.");
			e.printStackTrace();
			return -1; 
		}
	}
	
	public static double returnVariance(double[] data) {
		if (dataArrayNull(data)) {
			return -1;
		}
		try {
			return flanagan.analysis.Stat.variance(data);
		} catch (Exception e) {
			System.err.println("Error in Variance. Returning -1.");
			e.printStackTrace();
			return -1; 
		}
	}
	
	public static double returnInterQuartileMean(double[] data) {
		if (dataArrayNull(data)) {
			return -1;
		}		
		try {
			return flanagan.analysis.Stat.interQuartileMean(data);
		} catch (Exception e) {
			System.err.println("Error in Inter-Quartile Mean. Returning -1.");
			e.printStackTrace();
			return -1; 
		}
	}
	
	public static double returnPercentile(double[] data, double rank) {
		if (dataArrayNull(data)) {
			return -1;
		}
		if (rank < 0) {
			System.err.println("Rank: " + rank + " is negative. Returning -1.");
			return -1;
		}		
		try {
			return percentile.evaluate(data, rank);
		} catch (Exception e) {
			System.err.println("Error in Percentile. Rank: " + rank + ". Returning -1.");
			e.printStackTrace();
			return -1; 
		}
	}
	
	public static double[] returnCumulativeFractions(double[] data, int[] ranges) {
		if (dataArrayNull(data)) {
			System.err.println("Data array is null. Returning null.");
			return null;
		}
		else if (ranges == null) {
			System.err.println("Ranges array is null. Returning null.");
			return null;
		}
		if (checkRanges(ranges) == false) {
			System.err.println("Check ranges failed. Returning null.");
			return null;
		}	
		try {
			//result's length is ranges' length + 1 as it includes the maximum double as the last argument.
			double[] result = new double[ranges.length+1];
			for (int i=0; i<result.length; i++) {
				result[i] = 0;
			}		
			int rangeLastIndex = ranges.length-1;		
			for (int i=0; i<data.length; i++) {
				double val = data[i];
				if (val > ranges[rangeLastIndex]) {
					result[rangeLastIndex+1]++;
					continue;
				}			
				for (int r=0; r<ranges.length; r++) {
					if (val <= ranges[r]) {
						result[r]++;
						break;
					}
				}
			}		
			double length = (double) data.length;
			for (int i=0; i<result.length; i++) {
				result[i] /= length;
			}		
			return result;
		} catch (Exception e) {
			System.err.println("Error in cumulative fractions calculation. Return null. "); 
			e.printStackTrace();
			return null;
		}
	}
	
	public static double returnSkewnewss(double[] data) {
		if (dataArrayNull(data)) {
			return -1;
		}		
		try {
			return skew.evaluate(data);		
		} catch (Exception e) {
			System.err.println("Error in Skewness. Returning -1.");
			e.printStackTrace();
			return -1; 
		}
	}
	
	public static boolean checkRanges(int[] ranges) {
		boolean rangeOK = true;
		for (int i=0; i<ranges.length; i++) {
			for (int j=0; j<i; j++) {
				if (ranges[j] > ranges[i]) {
					System.err.println("Invalid ranges. All ranges values should be strictly monotonic.  " + ranges[j] + " > " + ranges[i] + ". Returning null.");
					rangeOK = false;
					break;
				}
			}
		}		
		return rangeOK;
	}	
	
	public static boolean dataArrayNull(double[] data) {
		if (data == null) {
			System.err.println("Data array is null. Returning -1.");
			return true;			
		}
		else {
			return false;
		}
	}
	
	/** Returns the probability given the rate*/
	public static double returnProbability(double rate) {
		double dt = 0.028571428571;
		double d =(1-Math.exp(-rate*dt)); 
		return d;
	}
	/** */
	public static double returnAHIDuration() {
		return psr.nextErlang(2, 4); 
	}
	/** */
	public static double returnCHIDuration() {
		//KURT: 0.503767378
		//SKEW: 0.561183659
		//		return psr.nextErlang(1, 7); 
		//		return psr.nextErlang(3, 25); cappa
		return psr.nextErlang(3, 24);
	}
	
	public static void main(String[] args) {
		int numData = 100;
		double[] data = new double[numData];
		for (int i=0; i<numData; i++) {
			do {
				data[i] = (int)Normal.staticNextDouble(5, 3);
			} while(data[i] <= 0);
			System.out.println(data[i]);
		}		
		
		int[] ranges = {1, 2, 5, 10};		
		double[] cumfrac = Stats.returnCumulativeFractions(data, ranges);
		for (int i=0; i<cumfrac.length; i++) {
			double cf = 0;
			for (int j=0; j<=i; j++) {
				cf += cumfrac[j];
			}
			System.out.println(cf);
		}
	}
}