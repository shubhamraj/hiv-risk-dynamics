package basemodel;

import basemodel.ParametersInterface.ActType;
import basemodel.ParametersInterface.InfectionStage;

public interface AgentInteface {
	public void step(int currentTick);
	public void updateInfectionStatus(int currentTick);
	public void resetOutbreakRecord(); 
	public boolean isAHI();
	public boolean isInfected();
	public boolean isSusceptible();
	public void setInfectionTimes(int currentTime);
	public int returnInfectionDuration();
	public boolean infectedByAHI();
	public boolean isDead();
	public void setDead(boolean dead);
	public InfectionStage getInfectorStatus();
	public void setInfectorStatus(InfectionStage infectorStatus);
	public Integer getAHIClusterID();
	public void setAHIClusterID(Integer ahiCluster);
	public void setStageOfInfection(InfectionStage infectionStatus);
	public boolean isRemovedAHICluster();
	public void setRemovedAHICluster(boolean removedAHICluster);	
	public int getOutbreakStartTime();
	public void setOutbreakStartTime(int outbreakStartTime);
	public boolean equals(AgentInteface agent); 
	public int getID();
	public void setID(int id);
	public int getInfectedTick(); 
	public void setInfectedTick(int infectedTick);
	public int getInfectorID();
	public void setInfectorID(int infectorID);
	public int getCHITick();
	public void setCHITick(int cHITick);
	public boolean isRoot();
	public void setRoot(boolean root);
	public InfectionStage getStageOfInfection();
	public ActType getActType();
	public void setActType(ActType actType);
	public void print();
}
