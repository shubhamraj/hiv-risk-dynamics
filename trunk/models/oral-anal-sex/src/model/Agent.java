package model;

import java.util.ArrayList;
import java.util.HashMap;

import com.sun.xml.internal.stream.Entity;

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
	/*time of enrolment for this individual */ 
	private int enrolledTick = -1;
	/*time of exit from enrolment for this individual*/
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
	private Integer observationPeriod = 0; 
	private HashMap<Integer, ArrayList<SexualHistory>> observationRecord = new HashMap<Integer, ArrayList<SexualHistory>>();
	
	public Agent() {}  

	public Agent(int _ID) {
		this.ID = _ID;
		this.infectionStatus = Settings.INFECTION_STATUS.SUSCEPTIBLE;		
	}

	public void addSexualHistory(Agent partner, int contactType, int currentTick) {
		SexualHistory sexualHistory = new SexualHistory(currentTick, this.ID, this.infectionStatus, 
				partner.getID(), partner.getInfectionStatus(), contactType);
		ArrayList<SexualHistory> obsRec;
		if (observationRecord.containsKey(observationPeriod)) {
			obsRec = observationRecord.get(observationPeriod);
		}
		else {
			obsRec = new ArrayList<SexualHistory>();
		}
		obsRec.add(sexualHistory);
		observationRecord.put(observationPeriod, obsRec);
	}

	public void step (int currentTick) {
		this.partnered = false;
		//1/30 Years
		double lifeProb = 9.259E-5;
		if (Math.random() <= lifeProb) {
			sexuallyActive = false;
			return;
		}
		if (infectionStatus == Settings.INFECTION_STATUS.PHI) {
			if (Math.random() <= Settings.STAGE_DURATION.PROB_STAY_PHI) {
				infectionStatus = Settings.INFECTION_STATUS.POST_PHI;
			}			
		}  		
		else if (infectionStatus == Settings.INFECTION_STATUS.POST_PHI) {
			if (Math.random() <= Settings.STAGE_DURATION.PROB_STAY_POSTPHI) {
				sexuallyActive = false;
				return;
			}			
		}
		if (enrolled) {
			 if ((currentTick - enrolledTick) % Settings.STUDY_PERIOD_LENGTH == 0) {
					observationPeriod++;
			 }
			 if (gotInfected) {
				 exitTick = (enrolledTick+(observationPeriod*Settings.STUDY_PERIOD_LENGTH));
			 }
		} 					
	}

	public void setHIVInfection( int currentTick) {
		setInfectedTick(currentTick);
		setInfectionStatus(Settings.INFECTION_STATUS.PHI);
	}

	public void print(String str) {
		System.out.println("" + str);
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

	public int getEnrolledTick() {
		return enrolledTick;
	}

	public void setEnrolledTick(int entryTick) {
		this.enrolledTick = entryTick;
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

	public HashMap<Integer, ArrayList<SexualHistory>> getObservationRecord() {
		return observationRecord;
	}

	public void setObservationRecord(
			HashMap<Integer, ArrayList<SexualHistory>> observationRecord) {
		this.observationRecord = observationRecord;
	}

	public int getObservationPeriod() {
		return observationPeriod;
	}

	public void setObservationPeriod(int observationPeriod) {
		this.observationPeriod = observationPeriod;
	}

}