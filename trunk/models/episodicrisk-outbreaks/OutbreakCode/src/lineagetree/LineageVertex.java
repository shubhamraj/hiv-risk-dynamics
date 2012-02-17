package lineagetree;

import interfaces.AgentInterface;
import interfaces.ParametersInterface.LineageEventType;

/**
 * 
 * @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 *
 */
public class LineageVertex {
	private static int lastID = 0;
	private int ID;
	private LineageEventType lineageEventType = LineageEventType.Transmission; 
	private double eventTime;
	private AgentInterface infector; 
	private AgentInterface infected;
	
	public LineageVertex(double eventTime, AgentInterface infector, AgentInterface infected) {
		this.ID = ++lastID;
		this.eventTime = eventTime;
		this.infector = infector;
		this.infected = infected;
		this.lineageEventType = LineageEventType.Transmission;
	}
	
	public LineageVertex(double eventTime, AgentInterface agent) {
		this.ID = ++lastID;
		this.eventTime = eventTime;
		this.infector = agent;
		this.lineageEventType = LineageEventType.Exit;
		this.infected = null;
	}

	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public LineageEventType getLineageEventType() {
		return lineageEventType;
	}
	public void setLineageEventType(LineageEventType eventType) {
		this.lineageEventType = eventType;
	}
	public double getEventTime() {
		return eventTime;
	}
	public void setEventTime(double eventTime) {
		this.eventTime = eventTime;
	}
	public AgentInterface getInfector() {
		return infector;
	}
	public void setInfector(AgentInterface infector) {
		this.infector = infector;
	}
	public AgentInterface getInfected() {
		return infected;
	}
	public void setInfected(AgentInterface infected) {
		this.infected = infected;
	}
}