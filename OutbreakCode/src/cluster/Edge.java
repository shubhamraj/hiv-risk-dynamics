/**
 * Edge.java
 * 
 */

package cluster;
import interfaces.TransmissionInterface;

import java.awt.Color;

import episodicriskmodel.EpisodicRiskTransmission;
import uchicago.src.sim.network.DefaultEdge;
import uchicago.src.sim.network.Node;

/**
 * 
 * @author shah
 *
 */
public class Edge extends DefaultEdge {
	public static int nextID = 0;
	public Color color;
	private int ID;
	private TransmissionInterface transmission;

	public Edge() {
		this.ID = nextID++;
	}
	
	public Edge(TransmissionInterface _transmission) {
		this.ID = nextID++;
		this.transmission = _transmission;
	}

	public Edge(Node toAgent, Node fromAgent) {
		super(toAgent, fromAgent);
		this.ID = nextID++;		
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
	
	public TransmissionInterface getTransmission() {
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