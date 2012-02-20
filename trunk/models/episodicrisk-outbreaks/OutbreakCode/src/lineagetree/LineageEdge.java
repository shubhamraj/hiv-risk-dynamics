package lineagetree;

/**
 * @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 * 
 */
public class LineageEdge {
	private static int lastID = 0;	
	private int ID;
	private double branchLength;
	private double length;

	public LineageEdge(double branchLength) {
		ID = ++lastID;		
		this.branchLength = branchLength;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public double getBranchLength() {
		return branchLength;
	}

	public void setBranchLength(double timeOfInfection) {
		this.branchLength = timeOfInfection;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}
}