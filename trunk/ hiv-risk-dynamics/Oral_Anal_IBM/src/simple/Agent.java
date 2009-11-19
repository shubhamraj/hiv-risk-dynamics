package simple;

import java.util.ArrayList;
import java.util.HashMap;

public class Agent {
	private int ID = -1;
	private int infectionStatus = Settings.INFECTION_STATUS.SUSCEPTIBLE;
	private int infectedTick = -1;
	private boolean sexuallyActive = true;
	private boolean partnered = false;
	private int infectorID = -1;
	private int infectorStatus = -1;
	private int infectionContactType = -1;
	private boolean gotInfected = false;	
	private int entryTick = -1;
	private int exitTick = -1;
    public int numVisits = 0;
    //        # unprotected receptive anal sex contacts with an HIV-positive partner
    public int numAnalSexContacts = 0;
    public int numOralSexContacts = 0;
    
    public int numSuscAnalSexContacts = 0;
    public int numPHIAnalSexContacts = 0;
    public int numPostPHIAnalSexContacts = 0;
    public int numSuscOralSexContacts = 0;
    public int numPHIOralSexContacts = 0;
    public int numPostPHIOralSexContacts = 0;
    
      
    private boolean enrolled = false;    
    private ArrayList<SexualHistory> mySexualHistory = new ArrayList<SexualHistory>();

	public Agent() {}  

	public Agent(int _ID) {
		this.ID = _ID;
		this.infectionStatus = Settings.INFECTION_STATUS.SUSCEPTIBLE;		
	}

	
	public void step (int currentTick) {
		double lifeProb = 9.259E-5;
		if (Math.random() <= lifeProb) {
			setSexuallyActive(false);	
		}	
		if (isSexuallyActive() && this.infectionStatus != Settings.INFECTION_STATUS.SUSCEPTIBLE) {
				updateInfectionStage();
		}					
	}
	
	public void setHIVInfection( int currentTick) {
		setInfectedTick(currentTick);
		setInfectionStatus(Settings.INFECTION_STATUS.PRIMARY_INFECTION);
	}
	
	public boolean isInfectSexPartner(Agent sexPartner, double f) {		
		double chanceForTransmission = 0;
		if (this.infectionStatus == Settings.INFECTION_STATUS.PRIMARY_INFECTION) {
			chanceForTransmission = (Settings.BASELINE_PROBABILITY * Settings.FACTOR);			
		}
		else if (this.infectionStatus == Settings.INFECTION_STATUS.CHRONIC) {
			chanceForTransmission = Settings.BASELINE_PROBABILITY;
		}
		chanceForTransmission *= f;
		return Math.random() <= chanceForTransmission ? true : false;
	}
	
	public void print(String str) {
		System.out.println("" + str);
	}
	
	public boolean updateInfectionStage(){
		boolean death = false ;
		int status = this.getInfectionStatus();
		//60days period for phi
		double d1 = 0.016667;
		//~100 years chronic stage duration
		double d2 = 2.77E-5;
		if(status == Settings.INFECTION_STATUS.PRIMARY_INFECTION && Math.random() <= d1) {
			setInfectionStatus(Settings.INFECTION_STATUS.CHRONIC);
		}
		else if( status == Settings.INFECTION_STATUS.CHRONIC && Math.random() <= d2) {
			sexuallyActive = false;
			death = true;
		}
		return death;
	}
	
	public int getID() {
		return ID;
	}

	public void setID(int id) {
		ID = id;
	}

	public int getInfectionStatus() {
		return infectionStatus;
	}

	public void setInfectionStatus(int infectionStatus) {
		this.infectionStatus = infectionStatus;
	}

	public boolean isSexuallyActive() {
		return sexuallyActive;
	}

	public void setSexuallyActive(boolean sexuallyActive) {
		this.sexuallyActive = sexuallyActive;
	}

	public void setInfectedTick(int infectedTick) {
		this.infectedTick = infectedTick;
	}

	public int getInfectedTick() {
		return infectedTick;
	}

	public boolean isPartnered() {
		return partnered;
	}

	public void setPartnered(boolean partnered) {
		this.partnered = partnered;
	}

	public int getInfectionContactType() {
		return infectionContactType;
	}

	public void setInfectionContactType(int contactType) {
		this.infectionContactType = contactType;
	}

	public boolean isGotInfected() {
		return gotInfected;
	}

	public void setGotInfected(boolean gotInfected) {
		this.gotInfected = gotInfected;
	}
	
	public int getInfectorID() {
		return infectorID;
	}

	public void setInfectorID(int infectorID) {
		this.infectorID = infectorID;
	}
	
	public int getInfectorStatus() {
		return infectorStatus;
	}

	public void setInfectorStatus(int infectorStatus) {
		this.infectorStatus = infectorStatus;
	}

	public int getNumVisits() {
		return numVisits;
	}

	public void setNumVisits(int numVisits) {
		this.numVisits = numVisits;
	}

	public int getNumAnalSexContacts() {
		return numAnalSexContacts;
	}

	public void setNumAnalSexContacts(int numAnalSexContacts) {
		this.numAnalSexContacts = numAnalSexContacts;
	}

	public int getEntryTick() {
		return entryTick;
	}

	public void setEntryTick(int entryTick) {
		this.entryTick = entryTick;
	}

	public int getExitTick() {
		return exitTick;
	}

	public void setExitTick(int exitTick) {
		this.exitTick = exitTick;
	}

	public boolean isEnrolled() {
		return enrolled;
	}

	public void setEnrolled(boolean enrolled) {
		this.enrolled = enrolled;
	}

	public ArrayList<SexualHistory> getMySexualHistory() {
		return mySexualHistory;
	}

	public void setMySexualHistory(ArrayList<SexualHistory> mySexualHistory) {
		this.mySexualHistory = mySexualHistory;
	}

}