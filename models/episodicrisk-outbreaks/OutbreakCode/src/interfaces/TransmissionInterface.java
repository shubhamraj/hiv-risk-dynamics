package interfaces;

public interface TransmissionInterface extends ParametersInterface {
	public String toString();	
	public int getTime();
	public void setTime(int time);
	public int getInfectorID();
	public void setInfectorID(int infectorID);
	public int getInfectedID();
	public void setInfectedID(int infectedID);
	public ActType getActType();
	public void setActType(ActType actType);
	public InfectionStage getInfectorStage();
	public void setInfectorStage(InfectionStage infectorStage);
	public int getTimeSinceLastInfection();
	public void setTimeSinceLastInfection(int timeSinceLastInfection);
	public void setCurrentClusterTransmissions(int currentClusterSize);
	public int returnCurrentClusterTransmissions();
	public int returnCurrentClusterAge();
	public void setCurrentClusterAge(int currentClusterAge);
	public int getBranchTime();
	public void setBranchTime(int branchTime);
	public int getObID();
	public void setObID(int obID);
}
