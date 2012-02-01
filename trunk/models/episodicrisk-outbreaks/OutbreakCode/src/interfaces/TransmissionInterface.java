package interfaces;

public interface TransmissionInterface extends ParametersInterface {
	/** Returns the time of this transmission event. */
	public int getTime();
	/** Sets the time of this transmission event. */
	public void setTime(int time);
	/** Returns ID of the infector agent. */
	public int getInfectorID();
	/** Sets ID of the infector agent. */
	public void setInfectorID(int infectorID);
	/** Returns ID of the infected agent. */
	public int getInfectedID();
	/** Sets ID of the infected agent. */
	public void setInfectedID(int infectedID);
	/** Returns the {@link ActType} for this transmission. */
	public ActType getActType();
	/** Sets the {@link ActType} for this transmission. */
	public void setActType(ActType actType);
	/** Returns the {@link InfectionStage} of the infector agent. */
	public InfectionStage getInfectorStage();
	/** Sets the {@link InfectionStage} of the infector agent. */
	public void setInfectorStage(InfectionStage infectorStage);
	/** Returns the time since last infection of the infector agent. */
	public int getTimeSinceLastInfection();
	/** Sets the time since last infection of the infector agent. */
	public void setTimeSinceLastInfection(int timeSinceLastInfection);
	/** Sets the current size in number of agents of the current early
	 * infection outbreak cluster. */
	public void setCurrentClusterTransmissions(int currentClusterSize);
	/** Returns the current size in number of agents of the current early
	 * infection outbreak cluster. */
	public int returnCurrentClusterTransmissions();
	/** Returns the duration of the current early infection outbreak cluster. */
	public int returnCurrentClusterAge();
	/** Sets the duration of the current early infection outbreak cluster. */
	public void setCurrentClusterAge(int currentClusterAge);
	/** Returns the branchtime defined by the time difference between the 
	 * current time and the time of the last infection of the 
	 * infected agent.*/
	public int getBranchTime();
	/** Sets the branchtime defined by the time difference between the 
	 * current time and the time of the last infection of the 
	 * infected agent.*/
	public void setBranchTime(int branchTime);
	/** Returns the early infection outbreak ID to which this transmission belongs. */
	public int getObID();
	/** Sets the early infection outbreak ID to which this transmission belongs. */
	public void setObID(int obID);
	/** Outputs all the properties of the transmission in a comma separated {@link String} */
	public String toString();
}
