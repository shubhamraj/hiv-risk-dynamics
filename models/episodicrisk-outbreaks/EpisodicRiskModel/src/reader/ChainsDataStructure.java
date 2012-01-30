package reader;

import cluster.Edge;
import model.Individual;

/**
 * 
 * @author shah
 *
 */
public class ChainsDataStructure {
	private Individual leaf; 
	private Edge transEdge;
	private double numAcutesOnPath;
	private double numChronicsOnPath;
	//time of infection of leaf - time of infection of root
	private double branchLength;	
	
	public ChainsDataStructure(Individual _leaf, Edge _transEdge) {
		this.leaf = _leaf;
		this.transEdge = _transEdge;
		numAcutesOnPath = 0;
		numChronicsOnPath = 0;
		branchLength = 0;		
	}

	public double getNumAcutesOnPath() {
		return numAcutesOnPath;
	}

	public void setNumAcutesOnPath(double numAcutesOnPath) {
		this.numAcutesOnPath = numAcutesOnPath;
	}

	public double getNumChronicsOnPath() {
		return numChronicsOnPath;
	}

	public void setNumChronicsOnPath(double numChronicsOnPath) {
		this.numChronicsOnPath = numChronicsOnPath;
	}

	public double getBranchLength() {
		return branchLength;
	}

	public void setBranchLength(double branchLength) {
		this.branchLength = branchLength;
	}

	public Individual getLeaf() {
		return leaf;
	}

	public Edge getTransEdge() {
		return transEdge;
	}	
}