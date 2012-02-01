package basemodel;

import basemodel.ParametersInterface.ActType;
import basemodel.ParametersInterface.InfectionStage;

/**
 * 
 * @author shah 
 *
 */
public interface AgentInteface {
	/** Returns this agent's ID*/
	public int getID();
	/** Sets this agent's ID*/
	public void setID(int id);
	/** Returns true if the agent is in acute stage of infection; false otherwise. */
	public boolean isAHI();
	/** Returns true if the agent is infected (not susceptible); false otherwise. */
	public boolean isInfected();
	/** Returns true if the agent is currently susceptible; false otherwise*/
	public boolean isSusceptible();
	/** Sets the timePenultimateInfection and timeLastInfection for this agent who has transmitted infection
	 * to a susceptible at the @param currentTime. */
	public void setInfectionTimes(int currentTime);
	/** Returns the time between the last infection and the penultimate infection, if any, for this agent.*/
	public int returnInfectionDuration();
	/** Returns <code>true</code> if this agent was infected by an agent in AHI; <code>false</code> otherwise. */
	public boolean infectedByAHI();
	/** Returns <code>true</code> if this agent is now dead; <code>false</code> otherwise. */
	public boolean isDead();
	/** Returns <code>true</code> if this agent is now removed from a AHI cluster; <code>false</code> otherwise. */
	public boolean isRemovedAHICluster();
	/** Sets this agent's removal from its AHI cluster.*/
	public void setRemovedAHICluster(boolean removedAHICluster);
	/** Sets this agent @param dead */
	public void setDead(boolean dead);
	/** Returns this agent's infector's stage of infection <code>InfectionStage</code>*/
	public InfectionStage getInfectorInfectionStage();
	/** Sets this agent's infector's stage of infection <code>InfectionStage</code>*/
	public void setInfectorInfectionStage(InfectionStage infectorStage);
	/** Returns this agent's AHI cluster's ID. */
	public Integer getAHIClusterID();
	/** Sets this agent's AHI cluster's ID. */
	public void setAHIClusterID(Integer ahiCluster);
	/** Returns this agent's stage of infection <code>InfectionStage</code>*/
	public InfectionStage getStageOfInfection();
	/** Sets this agent's stage of infection <code>InfectionStage</code>*/
	public void setStageOfInfection(InfectionStage infectionStage);
	/** Returns the time when this agent's AHI outbreak cluster started.*/
	public int getOutbreakStartTime();
	/** Sets the time when this agent's AHI outbreak cluster started.*/
	public void setOutbreakStartTime(int outbreakStartTime); 
	/** Returns the time when this agent got infected; for susceptible, this function returns -1.*/
	public int getInfectedTick(); 
	/** Sets the time when this agent got infected. */
	public void setInfectedTick(int infectedTick);
	/** Returns the ID of the infector for this agent; if this agent is susceptible, this function returns -1.*/
	public int getInfectorID();
	/** Sets the ID of the infector for this agent. */
	public void setInfectorID(int infectorID);
	/** Returns the time when this agent progressed to CHI stage from AHI; if this agent is susceptible, this function returns -1.*/
	public int getCHITick();
	/** Sets the time when this agent progressed to CHI stage from AHI.*/
	public void setCHITick(int cHITick);
	/** Returns <code>true</code> if this agent is a root of a transmission tree in the infection forest; <code>false</code> otherwise. */	
	public boolean isRoot();
	/** Sets if this agent is a root of a transmission tree in the infection forest. */
	public void setRoot(boolean root);
	/** Returns the ActType for which this agent got infected; Returns NONE if susceptible. */
	public ActType getActType();
	/** Sets the ActType for which this agent got infected. */
	public void setActType(ActType actType);
	/** Returns <code>true</code> if the agent in the argument is same; <code>false</code> otherwise. */
	public boolean equals(AgentInteface agent);
	/** Returns the time of last infection transmission event by this agent. */
	public int getTimeLastInfection();
	/** Sets the time of last infection transmission event by this agent. */
	public void setTimeLastInfection(int timeLastInfection);
	/** Returns the time of penultimate infection transmission event by this agent. */
	public int getTimePenultimateInfection();
	/** Sets the time of penultimate infection transmission event by this agent. */
	public void setTimePenultimateInfection(int timePenultimateInfection);
	/** This method is called to reset an agent's record of 
	 * a membership in an early infection outbreak. 
	 * An implementation of this method must call the following methods. 
	 * 		setAHIClusterID(-1);
			setRemovedAHICluster(false);
			setOutbreakStartTime(-1);
	 * */
	public void resetOutbreakRecord();
	public void print();
}