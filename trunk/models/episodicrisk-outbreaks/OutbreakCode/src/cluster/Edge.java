/**
 * Edge.java
 * 
 */

package cluster;

import java.awt.Color;
import episodicriskmodel.EpisodicRiskTransmission;

/**
 * 
 * @author shah
 *
 */
public class Edge {
	public static int nextID = 0;
	public Color color;
	private int ID;
	private EpisodicRiskTransmission transmission;

	public Edge() {
		this.ID = nextID++;
	}
	
	public Edge(EpisodicRiskTransmission _transmission) {
		this.ID = nextID++;
		this.transmission = _transmission;
	}

	public void print() {
		System.out.println("ID:" + this.getID());
	}

	public void setColor(Color _color) {
		color = _color;
	}
		
	public int getID() {
		return ID;
	}
	
	public EpisodicRiskTransmission getTransmission() {
		return transmission;
	}
	
	public void setTransmission(EpisodicRiskTransmission transmission) {
		this.transmission = transmission;
	}
	
	public Color getColor() {
		return color;
	}
	
	public String toString() {
		return transmission.toString();
	}
}