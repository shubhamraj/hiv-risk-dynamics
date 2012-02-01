package cluster;



import basemodel.AgentInteface;
import basemodel.ParametersInterface;

/**
 * 
 * @author shah
 *
 */
public class BaseTransmission implements TransmissionInterface, ParametersInterface {
	int obID; 
	int time;
	int infectorID;
	int infectedID;
	/** For the infector */
	int timeSinceLastInfection;
	ACT_TYPE actType = ACT_TYPE.NONE;
	STAGE infectorStage = STAGE.SUSCEPTIBLE;	
	int currentClusterTransmissions;
	int branchTime;
	int currentClusterAge;
	
	public BaseTransmission() {
		obID = -1; 
		time = -1;
		infectorID = -1;
		infectedID = -1;		
		timeSinceLastInfection = -1;
		actType = ACT_TYPE.NONE;
		infectorStage = STAGE.SUSCEPTIBLE;
		currentClusterTransmissions = 0;
		branchTime = 0;
		currentClusterAge = 0;		
	}
	
	public BaseTransmission(Integer _time, AgentInteface infector, AgentInteface infected) {
		obID = -1;
		time = _time;
		infectorID = infector.getID();
		infectedID = infected.getID();
		timeSinceLastInfection = infector.returnInfectionDuration();
		actType = infected.getActType();
		infectorStage = infector.getStageOfInfection();
		branchTime = time - infector.getInfectedTick();
	}
	
	public String toString() {
		String str = "";
		str = getTime() + "," + getInfectorID() + "," + (getTime() - getBranchTime()) 
			+ "," + getInfectedID() + "," + getActType() + "," + getTimeSinceLastInfection()
			+ "," + getInfectorStage() + ", " + getBranchTime();
		return str;		
	}
	
	public void print() {
		System.out.println(toString());
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getInfectorID() {
		return infectorID;
	}

	public void setInfectorID(int infectorID) {
		this.infectorID = infectorID;
	}

	public int getInfectedID() {
		return infectedID;
	}

	public void setInfectedID(int infectedID) {
		this.infectedID = infectedID;
	}

	public ACT_TYPE getActType() {
		return actType;
	}

	public void setActType(ACT_TYPE actType) {
		this.actType = actType;
	}

	public STAGE getInfectorStage() {
		return infectorStage;
	}

	public void setInfectorStage(STAGE infectorStage) {
		this.infectorStage = infectorStage;
	}

	public int getTimeSinceLastInfection() {
		return timeSinceLastInfection;
	}

	public void setTimeSinceLastInfection(int timeSinceLastInfection) {
		this.timeSinceLastInfection = timeSinceLastInfection;
	}
	
	public void setCurrentClusterTransmissions(int currentClusterSize) {
		this.currentClusterTransmissions = currentClusterSize;
	}

	public int returnCurrentClusterTransmissions() {
		return currentClusterTransmissions;
	}

	public int returnCurrentClusterAge() {
		return currentClusterAge;
	}

	public void setCurrentClusterAge(int currentClusterAge) {
		this.currentClusterAge = currentClusterAge;
	}

	public int getBranchTime() {
		return branchTime;
	}

	public void setBranchTime(int branchTime) {
		this.branchTime = branchTime;
	}

	public int getObID() {
		return obID;
	}

	public void setObID(int obID) {
		this.obID = obID;
	}
}