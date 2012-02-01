package plfit;

import java.util.ArrayList;

import basemodel.Parameters;


public class ExtStats extends Parameters {	
	/*	M: total number of elements
		N: total number of groups
		kmax: the number of elements in the largest group
		k0: the number of elements in the smallest group in your data set
	 */
	private double M; 
	private double N;
	private double kmax;
	private double kmin;
	private ArrayList<Double> outbreakSizes;
	
	public ExtStats() {
		M = 0d;
		N = 0d;
		kmax = 0d;
		kmin = 0d;
		outbreakSizes = new ArrayList<Double>();
	}
	
	public double getM() {
		return M;
	}

	public void setM(double m) {
		M = m;
	}

	public double getN() {
		return N;
	}

	public void setN(double n) {
		N = n;
	}

	public double getKmax() {
		return kmax;
	}

	public void setKmax(double kmax) {
		this.kmax = kmax;
	}

	public double getKmin() {
		return kmin;
	}

	public void setKmin(double kmin) {
		this.kmin = kmin;
	}

	public ArrayList<Double> getOutbreakSizes() {
		return outbreakSizes;
	}

	public void setOutbreakSizes(ArrayList<Double> outbreakSizes) {
		this.outbreakSizes = outbreakSizes;
	}
}