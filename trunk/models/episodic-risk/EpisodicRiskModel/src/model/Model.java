package model;

import java.util.ArrayList;
import cluster.ClusterRecorder;

/**
 * 
 * @author shah
 *
 */
public class Model extends Parameters {
	/** AHI transmission potential */
	double ahiTransPotential;
	double beta1; 
	double beta2;
	ArrayList<Individual> individuals;
	int currentTick;
	
	ArrayList<String> output;
	OUTBREAK_TYPE outbreakType;	
	/** Cluster Recorder array for one or more recording outbreaks, e.g. early period and transient*/
	ClusterRecorder clusterRecorder;
	
	int run;
	String prefix;
	int numHIV;
	int maxIterations;

	public Model(int _run, String _prefix, OUTBREAK_TYPE _outbreakType) {
		this.run = _run;
		this.prefix = _prefix;
		this.outbreakType = _outbreakType;
		this.currentTick = -1;
		this.maxIterations = MAX_ITERATIONS;		
		if (output != null) {
			output.clear();
			output = null;
		}
		output = new ArrayList<String>();
		if (individuals != null) {
			individuals = null;
		}
		individuals = new ArrayList<Individual>(initialPopulation);		
		if (clusterRecorder != null) {
			clusterRecorder = null;
		}		
		/** Cluster Recorder*/
		clusterRecorder = new ClusterRecorder(this, this.prefix, this.outbreakType);
	}

	public ArrayList<Individual> getIndividuals() {
		return individuals;
	}

	public void setIndividuals(ArrayList<Individual> individuals) {
		this.individuals = individuals;
	}

	public ArrayList<String> getOutput() {
		return output;
	}

	public void setOutput(ArrayList<String> output) {
		this.output = output;
	}

	public OUTBREAK_TYPE getOutbreakType() {
		return outbreakType;
	}

	public void setOutbreakType(OUTBREAK_TYPE outbreakType) {
		this.outbreakType = outbreakType;
	}

	public ClusterRecorder getClusterRecorder() {
		return clusterRecorder;
	}

	public void setClusterRecorder(ClusterRecorder clusterRecorder) {
		this.clusterRecorder = clusterRecorder;
	}

	public int getRun() {
		return run;
	}

	public void setRun(int run) {
		this.run = run;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public int getNumHIV() {
		return numHIV;
	}

	public void setNumHIV(int numHIV) {
		this.numHIV = numHIV;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public double getAhiTransPotential() {
		return ahiTransPotential;
	}

	public int getCurrentTick() {
		return currentTick;
	}

	public void setCurrentTick(int currentTick) {
		this.currentTick = currentTick;
	}
}