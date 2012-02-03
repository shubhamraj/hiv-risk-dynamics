package partnership;

import interfaces.AgentInteface;
import interfaces.ParametersInterface.ActType;
import interfaces.ParametersInterface.InfectionStage;
import interfaces.ParametersInterface.MIXING_SITE;
import interfaces.ParametersInterface.RISK_STATE;

public class JongHoonAgent implements JongHoonInterface {
	static int lastID = -1;
	private int ID = -1;
	private InfectionStage stageOfInfection = InfectionStage.Susceptible;
	private InfectionStage infectorStageOfInfection = InfectionStage.Susceptible;
	private ActType actType = ActType.None;

	private int infectedTick = -1;
	private int infectorID = -1;
	private int CHITick = -1;

	/**AHI Cluster ID of the individual */
	private Integer ahiClusterID = -1;
	private int timeLastInfection = 0;
	private int timePenultimateInfection = 0;
	private int outbreakStartTime = -1;

	private boolean removedAHICluster = false;	
	private boolean dead = false;	
	private boolean root = false;

	private int entryTick = -1;
	private int exitTick = -1;

	@Override
	public int getID() {
		return this.ID;
	}

	@Override
	public void setID(int id) {
		this.ID = id;
	}

	@Override
	public boolean isAHI() {
		return stageOfInfection.equals(InfectionStage.Acute);
	}

	@Override
	public boolean isInfected() {
		return stageOfInfection.equals(InfectionStage.Acute) || stageOfInfection.equals(InfectionStage.Chronic);
	}

	@Override
	public boolean isSusceptible() {
		return stageOfInfection.equals(InfectionStage.Susceptible);
	}

	@Override
	public void setInfectionTimes(int currentTime) {
		if (timePenultimateInfection == 0) {
			timePenultimateInfection = currentTime;
			timeLastInfection = currentTime;
			return;
		}
		else {
			timePenultimateInfection = timeLastInfection;
			timeLastInfection = currentTime;
		}
	}

	@Override
	public int returnInfectionDuration() {
		return (timeLastInfection - timePenultimateInfection + 1);
	}

	@Override
	public boolean infectedByAHI() {
		return infectorStageOfInfection.equals(InfectionStage.Acute) ? true : false;
	}

	@Override
	public boolean isDead() {
		return this.dead;
	}

	@Override
	public boolean isRemovedAHICluster() {
		return this.removedAHICluster;
	}

	@Override
	public void setRemovedAHICluster(boolean removedAHICluster) {
		this.removedAHICluster = removedAHICluster;
	}

	@Override
	public void setDead(boolean dead) {
		this.dead = dead;
	}

	@Override
	public InfectionStage getInfectorInfectionStage() {
		return this.infectorStageOfInfection;
	}

	@Override
	public void setInfectorInfectionStage(InfectionStage infectorStage) {
		this.infectorStageOfInfection = infectorStage;
	}

	@Override
	public Integer getAHIClusterID() {
		return this.ahiClusterID;
	}

	@Override
	public void setAHIClusterID(Integer ahiCluster) {
		this.ahiClusterID = ahiCluster;
	}

	@Override
	public InfectionStage getStageOfInfection() {
		return this.stageOfInfection;
	}

	@Override
	public void setStageOfInfection(InfectionStage infectionStage) {
		this.stageOfInfection = infectionStage;
	}

	@Override
	public int getOutbreakStartTime() {
		return this.outbreakStartTime;
	}

	@Override
	public void setOutbreakStartTime(int outbreakStartTime) {
		this.outbreakStartTime = outbreakStartTime;
	}

	@Override
	public int getInfectedTick() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setInfectedTick(int infectedTick) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getInfectorID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setInfectorID(int infectorID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getCHITick() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setCHITick(int cHITick) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRoot() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setRoot(boolean root) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ActType getActType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setActType(ActType actType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean equals(AgentInteface agent) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getTimeLastInfection() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTimeLastInfection(int timeLastInfection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getTimePenultimateInfection() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTimePenultimateInfection(int timePenultimateInfection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetOutbreakRecord() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void print() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getExitTick() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setExitTick(int exitTick) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEntryTick() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setEntryTick(int entryTick) {
		// TODO Auto-generated method stub
		
	}

}
