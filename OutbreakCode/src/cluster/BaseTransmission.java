package cluster;



import interfaces.AgentInterface;
import interfaces.TransmissionInterface;

/**
 * 
 * @author shah
 *
 */
public class BaseTransmission implements TransmissionInterface {
	int obID; 
	int time;
	int infectorID;
	int infectedID;
	/** For the infector */
	int timeSinceLastInfection;
	ActType actType = ActType.None;
	InfectionStage infectorStage = InfectionStage.Susceptible;	
	int currentClusterTransmissions;
	int branchTime;
	int currentClusterAge;
	
	public BaseTransmission() {
		obID = -1; 
		time = -1;
		infectorID = -1;
		infectedID = -1;		
		timeSinceLastInfection = -1;
		actType = ActType.None;
		infectorStage = InfectionStage.Susceptible;
		currentClusterTransmissions = 0;
		branchTime = 0;
		currentClusterAge = 0;		
	}
	
	public BaseTransmission(Integer _time, AgentInterface infector, AgentInterface infected) {
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

	public ActType getActType() {
		return actType;
	}

	public void setActType(ActType actType) {
		this.actType = actType;
	}

	public InfectionStage getInfectorStage() {
		return infectorStage;
	}

	public void setInfectorStage(InfectionStage infectorStage) {
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

	@Override
	public void print() {
		System.out.println(toString());
	}	
}