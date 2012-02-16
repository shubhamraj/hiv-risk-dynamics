package lineagetree;

/**
 * 
 * @author shah
 *
 */
public class LineageEdge {
	private static int lastID = 0;	
	private int ID;
	private double timeOfInfection;
	private double length;

	public LineageEdge(double timeOfInfection) {
		ID = ++lastID;		
		this.timeOfInfection = timeOfInfection;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public double getTimeOfInfection() {
		return timeOfInfection;
	}

	public void setTimeOfInfection(double timeOfInfection) {
		this.timeOfInfection = timeOfInfection;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}
}