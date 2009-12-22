package ibm_alt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import cern.jet.random.Uniform;
import flanagan.math.PsRandom;
import uchicago.src.sim.util.SimUtilities;

public class Model {
	
	FileWriter fWriter;
	PsRandom psr = new PsRandom();
	
	public static double             durIncub = 9,
	 durEarly = 49,
	 durAsymp = 365 * 7, 
	 durLate = 365;
	
	int currentTick = -1;
	
	public static class COHORT {
		public static final int NO_SEX = 0;
		public static final int INSERTIVES = 1;
		public static final int RECEPTIVES = 2;
		public static final int DUALS = 3;
	}

	FileWriter fstream;
	
	public int numIterations = 10000;
	public int numInitialAgents = 10000;
	public double initialInfection = 0.01;
	public double durationLife = 25*12;

	public double PPR = 0.85;
	public double APR = 0.00694444;
//	public double X = 0.00568801;
	public double X = 0.01;
	public double E = 60;
//	public double TFIT = 0.05;
	public double TFIT = 0.07;
	public double BRCR = 4;
	public double SCP = 0.5;
	public double CRRID = 1;
	public double CRRD = 1;
	public double DTIME = 1;
	public double ITIME = 1;
	public double RTIME = 1;
	public double NTIME = 1;
	public double TOT_TIME = 1;
	public double IRSWITCH = 1;
	//dt is assumed as 1 month
//	public double dt = 1/30;
	public double dt = 0.03333;

	public double FrctLvP = 1;   		
	public double FrctLvA = 1;
	double InitRplusD = 0.311004;

	ArrayList<Agent> agentList = new ArrayList<Agent>();
	//0->NoSex; 1->Insertives; 2->Receptives; 3->Duals
	HashMap<Integer, ArrayList<Agent>> cohorts = new HashMap<Integer, ArrayList<Agent>>();

	int numInsertives = 0, numReceptives = 0, numNoSex = 0, numDuals = 0;
	double numHIV = 0; 	double numPHI = 0, numPostPHI = 0;
	int lastID = -1;


	double eR = 0, eI = 0, eD = 0,eN = 0;
	int initR = 0, initI = 0, initD = 0, initN = 0; 
	double DRCR = 0, RRCR = 0, IDTc = 0, IRTc = 0, DRTc = 0, DUTc = 0;
	double teR=0, teI=0, teD=0, teN=0;
	double ObsEplusD = 0;

	ArrayList<Agent> newNoSex = new ArrayList<Agent>(), 
	newInsertives = new ArrayList<Agent>(),
	newReceptives = new ArrayList<Agent>(), 
	newDuals = new ArrayList<Agent>();	

	double NTime = 1, TotTime = 1, DTime = 1, ITime = 1, RTime = 1;
	double commonDenominator = NTime*TotTime*3;

	/*public double rND = 1/(NTime*TotTime*3);
	public double rDN = 1/(DTime*TotTime*3);
	public double rNI = 1/(NTime*TotTime*3);
	public double rIN = 1/(ITime*TotTime*3);
	public double rNR = 1/(NTime*TotTime*3);
	public double rRN = 1/(RTime*TotTime*3);
	public double rDI = 1/(DTime*TotTime*3);
	public double rID = 1/(ITime*TotTime*3);
	public double rDR = 1/(DTime*TotTime*3);
	public double rRD = 1/(RTime*TotTime*3);
	public double rIR = 1/(ITime*TotTime*3);
	public double rRI = 1/(RTime*TotTime*3);*/

	//From Blower's paper 
	public double rND=0.0138;		
	public double rDN=0.03106;
	public double rNI=0.0213;
	public double rIN=0.0594;
	public double rNR=0.0121;
	public double rRN=0.0643;		
	public double rDI=0.0291;	
	public double rID=0.0311;
	public double rDR=0.0176;
	public double rRD=0.0331;
	public double rIR=0.0068;	
	public double rRI=0.00855;

	public double probNI = 0, probNR = 0, probND = 0,
	probIN = 0, probIR = 0, probID = 0,
	probRN = 0, probRI = 0, probRD = 0,
	probDN = 0, probDI = 0, probDR = 0;

	public double probDRCR = 0, probRRCR = 0;

	public double VE = 0, VA = 0;
	public double S = 1 / ((1/durationLife)+PPR);
	//	public double S = 1;
	//{Expected lifetime in A}
	public double T = FrctLvP / ((1/durationLife)+APR); 			 
	//	public double T = 1;
	//	{Baseline transmission probability in the asymptomatic stage of infection}
	public double Y = 1;			
	//	aaR = Y*E*(1-VE)*(1-TFIT){Transmission probability from PHI to a receptive after reduction}
	public double aaR = 1;				
	//	bR = Y*(1-VA)*(1-TFIT){Transmission probability from Post-PHI to a receptive after reduction}
	public double bR = 1;	
	//	aaI = Y*E*(1-VE)*TFIT {Transmission probability from PHI to an insertive after reduction}
	public double aaI = 1;
	//	bI = Y*(1-VA)*TFIT	{Transmission probability from Post-PHI to an insertive after reduction}
	public double bI = 1;

	private double numInfectorsInsertive=0, numInfectorsReceptive=0, numInfectorsDuals=0;
	private double numInfectedsInsertive=0, numInfectedsReceptive=0, numInfectedsDuals=0;
	private double numHIVInsertives=0, numHIVRecpetives=0, numHIVDuals=0, numHIVNonSex=0;
	
	public void initializeVariables() {
		double denominator = (double) 1/(25*12);
		denominator = 0.00332969;
		FrctLvP =  (double) (PPR / (denominator+PPR));
		//		FrctLvP =  (double) (PPR / ((1/(25*12))+PPR));
		FrctLvA =  (double) (APR / (denominator+APR));
		//		FrctLvA =  APR / ((1/(25*12))+APR);

		S = 1 / (denominator+PPR);
		T = FrctLvP /(denominator+APR);
		
		Y = X*(S+T)/((E*S)+T);			
		//	aaR = Y*E*(1-VE)*(1-TFIT){Transmission probability from PHI to a receptive after reduction}
		aaR = Y*E*(1-VE)*(1-TFIT);				
		//		bR = Y*(1-VA)*(1-TFIT){Transmission probability from Post-PHI to a receptive after reduction}
		bR = Y*(1-VA)*(1-TFIT);	
		//		aaI = Y*E*(1-VE)*TFIT {Transmission probability from PHI to an insertive after reduction}
		aaI = Y*E*(1-VE)*TFIT;
		//		bI = Y*(1-VA)*TFIT	{Transmission probability from Post-PHI to an insertive after reduction}
		bI = Y*(1-VA)*TFIT;

//		probNI = returnProbability(rNI)/30;
//		probNR = returnProbability(rNR)/30;
//		probND = returnProbability(rND)/30;
//		probIN = returnProbability(rIN)/30;
//		probIR = returnProbability(rIR)/30;
//		probID = returnProbability(rID)/30;
//		probRN = returnProbability(rRN)/30;
//		probRI = returnProbability(rRI)/30;
//		probRD = returnProbability(rRD)/30;
//		probDN = returnProbability(rDN)/30;
//		probDI = returnProbability(rDI)/30;
//		probDR = returnProbability(rDR)/30;

//		probNI = returnProbability(rNI);
//		probNR = returnProbability(rNR);
//		probND = returnProbability(rND);
//		probIN = returnProbability(rIN);
//		probIR = returnProbability(rIR);
//		probID = returnProbability(rID);
//		probRN = returnProbability(rRN);
//		probRI = returnProbability(rRI);
//		probRD = returnProbability(rRD);
//		probDN = returnProbability(rDN);
//		probDI = returnProbability(rDI);
//		probDR = returnProbability(rDR);
	}
	
	public void refreshVariables() {
		numInfectorsInsertive=0; numInfectorsReceptive=0; numInfectorsDuals=0;
		numInfectedsInsertive=0; numInfectedsReceptive=0; numInfectedsDuals=0;
		numHIVInsertives=0; numHIVRecpetives=0; numHIVDuals=0; numHIVNonSex=0;
	}

	public void initializeCohortSizes() {
		/* {The following are elements in the derivation of the equilibrium population sizes in the absence of HIV infection deaths.  The equilibrium
		    		population size sums to one.  Derivation involved solving the four linear equilibrium equations for four unknowns.}
		 */
		double aa= rDN + rDI + rDR;
		double bb= rIN + rID + rIR;
		double cc= rRN + rRD +rRI;
		double ee= aa + rND;
		double ff= rRD - rND;
		double gg= rID - rND;
		double hh= bb + rNI;
		double jj= rRI - rNI;
		double kk= rDI - rNI;
		double ll= cc + rNR;
		double mm= rIR - rNR;
		double nn= rDR - rNR;
		double oo= hh - gg*kk / ee;
		double pp= jj + ff*kk / ee;
		double qq= rND*kk / ee + rNI;
		double ss= ll - ff*nn / ee;
		double tt= mm + gg*nn / ee;
		double uu= rND*nn / ee +rNR;

		if (oo == 0) {
			teR = -qq / pp;		
		}
		else {
			teR = ((qq*tt / oo) + uu) / (ss - (pp*tt / oo));
		}

		if (oo == 0) {
			teI = (teR*ss - uu) / tt;
		}
		else {
			teI = (teR*pp + qq) / oo;
		}

		teD = (teR*ff + teI*gg + rND) / ee;
		teN = 1 - teD - teR - teI;

		ObsEplusD = teD + teR;

		/* eR = teR*(InitRplusD / (teR+teD))						{The size of the eR when the total is adjusted to keep the size of the receptive population constant}
				   eI = teI*(InitRplusD / (teR+teD))						{The size of the eR when the total is adjusted to keep the size of the receptive population constant}
				   eD = teD*(InitRplusD / (teR+teD))						{The size of the eR when the total is adjusted to keep the size of the receptive population constant}
				   eN = teN*(InitRplusD / (teR+teD))						{The size of the eR when the total is adjusted to keep the size of the receptive population constant}
		 */

		eR = teR*(InitRplusD / (teR+teD));
		eI =  teI*(InitRplusD / (teR+teD));
		eD = teD*(InitRplusD / (teR+teD));
		eN = teN*(InitRplusD / (teR+teD));

		numReceptives = (int) (eR*numInitialAgents); 
		numInsertives = (int) (eI*numInitialAgents);
		numNoSex = (int) (eN*numInitialAgents);
		numDuals = (int) (eD*numInitialAgents);
//		numReceptives = (int) (teR*numInitialAgents); 
//		numInsertives = (int) (teI*numInitialAgents);
//		numNoSex = (int) (teN*numInitialAgents);
//		numDuals = (int) (teD*numInitialAgents);		

		print ("eN: " + eN);
		print ("eR: " + eR);
		print ("eI: " + eI);
		print ("eD " + eD);
		print("num No sex: " + numNoSex);
		print("num  insert: " + numInsertives);
		print("num  receptiov: " + numReceptives);
		print("num duals : " + numDuals);	

	}

	public double returnProbability(double rate) {
		double d =(1-Math.exp(-rate*dt)); 
		return d;
	}
	
	public double returnProbability(double rate, double dt2) {
		double d =(1-Math.exp(-rate*dt)); 
		return d;
	}
	
	public double rP(double rate) {
		double d =(1-Math.exp(-rate)); 
		return d;
	}

	public void createInitialAgents() {
		for (int i=0; i<=3; i++) {
			cohorts.put(new Integer(i), new ArrayList<Agent>());
		}
		//0->NoSex; 1->Insertive; 2->Receptive; 3->Duals
		for (int i=0; i<numNoSex; i++) {
			//0->NoSex
			Agent agent = new Agent(++lastID, COHORT.NO_SEX, this);
			cohorts.get(new Integer(COHORT.NO_SEX)).add(agent);
			agentList.add(agent);
		}
		for (int i=0; i<numInsertives; i++) {
			//1->Insertive
			Agent agent = new Agent(++lastID, COHORT.INSERTIVES, this);
			cohorts.get(new Integer(COHORT.INSERTIVES)).add(agent);
			agentList.add(agent);
		}
		for (int i=0; i<numReceptives; i++) {
			Agent agent = new Agent(++lastID, COHORT.RECEPTIVES, this);
			cohorts.get(new Integer(COHORT.RECEPTIVES)).add(agent);
			agentList.add(agent);
		}
		for (int i=0; i<numDuals; i++) {
			Agent agent = new Agent(++lastID, COHORT.DUALS, this);
			cohorts.get(new Integer(COHORT.DUALS)).add(agent);
			agentList.add(agent);
		}		
	}

	public void setup() {
		try {
		     fWriter = new FileWriter("data6.txt", true);
		} catch(IOException e) {
			e.printStackTrace();
		}
		initializeCohortSizes();
		initializeVariables();
		initializeContactProbabilities();
		createInitialAgents();
		initializeInfection();
	}

	public void updateAgents() {
		numHIV=0; numPHI=0; numPostPHI=0;
		ArrayList<Agent> dead = new ArrayList<Agent>();
		for (Agent agent : agentList) {
			agent.step(currentTick);
			if (agent.getInfectionStatus() != "susceptible") {
				numHIV++;
				if (agent.getCohort() == COHORT.NO_SEX) {
					numHIVNonSex++;
				}
				else if (agent.getCohort() == COHORT.INSERTIVES) {
					numHIVInsertives++;
				}
				else if (agent.getCohort() == COHORT.RECEPTIVES) {
					numHIVRecpetives++;
				}
				else {
					numHIVDuals++;
				}
				if (agent.getInfector() == 0) {
					numPHI++;					
				}
				else if (agent.getInfector() == 1) {
					numPostPHI++;
				}
			}			
			if (agent.isDeath()) {
				dead.add(agent);
			}					
		}
		reapAndReplaceAgents(dead);
	}

	public void reapAndReplaceAgents(ArrayList<Agent> dead) {
		int suscDeaths = 0;
		int numI = 0, numR = 0, numD = 0, numN = 0;
		for (Agent agent : dead) {
			int cohort = agent.getCohort();
			cohorts.get(new Integer(cohort)).remove(agent);				
			agentList.remove(agent);
		}
		introduceNewAgents();
	}
	

	public void introduceNewAgents() {
		double numActualAgents = (double) numInitialAgents;
		double d = (double) numInitialAgents;
		double numNewAgents = (double) (numActualAgents /d );
		double rand = 0;
		for (int i=0; i<numNewAgents; i++) {			
			Agent agent = new Agent(++lastID, COHORT.NO_SEX, this);
			int index = -1; 
//			double rnd = Math.random();
//			if (rnd <= teN) {
//				index = COHORT.NO_SEX;
//			}
//			else if (rnd > teN && rnd <= teN+teI) {
//				index = COHORT.INSERTIVES;
//			}
//			else if (rnd > teN+teI && rnd <= teN+teI+teR) {
//				index = COHORT.RECEPTIVES;
//			}
//			else if (rnd > teN+teI+teR && rnd <= teN+teI+teR+teD) {
//				index = COHORT.DUALS;
//			}
			index = Uniform.staticNextIntFromTo(0, 3);
			agent.setCohort(index);
			agentList.add(agent);
			cohorts.get(new Integer(index)).add(agent);
		}
	}

	public void initializeInfection() {
		int numInitInfection = (int) (agentList.size() * initialInfection);
		SimUtilities.shuffle(agentList);
		for (int i=0; i<numInitInfection; i++) {
			Agent agent = agentList.get(i);
			agent.setInfectionStatus("PHI");
			agent.setInfectedTick(0);
		}
	}

	public void run() {
		for (int iter=0; iter<numIterations; iter++) {
			refreshVariables();
			currentTick = iter;
			numNoSex = cohorts.get(0).size();
			numInsertives = cohorts.get(1).size();
			numReceptives = cohorts.get(2).size();
			numDuals = cohorts.get(3).size();			
			sexualInteraction();
			updateAgents();
			transitions();
//			System.out.println("PrevN: " + (double) (numHIVNonSex/agentList.size())*100 + " PrevI: " + (double) (numHIVInsertives/agentList.size())*100
//				 + " PrevR: " + (double) (numHIVRecpetives/agentList.size())*100 + " PrevD: " + (double) (numHIVDuals/agentList.size())*100);
//			System.out.println("PrevN: " + (double) (numHIVNonSex/numNoSex)*100 + " PrevI: " + (double) (numHIVInsertives/numInsertives)*100
//					 + " PrevR: " + (double) (numHIVRecpetives/numReceptives)*100 + " PrevD: " + (double) (numHIVDuals/numDuals)*100);

			//0->NoSex; 1->Insertives; 2->Receptives; 3->Duals			
//			print("numPHI: " + numPHI + " frac PHI: " + numPHI/numHIV);
//			print("numPostPHI: " + numPostPHI + " frac PostPHI: " + numPostPHI/numHIV);
			
			System.out.println("tick:" + iter + " agents: " + agentList.size());
			print("Nosex: " + numNoSex + " Duals: " + numDuals + " Insertives: " + numInsertives + " Receptives: " + numReceptives);
//			System.out.println("PrevN: " + numHIVNonSex + " PrevD: " + numHIVDuals + " PrevI: " + numHIVInsertives
//					 + " PrevR: " + numHIVRecpetives);
//			System.out.println("Prev: " + (double) (numHIV/agentList.size())*100 + " " + (numPHI/numHIV)*100);

		}
		try {
			BufferedWriter out = new BufferedWriter(fWriter);
		    out.write((double) (numHIV/agentList.size())*100 + " " + (numPHI/numHIV)*100);
		    out.newLine();
		    out.close();
		} catch(IOException e) {e.printStackTrace();};
		System.out.println("Prev: " + (double) (numHIV/agentList.size())*100 + " " + (numPHI/numHIV)*100);
//		print("num No sex: " + numNoSex);
//		print("num  insert: " + numInsertives);
//		print("num  receptiov: " + numReceptives);
//		print("num duals : " + numDuals);	
	}

	public void initializeContactProbabilities() {
		//{The total receptive contact rate of dual role individuals}
		DRCR = (double) (BRCR/ (eD+eR*CRRD));		
		print("DRCR: " + DRCR);
		//	{The total receptive contact rate of receptive only individuals}
		RRCR = DRCR*CRRD;		
		probDRCR = returnProbability(DRCR);
		probRRCR = returnProbability(RRCR);
		print("probDRCR: " + probDRCR);
		print("probRRCR: " + probRRCR);
	}

	public void setNewCohort(Agent agent, int cohort) {
		agent.setCohort(cohort);
		switch (cohort) {
		case COHORT.NO_SEX:
			newNoSex.add(agent);
			agent.setBehavior("no-sex");
			break;
		case COHORT.INSERTIVES:
			newInsertives.add(agent);
			break;
		case COHORT.RECEPTIVES:
			newReceptives.add(agent);
			break;
		case COHORT.DUALS:
			newDuals.add(agent);
			break;
		default:break;
		}		
	}

	public void sexualTransmission(Agent agent1, Agent agent2) {
		if ((agent1.getInfectionStatus() == "susceptible"
			&& agent2.getInfectionStatus() == "susceptible")
			|| (agent1.getInfectionStatus() != "susceptible"
				&& agent2.getInfectionStatus() != "susceptible")) {			
			return;
		}
		//either of the two partners is exclusively infected 
		if (agent1.getInfectionStatus() != "susceptible") {
			infect(agent1, agent2);
		}
		else if (agent2.getInfectionStatus() != "susceptible") {
			infect(agent2, agent1);
		}
	}

	public void infect(Agent infector, Agent infected) {
		double prob = 0;
		boolean infect = false;
		if (infector.getInfectionStatus() == "PHI") {			
			if (infected.getBehavior() == "insertive") {
				prob = aaI;				
			}
			else if (infected.getBehavior() == "receptive") {
				prob = aaR;
			}
			else if (infected.getBehavior() == "reciprocal") {
				prob = aaI + aaR;
			}
			if (Math.random() <= prob) {
				infected.setInfectionStatus("PHI");
				infected.setInfector(0);
				infected.setInfectedTick(currentTick);
				infect = true;
			}
		}
		//agent1 is Post-PHI
		else {
			if (infected.getBehavior() == "insertive") {
				prob = bI;
			}
			else if (infected.getBehavior() == "receptive") {
				prob = bR;
			}			
			else if (infected.getBehavior() == "reciprocal") {
				prob = bI + bR;
			}
			if (Math.random() <= prob) {
				infected.setInfectionStatus("PHI");
				infected.setInfector(1);
				infected.setInfectedTick(currentTick);
				infect = true;
			}			
		}
		
		if (infect) {
			if (infector.getCohort() == COHORT.INSERTIVES) {
				numInfectorsInsertive++;
			}
			else if (infector.getCohort() == COHORT.RECEPTIVES) {
				numInfectorsReceptive++;
			}
			else {
				numInfectorsDuals++;
			}
			
			if (infected.getCohort() == COHORT.INSERTIVES) {
				numInfectedsInsertive++;
			}
			else if (infected.getCohort() == COHORT.RECEPTIVES) {
				numInfectedsReceptive++;
			}
			else {
				numInfectedsDuals++;
			}					
		}				
	}

	public void print(String str) {
		System.out.println(""+str);
	}

	public void sexualInteraction() {
		SimUtilities.shuffle(cohorts.get(COHORT.RECEPTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.INSERTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.DUALS));

		ArrayList<Agent> receptiveOnlySex = new ArrayList<Agent>(),
		dualUnidirectionalSex = new ArrayList<Agent>(),
		dualBidirectionalSex = new ArrayList<Agent>();

		ArrayList<Agent> temp1 = new ArrayList<Agent>(cohorts.get(COHORT.RECEPTIVES)),
		temp2 = new ArrayList<Agent>();

		for (Agent receptive : temp1) {
			if (Math.random() <= probRRCR) {
				receptiveOnlySex.add(receptive);
			}
		}
		for (Agent dual : cohorts.get(COHORT.DUALS)) {
			if (Math.random() <= probDRCR) {
				temp2.add(dual);
			}
		}
		double proportion = 1-SCP;
		for (Agent dual : temp2) {
			if (Math.random() <= proportion)  {
				dualUnidirectionalSex.add(dual);
			}
			else {
				dualBidirectionalSex.add(dual);
			}
		}

		double numInsertives = cohorts.get(COHORT.INSERTIVES).size();		
		double numUnidirectionalDuals = dualUnidirectionalSex.size();
		
		ArrayList<Agent> tmp = new ArrayList<Agent>();
		tmp.addAll(receptiveOnlySex); tmp.addAll(dualUnidirectionalSex);		
		SimUtilities.shuffle(tmp);
		for (Agent receptive : tmp) {
			Agent insertive = null;								
			if (Math.random() <= (numInsertives*CRRID/(numUnidirectionalDuals+numInsertives*CRRID))) {
				insertive = (Agent) cohorts.get(COHORT.INSERTIVES)
				.get(Uniform.staticNextIntFromTo(0, (int)numInsertives-1)); 				
			}
			else {
				do {
					insertive = (Agent)dualUnidirectionalSex.get(Uniform.staticNextIntFromTo(0, (int) numUnidirectionalDuals-1));	
				} while (receptive.getId() == insertive.getId());
				
			}
			insertive.setBehavior("insertive");
			receptive.setBehavior("receptive");
			sexualTransmission(insertive, receptive);
		}

/*		SimUtilities.shuffle(receptiveOnlySex);
		for (Agent receptive : receptiveOnlySex) {
			Agent insertive = null;								
			if (Math.random() <= (numInsertives*CRRID/(numUnidirectionalDuals+numInsertives*CRRID))) {
				insertive = (Agent) cohorts.get(COHORT.INSERTIVES)
				.get(Uniform.staticNextIntFromTo(0, (int)numInsertives-1)); 				
			}
			else {
				insertive = (Agent)dualUnidirectionalSex.get(Uniform.staticNextIntFromTo(0, (int) numUnidirectionalDuals-1));
			}
			insertive.setBehavior("insertive");
			receptive.setBehavior("receptive");
			sexualTransmission(insertive, receptive);
		}

		//those driving unidirectional receptive sex from the Duals
		SimUtilities.shuffle(dualUnidirectionalSex);
		for (Agent receptive : dualUnidirectionalSex) {
			Agent insertive = null;
			if (Math.random() <= (numInsertives*CRRID/(numUnidirectionalDuals+numInsertives*CRRID))) {
				insertive = (Agent) cohorts.get(COHORT.INSERTIVES)
				.get(Uniform.staticNextIntFromTo(0, (int)numInsertives-1));
			}
			else {
				do {
					insertive = (Agent)dualUnidirectionalSex.get(Uniform.staticNextIntFromTo(0, (int) numUnidirectionalDuals-1));
				} while (insertive.getId() == receptive.getId());
			}		
			insertive.setBehavior("insertive");
			receptive.setBehavior("receptive");
			sexualTransmission(insertive, receptive);
		}*/

		SimUtilities.shuffle(dualBidirectionalSex);
		for (Agent recep1 : dualBidirectionalSex) {
			Agent recep2 = null;
			do {
				recep2 = (Agent)dualBidirectionalSex.get(Uniform.staticNextIntFromTo(0, dualBidirectionalSex.size()-1));
			} while (recep1.getId() == recep2.getId());
			recep1.setBehavior("reciprocal");
			recep2.setBehavior("reciprocal");
			sexualTransmission(recep1, recep2);
		}
	}
	
	public void transitions() {
		newNoSex.clear();
		newInsertives.clear();
		newReceptives.clear();
		newDuals.clear();

		for (Agent agent : agentList) {
			int oldCohort = agent.getCohort();			
			double rand = Math.random();
			switch (agent.getCohort()) {
			case COHORT.NO_SEX:
				if (rand <= probNI) {
					setNewCohort(agent, COHORT.INSERTIVES);
					break;
				}
				else if (probNI > rand && rand <= probNI+probNR) {
					setNewCohort(agent, COHORT.RECEPTIVES);
					break;
				}
				else if (probNI+probNR > rand && rand <= probNI+probNR+probND) {
					setNewCohort(agent, COHORT.DUALS);
					break;
				}				
				newNoSex.add(agent);
				agent.setBehavior("no-sex");
				break;
			case COHORT.INSERTIVES:
				if (rand <= probIN) {
					setNewCohort(agent, COHORT.NO_SEX);
					break;
				}
				else if (probIN > rand && rand <= probIN+probIR) {
					setNewCohort(agent, COHORT.RECEPTIVES);
					break;
				}
				else if (probIN+probIR > rand && rand <= probIN+probIR+probID) {
					setNewCohort(agent, COHORT.DUALS);
					break;
				}				
				newInsertives.add(agent);
				break;
			case COHORT.RECEPTIVES:
				if (rand <= probRN) {
					setNewCohort(agent, COHORT.NO_SEX);
					break;
				}
				else if (probRN > rand && rand <= probRN+probRI) {
					setNewCohort(agent, COHORT.INSERTIVES);
					break;
				}
				else if (probRN+probRI > rand && rand <= probRN+probRI+probRD) {
					setNewCohort(agent, COHORT.DUALS);
					break;
				}						
				newReceptives.add(agent);
				break;
			case COHORT.DUALS:
				if (rand <= probDN) {
					setNewCohort(agent, COHORT.NO_SEX);
					break;
				}
				else if (probDN > rand && rand <= probDN+probDI) {
					setNewCohort(agent, COHORT.INSERTIVES);
					break;
				}
				else if (probDN+probDI > rand && rand <= probDN+probDI+probDR) {
					setNewCohort(agent, COHORT.RECEPTIVES);
					break;
				}		
				newDuals.add(agent);
				break;
			default: break;
			}
//			if (agent.getCohort() != oldCohort) {
//				print("Agent: " + agent.getId() + " - moved from : " + oldCohort + " to new Cohort: " + agent.getCohort());
//			}
		}
		cohorts.get(0).clear(); cohorts.get(0).addAll(newNoSex);
		cohorts.get(1).clear(); cohorts.get(1).addAll(newInsertives);
		cohorts.get(2).clear(); cohorts.get(2).addAll(newReceptives);
		cohorts.get(3).clear(); cohorts.get(3).addAll(newDuals);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Model model = new Model();
		model.setup();
		model.run();
	}

	public PsRandom getPsr() {
		return psr;
	}

	public void setPsr(PsRandom psr) {
		this.psr = psr;
	}
}