package episodicriskmodel;

import interfaces.AgentInteface;
import interfaces.BaseModelInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;


import cluster.ClusterRecorder;

import cern.jet.random.Uniform;

/**
 * 
 * @author shah
 *
 */
public class EpisodicModel implements BaseModelInterface {
	/** AHI transmission potential */
	double ahiTransPotential;
	double betaAHI; 
	double betaCHI;
	int currentTick;
	
	ArrayList<String> output;
	OutbreakType outbreakType;	
	/** Cluster recorder array for one or more recording outbreaks, e.g. early period and transient*/
	ClusterRecorder clusterRecorder;
	
	int run;
	String prefix;
	int numHIV;
	int maxIterations;
	private ArrayList<Person> individuals;
	
	private EnumMap<RISK_STATE, ArrayList<Person>> riskStates;
	private double probHighToLow;
	private double probLowToHigh;
	private double probHH;
	private double probLL;
	/** Duration of stay in high risk phase */
	double durHighRisk;
	/** Duration of stay in the low risk phase */
	double durLowRisk; 
	/** Contact rate for low risk state */
	private double lowCR;
	/** Contact rate for high risk state */
	private double highCR;
	/** Fraction of high risk phase contacts made at the high risk site */
	private double fracHtoH;
	/** Average contact rate */
	private double avgCRR;
	/** Ratio of contacts from high to low risk states */
	private double ratioCHL;
	/** Fraction of high risk population */
	private double fracHighRisk;

	private int countTot, countHigh, countLow, countHL;
	private int inlow, inhi, inlh;
	
	public EpisodicModel(double[] params, int _run, String _prefix, OutbreakType _outbreakType) {	
		this.durHighRisk = params[2];
		this.ratioCHL = params[3];
		this.ahiTransPotential = params[4];
		this.avgCRR = params[5];		
		this.fracHighRisk = params[6];
		this.fracHtoH = params[7];
		this.run = _run;
		this.prefix = _prefix;
		this.outbreakType = _outbreakType;
		this.currentTick = -1;
	}

	public void setup() {
		lowCR = avgCRR / (1-fracHighRisk+(ratioCHL*fracHighRisk));
		highCR = ratioCHL * lowCR;
		durLowRisk = durHighRisk*(1-fracHighRisk)/fracHighRisk;
		if (individuals != null) {
			individuals = null;
		}
		individuals = new ArrayList<Person>();
		
		this.maxIterations = MaxIterations;
		
		if (output != null) {
			output.clear();
			output = null;
		}
		output = new ArrayList<String>();
		
		/* Cluster Recorder */
		createClusterRecorder(); 
		
		if (riskStates != null) {
			riskStates = null;
		}		
		riskStates = new EnumMap<RISK_STATE, ArrayList<Person>>(RISK_STATE.class);
		
		countTot = 0; countHigh = 0; countLow = 0; countHL = 0; inlow=0; inhi=0; inlh=0; ;
		
		initializeProbabilities();

		for (RISK_STATE riskState : RISK_STATE.values()) {
			riskStates.put(riskState, new ArrayList<Person>());
		}
		for (int i=0; i<InitialPopulation; i++) {
			createIndividual();
		}
		initializeInfection();		
	}

	public void run() {
		for (int iter=1; iter<=maxIterations; iter++) {
			currentTick = iter;
			refreshVariables();
			
			/* Cluster Recorder step */
			callClusterRecorderStep();
			
			updateIndividuals();
			mixing();
			riskTransitions();

			if (iter >= maxIterations-100) {
				ud();
			}
		}		
	}

	private void mixing() {
		int numHighRisk = riskStates.get(RISK_STATE.High).size();
		int numLowRisk = riskStates.get(RISK_STATE.Low).size();

		ArrayList<Person> hToh = new ArrayList<Person>();
		ArrayList<Person> hTol = new ArrayList<Person>();
		ArrayList<Person> lowRisks = new ArrayList<Person>();
		double p1 = fracHtoH;

		for (Person individual : riskStates.get(RISK_STATE.High)) {
			if (Uniform.staticNextDouble() <= probHH) {
				if (Uniform.staticNextDouble() <= p1) {
					hToh.add(individual);
				}
				else {
					hTol.add(individual);
				}
			}
		}

		for(Person individual : riskStates.get(RISK_STATE.Low)) {
			if (Uniform.staticNextDouble() <= probLL) {
				lowRisks.add(individual);
			}
		}		
		Person ind2;		
		double numerator = (numHighRisk*(1-fracHtoH)*highCR);
		double p2 = numerator/((numLowRisk*lowCR) + numerator);

		Person low2;
		for (Person low1 : lowRisks) {
			if (Uniform.staticNextDouble() <= (p2)) {
				low2 = (Person) riskStates.get(RISK_STATE.High).get(Uniform.staticNextIntFromTo(0, numHighRisk-1));
			}
			else {
				do {
					//low2 = (Individual) riskStates.get(RISK_STATE.LOW).get(Uniform.staticNextIntFromTo(0, numLowRisk-1));
					low2 = (Person) lowRisks.get(Uniform.staticNextIntFromTo(0, lowRisks.size()-1));
				} while(low1.equals(low2));
			}
			countLow++;
			if (sexualTransmission(low1, low2, MIXING_SITE.Common) && currentTick >= 29990) {
				inlow++;
			}
			countTot++;
		}				

		if (hTol.size() > 1) {
			try {
				for (Person ind1 : hTol) {
					if (Uniform.staticNextDouble() <= (p2)) {
						do {
							//ind2 = (Individual) riskStates.get(RISK_STATE.HIGH).get(Uniform.staticNextIntFromTo(0, numHighRisk-1));
							ind2 = (Person) hTol.get(Uniform.staticNextIntFromTo(0, hTol.size()-1));
						} while (ind1.equals(ind2));
					}
					else {
						//ind2 = (Individual) riskStates.get(RISK_STATE.LOW).get(Uniform.staticNextIntFromTo(0, numLowRisk-1));
						ind2 = (Person) lowRisks.get(Uniform.staticNextIntFromTo(0, lowRisks.size()-1));
					}
					countHL++;
					if (sexualTransmission(ind1, ind2, MIXING_SITE.Common) && currentTick >= 29990) {		
						inlh++;
					};
					countTot++;
				}							
			} catch (Exception e) {}
		}

		for (Person ind1 : hToh) {
			do {
				ind2 = (Person) riskStates.get(RISK_STATE.High).get(Uniform.staticNextIntFromTo(0, numHighRisk-1));
				//ind2 = (Individual) hToh.get(Uniform.staticNextIntFromTo(0, hToh.size()-1));
			} while (ind1.equals(ind2));
			countHigh++;
			if(sexualTransmission(ind1, ind2, MIXING_SITE.HighRisk) && currentTick >= 29990) {
				inhi++;
			}
			countTot++;
		}
	}

	private boolean sexualTransmission(Person agent1, Person agent2, MIXING_SITE mixingSite) {
		if (agent1.equals(agent2)) {
			System.err.println("Error. ST. EpisodicAgent 1 and 2 are same.");
			System.exit(1);
		}
		if ((agent1.isSusceptible() && agent2.isSusceptible())
				|| (agent1.isInfected() && agent2.isInfected())) {
			return false;			
		}
		if (agent1.getInfectedTick() == currentTick
				|| agent2.getInfectedTick() == currentTick) {
			return false;
		}
		//either of the two partners is exclusively infected 
		if (agent1.isInfected()) {
			infect(agent1, agent2, mixingSite);
		}
		else if (agent2.isInfected()) {
			infect(agent2, agent1, mixingSite);
		}
		return true;		
	}

	void infect(Person infector, Person susceptible, MIXING_SITE mixingSite) {
		double prob = 0;
		boolean infectorAHI = infector.isAHI();
		if (infectorAHI == true) {
			prob = betaAHI;
		}
		else {
			prob = betaCHI;
		}
		if (Math.random() <= prob) {			
			ActType actType = ActType.None;			
			if (infectorAHI == true) {
				actType = ActType.Acute_Susceptible;
			}
			else {
				actType = ActType.Chronic_Susceptible;
			}
			susceptible.setStageOfInfection(InfectionStage.Acute);
			susceptible.setInfectedTick(currentTick);
			susceptible.setInfectorID(infector.getID());
			susceptible.setInfectorStageOfInfection(infector.getStageOfInfection());						
			susceptible.setActType(actType);
			susceptible.setInfectedMixingSite(mixingSite);
			susceptible.setInfectedRiskState(susceptible.getRiskState());
			infector.setInfectionTimes(currentTick);

			/** Add transmission to the cluser recorder*/
			addTransmissionToClusterRecord(infector, susceptible);
		}
	}

	private void riskTransitions() {
		for (RISK_STATE riskState : RISK_STATE.values()) {
			riskStates.get(riskState).clear();
		}
		RISK_STATE curState;
		RISK_STATE newState;
		double rand;
		for (Person individual : individuals) {
			curState = individual.getRiskState();
			newState = curState;
			rand = Uniform.staticNextDouble();
			if (curState.equals(RISK_STATE.High)) {				
				if (rand <= probHighToLow) {
					newState = RISK_STATE.Low;
				}
			}
			else if (curState.equals(RISK_STATE.Low)) {
				if (rand <= probLowToHigh) {
					newState = RISK_STATE.High;
				}
			}
			else {
				System.err.println("Error in risk states.");				
			}
			individual.setRiskState(newState);
			riskStates.get(newState).add(individual);
		}
	}

	public void updateIndividuals() {
		ArrayList<Person> deads = new ArrayList<Person>();
		for (Person individual : individuals) {
			individual.step(currentTick);
			if (individual.isDead()) {
				deads.add(individual);
			}
			clusterRecorder.updateEpisodicEpisodicAgentClusterRecord(individual);			
		}
		
		for (Person individual : deads) {
			individual.setExitTick(currentTick);
			riskStates.get(individual.getRiskState()).remove(individual);
			individuals.remove(individual);			
		}
		for (int i=0; i<InitialPopulation; i++) {
			if (Math.random() <= ((double)1/(DurationLife))) {
				createIndividual();
			}
		}
	}

	private void initializeProbabilities() {
		double df1 = 1/((1/DurationAHI)+(1/DurationLife));
		double df2 = 1/((1/DurationCHI)+(1/DurationLife));
		betaCHI = (1-ahiTransPotential)*(df1+df2)*BaseTransProb / df2;
		betaAHI = ahiTransPotential*df2*betaCHI/((1-ahiTransPotential)*df1);
		//		print("beta1: " + beta1);
		//		print("beta2: " + beta2);
		//		print("CH: " + highCR);
		//		print("CL: " + lowCR);
		//		probHighToLow = Parameters.returnProbability(durHighRisk);
		//		probLowToHigh = Parameters.returnProbability(durLowRisk);
		probHighToLow = 1/(durHighRisk*30);		
		probLowToHigh = 1/(durLowRisk*30);

		//		System.out.println("ProbHtoL: " + probHighToLow);
		//		System.out.println("ProbLtoH: " + probLowToHigh);
		//		System.out.println("Ratio: " + probHighToLow/probLowToHigh);

		//		probHH = Parameters.returnProbability(highCR);
		//		probLL = Parameters.returnProbability(lowCR);

		probHH = highCR/60;
		probLL = lowCR/60;		
	}

	public void createIndividual() {
		Person individual = (Person) new Person();
		individual.setEntryTick(currentTick);
		double rand = Uniform.staticNextDouble();
		RISK_STATE riskState = RISK_STATE.Low;
		if (rand <= fracHighRisk) {
			riskState = RISK_STATE.High;
		}
		individual.setRiskState(riskState);
		riskStates.get(riskState).add(individual);
		individuals.add(individual);				
	}

	private void initializeInfection() {
		int numInitInfection = (int) (individuals.size() * InitialInfection);
		Collections.shuffle(individuals);
		for (int i=0; i<numInitInfection; i++) {
			Person individual = individuals.get(i);
			individual.setStageOfInfection(InfectionStage.Acute);
			individual.setInfectedTick(0);
		}
	}

	private void refreshVariables() {
		countTot=0;countHigh=0;countLow=0;countHL=0;
		inlow=0; inhi=0; inlh=0;
	}
	
	private void ud() {
		double popSize = 0;
		double hiv = 0;
		double highHIV = 0;
		double lowHIV = 0;
		double phi = 0;
		for (Person individual : individuals) {			
			if (individual.isInfected()) {
				hiv++;
				if (individual.getRiskState().equals(RISK_STATE.High)) {
					highHIV++;
				}
				else {
					lowHIV++;
				}
				if (individual.getInfectorInfectionStage().equals(InfectionStage.Acute)) {
					phi++;
				}
			}
			popSize++;
		}

		this.numHIV = (int)hiv;

		double popHigh = riskStates.get(RISK_STATE.High).size();
		double popLow = riskStates.get(RISK_STATE.Low).size();
		//		print("population: " + count + " hiv: " + hiv + " prev: " + (hiv/count) + " highprev: " + highHIV/countHigh + " lowprev: " + lowHIV/countLow
		//				+ " fracphi: " + phi/hiv + " inhi: " + inhi + " inlow: " + inlow +   " inhl: " + inlh + " ratio: " + ((double) inhi/inlow));
		//print(run + "," + popSize + "," + hiv + "," + (hiv/popSize) + "," + highHIV/popHigh + "," + lowHIV/popLow + "," + phi/hiv);
		addToOutput(new String(run + "," + popSize + "," + hiv + "," + (hiv/popSize) + "," + highHIV/popHigh + "," + lowHIV/popLow + "," + phi/hiv));
	}
	
	public int returnPopulationSize() {
		return this.individuals.size();
	}

	public void resetIndividualsOutbreakRecord() {
		for (Person individual : this.individuals) {
			individual.resetOutbreakRecord();
		}		
	}

	public ArrayList<Person> getIndividuals() {
		return individuals;
	}

	public void setIndividuals(ArrayList<Person> individuals) {
		this.individuals = individuals;
	}

	public void printStr(String str) {
		System.out.println(str);
	}

	@Override
	public ArrayList<String> getOutput() {
		return this.output;
	}

	@Override
	public ClusterRecorder getClusterRecorder() {
		return this.clusterRecorder;
	}

	@Override
	public int getNumHIV() {
		return this.numHIV;
	}

	@Override
	public double getAhiTransPotential() {
		return this.ahiTransPotential;
	}

	@Override
	public int getCurrentTick() {
		return this.currentTick;
	}

	@Override
	public void addTransmissionToClusterRecord(AgentInteface infector, AgentInteface susceptible) {
		clusterRecorder.recordTransmission(infector, susceptible);
	}

	@Override
	public void createClusterRecorder() {
		if (clusterRecorder != null) {
			clusterRecorder = null;
		}		
		clusterRecorder = new ClusterRecorder(this, this.prefix, this.outbreakType);		
	}

	@Override
	public int getMaximumIterations() {
		return this.maxIterations;
	}

	@Override
	public void callClusterRecorderStep() {
		this.clusterRecorder.step();
	}

	@Override
	public void addToOutput(String strOutput) {
		this.output.add(strOutput);
	}
}