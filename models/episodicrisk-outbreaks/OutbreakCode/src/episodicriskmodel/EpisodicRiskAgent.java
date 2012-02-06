package episodicriskmodel;



import interfaces.AgentInterface;
import cern.jet.random.Uniform;

/**
 * 
 * @author shah
 *
 */
public class EpisodicRiskAgent implements EpisodicAgentInterface {
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

	boolean removedAHICluster = false;	
	boolean dead = false;	
	boolean root = false;

	private int entryTick = -1;
	private int exitTick = -1;
	
	private RISK_STATE riskState = RISK_STATE.None;		
	private RISK_STATE infectedRiskState = RISK_STATE.None;
	private MIXING_SITE infectedMixingSite = MIXING_SITE.None;

	public EpisodicRiskAgent() {		
		this.ID = ++lastID;
	}

	public EpisodicRiskAgent(int id) {
		this.ID = id;
	}

	public void step(int currentTick) {
		if (Uniform.staticNextDouble() <= ((double)1/DurationLife)) {
			dead = true;
			return;
		}
		if (stageOfInfection != InfectionStage.Susceptible) {
			updateInfectionStatus(currentTick);
		}
	}

	public void updateInfectionStatus(int currentTick) {
		double rand = Uniform.staticNextDouble();
		if (stageOfInfection.equals(InfectionStage.Acute)
				&& rand <= ((double)1/DurationAHI)) {
			stageOfInfection = InfectionStage.Chronic;
			setCHITick(currentTick);
		}

		else if (stageOfInfection.equals(InfectionStage.Chronic) 
				&& rand <= ((double)1/DurationCHI)) {
			setDead(true);		
		} 	
	}
	
	/** This method is called to reset an agent's record of 
	 * a membership in an early infection outbreak. 
	 * An implementation of this method must call the following methods. 
	 * 		setAHIClusterID(-1);
			setRemovedAHICluster(false);
			setOutbreakStartTime(-1);
	 */
	public void resetOutbreakRecord() {
		setAHIClusterID(-1);
		setRemovedAHICluster(false);
		setOutbreakStartTime(-1);
	}
	
	/** Returns if the agent is in acute stage of infection.*/
	public boolean isAHI() {
		return stageOfInfection.equals(InfectionStage.Acute);
	}

	public boolean isInfected() {
		return stageOfInfection.equals(InfectionStage.Acute) || stageOfInfection.equals(InfectionStage.Chronic);
	}

	public boolean isSusceptible() {
		return stageOfInfection.equals(InfectionStage.Susceptible);
	}

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

	public int returnInfectionDuration() {
		return (timeLastInfection - timePenultimateInfection + 1);
	}

	public boolean infectedByAHI() {
		return infectorStageOfInfection.equals(InfectionStage.Acute) ? true : false;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean exit) {
		this.dead = exit;
	}

	public InfectionStage getInfectorInfectionStage() {
		return infectorStageOfInfection;
	}

	public void setInfectorStageOfInfection(InfectionStage infectorStatus) {
		this.infectorStageOfInfection = infectorStatus;
	}

	public Integer getAHIClusterID() {
		return ahiClusterID;
	}

	public void setAHIClusterID(Integer ahiCluster) {
		this.ahiClusterID = ahiCluster;
	}


	public void setStageOfInfection(InfectionStage infectionStatus) {
		this.stageOfInfection = infectionStatus;
	}

	public boolean isRemovedAHICluster() {
		return removedAHICluster;
	}

	public void setRemovedAHICluster(boolean removedAHICluster) {
		this.removedAHICluster = removedAHICluster;
	}

	public String toString() {
		return "Individual-" + ID;
	}

	public int getOutbreakStartTime() {
		return outbreakStartTime;
	}

	public void setOutbreakStartTime(int outbreakStartTime) {
		this.outbreakStartTime = outbreakStartTime;
	}

	public boolean equals(EpisodicRiskAgent person) {
		return this.ID == person.getID() ? true : false;
	}

	public int getID() {
		return ID;
	}

	public void setID(int id) {
		this.ID = id;
	}

	public int getInfectedTick() {
		return infectedTick;
	}

	public void setInfectedTick(int infectedTick) {
		this.infectedTick = infectedTick;
	}

	public int getInfectorID() {
		return infectorID;
	}

	public void setInfectorID(int infectorID) {
		this.infectorID = infectorID;
	}

	public int getCHITick() {
		return CHITick;
	}

	public void setCHITick(int cHITick) {
		CHITick = cHITick;
	}

	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean root) {
		this.root = root;
	}

	public InfectionStage getStageOfInfection() {
		return stageOfInfection;
	}

	@Override
	public boolean equals(AgentInterface agent) {
		return this.ID == agent.getID() ? true : false;
	}

	@Override
	public ActType getActType() {
		return actType;
	}

	@Override
	public void setActType(ActType actType) {
		this.actType = actType;
	}

	public RISK_STATE getRiskState() {
		return riskState;
	}

	public void setRiskState(RISK_STATE riskState) {
		this.riskState = riskState;
	}
	
	public RISK_STATE getInfectedRiskState() {
		return infectedRiskState;
	}

	public void setInfectedRiskState(RISK_STATE infectedRiskState) {
		this.infectedRiskState = infectedRiskState;
	}

	public MIXING_SITE getInfectedMixingSite() {
		return infectedMixingSite;
	}

	public void setInfectedMixingSite(MIXING_SITE infectedMixingSite) {
		this.infectedMixingSite = infectedMixingSite;
	}

	public int getExitTick() {
		return exitTick;
	}

	public void setExitTick(int exitTick) {
		this.exitTick = exitTick;
	}
	
	public int getEntryTick() {
		return entryTick;
	}

	public void setEntryTick(int entryTick) {
		this.entryTick = entryTick;
	}

	@Override
	public void print() {
		System.out.println("Person-" + this.ID);
	}

	public Integer getAhiClusterID() {
		return ahiClusterID;
	}

	public void setAhiClusterID(Integer ahiClusterID) {
		this.ahiClusterID = ahiClusterID;
	}

	public int getTimeLastInfection() {
		return timeLastInfection;
	}

	public void setTimeLastInfection(int timeLastInfection) {
		this.timeLastInfection = timeLastInfection;
	}

	public int getTimePenultimateInfection() {
		return timePenultimateInfection;
	}

	public void setTimePenultimateInfection(int timePenultimateInfection) {
		this.timePenultimateInfection = timePenultimateInfection;
	}

	@Override
	public void setInfectorInfectionStage(InfectionStage infectorStatus) {
		this.infectorStageOfInfection = infectorStatus;
	}	
}