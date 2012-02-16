package reader;

import interfaces.AgentInterface;
import cluster.Edge;

/**
 * 
 * @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 *
 */
public class ChainsDataStructure {
	private AgentInterface leafNode; 
	private Edge transEdge;
	private double numAcutesOnPath;
	private double numChronicsOnPath;
	/** time of infection of leaf - time of infection of root */
	private double branchLength;	
	
	public ChainsDataStructure(AgentInterface _leaf, Edge _transEdge) {
		this.leafNode = _leaf;
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

	public AgentInterface getLeafNode() {
		return leafNode;
	}

	public Edge getTransEdge() {
		return transEdge;
	}	
}