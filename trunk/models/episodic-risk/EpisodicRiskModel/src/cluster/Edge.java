/**
 * Edge.java
 * 
 */

package cluster;

import java.awt.Color;

import model.Individual;

/**
 * 
 * @author shah
 *
 */
public class Edge  {
	public static int nextID = 0;
	public Color  color;
	private int ID;
	private Transmission transmission;

	public Edge() {
		this.ID = nextID++;
	}
	
	public Edge(Transmission _transmission) {
		this.ID = nextID++;
		this.transmission = _transmission;
	}

	public Edge( Individual from, Individual to, double risk, Color color ) {
		this.ID = nextID++;
		this.color = color;		
	}
	
	public Edge( Individual from, Individual to ) {
		this.ID = nextID++;
	}

	public void print(String str) {
		System.out.println(str);
	}
	
	public void print () {
		System.out.println("ID:" + this.getID());
	}

	public void setColor( Color c ) {
		color = c;
	}
		
	public int getID () {
		return ID;
	}
	
	public void setID ( int i ) {
		ID = i;
	}
	
	public Transmission getTransmission() {
		return transmission;
	}
	public void setTransmission(Transmission transmission) {
		this.transmission = transmission;
	}
	public Color getColor() {
		return color;
	}
	public String toString() {
		//return "" + this.transmission.getTime();
		return "" + this.transmission.getBranchTime();
	}
}