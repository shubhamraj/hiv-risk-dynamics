package model;

import flanagan.math.PsRandom;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import cern.jet.random.Uniform;
import uchicago.src.sim.util.SimUtilities;

public class DirectionalSexModel {
	public static class COHORT {
		public static final int NO_SEX = 0;
		public static final int INSERTIVES = 1;
		public static final int RECEPTIVES = 2;
		public static final int DUALS = 3;
	}
	FileWriter fWriter;
	double numCount=0, globalPrev=0, globalFRPHI=0;
	int a=0, b=0, c=0, d=0;

	double numHIVNonSex = 0, numHIVInsertives = 0, numHIVReceptives = 0, numHIVDuals = 0;
	PsRandom psr = new PsRandom();
	//SET dt so that the we match Jim's duration for PHI and 
	//Post-PHI that is 35 days and ~4320 days respectively.
	double dt = 0.024;
	public double d1=0, d2=0;
	//this is 1/Risk_Duration in days
//	double mu = 0.00332969;
	double mu = 0.0020833;

	int nR = 0, nD = 0, nSym = 0;
	public int numIterations = 30000;
	public int numInitialAgents = 20000;
	public double initialInfection = 0.01;
	public double durationLife = 40*12*30;
	public double PPR = 0.85;
	public double APR = 0.00694444;
	public double X = 0.01;
	public double E = 60;
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

	public double FrctLvP = 1;   		
	public double FrctLvA = 1;
	double InitRplusD = 0.311004;

	ArrayList<Individual> agentList = new ArrayList<Individual>();
	HashMap<Integer, ArrayList<Individual>> cohorts = new HashMap<Integer, ArrayList<Individual>>();

	
	int currentTick = -1;
	int initInsertives = 0, initReceptives = 0, initNoSex = 0, initDuals = 0;
	int numInsertives = 0, numReceptives = 0, numNoSex = 0, numDuals = 0;
	double numHIV = 0; 	double numPHI = 0, numPostPHI = 0, numNewHIV = 0;
	int lastID = -1;

	double eR = 0, eI = 0, eD = 0,eN = 0;
	int initR = 0, initI = 0, initD = 0, initN = 0; 
	double DRCR = 0, RRCR = 0, IDTc = 0, IRTc = 0, DRTc = 0, DUTc = 0;
	double teR=0, teI=0, teD=0, teN=0;
	double ObsEplusD = 0;

	public double probNI = 0, probNR = 0, probND = 0,
	probIN = 0, probIR = 0, probID = 0,
	probRN = 0, probRI = 0, probRD = 0,
	probDN = 0, probDI = 0, probDR = 0;

	ArrayList<Individual> newNoSex = new ArrayList<Individual>(), 
	newInsertives = new ArrayList<Individual>(),
	newReceptives = new ArrayList<Individual>(), 
	newDuals = new ArrayList<Individual>();	

	double NTime = 1, TotTime = 1, DTime = 1, ITime = 1, RTime = 1;
	double commonDenominator = NTime*TotTime*3;

	public double intoD = 1;
	public double intoI = 1;	
	public double intoR = 1;
	public double intoN = 1;
	public double  IRSwitch = 1;
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

	/*	  public double rND=intoD/(NTime*TotTime*3);				
	   public double rDN=intoN/(DTime*TotTime*3);
	   public double rNI=intoI/(NTime*TotTime*3);
	   public double rIN=intoN/(ITime*TotTime*3);
	   public double rNR=intoR/(NTime*TotTime*3);
	   public double rRN=intoN/(RTime*TotTime*3);
	   public double rDI=intoI/(DTime*TotTime*3);
	   public double rID=intoD/(ITime*TotTime*3);
	   public double rDR=intoR/(DTime*TotTime*3);
	   public double rRD=intoD/(RTime*TotTime*3);
	   public double rIR=intoR*IRSwitch/(ITime*TotTime*3);
	   public double rRI=intoI*IRSwitch/(RTime*TotTime*3);*/

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

	public void initializeInfection() {
		int numInitInfection = (int) (agentList.size() * initialInfection);
		SimUtilities.shuffle(agentList);
		for (int i=0; i<numInitInfection; i++) {
			Individual individual = agentList.get(i);
			individual.setInfectionStatus("PHI");
			individual.setInfectedTick(0);		
		}
	}

	public void infect(Individual infector, Individual infected) {		
		double prob = 0;
		if (infector.getInfectionStatus() == "PHI") {			
			if (infected.getBehavior() == "insertive") {
//				prob = aaI;				
				prob = infected.phiProbInsertive;
			}
			else if (infected.getBehavior() == "receptive") {
//				prob = aaR;
				prob = infected.phiProbReceptive;
			}
			else if (infected.getBehavior() == "dual-role") {
//				prob = aaI + aaR;
				prob = infected.phiProbInsertive + infected.phiProbReceptive;
			}
		}
		//infector is Post-PHI
		else if (infector.getInfectionStatus() == "Post-PHI"){
			if (infected.getBehavior() == "insertive") {
				prob = bI;
			}
			else if (infected.getBehavior() == "receptive") {
				prob = bR;
			}			
			else if (infected.getBehavior() == "dual-role") {
				prob = bI + bR;
			}
		}
		if (Math.random() <= prob) {
			infected.setInfectionStatus("PHI");
			infected.setInfectorID(infector.getId());
			infected.setInfectedTick(currentTick);
			infected.setInfectorStatus(infector.getInfectionStatus());
		}				
	}

	public void initializeVariables() {
		//probability to move from PHI -> Post-PHI 
		//		d1 = returnProbability(PPR);
//		d1=0.028571;
		d1=0.029;
		//		d1 = returnProbability(PPR);
		//		d1 = 0.027777;		
		//		d1 = 0.028;
		//probability to move from Post-PHI -> death
		//		d2 = returnProbability(APR);
		d2 = 2.31484848E-4;
		//		d2 = returnProbability(APR);
		print ("d1: " + d1);
		print("d2: " + d2);
		FrctLvP =  (double) (PPR / (mu+PPR));
		FrctLvA =  (double) (APR / (mu+APR));
		S = 1 /(mu+PPR);
		T = FrctLvP /(mu+APR);
		T = 110.498;
		Y = X*(S+T)/((E*S)+T);
		print ("S: " + S);
		print ("T: " + T);
		print ("Y: " + Y);
		//	aaR = Y*E*(1-VE)*(1-TFIT){Transmission probability from PHI to a receptive after reduction}
		aaR = Y*E*(1-TFIT);				
		//		bR = Y*(1-VA)*(1-TFIT){Transmission probability from Post-PHI to a receptive after reduction}
		bR = Y*(1-TFIT);	
		//		aaI = Y*E*(1-VE)*TFIT {Transmission probability from PHI to an insertive after reduction}
		aaI = Y*E*TFIT;
		//		bI = Y*(1-VA)*TFIT	{Transmission probability from Post-PHI to an insertive after reduction}
		bI = Y*TFIT;

		print("aaR: " + aaR);
		print("aaI: " + aaI);
		print("bR: " + bR);
		print("bI: " + bI);	

		/*	probNI = returnProbability(rNI);
		probNR = returnProbability(rNR);
		probND = returnProbability(rND);
		probIN = returnProbability(rIN);
		probIR = returnProbability(rIR);
		probID = returnProbability(rID);
		probRN = returnProbability(rRN);
		probRI = returnProbability(rRI);
		probRD = returnProbability(rRD);
		probDN = returnProbability(rDN);
		probDI = returnProbability(rDI);
		probDR = returnProbability(rDR);*/

		probNI = rNI/30;
		probNR = rNR/30;
		probND = rND/30;
		probIN = rIN/30;
		probIR = rIR/30;
		probID = rID/30;
		probRN = rRN/30;
		probRI = rRI/30;
		probRD = rRD/30;
		probDN = rDN/30;
		probDI = rDI/30;
		probDR = rDR/30;		

		/*		probNI = rNI;
		probNR = rNR;
		probND = rND;
		probIN = rIN;
		probIR = rIR;
		probID = rID;
		probRN = rRN;
		probRI = rRI;
		probRD = rRD;
		probDN = rDN;
		probDI = rDI;
		probDR = rDR;		
		 */
	}

	public void initializeCohortSizes() {
		/* JKS{The following are elements in the derivation of the equilibrium population sizes in the absence of HIV infection deaths.  The equilibrium
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

		eR = teR*(InitRplusD / (teR+teD));
		eI =  teI*(InitRplusD / (teR+teD));
		eD = teD*(InitRplusD / (teR+teD));
		eN = teN*(InitRplusD / (teR+teD));

		initReceptives = (int) (eR*numInitialAgents); 
		initInsertives = (int) (eI*numInitialAgents);
		initNoSex = (int) (eN*numInitialAgents);
		initDuals = (int) (eD*numInitialAgents);
	}

	public void createInitialAgents() {
		for (int i=0; i<=3; i++) {
			cohorts.put(new Integer(i), new ArrayList<Individual>());
		}
		//0->NoSex; 1->Insertive; 2->Receptive; 3->Duals
		for (int i=0; i<initNoSex; i++) {
			//0->NoSex
			Individual agent = new Individual(++lastID, "no-sex", this);
			cohorts.get(new Integer(COHORT.NO_SEX)).add(agent);
			agentList.add(agent);
		}
		for (int i=0; i<initInsertives; i++) {
			//1->Insertive
			Individual agent = new Individual(++lastID, "insertive-only", this);
			cohorts.get(new Integer(COHORT.INSERTIVES)).add(agent);
			agentList.add(agent);
		}
		for (int i=0; i<initReceptives; i++) {
			Individual agent = new Individual(++lastID, "receptive-only", this);
			cohorts.get(new Integer(COHORT.RECEPTIVES)).add(agent);
			agentList.add(agent);
		}
		for (int i=0; i<initDuals; i++) {
			Individual agent = new Individual(++lastID, "dual-role", this);
			cohorts.get(new Integer(COHORT.DUALS)).add(agent);
			agentList.add(agent);
		}		
	}

	public void initializeContactProbabilities() {
		//{The total receptive contact rate of dual role individuals}		
		DRCR = BRCR * (eD+eR) / (eD+eR*CRRD);	
		//	{The total receptive contact rate of receptive only individuals}
		RRCR = DRCR*CRRD;

		//		DRCR /= 30; RRCR /= 30;

		probDRCR = returnProbability(DRCR);
		probRRCR = returnProbability(RRCR);

		print("probDRCR: " + probDRCR);	
		print("probRRCR: " + probRRCR);
	}

	public void setup() {
		try {
			fWriter = new FileWriter("data-1.txt", true);
		} catch(IOException e) {
			e.printStackTrace();
		}
		initializeCohortSizes();
		initializeVariables();
		initializeContactProbabilities();
		createInitialAgents();
		initializeInfection();
	}

	public void sexualTransmission(Individual agent1, Individual agent2) {
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


	public void transitions() {
		newNoSex.clear();
		newInsertives.clear();
		newReceptives.clear();
		newDuals.clear();
		for (Individual individual : agentList) {
			String category = individual.getCategory();
			double rand = Math.random();
			if (category == "no-sex") {
				if (rand <= probNI) {
					individual.setCategory("insertive-only");
					newInsertives.add(individual);					
				}
				else if (rand > probNI && rand <= probNI+probNR) {
					individual.setCategory("receptive-only");
					newReceptives.add(individual);
				}
				else if (rand > probNI+probNR && rand <= probNI+probNR+probND) {
					individual.setCategory("dual-role");
					newDuals.add(individual);
				}				
				else {
					newNoSex.add(individual);
					individual.setCategory("no-sex");
				}
			}
			else if (category == "insertive-only") {
				if (rand <= probIN) {
					newNoSex.add(individual);
					individual.setCategory("no-sex");
				}
				else if (rand > probIN && rand <= probIN+probIR) {
					individual.setCategory("receptive-only");
					newReceptives.add(individual);
				}
				else if (rand > probIN+probIR && rand <= probIN+probIR+probID) {
					individual.setCategory("dual-role");
					newDuals.add(individual);
				}				
				else {					
					newInsertives.add(individual);
					individual.setCategory("insertive-only");
				}
			}
			else if (category == "receptive-only") {
				if (rand <= probRN) {
					newNoSex.add(individual);
					individual.setCategory("no-sex");
				}
				else if (rand > probRN && rand <= probRN+probRI) {
					individual.setCategory("insertive-only");
					newInsertives.add(individual);
				}
				else if (rand > probRN+probRI && rand <= probRN+probRI+probRD) {
					individual.setCategory("dual-role");
					newDuals.add(individual);
				}						
				else {
					individual.setCategory("receptive-only");
					newReceptives.add(individual);
				}
			}
			else if (category == "dual-role") {
				if (rand <= probDN) {
					newNoSex.add(individual);
					individual.setCategory("no-sex");
				}
				else if (rand > probDN && rand <= probDN+probDI) {
					individual.setCategory("insertive-only");
					newInsertives.add(individual);
				}
				else if (rand > probDN+probDI && rand <= probDN+probDI+probDR) {
					individual.setCategory("receptive-only");
					newReceptives.add(individual);
				}		
				else {
					individual.setCategory("dual-role");
					newDuals.add(individual);
				}
			}
		}
		cohorts.get(0).clear(); cohorts.get(0).addAll(newNoSex);
		cohorts.get(1).clear(); cohorts.get(1).addAll(newInsertives);
		cohorts.get(2).clear(); cohorts.get(2).addAll(newReceptives);
		cohorts.get(3).clear(); cohorts.get(3).addAll(newDuals);
	}

	public void run() {
		numNoSex = cohorts.get(0).size();
		numInsertives = cohorts.get(1).size();
		numReceptives = cohorts.get(2).size();
		numDuals = cohorts.get(3).size();
		int index = numIterations-100;
		System.out.println("num N: " + numNoSex + "num D: " + numDuals + "num R: " + numReceptives + "num I: " + numInsertives);	
		for (int iter=1; iter<=numIterations; iter++) {			
			currentTick = iter;
			refreshVariables();
			updateAgents();
			numNoSex = cohorts.get(0).size();
			numInsertives = cohorts.get(1).size();
			numReceptives = cohorts.get(2).size();
			numDuals = cohorts.get(3).size();
			sexualInteractionABC();
			ud();
			System.out.println("Tick: " + currentTick 
					+ " num agents: " + agentList.size() + " Prev: " + (double) (numHIV/agentList.size()) + " " + (numPHI/numHIV));
			System.out.println("#PHI: " + numPHI + " Post PHI: " + numPostPHI + " ratio: " + (double) numPHI/numPostPHI);
			System.out.println("a: "  + a + " b: " + b + " c: " + c + " d: " + d);
			transitions();		
			if (iter >= index) {
				numCount++;
				globalPrev += (double) (numHIV/agentList.size());
				globalFRPHI += (double) (numPHI/numHIV);	 							
			}			
		}		
		numNoSex = cohorts.get(0).size();
		numInsertives = cohorts.get(1).size();
		numReceptives = cohorts.get(2).size();
		numDuals = cohorts.get(3).size();
		ud();
		System.out.println("num N: " + numNoSex + "num D: " + numDuals + "num R: " + numReceptives + "num I: " + numInsertives);		
		System.out.println("NEW: PrevN: " + numHIVNonSex/numNoSex + " PrevD: " + numHIVDuals/numDuals + " PrevR: " + numHIVReceptives/numReceptives
				+ " PrevI: " + numHIVInsertives/numInsertives);
		System.out.println("Final. Tick: " + currentTick 
				+ " num agents: " + agentList.size() + " Prev: " + (double) (numHIV/agentList.size()) + " " + (numPHI/numHIV));
		globalPrev/=numCount; 
		globalFRPHI/=numCount;
		String str = "" + globalPrev + " " + globalFRPHI;
		try {
			fWriter.write(str);
			fWriter.append('\n');
			fWriter.close();
		} catch (IOException e){};
	}
	

	public double returnProbability(double rate) {
		double d =(1-Math.exp(-rate*dt)); 
		return d;
	}

	public static void main(String[] args) {
		DirectionalSexModel model = new DirectionalSexModel();
		model.setup();
		model.run();		
	}

	public void refreshVariables() {
		numNoSex = cohorts.get(0).size();
		numInsertives = cohorts.get(1).size();
		numReceptives = cohorts.get(2).size();
		numDuals = cohorts.get(3).size();			
	}

	public void print(String str) {
		System.out.println("" + str);
	}

	public void sexualInteraction2() {
		SimUtilities.shuffle(cohorts.get(COHORT.RECEPTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.INSERTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.DUALS));

		a=0; b=0; c=0; d=0;
		Individual receptive=null, insertive=null;
		double p1 = (numInsertives)/((numInsertives*CRRID) + (numDuals*(1-SCP)));
		System.out.println("---------- p 1: " + p1);
		for (int i=0; i<numReceptives; i++) {
			if (Math.random() <= probRRCR) {
				receptive = (Individual) cohorts.get(COHORT.RECEPTIVES)
				.get(Uniform.staticNextIntFromTo(0, (int)numReceptives-1));
				if (Math.random() <= p1) {
					a++;
					insertive = (Individual) cohorts.get(COHORT.INSERTIVES)
					.get(Uniform.staticNextIntFromTo(0, (int)numInsertives-1));
				}
				else {
					b++;
					insertive = (Individual) cohorts.get(COHORT.DUALS)
					.get(Uniform.staticNextIntFromTo(0, (int)numDuals-1));	
				}
				insertive.setBehavior("insertive");
				receptive.setBehavior("receptive");
				sexualTransmission(insertive, receptive);					
			}
		}

		SimUtilities.shuffle(cohorts.get(COHORT.RECEPTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.INSERTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.DUALS));

		for (int i=0; i<numDuals; i++) {
			if (Math.random() <= probDRCR) {
				receptive = (Individual) cohorts.get(COHORT.DUALS)
				.get(Uniform.staticNextIntFromTo(0, (int)numDuals-1));
				if (Math.random()  <= p1) {
					c++;
					insertive = (Individual) cohorts.get(COHORT.INSERTIVES)
					.get(Uniform.staticNextIntFromTo(0, (int)numInsertives-1));
				}
				else {
					do {
						d++;
						insertive = (Individual) cohorts.get(COHORT.DUALS)
						.get(Uniform.staticNextIntFromTo(0, (int)numDuals-1));
					} while (insertive.getId() == receptive.getId());
				}
				insertive.setBehavior("insertive");
				receptive.setBehavior("receptive");
				sexualTransmission(insertive, receptive);	
			}			
		}		

		Individual dual1=null, dual2=null;
		SimUtilities.shuffle(cohorts.get(COHORT.DUALS));
		for (int i=0; i<(numDuals*SCP); i++) {
			if (Math.random() <= probDRCR) {
				do {dual1 = (Individual) cohorts.get(COHORT.DUALS)
					.get(Uniform.staticNextIntFromTo(0, (int)numDuals-1));
				} while (dual1.contacted);
				do {
					dual2 = (Individual) cohorts.get(COHORT.DUALS)
					.get(Uniform.staticNextIntFromTo(0, (int)numDuals-1));
				} while ((receptive.getId() == insertive.getId()) || dual2.contacted);		
				insertive.setBehavior("dual-role");
				receptive.setBehavior("dual-role");
				sexualTransmission(dual1, dual2);								
			}
		}		
	}


	public void sexualInteraction() {
		SimUtilities.shuffle(cohorts.get(COHORT.RECEPTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.INSERTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.DUALS));

		double dutc =  numDuals*DRCR*(1-SCP)*((numDuals*(1-SCP) / (numInsertives*CRRID + numDuals*(1-SCP))));
		double idtc =  numDuals*DRCR*(1-SCP)*((numInsertives*CRRID) / (numInsertives*CRRID + numDuals*(1-SCP)));

		Individual insertive=null, receptive=null, dual1=null, dual2=null;

		for (int i=0; i<dutc; i++) {
			do {
				receptive = (Individual) cohorts.get(COHORT.DUALS)
				.get(Uniform.staticNextIntFromTo(0, (int)numDuals-1));	
			} while (receptive.contacted);

			do {
				insertive = (Individual) cohorts.get(COHORT.DUALS)
				.get(Uniform.staticNextIntFromTo(0, (int)numDuals-1));
			} while ((receptive.getId() == insertive.getId()) || insertive.contacted);
			insertive.setBehavior("insertive");
			receptive.setBehavior("receptive");
			sexualTransmission(insertive, receptive);	
		}

		SimUtilities.shuffle(cohorts.get(COHORT.INSERTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.DUALS));

		for (int i=0; i<idtc; i++) {
			do {
				receptive = (Individual) cohorts.get(COHORT.DUALS)
				.get(Uniform.staticNextIntFromTo(0, (int)numDuals-1));
			} while (receptive.contacted);

			do {
				insertive = (Individual) cohorts.get(COHORT.INSERTIVES)
				.get(Uniform.staticNextIntFromTo(0, (int)numInsertives-1));
			} while (insertive.contacted);

			insertive.setBehavior("insertive");
			receptive.setBehavior("receptive");
			sexualTransmission(insertive, receptive);	
		}

		SimUtilities.shuffle(cohorts.get(COHORT.RECEPTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.INSERTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.DUALS));

		double irtc =  numReceptives*RRCR*((numInsertives*CRRID) / (numInsertives*CRRID + numDuals*(1-SCP)));
		double drtc = numReceptives*RRCR*((numDuals*(1-SCP)) / (numInsertives*CRRID + numDuals*(1-SCP)));

		SimUtilities.shuffle(cohorts.get(COHORT.RECEPTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.INSERTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.DUALS));

		for (int i=0; i<irtc; i++) {
			do {
				receptive = (Individual) cohorts.get(COHORT.RECEPTIVES)
				.get(Uniform.staticNextIntFromTo(0, (int)numReceptives-1));
			} while (receptive.contacted);
			do {
				insertive = (Individual) cohorts.get(COHORT.INSERTIVES)
				.get(Uniform.staticNextIntFromTo(0, (int)numInsertives-1));
			} while (insertive.contacted);
			insertive.setBehavior("insertive");
			receptive.setBehavior("receptive");
			sexualTransmission(insertive, receptive);
		}

		SimUtilities.shuffle(cohorts.get(COHORT.RECEPTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.INSERTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.DUALS));

		for (int i=0; i<drtc; i++) {
			do {
				receptive = (Individual) cohorts.get(COHORT.RECEPTIVES)
				.get(Uniform.staticNextIntFromTo(0, (int)numReceptives-1));
			} while (receptive.contacted);
			do {insertive = (Individual) cohorts.get(COHORT.DUALS)
				.get(Uniform.staticNextIntFromTo(0, (int)numDuals-1));
			} while (insertive.contacted);
			insertive.setBehavior("insertive");
			receptive.setBehavior("receptive");
			sexualTransmission(insertive, receptive);
		}

		double numSCP = numDuals*DRCR*SCP;

		SimUtilities.shuffle(cohorts.get(COHORT.DUALS));
		for (int i=0; i<(numSCP); i++) {
			do {dual1 = (Individual) cohorts.get(COHORT.DUALS)
				.get(Uniform.staticNextIntFromTo(0, (int)numDuals-1));
			} while (dual1.contacted);
			do {
				dual2 = (Individual) cohorts.get(COHORT.DUALS)
				.get(Uniform.staticNextIntFromTo(0, (int)numDuals-1));
			} while ((receptive.getId() == insertive.getId()) || dual2.contacted);		
			insertive.setBehavior("dual-role");
			receptive.setBehavior("dual-role");
			sexualTransmission(dual1, dual2);								
		}
	}

	public void sexualInteractionABC() {
		double totalActs = 0, dualacts = 0;
		SimUtilities.shuffle(cohorts.get(COHORT.RECEPTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.INSERTIVES));
		SimUtilities.shuffle(cohorts.get(COHORT.DUALS));

		ArrayList<Individual> receptiveOnlySex = new ArrayList<Individual>(),
		dualUnidirectionalSex = new ArrayList<Individual>(),
		dualBidirectionalSex = new ArrayList<Individual>();

		for (Individual receptive : cohorts.get(COHORT.RECEPTIVES)) {
			if (Math.random() <= probRRCR) {
				receptiveOnlySex.add(receptive);
			}
		}
		SimUtilities.shuffle(receptiveOnlySex);		
		/// hardcoded: with the SCP = 0.5, the probability is 0.5
		// need to check as
		//   SCP = 1		{Symmetric Contact Proportion: The fraction of D's receptive contacts that are symmetric (both insertive and receptive)}
		for (Individual dual : cohorts.get(COHORT.DUALS)) {
			if (Math.random() <= probDRCR) {
				if (Math.random() <= 0.5) {
					dualUnidirectionalSex.add(dual);
				}
				else {
					dualBidirectionalSex.add(dual);
				}
			}
		}

		double p1 = numInsertives*CRRID/(numDuals*(1-SCP)+numInsertives*CRRID);

		for (Individual receptive : receptiveOnlySex) {
			Individual insertive = null;
			if (Math.random()<=p1) {
				insertive = (Individual) cohorts.get(COHORT.INSERTIVES)
				.get(Uniform.staticNextIntFromTo(0, (int)numInsertives-1));
			}
			else {
				insertive = (Individual) dualUnidirectionalSex
				.get(Uniform.staticNextIntFromTo(0, (int)dualUnidirectionalSex.size()-1));				
			}
			insertive.setBehavior("insertive");
			receptive.setBehavior("receptive");
			sexualTransmission(insertive, receptive);
			totalActs++;
		}


		for (Individual receptive : dualUnidirectionalSex) {
			Individual insertive = null;
			if (Math.random()<=p1) {
				insertive = (Individual) cohorts.get(COHORT.INSERTIVES)
				.get(Uniform.staticNextIntFromTo(0, (int)numInsertives-1));
				insertive.setBehavior("insertive");
				receptive.setBehavior("receptive");
				sexualTransmission(insertive, receptive);
				totalActs++;
			}
			else {
				do {
					insertive = (Individual)dualUnidirectionalSex.get(Uniform.staticNextIntFromTo(0, (int) dualUnidirectionalSex.size()-1));
				} while (insertive.getId() == receptive.getId());
				insertive.setBehavior("insertive");
				receptive.setBehavior("receptive");
				sexualTransmission(insertive, receptive);
				totalActs++;
				dualacts++;
			}
		}

		for (Individual dual1 : dualBidirectionalSex) {
			Individual dual2;
			do {
				//				dual2 = (Individual)dualBidirectionalSex.get(Uniform.staticNextIntFromTo(0, (int) dualBidirectionalSex.size()-1));
				dual2 = (Individual) cohorts.get(COHORT.DUALS).get(Uniform.staticNextIntFromTo(0, (int) numDuals-1));
			} while (dual1.getId() == dual2.getId());
			dual1.setBehavior("dual-role");
			dual2.setBehavior("dual-role");
			sexualTransmission(dual1, dual2);
			totalActs++;
			dualacts++;
		}
	}



	public void updateAgents() {
		ArrayList<Individual> dead = new ArrayList<Individual>();
		for (Individual individual : agentList) {
			individual.step(currentTick);
			individual.contacted = false;
			if (individual.isDeath()) {
				dead.add(individual);
			}					
		}
		reapAndReplaceIndividuals(dead);
	}

	public void ud() {
		numHIV=0; numPHI=0; numPostPHI=0; numNewHIV = 0;
		numHIVNonSex = 0; numHIVInsertives = 0; numHIVReceptives = 0; numHIVDuals = 0;
		for (Individual individual : agentList) {
			if (individual.getInfectionStatus() != "susceptible") {
				numHIV++;
				if (individual.getInfectorStatus() == "PHI") {
					numPHI++;					
				}
				else if (individual.getInfectorStatus() == "Post-PHI") {
					numPostPHI++;
				}
				String category = individual.getCategory();
				if (category == "no-sex") {
					numHIVNonSex++;
				}
				else if (category == "insertive-only") {
					numHIVInsertives++;
				}
				else if (category == "receptive-only") {
					numHIVReceptives++;
				}
				else if (category == "dual-role") {
					numHIVDuals++;
				}
			}
		}
	}

	public void reapAndReplaceIndividuals(ArrayList<Individual> dead) {
		for (Individual individual : dead) {
			String category = individual.getCategory();
			if (category == "no-sex") {
				cohorts.get(COHORT.NO_SEX).remove(individual);
			}
			else if (category == "insertive-only") {
				cohorts.get(COHORT.INSERTIVES).remove(individual);
			}
			else if (category == "receptive-only") {
				cohorts.get(COHORT.RECEPTIVES).remove(individual);
			}
			else if (category == "dual-role") {
				cohorts.get(COHORT.DUALS).remove(individual);
			}			
			agentList.remove(individual);
		}

		Individual individual; 
		for (int i=0; i<numInitialAgents; i++) {
			if (Math.random() <= (6.94E-5)) {
//			if (Math.random() <= 1.11E-4) {
				lastID++;
				double rand = Math.random();							
				if (rand <= eN) {
					individual = new Individual(lastID, "no-sex", this);
					cohorts.get(COHORT.NO_SEX).add(individual);				
				}
				else if (rand > eN && rand <= eN+eI) {
					individual = new Individual(lastID, "insertive-only", this);
					cohorts.get(COHORT.INSERTIVES).add(individual);
				}
				else if (rand > eN+eI && rand <= eN+eI+eR) {
					individual = new Individual(lastID, "receptive-only", this);
					cohorts.get(COHORT.RECEPTIVES).add(individual);				
				}
				else {
					individual = new Individual(lastID, "dual-role", this);
					cohorts.get(COHORT.DUALS).add(individual);	
				}
				agentList.add(individual);
			}
		}
	}

}