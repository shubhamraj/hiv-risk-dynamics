package cluster;

import interfaces.AgentInterface;

import java.util.ArrayList;



/**
 * 
 * @author shah
 *
 */
public class Cluster {	
	private int id = -1;
	private double startTime = -1; 
	private ArrayList<Double> members;
	private int timeLastTransmission = 0;
	private int totalTransmissions = 0;
	private int currentAge = 0;
	
	public Cluster(double _time, int _id) {
		this.id = _id;
		startTime = _time;
		members = new ArrayList<Double>();
	}
	
	public void addMember(AgentInterface individual) {
		Double id = new Double(individual.getID());
		if (members.contains(id) == false) {
			members.add(id);
			individual.setAHIClusterID(this.id);
			individual.setOutbreakStartTime((int)startTime);
		}
		else {
			System.err.println("ID: " + id + " couldn't be added.");
		}
	}
	
	public void removeMember(AgentInterface individual) {
		Double id = new Double(individual.getID());
		if (members.contains(id) == true) {
			members.remove(id);
			individual.setRemovedAHICluster(true);
		}
		else {
			System.err.println("ID: " + id + " couldn't be removed.");
			System.err.println("Time stamp: " + startTime  
					+ " cluster size: " + members.size()
					+ " cluster members: " + members.toString()
					);
			System.exit(1);
		}
	}
	
	public void step(Integer time, AgentInterface ... members) {
		for (int i=0; i<members.length; i++) {
			addMember(members[i]);
		}	
		setTimeLastTransmission(time);
		incrementTransmissions();
		updateAge(time);
	}
	
	public void incrementTransmissions() {
		totalTransmissions++;
	}
	
	public void updateAge(int curTime) {
		currentAge = (int) (curTime - startTime);
	}
	
	public int returnTotalTransmissions () {
		return totalTransmissions;
	}
	
	public int returnMembersSize() {
		return this.members.size();
	}

	public double getStartTime() {
		return startTime;
	}

	public void setStartTime(double timestep) {
		this.startTime = timestep;
	}

	public ArrayList<Double> getMembers() {
		return members;
	}

	public void setMembers(ArrayList<Double> members) {
		this.members = members;
	}

	public int getId() {
		return id;
	}
	public int getTimeLastTransmission() {
		return timeLastTransmission;
	}

	public void setTimeLastTransmission(int timeLastTransmission) {
		this.timeLastTransmission = timeLastTransmission;
	}

	public int returnCurrentAge() {
		return currentAge;
	}

	public void setCurrentAge(int currentAge) {
		this.currentAge = currentAge;
	}
}