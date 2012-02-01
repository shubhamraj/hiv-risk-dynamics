package reader;



import interfaces.ParametersInterface;
import cern.jet.random.Normal;

/**
 * Contains static functions for various statistics used for outbreak distributions.
 * Uses MathCommons, Colt and Flanagan libraries.
 *
 */
public class Stats implements ParametersInterface {	
	public static double returnMean(double[] data) {
		if (data != null) {
			return flanagan.analysis.Stat.mean(data);
		}
		else {
			System.err.println("Data array is null. Returning -1.");
			return -1;
		}
	}
	
	public static double returnMedian(double[] data) {
		if (data != null) {
			return flanagan.analysis.Stat.median(data);
		}
		else {
			System.err.println("Data array is null. Returning -1.");
			return -1;
		}
	}

	public static double returnMaximum(double[] data) {
		if (data != null) {
			return flanagan.math.Fmath.maximum(data);
		}
		else {
			System.err.println("Data array is null. Returning -1.");
			return -1;
		}
	}
	
	public static double returnVariance(double[] data) {
		if (data != null) {
			return flanagan.analysis.Stat.variance(data);
		}
		else {
			System.err.println("Data array is null. Returning -1.");
			return -1;
		}
	}
	
	public static double returnInterQuartileMean(double[] data) {
		if (data != null) {
			return flanagan.analysis.Stat.interQuartileMean(data);
		}
		else {
			System.err.println("Data array is null. Returning -1.");
			return -1;
		}
	}
	
	public static double returnPercentile(double[] data, double rank) {
		if (data != null && rank >= 0) {
			return percentile.evaluate(data, rank);
		}
		else if (data == null){
			System.err.println("Data array is null. Returning -1.");
			return -1;
		}
		else if (rank < 0) {
			System.err.println("Rank is negative. Returning -1.");
			return -1;
		}
		else {
			System.err.println("Invalid input. Returning -1.");
			return -1;
		}
	}
	
	public static double[] returnCumulativeFractions(double[] data, int[] ranges) {
		if (data == null){
			System.err.println("Data array is null. Returning null.");
			return null;
		}
		else if (ranges == null) {
			System.err.println("Ranges array is null. Returning null.");
			return null;
		}
		if (checkRanges(ranges) == false) {
			return null;
		}		
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
	}
	
	public static double returnSkewnewss(double[] data) {
		if (data != null) {
			return skew.evaluate(data);		
		}
		else {
			System.err.println("Data array is null. Returning -1.");
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