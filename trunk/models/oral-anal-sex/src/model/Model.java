package model;

import reader.Reader;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.util.SimUtilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;
import cern.jet.random.Normal;
import cern.jet.random.Uniform;

public class Model {
	private ArrayList<Agent> agentList = new ArrayList<Agent>();
	public static double partnershipProb = Settings.PARTNERSHIP_PROBABILITY;
	//population size
	private int numAgents = Settings.NUMBER_AGENTS;
	private int lastAgentID = -1;
	public double numHIV = 0;
	int currentTick = 0;
	double oralInfectivityFactor = 0;
	double contactRateRatio = 0;
	Vector<Double> param = new Vector<Double>();
	PrintWriter recorder, recorder1, recorder2, recorder3, recorder4;
	private ArrayList<Agent> enrolledAgents = new ArrayList<Agent>();
	int numDeadEnrolledAgents = 0;
	private int studyPeriod = Settings.STUDY_DURATION;
	private double preferenceForOralSex = Settings.ANAL_SEX_PROPORTION;
	private int stopTime = 0;
	Reader reader = new Reader();	
	int currentIndex = 0;
	public boolean recordData = true;
	private String parametersFilePath = "./parameters.txt";
	
	public Model() {
	}

	// builds the model
	public void buildModel(Vector<Double> param, int index) {		
		//contact rate, proportion oral, oral risk ratio 
		double dt = Settings.DT;
		contactRateRatio = (param.get(0).doubleValue());
//		print(""+partnershipProb);
		preferenceForOralSex = param.get(1).doubleValue();
//		print(""+preferenceForOralSex);
		oralInfectivityFactor = param.get(2).doubleValue();
//		print(""+oralInfectivityFactor);
		
		if (preferenceForOralSex > 0.4 && preferenceForOralSex <= 0.5) {
			dt = 0.018;
		}
		else if (preferenceForOralSex > 0.5 && preferenceForOralSex <= 0.65) {
			dt = 0.02;
		}
		else if (preferenceForOralSex > 0.65 && preferenceForOralSex <= 0.7) {
			dt = 0.02333;
		}
		else if (preferenceForOralSex > 0.7) {
			dt = 0.03333;
		}
		
		partnershipProb = 1 - Math.exp(-contactRateRatio*dt);
		
		numHIV = 0;
		lastAgentID = -1;
		currentTick = 0;		
		if (agentList != null) {
			enrolledAgents = null;
		}
		agentList = new ArrayList<Agent>();
		if (enrolledAgents != null) {
			enrolledAgents = null;
		}
		enrolledAgents = new ArrayList<Agent>();		
		Random.createUniform();
		for (int i=0; i<numAgents; i++) {
			Agent agent = new Agent(i);
			agentList.add(agent);
			agent.setEnrolledTick(0);
		}
		lastAgentID = numAgents;
	}


	public void sexualInteraction() {
		int size = agentList.size();
		SimUtilities.shuffle(agentList);
		Agent agent2 = null;
		int numParnerships = 0;
		Agent agent1 = null;
		int contactType = -1;
		for( int i=0; i<size; i++ ) {
			if (Math.random() <= partnershipProb) {
				agent1 = agentList.get(Uniform.staticNextIntFromTo(0, size-1));
				do {
					agent2 = agentList.get(Uniform.staticNextIntFromTo(0, size-1));
				} while (agent2.getID() == agent1.getID());
				agent1.setPartnered(true);
				agent2.setPartnered(true);
				numParnerships++;
				contactType = -1;
				if (Math.random() <= preferenceForOralSex) {
					contactType = Settings.CONTACT_TYPE.ORAL;
					assignVariablesOral(agent1, agent2);
					assignVariablesOral(agent2, agent1);	
				}
				else {
					contactType = Settings.CONTACT_TYPE.ANAL;					
					assignVariablesAnal(agent1, agent2);
					assignVariablesAnal(agent2, agent1);
				}
				if (!		
						((agent1.getInfectionStatus() == Settings.INFECTION_STATUS.SUSCEPTIBLE
								&& agent2.getInfectionStatus() == Settings.INFECTION_STATUS.SUSCEPTIBLE)
								||

								(agent1.getInfectionStatus() != Settings.INFECTION_STATUS.SUSCEPTIBLE
										&& agent2.getInfectionStatus() != Settings.INFECTION_STATUS.SUSCEPTIBLE))																
				) {
					if (agent1.getInfectionStatus() != Settings.INFECTION_STATUS.SUSCEPTIBLE
							&& agent2.getInfectionStatus() == Settings.INFECTION_STATUS.SUSCEPTIBLE) {
						tryInfectPartner(agent1, agent2, contactType);
					}
					else if (agent2.getInfectionStatus() != Settings.INFECTION_STATUS.SUSCEPTIBLE
							&& agent1.getInfectionStatus() == Settings.INFECTION_STATUS.SUSCEPTIBLE) {
						tryInfectPartner(agent2, agent1, contactType);
					}

				}
				if (recordData) {
					if (shouldAddSexualHistory(agent1)) {
						agent1.addSexualHistory(agent2, contactType, currentTick);
					}
					if (shouldAddSexualHistory(agent2)) {
						agent2.addSexualHistory(agent1, contactType, currentTick);
					}					
				}
			}
		}			
	}

	public void sexualInteraction2() {
		int size = agentList.size();
		SimUtilities.shuffle(agentList);
		Agent agent2 = null;
		int numParnerships = 0;

		for (Agent agent1 : agentList) {
			if (Math.random() <= partnershipProb) {
				do {
					agent2 = agentList.get(Uniform.staticNextIntFromTo(0, size-1));
				} while (agent2.getID() == agent1.getID() && agent2.isPartnered());
				agent1.setPartnered(true);
				agent2.setPartnered(true);
				numParnerships++;
				int contactType = -1;
				if (Math.random() <= preferenceForOralSex) {
					contactType = Settings.CONTACT_TYPE.ORAL;
					assignVariablesOral(agent1, agent2);
					assignVariablesOral(agent2, agent1);	
				}
				else {
					contactType = Settings.CONTACT_TYPE.ANAL;					
					assignVariablesAnal(agent1, agent2);
					assignVariablesAnal(agent2, agent1);
				}

				if (!		
						((agent1.getInfectionStatus() == Settings.INFECTION_STATUS.SUSCEPTIBLE
								&& agent2.getInfectionStatus() == Settings.INFECTION_STATUS.SUSCEPTIBLE)
								||

								(agent1.getInfectionStatus() != Settings.INFECTION_STATUS.SUSCEPTIBLE
										&& agent2.getInfectionStatus() != Settings.INFECTION_STATUS.SUSCEPTIBLE))																
				) {
					if (agent1.getInfectionStatus() != Settings.INFECTION_STATUS.SUSCEPTIBLE
							&& agent2.getInfectionStatus() == Settings.INFECTION_STATUS.SUSCEPTIBLE) {
						tryInfectPartner(agent1, agent2, contactType);
					}
					else if (agent2.getInfectionStatus() != Settings.INFECTION_STATUS.SUSCEPTIBLE
							&& agent1.getInfectionStatus() == Settings.INFECTION_STATUS.SUSCEPTIBLE) {
						tryInfectPartner(agent2, agent1, contactType);
					}

				}

				if (shouldAddSexualHistory(agent1)) {
					agent1.addSexualHistory(agent2, contactType, currentTick);
				}
				if (shouldAddSexualHistory(agent2)) {
					agent2.addSexualHistory(agent1, contactType, currentTick);
				}
			}			
		}	
//		print("tick: " + currentTick + " - NumPartnerships: " + numParnerships);
	}


	public void addAgent() {
		Agent agent = new Agent(lastAgentID);
		lastAgentID++;
		this.agentList.add(agent);
		if (currentTick >= Settings.ENROLLED_TIME
				&& enrolledAgents.size() < Settings.NUM_ENROLLED_AGENTS
				&& Math.random() <= Settings.CHANCE_FOR_GETTING_ENROLLED
			) {
			if (!enrolledAgents.contains(agent)) {
				enrolledAgents.add(agent);
				agent.setEnrolled(true);
				agent.setEnrolledTick(currentTick);				
				if (enrolledAgents.size() == Settings.NUM_ENROLLED_AGENTS) {
					stopTime = currentTick + studyPeriod+60;
				}
			}
		}
	}

	public void introduceInfection() {
		SimUtilities.shuffle(agentList);
		for (int i=0; i<(int) (Settings.PERCENTAGE_INITIAL_INFECTIONS * numAgents); i++) {
			setHIVInfection(agentList.get(i), null);			
		}		
	}

	public void setHIVInfection(Agent agent, Agent infector) {		
		agent.setInfectionStatus(Settings.INFECTION_STATUS.PHI);
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

	public void updateAgents() {
		//--- 1/30 years
		double lifeProb = 9.259E-5;
		ArrayList<Agent> temp = new ArrayList<Agent>();		
		for (Agent agent : agentList) {
			agent.step(currentTick);			
			if (!agent.isSexuallyActive()) {
				temp.add(agent);
			}				
		}						
		for (Agent agent : temp) {
			removeAgent(agent);
		}
		temp = null;
		for (int i=0; i<numAgents; i++) {
			if (Math.random() <= lifeProb) {
				addAgent();				
			}
		}		
	}

	public void run() {
		introduceInfection();
		for (int i=0; i<Settings.MAXIMUM_STEPS; i++) {
			currentTick = i;
			numHIV = 0;			
			updateAgents();
			sexualInteraction();
			HIV_Stat();
			String str = new String("tick: "  + i + " numHIV: " + numHIV + " numAgents: "  + agentList.size() + " Prev: " + (double) (numHIV/agentList.size())); 
			recorder4.println(str);
			print(str);
			if (stopTime != 0 && stopTime==i) {
				break;
			}
/*			if (i%100 == 0) {				
				recorder4.flush();
			}
*/		}
		/*double prevalence = (double) (numHIV/agentList.size());
		if (prevalence < 0.08) {
			print("Filename: " + currentIndex);
		}*/
		recorder4.close();
	}

	public boolean tryInfectPartner(Agent potentialInfector, Agent susceptible, int contactType) {
		boolean flag = false;
		double contactTypeFactor = 1;
		if (contactType == Settings.CONTACT_TYPE.ORAL) {
			contactTypeFactor = oralInfectivityFactor;
		}				
		int infectionStatus = potentialInfector.getInfectionStatus();
		double chanceForTransmission = 0;		
		if (infectionStatus == Settings.INFECTION_STATUS.PHI) {
			chanceForTransmission = (Settings.BASELINE_PROBABILITY * Settings.PHI_INFECTIVITY_FACTOR);			
		}
		else if (infectionStatus == Settings.INFECTION_STATUS.POST_PHI) {
			chanceForTransmission = Settings.BASELINE_PROBABILITY;
		}
		chanceForTransmission *= contactTypeFactor;
		if (Math.random() <= chanceForTransmission) {
			flag = true;
			potentialInfector.setInfectionContactType(contactType);
			susceptible.setInfectionContactType(contactType);
			setHIVInfection(susceptible, potentialInfector);			
		}		
		return flag;
	}

	public void assignVariablesAnal(Agent agent, Agent partner) {
		agent.numAnalSexContacts++;
		int partnerInfectionStatus = partner.getInfectionStatus();
		if (partnerInfectionStatus == Settings.INFECTION_STATUS.SUSCEPTIBLE) {
			agent.numSuscAnalSexContacts++;
		}
		else if (partnerInfectionStatus == Settings.INFECTION_STATUS.PHI) {
			agent.numPHIAnalSexContacts++;
		}
		else if (partnerInfectionStatus == Settings.INFECTION_STATUS.POST_PHI) {
			agent.numPostPHIAnalSexContacts++;
		}
	}

	public void assignVariablesOral (Agent agent, Agent partner) {		
		agent.numOralSexContacts++;
		int partnerInfectionStatus = partner.getInfectionStatus();
		if (partnerInfectionStatus == Settings.INFECTION_STATUS.SUSCEPTIBLE) {
			agent.numSuscOralSexContacts++;
		}
		else if (partnerInfectionStatus == Settings.INFECTION_STATUS.PHI) {
			agent.numPHIOralSexContacts++;
		}
		else if (partnerInfectionStatus == Settings.INFECTION_STATUS.POST_PHI) {
			agent.numPostPHIOralSexContacts++;
		}		
	}

	public boolean shouldAddSexualHistory(Agent agent) {
		//quick&dirty
		if (agent.isEnrolled()
				&& agent.getExitTick() != -1
				&& agent.getExitTick() <= currentTick
				) {
			return true;
		}
		return false;
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
		reader.read(parametersFilePath);
		param = new Vector<Double>();
		int prevIndex = 0, newIndex = 0;
		for (int i=1; i<=reader.getLineNo(); i++) {			
			param = reader.getParametersSet().get(new Integer(i));
			newIndex = prevIndex+i;
			buildModel(param, newIndex);
			createRecordFiles(newIndex);
			recorderHeaders();
			run();
			recordLevels();
			closeRecorders();
			System.gc();
		}				
	}

	public void createRecordFiles(int index) {
		try {
			currentIndex = index;
			recorder = new PrintWriter(new BufferedWriter(new FileWriter(index+"_level0" + ".csv")));
			recorder1 = new PrintWriter(new BufferedWriter(new FileWriter(index+"_level1" + ".csv")));
			recorder2 = new PrintWriter(new BufferedWriter(new FileWriter(index+"_level2" + ".csv")));
			recorder3 = new PrintWriter(new BufferedWriter(new FileWriter(index+"_level3" + ".csv")));
			recorder4 = new PrintWriter(new BufferedWriter(new FileWriter(index+"_Prevalence" + ".txt")));		
		} catch (IOException e) {e.printStackTrace();}
	}

	public static void main(String[] args) {
		Model model = new Model();
		model.setup();
	}

	public void recordLevels() {
		int numSuscAnalSexContacts=0, numPHIAnalSexContacts=0, numPostPHIAnalSexContacts=0,
		numSuscOralSexContacts=0, numPHIOralSexContacts=0, numPostPHIOralSexContacts=0;	
		String level0 = "", level1 = "", level2 = "", level3 = "";
		int infection = 0;

		int i=0;
		for (Agent agent : enrolledAgents) {
			i++;
			numSuscAnalSexContacts=0; numPHIAnalSexContacts=0; numPostPHIAnalSexContacts=0;
			numSuscOralSexContacts=0; numPHIOralSexContacts=0; numPostPHIOralSexContacts=0;
			String strID = "" + "'" + agent.getID() + "'";
			strID += ",";
			for (Integer period : agent.getObservationRecord().keySet()) {
				for (SexualHistory sexContact : agent.getObservationRecord().get(period)) {										
					if (sexContact.contactType == Settings.CONTACT_TYPE.ANAL) {
						if (sexContact.myPartnerInfectionStatus == Settings.INFECTION_STATUS.SUSCEPTIBLE) {
							numSuscAnalSexContacts++;
						}
						else if (sexContact.myPartnerInfectionStatus == Settings.INFECTION_STATUS.PHI) {
							numPHIAnalSexContacts++;
						}
						else if (sexContact.myPartnerInfectionStatus == Settings.INFECTION_STATUS.POST_PHI) {
							numPostPHIAnalSexContacts++;
						}
					}
					else if (sexContact.contactType == Settings.CONTACT_TYPE.ORAL) {
						if (sexContact.myPartnerInfectionStatus == Settings.INFECTION_STATUS.SUSCEPTIBLE) {
							numSuscOralSexContacts++;
						}
						else if (sexContact.myPartnerInfectionStatus == Settings.INFECTION_STATUS.PHI) {
							numPHIOralSexContacts++;
						}
						else if (sexContact.myPartnerInfectionStatus == Settings.INFECTION_STATUS.POST_PHI) {
							numPostPHIOralSexContacts++;
						}						
					}										
				}
				infection = agent.isGotInfected() ? 1 : 0;					
				level0 = period + ","
				+ infection + ","
				+ numSuscAnalSexContacts + ","
				+ numPHIAnalSexContacts + ","
				+ numPostPHIAnalSexContacts + ","
				+ numSuscOralSexContacts + ","
				+ numPHIOralSexContacts + ","
				+ numPostPHIOralSexContacts + ",";
				int numHIVAnalContacts = numPHIAnalSexContacts + numPostPHIAnalSexContacts;
				int numHIVOralContacts = numPHIOralSexContacts + numPostPHIOralSexContacts;
				level1 = period + ","  
				+ infection + ","
				+ numSuscAnalSexContacts + ","
				+ numHIVAnalContacts + ","
				+ numSuscOralSexContacts + ","
				+ numHIVOralContacts + ",";
				int totalOralContacts = (numHIVOralContacts + numSuscOralSexContacts);
				int totalAnalContacts = (numHIVAnalContacts + numSuscAnalSexContacts);
				level2 = period + "," 
				+infection + ","
				+ totalAnalContacts + ","
				+ totalOralContacts + ",";
				double n1 = Normal.staticNextDouble(totalAnalContacts, 0.1*totalAnalContacts);
				double n2 = Normal.staticNextDouble(totalOralContacts, 0.1*totalOralContacts);
				level3 = period + "," 
				+ infection + ","
				+ (int) n1 + ","
				+ (int) n2 + ",";
			}
			recorder.println(""+strID+level0);
			recorder1.println(""+strID+level1);
			recorder2.println(""+strID+level2);
			recorder3.println(""+strID+level3);

		}
		if (i%100==0) {
			flushRecorders();
		}			
		flushRecorders();
	}	

	public void recorderHeaders() {
		recorder4.println("CRR: " + contactRateRatio);
		recorder4.println("analtoOral_Proportion: " + preferenceForOralSex);
		recorder4.println("risk_factor: " + oralInfectivityFactor);		
		String level0="", level1="", level2="", level3="";
		String l0="", l1="", l2="", l3="";
		level0 = "period," + "infection," + "SusceptibleAnalContacts," + "PHIAnalContacts," 
		+ "PostPHIAnalContacts," + "SusceptibleOralContacts," 
		+ "PHIOralContacts," + "PostPHIOralContacts,";
		level1 = "period," + "infection," + "SusceptibleAnalContacts," + "HIVAnalContacts,"
		+ "SusceptibleOralContacts," + "HIVOralContacts,";
		level2 = "period," + "infection," + "AnalContacts," + "OralContacts,";
		level3 = "period," + "infection," + "AnalContacts," + "OralContacts,";

		l0 += "agentID,";
		l1 += "agentID,"; 
		l2 += "agentID,"; 
		l3 += "agentID,"; 
		for (int i=0; i<Settings.NUMBER_STUDY_PERIODS; i++) {
			l0 += level0;			
			l1 += level1;
			l2 += level2;
			l3 += level3;
		}
		recorder.println(l0);
		recorder1.println(l1);
		recorder2.println(l2);
		recorder3.println(l3);
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

	public void HIV_Stat() {
		int numPHI = 0, numAsym = 0;
		for (Agent agent : agentList) {
			if (agent.getInfectionStatus() != Settings.INFECTION_STATUS.SUSCEPTIBLE) {
				numHIV++;
			}
			if (agent.getInfectionStatus() == Settings.INFECTION_STATUS.PHI) {
				numPHI++;
			}
			else if (agent.getInfectionStatus() == Settings.INFECTION_STATUS.POST_PHI) {
				numAsym++;
			}
		}
	}

	public void print(String str) {
		System.out.println(""+str);
	}
}