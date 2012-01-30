package model;

import cern.jet.random.Uniform;

/**
 * 
 * @author shah
 *
 */
public class Person extends Parameters {
	static int lastID = -1;
	int ID = -1;
	STAGE stageOfInfection = STAGE.SUSCEPTIBLE;
	STAGE infectorStatus = STAGE.SUSCEPTIBLE;
	ACT_TYPE actType = ACT_TYPE.NONE;

	int infectedTick = -1;
	int infectorID = -1;
	int CHITick = -1;

	/**AHI Cluster ID of the individual */
	Integer ahiClusterID = -1;
	int timeLastInfection = 0;
	int timePenultimateInfection = 0;
	int outbreakStartTime = -1;

	boolean removedAHICluster = false;	
	boolean dead = false;	
	boolean root = false;

	/*	private double phiDur = 0;
		private double postPHIDur = 0;*/


	public Person() {		
		this.ID = ++lastID;
		/*		this.phiDur = Parameters.returnPHIDuration() * 30;
		this.postPHIDur = Parameters.returnPostPHIDuration() * 12 * 30;*/
	}

	protected void step(int currentTick) {
		if (Uniform.staticNextDouble() <= ((double)1/Parameters.durationLife)) {
			dead = true;
			return;
		}
		if (stageOfInfection != STAGE.SUSCEPTIBLE) {
			updateInfectionStatus(currentTick);
		}
	}

	protected void updateInfectionStatus(int currentTick) {
		/*		if (stageOfInfection.equals(STAGE.PHI)
				&& currentTick - this.infectedTick >= this.phiDur) {
				stageOfInfection = STAGE.POST_PHI;
				this.CHITick = currentTick;
			}
		else if (stageOfInfection.equals(STAGE.POST_PHI) 
			&& currentTick - this.CHITick >= this.postPHIDur) {
			dead = true;
		}*/
		double rand = Uniform.staticNextDouble();
		if (stageOfInfection.equals(STAGE.ACUTE)
				&& rand <= ((double)1/Parameters.durationAHI)) {
			stageOfInfection = STAGE.CHRONIC;
			this.CHITick = currentTick;
		}

		else if (stageOfInfection.equals(STAGE.CHRONIC) 
				&& rand <= ((double)1/Parameters.durationCHI)) {
			dead = true;		
		} 	
	}
	
	public void resetOutbreakRecord() {
		setAHIClusterID(-1);
		setRemovedAHICluster(false);
		setOutbreakStartTime(-1);
	}

	public boolean isAHI() {
		return stageOfInfection.equals(STAGE.ACUTE);
	}

	public boolean isInfected() {
		return stageOfInfection.equals(STAGE.ACUTE) || stageOfInfection.equals(STAGE.CHRONIC);
	}

	public boolean isSusceptible() {
		return stageOfInfection.equals(STAGE.SUSCEPTIBLE);
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
		return infectorStatus.equals(STAGE.ACUTE) ? true : false;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean exit) {
		this.dead = exit;
	}

	public STAGE getInfectorStatus() {
		return infectorStatus;
	}

	public void setInfectorStatus(STAGE infectorStatus) {
		this.infectorStatus = infectorStatus;
	}

	public Integer getAHIClusterID() {
		return ahiClusterID;
	}

	public void setAHIClusterID(Integer ahiCluster) {
		this.ahiClusterID = ahiCluster;
	}


	public void setStageOfInfection(STAGE infectionStatus) {
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

	public boolean equals(Person person) {
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

	public STAGE getStageOfInfection() {
		return stageOfInfection;
	}	
}