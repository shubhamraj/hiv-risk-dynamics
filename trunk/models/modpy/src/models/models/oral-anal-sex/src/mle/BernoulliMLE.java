package mle;
/* Michael Thomas Flanagan
http://www.ee.ucl.ac.uk/~mflanaga/java/Regression.html
April 2007
 */

import java.util.Vector;

import reader.Reader;
import flanagan.analysis.Regression;
import flanagan.analysis.RegressionFunction;

class Function implements RegressionFunction{
	public double function(double[] p, double[] x){
		double y = 1 - (Math.pow(1-p[0],x[0])*Math.pow(1-p[1],x[1]));	  			
//		double y = 1 - Math.pow(1-p[0],x[0]);
		return y;
	}
	
}

public class BernoulliMLE{
	private int numDataPoints = 0;
	private double[] analSexFrequency;
	private double[] oralSexFrequency;
	private double[] seroconversion;
	private double[] start = {0.01, 0.01};
	private double[] step = {0.5, 0.5};
//	private double[] start = {0.01};
//	private double[] step = {0.05D};

	private Regression regression;

	public BernoulliMLE(int _numDataPoints) {
		numDataPoints = _numDataPoints;
		analSexFrequency = new double[numDataPoints];
		oralSexFrequency = new double[numDataPoints];
		seroconversion = new double[numDataPoints];
	}
	
	public void fn() {
		double ll = 0; double theta = 0.001;
		for (int i=0; i<numDataPoints; i++) {
//			double p1 = Math.pow(1-theta, analSexFrequency[i]); 
//			ll *= Math.pow(1-p1, seroconversion[i]) * Math.pow(p1,1-seroconversion[i]);
			double l = 0;
			if (seroconversion[i] == 0.0) {
				l = Math.log(Math.pow(1-theta, analSexFrequency[i]));
				System.out.println("i: " + i + " l: " + l + " - " + analSexFrequency[i]);
			}
			else {
				l = Math.log(1-Math.pow(1-theta, analSexFrequency[i]));
				System.out.println("i: " + i + " l: " + l + " - " + analSexFrequency[i]);
			}
//			ll += seroconversion[i] * Math.log(1-(Math.pow(1-theta, analSexFrequency[i]))) 
//					+ (1-seroconversion[i]) * Math.log(1-(Math.pow(theta, analSexFrequency[i])));
			ll+=l;
		}
		System.out.println("ll: " + ll);
	}

	public void bernoulliRegression() {        
		double[][] doubleArray = new double[2][numDataPoints];
		for(int i=0; i<numDataPoints; i++){
			doubleArray[0][i] = analSexFrequency[i];
			doubleArray[1][i] = oralSexFrequency[i];
		}
		Function function = new Function();
		regression = new Regression(doubleArray, seroconversion);
//		regression = new Regression(analSexFrequency, seroconversion);
		regression.simplexPlot(function, start,step);
		double[] coeff = regression.getBestEstimates();                     
		System.out.println("analInfectivity: " + coeff[0]);
		System.out.println("oralInfectivity: " + coeff[1]);
	}

	public static void main(String[] arg){
		BernoulliMLE bmle = new BernoulliMLE(3889);
		Reader reader = new Reader();
		reader.read("./src/vt-processed-R.csv");
		Vector<String> input = new Vector<String>();
		int index = 0;
		for (int i=1; i<=reader.getLineNo(); i++) {
			input = reader.getDataSet().get(new Integer(i));
			bmle.getSeroconversion()[index] = Double.valueOf(input.get(1)).doubleValue();
			bmle.getAnalSexFrequency()[index] = Double.valueOf(input.get(2)).doubleValue();
			bmle.getOralSexFrequency()[index] = Double.valueOf(input.get(3)).doubleValue();
			index++;
		}
		bmle.bernoulliRegression();		
		bmle.fn();
	}

	public int getNumDataPoints() {
		return numDataPoints;
	}

	public void setNumDataPoints(int numDataPoints) {
		this.numDataPoints = numDataPoints;
	}

	public double[] getAnalSexFrequency() {
		return analSexFrequency;
	}

	public void setAnalSexFrequency(double[] analSexFrequency) {
		this.analSexFrequency = analSexFrequency;
	}

	public double[] getOralSexFrequency() {
		return oralSexFrequency;
	}

	public void setOralSexFrequency(double[] oralSexFrequency) {
		this.oralSexFrequency = oralSexFrequency;
	}

	public double[] getSeroconversion() {
		return seroconversion;
	}

	public void setSeroconversion(double[] seroconversion) {
		this.seroconversion = seroconversion;
	}

	public double[] getStart() {
		return start;
	}

	public void setStart(double[] start) {
		this.start = start;
	}

	public double[] getStep() {
		return step;
	}

	public void setStep(double[] step) {
		this.step = step;
	}

	public Regression getRegression() {
		return regression;
	}

	public void setRegression(Regression regression) {
		this.regression = regression;
	}
}