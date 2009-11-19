package simple;

import reader.Reader;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.util.SimUtilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import cern.jet.random.Normal;
import cern.jet.random.Uniform;

import javasql.RunDB;

public class Model {
	private ArrayList<Agent> agentList = new ArrayList<Agent>();
	public static double partnershipProb = Settings.PARTNERSHIP_PROBABILITY;
	//population size
	private int numAgents = Settings.NUMBER_AGENTS;
	private int lastAgentID = -1;
	public double numHIV = 0;
	int currentTick = 0;
	double factor = 0;
	double contactRateRatio = 0;
	Vector<Double> param = new Vector<Double>();
	PrintWriter recorder, recorder1, recorder2, recorder3, recorder4;
	private ArrayList<Agent> enrolledAgents = new ArrayList<Agent>();
	int numDeadEnrolledAgents = 0;
	private int studyPeriod = Settings.STUDY_DURATION, recallPeriod = Settings.RECALL_PERIOD;
	private double analToOralProportion = Settings.ANAL_SEX_PROPORTION;
	private int stopTime = 0;

	Reader reader = new Reader();
	RunDB runDB = new RunDB();
	private boolean recordDB = Settings.RECORD_DATABASE;

	public Model() {
	}

	// builds the model
	public void buildModel(Vector<Double> param, int index) {		
		//contact rate, proportion oral, oral risk ratio 
//		double dt = 0.023333;
		double dt = 0.0051;
		contactRateRatio = (param.get(1).doubleValue());
		partnershipProb = 1 - Math.exp(-contactRateRatio*dt);
		analToOralProportion = param.get(2).doubleValue();
		double f = (param.get(3).doubleValue());
		factor = f;
		numHIV = 0;
		agentList.clear();
		lastAgentID = -1;
		currentTick = 0;
		enrolledAgents.clear();	
			
		Random.createUniform();
		for (int i=0; i<numAgents; i++) {
			Agent agent = new Agent(i);
			agentList.add(agent);
			agent.setEntryTick(0);
		}
		lastAgentID = numAgents;
		createRecordFiles(index);
	}

	public void insertRecord(String tableName, String statement) {
		runDB.insertRecord(tableName, statement);
	}

	public void sexualInteraction() {		
		int size = agentList.size();
		Agent 
		agent2=null;
		SimUtilities.shuffle(agentList);
		for (Agent agent1 : agentList) {
			if (Math.random() <= partnershipProb) {
				do {
					agent2 = agentList.get(Uniform.staticNextIntFromTo(0, size-1));
				} while (agent2.getID() == agent1.getID());
				
				agent1.setPartnered(true);
				agent2.setPartnered(true);			
				int contactType = -1;
				
				if (Math.random() <= analToOralProportion) {
					contactType = Settings.CONTACT_TYPE.ORAL;
					assignVariablesAnal(agent1, agent2);
					assignVariablesAnal(agent2, agent1);
				}
				else {
					contactType = Settings.CONTACT_TYPE.ANAL;
					assignVariablesOral(agent1, agent2);
					assignVariablesOral(agent2, agent1);
				}			
				if (!((agent1.getInfectionStatus() == Settings.INFECTION_STATUS.SUSCEPTIBLE
						&& agent2.getInfectionStatus() == Settings.INFECTION_STATUS.SUSCEPTIBLE)
						|| (agent1.getInfectionStatus() != Settings.INFECTION_STATUS.SUSCEPTIBLE
								&& agent2.getInfectionStatus() != Settings.INFECTION_STATUS.SUSCEPTIBLE))) {
					if (agent1.getInfectionStatus() != Settings.INFECTION_STATUS.SUSCEPTIBLE) {
						infectPartner(agent1, agent2, contactType);
					}
					else {
						infectPartner(agent2, agent1, contactType);
					}
				}	
				if (shouldAddSexualHistory(agent1)) {
					addSexualHistory(agent1, agent2, contactType);
				}
				if (shouldAddSexualHistory(agent2)) {
					addSexualHistory(agent2, agent1, contactType);
				}
	
				
			}
		}
	}
	
	
	public void addAgent() {
		Agent agent = new Agent(lastAgentID);
		lastAgentID++;
		this.agentList.add(agent);
		agent.setEntryTick(currentTick);
		agent.setExitTick(currentTick + studyPeriod);
		if (currentTick >= Settings.ENROLLED_TIME
				&& enrolledAgents.size() < Settings.NUM_ENROLLED_AGENTS) {
			if (!enrolledAgents.contains(agent)) {
				enrolledAgents.add(agent);
				agent.setEnrolled(true);			
				if (enrolledAgents.size() == Settings.NUM_ENROLLED_AGENTS) {
					stopTime = currentTick + studyPeriod + 60;
				}
			}
		}
	}

	public void addSexualHistory(Agent agent, Agent partner, int contactType) {
		SexualHistory sexualHistory = new SexualHistory(currentTick, agent.getID(), agent.getInfectionStatus(), 
				partner.getID(), partner.getInfectionStatus(), contactType);
		agent.getMySexualHistory().add(sexualHistory);
	}

	public void introduceInfection() {
		int numInfected = (int) (Settings.PERCENTAGE_INITIAL_INFECTIONS * numAgents);
		SimUtilities.shuffle(agentList);
		for (int i=0; i<numInfected; i++) {
			setHIVInfection(agentList.get(i), null);			
		}		
	}

	public void setHIVInfection(Agent agent, Agent infector) {		
		agent.setInfectionStatus(Settings.INFECTION_STATUS.PRIMARY_INFECTION);
		agent.setInfectedTick(currentTick);				
		agent.setGotInfected(true);
		if (infector == null) {
			agent.setInfectorID(-1);
			agent.setInfectorStatus(-1);						
		}
		else {
			agent.setInfectorID(infector.getID());
			agent.setInfectorStatus(infector.getInfectionStatus());			
		}
	}

	public void infectionProgression () {
		for (Agent agent : agentList) { 
			agent.step(currentTick);
			if(agent.isSexuallyActive()){
				agent.setInfectedTick(-1);				
				agent.setInfectionStatus(Settings.INFECTION_STATUS.SUSCEPTIBLE);			
			}					
		}
	}

	public void updateAgents() {
		//--- 1/30 years
		double lifeProb = 9.259E-5;
		ArrayList<Agent> temp = new ArrayList<Agent>(agentList);		
		for (Agent agent : temp) {
			agent.step(currentTick);
			agent.setPartnered(false);
			if (!agent.isSexuallyActive()) {
				removeAgent(agent);
			}				
		}				
		for (int i=0; i<numAgents; i++) {
			if (Math.random() <= lifeProb) {
				addAgent();
			}
		}
	}

	public void run() {
		introduceInfection();
		recorder4.println("CRR: " + contactRateRatio);
		recorder4.println("analtoOral_Proportion: " + analToOralProportion);
		recorder4.println("risk_factor: " + factor);
		
		for (int i=0; i<Settings.MAXIMUM_STEPS; i++) {
			currentTick = i;
			numHIV = 0;
			SimUtilities.shuffle(agentList);
			updateAgents();
			sexualInteraction();
			HIV_Stat();
			String str = new String("tick: "  + i + " numHIV: " + numHIV + " numAgents: "  + agentList.size() + " Prev: " + (double) (numHIV/agentList.size())); 
			recorder4.println(str);
			if (i%100 == 0) {				
				System.gc();
				recorder4.flush();
			}
			print(str);
			if (stopTime != 0 && stopTime==i) {
				break;
			}
		}	
		
		recorder4.close();
	}
	
	public boolean infectPartner(Agent potentialInfector, Agent susceptible, int contactType) {
		double f = 1;
		boolean flag = false;
		if (contactType == Settings.CONTACT_TYPE.ANAL) {
			f = 1;
		}
		else {			
			f = factor;
		}		
		if (potentialInfector.isInfectSexPartner(susceptible, f)) {
			flag = true;
			potentialInfector.setInfectionContactType(contactType);
			setHIVInfection(susceptible, potentialInfector);
			susceptible.setInfectionContactType(contactType);
		}		
		return flag;
	}

	public void assignVariablesAnal(Agent agent, Agent partner) {
		agent.numAnalSexContacts++;
		int partnerInfectionStatus = partner.getInfectionStatus();
		if (partnerInfectionStatus == Settings.INFECTION_STATUS.SUSCEPTIBLE) {
			agent.numSuscAnalSexContacts++;
		}
		else if (partnerInfectionStatus == Settings.INFECTION_STATUS.PRIMARY_INFECTION) {
			agent.numPHIAnalSexContacts++;
		}
		else {
			agent.numPostPHIAnalSexContacts++;
		}
	}

	public void assignVariablesOral (Agent agent, Agent partner) {		
		agent.numOralSexContacts++;
		int partnerInfectionStatus = partner.getInfectionStatus();
		if (partnerInfectionStatus == Settings.INFECTION_STATUS.SUSCEPTIBLE) {
			agent.numSuscOralSexContacts++;
		}
		else if (partnerInfectionStatus == Settings.INFECTION_STATUS.PRIMARY_INFECTION) {
			agent.numPHIOralSexContacts++;
		}
		else {
			agent.numPostPHIOralSexContacts++;
		}		
	}

	public boolean shouldAddSexualHistory(Agent agent) {
		if (agent.isEnrolled() 
				&& agent.getInfectedTick() <= currentTick
				&& agent.getExitTick() > currentTick) {
			return true;
		}
		else {
			return false;
		}
	}

	public void updateAgentList() {
		ArrayList<Agent> removedAgents = new ArrayList<Agent>();
		for (Agent agent : this.agentList) {
			agent.step(currentTick);
			if (!agent.isSexuallyActive()) {
				removedAgents.add(agent);
			}
		}		
		int numRemoved = removedAgents.size();
		for (Agent agent : removedAgents) {
			removeAgent(agent);
		}						
		for (int i=0; i<numRemoved; i++) {
			addAgent();
		}
	}

	public void removeAgent(Agent agent) {
		agent.setPartnered(false);
		if (this.agentList.contains(agent)) {
			this.agentList.remove(agent);
		}		
		if (enrolledAgents.contains(agent)) {
			numDeadEnrolledAgents++;
		}
	}


	public void setup() {
		reader.read();
		param = new Vector<Double>();
		int prevIndex = 0;
		int newIndex = 0;
		for (int i=1; i<=reader.getLineNo(); i++) {			
			param = reader.getParametersSet().get(new Integer(i));
			newIndex = i+prevIndex;
			buildModel(param, newIndex);
			run();
			recorderHeaders();
			recordLevels();
		}				
	}

	public static void main(String[] args) {
		Model model = new Model();
		model.setup();
	}
	
	public void recordLevels() {
		int numSuscAnalSexContacts=0, numPHIAnalSexContacts=0, numPostPHIAnalSexContacts=0,
		numSuscOralSexContacts=0, numPHIOralSexContacts=0, numPostPHIOralSexContacts=0;	
		int i=0;
		for (Agent agent : enrolledAgents) {
			i++;
			numSuscAnalSexContacts=0; numPHIAnalSexContacts=0; numPostPHIAnalSexContacts=0;
			numSuscOralSexContacts=0; numPHIOralSexContacts=0; numPostPHIOralSexContacts=0;

			for (SexualHistory sexContact : agent.getMySexualHistory()) {
				//				if (sexContact.timeStep <= studyPeriod && sexContact.timeStep >= studyPeriod-recallPeriod) {
				if (sexContact.timeStep <= agent.getExitTick() && sexContact.timeStep >= agent.getExitTick()-recallPeriod) {
					if (sexContact.contactType == Settings.CONTACT_TYPE.ANAL) {
						if (sexContact.myPartnerInfectionStatus == Settings.INFECTION_STATUS.SUSCEPTIBLE) {
							numSuscAnalSexContacts++;
						}
						else if (sexContact.myPartnerInfectionStatus == Settings.INFECTION_STATUS.PRIMARY_INFECTION) {
							numPHIAnalSexContacts++;
						}
						else {
							numPostPHIAnalSexContacts++;
						}
					}
					else {
						if (sexContact.myPartnerInfectionStatus == Settings.INFECTION_STATUS.SUSCEPTIBLE) {
							numSuscOralSexContacts++;
						}
						else if (sexContact.myPartnerInfectionStatus == Settings.INFECTION_STATUS.PRIMARY_INFECTION) {
							numPHIOralSexContacts++;
						}
						else {
							numPostPHIOralSexContacts++;
						}						
					}
				}
			}
			int infection = agent.isGotInfected() ? 1 : 0;
			String strID = "" + "'" + agent.getID() + "'";
			String level0 = strID + " "
			+ infection + " "
			+ numSuscAnalSexContacts + " "
			+ numPHIAnalSexContacts + " "
			+ numPostPHIAnalSexContacts + " "
			+ numSuscOralSexContacts + " "
			+ numPHIOralSexContacts + " "
			+ numPostPHIOralSexContacts;

			int numHIVAnalContacts = numPHIAnalSexContacts + numPostPHIAnalSexContacts;
			int numHIVOralContacts = numPHIOralSexContacts + numPostPHIOralSexContacts;
			String level1 = strID + " "
			+ infection + " "
			+ numSuscAnalSexContacts + " "
			+ numHIVAnalContacts + " "
			+ numSuscOralSexContacts + " "
			+ numHIVOralContacts;

			int totalOralContacts = (numHIVOralContacts + numSuscOralSexContacts);
			int totalAnalContacts = (numHIVAnalContacts + numSuscAnalSexContacts);

			String level2 = strID + " "
			+ infection + " "
//			+ (numHIVAnalContacts + numSuscAnalSexContacts) + ", "
			+ totalAnalContacts + " "
			+ totalOralContacts;

			double n1 = Normal.staticNextDouble(totalAnalContacts, 0.1*totalAnalContacts);
			double n2 = Normal.staticNextDouble(totalOralContacts, 0.1*totalOralContacts);
			//			double noise1 = 0.9 * totalAnalContacts;
			//			double noise2 = 0.9 * totalOralContacts;
			String level3 = strID + " "
			+ infection + " "
			+ (int) n1 + " "
			+ (int) n2;

			recorder.println(level0);
			recorder1.println(level1);
			recorder2.println(level2);
			recorder3.println(level3);

			if (i%100==0) {
				flushRecorders();
			}			
		}
		flushRecorders();
		closeRecorders();
	}
	
	public void createRecordFiles(int index) {
		try {
			recorder = new PrintWriter(new BufferedWriter(new FileWriter(index+"_level0" + ".txt")));
			recorder1 = new PrintWriter(new BufferedWriter(new FileWriter(index+"_level1" + ".txt")));
			recorder2 = new PrintWriter(new BufferedWriter(new FileWriter(index+"_level2" + ".txt")));
			recorder3 = new PrintWriter(new BufferedWriter(new FileWriter(index+"_level3" + ".txt")));
			recorder4 = new PrintWriter(new BufferedWriter(new FileWriter(index+"_Prevalence" + ".txt")));
			
			if (recordDB) {
				runDB = new RunDB();				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
		if (recordDB) {
			try {
				runDB.loadDriver();
				runDB.makeConnection();
				runDB.buildStatement();
				runDB.createTable();
				//		        runDB.commitChanges();
				//		        runDB.getConnection().close();
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
	}

	public void recorderHeaders() {		
		recorder.println("agentID" + " infection " + " SusceptibleAnalContacts" + " PHIAnalContacts" 
				+ " PostPHIAnalContacts" + " SusceptibleOralContacts" 
				+ " PHIOralContacts" + " PostPHIOralContacts");
		

		recorder1.println("agentID" + " infection " + " SusceptibleAnalContacts" + " HIVAnalContacts" 

				+ " SusceptibleOralContacts" + " HIVOralContacts");

		recorder2.println("agentID" + " infection " + " AnalContacts" + " OralContacts");

		recorder3.println("agentID" + " infection " + " AnalContacts" + " OralContacts");
	}
	
	public void print(String str) {
		System.out.println(""+str);
	}

	public void flushRecorders() {
		recorder.flush();
		recorder1.flush();
		recorder2.flush();
		recorder3.flush();
		recorder4.flush();
	}

	public void closeRecorders() {
		recorder.close();
		recorder1.close();
		recorder2.close();
		recorder3.close();
	}
	
	public void recordSexualHistories() {
		print("------------------------------------END-------------- tick: " + currentTick);
		for (Agent agent : enrolledAgents) {
			int infected = agent.getInfectionStatus() != Settings.INFECTION_STATUS.SUSCEPTIBLE ? 1 : 0;
			String str = 	agent.getID() + "," 
			+ agent.getEntryTick() + ","
			+ agent.getExitTick() + ","
			+ infected + ","
			+ agent.getInfectedTick() + ","
			+ agent.getInfectorID() + ","
			+ agent.getInfectorStatus() + ","
			+  agent.getInfectionContactType();
			insertRecord("tblAgentInfo2", str);

			for (SexualHistory sexualHistory : agent.getMySexualHistory()) {
				String str2  =	agent.getID() + "," 
				+ sexualHistory.timeStep + ","
				+ sexualHistory.myInfectionStatus + ","
				+ sexualHistory.myPartnerID + ","
				+ sexualHistory.myPartnerInfectionStatus + ","
				+ sexualHistory.contactType;
				insertRecord("tblSexualHistory2", str2);
			}
		}
		try {
			runDB.commitChanges();
			runDB.getConnection().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void HIV_Stat() {
		int numPHI = 0, numAsym = 0, numLate = 0;
		for (Agent agent : agentList) {
			if (agent.getInfectionStatus() != Settings.INFECTION_STATUS.SUSCEPTIBLE) {
				numHIV++;
			}
			if (agent.getInfectionStatus() == Settings.INFECTION_STATUS.PRIMARY_INFECTION) {
				numPHI++;
			}
			else if (agent.getInfectionStatus() == Settings.INFECTION_STATUS.ASYMPTOMATIC_INFECTION) {
				numAsym++;
			}
			else if (agent.getInfectionStatus() == Settings.INFECTION_STATUS.LATE_INFECTION) {
				numLate++;
			}
		}
	}


	public ArrayList<Agent> getEnrolledAgents() {
		return enrolledAgents;
	}

	public void setEnrolledAgents(ArrayList<Agent> enrolledAgents) {
		this.enrolledAgents = enrolledAgents;
	}

	public int getNumDeadEnrolledAgents() {
		return numDeadEnrolledAgents;
	}

	public void setNumDeadEnrolledAgents(int numDeadEnrolledAgents) {
		this.numDeadEnrolledAgents = numDeadEnrolledAgents;
	}
}