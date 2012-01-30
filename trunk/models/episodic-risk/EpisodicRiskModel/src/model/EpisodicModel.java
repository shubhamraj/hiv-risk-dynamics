package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;

import cern.jet.random.Uniform;

/**
 * 
 * @author shah
 *
 */
public class EpisodicModel extends Model {
	private EnumMap<RISK_STATE, ArrayList<Individual>> riskStates;
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

	public EpisodicModel(double[] params, int _run, String _prefix, OUTBREAK_TYPE _outbreakType) {
		super(_run, _prefix, _outbreakType);	
		this.durHighRisk = params[2];
		this.ratioCHL = params[3];
		this.ahiTransPotential = params[4];
		this.avgCRR = params[5];		
		this.fracHighRisk = params[6];
		this.fracHtoH = params[7];
	}

	public void setup() {
		lowCR = avgCRR / (1-fracHighRisk+(ratioCHL*fracHighRisk));
		highCR = ratioCHL * lowCR;
		durLowRisk = durHighRisk*(1-fracHighRisk)/fracHighRisk;
		if (riskStates != null) {
			riskStates = null;
		}
				
		riskStates = new EnumMap<RISK_STATE, ArrayList<Individual>>(RISK_STATE.class);
		countTot = 0; countHigh = 0; countLow = 0; countHL = 0; inlow=0; inhi=0; inlh=0; ;
		
		initializeProbabilities();

		for (RISK_STATE riskState : RISK_STATE.values()) {
			riskStates.put(riskState, new ArrayList<Individual>());
		}
		for (int i=0; i<initialPopulation; i++) {
			createIndividual();
		}
		initializeInfection();		
	}

	public void run() {
		for (int iter=1; iter<=maxIterations; iter++) {
			currentTick = iter;
			refreshVariables();
			
			/* Cluster Recorder */
			clusterRecorder.step();
			
			updateIndividuals();
			mixing();
			riskTransitions();

			if (iter >= maxIterations-10) {
				ud();
			}
		}		
	}

	private void mixing() {
		int numHighRisk = riskStates.get(RISK_STATE.HIGH).size();
		int numLowRisk = riskStates.get(RISK_STATE.LOW).size();

		ArrayList<Individual> hToh = new ArrayList<Individual>();
		ArrayList<Individual> hTol = new ArrayList<Individual>();
		ArrayList<Individual> lowRisks = new ArrayList<Individual>();
		double p1 = fracHtoH;

		for (Individual individual : riskStates.get(RISK_STATE.HIGH)) {
			if (Uniform.staticNextDouble() <= probHH) {
				if (Uniform.staticNextDouble() <= p1) {
					hToh.add(individual);
				}
				else {
					hTol.add(individual);
				}
			}
		}

		for (Individual individual : riskStates.get(RISK_STATE.LOW)) {
			if (Uniform.staticNextDouble() <= probLL) {
				lowRisks.add(individual);
			}
		}		
		Individual ind2;		
		double numerator = (numHighRisk*(1-fracHtoH)*highCR);
		double p2 = numerator/((numLowRisk*lowCR) + numerator);

		Individual low2;
		for (Individual low1 : lowRisks) {
			if (Uniform.staticNextDouble() <= (p2)) {
				low2 = (Individual) riskStates.get(RISK_STATE.HIGH).get(Uniform.staticNextIntFromTo(0, numHighRisk-1));
			}
			else {
				do {
					//low2 = (Individual) riskStates.get(RISK_STATE.LOW).get(Uniform.staticNextIntFromTo(0, numLowRisk-1));
					low2 = (Individual) lowRisks.get(Uniform.staticNextIntFromTo(0, lowRisks.size()-1));
				} while(low1.equals(low2));
			}
			countLow++;
			if (sexualTransmission(low1, low2, MIXING_SITE.COMMON) && currentTick >= 29990) {
				inlow++;
			}
			countTot++;
		}				

		if (hTol.size() > 1) {
			try {
				for (Individual ind1 : hTol) {
					if (Uniform.staticNextDouble() <= (p2)) {
						do {
							//ind2 = (Individual) riskStates.get(RISK_STATE.HIGH).get(Uniform.staticNextIntFromTo(0, numHighRisk-1));
							ind2 = (Individual) hTol.get(Uniform.staticNextIntFromTo(0, hTol.size()-1));
						} while (ind1.equals(ind2));
					}
					else {
						//ind2 = (Individual) riskStates.get(RISK_STATE.LOW).get(Uniform.staticNextIntFromTo(0, numLowRisk-1));
						ind2 = (Individual) lowRisks.get(Uniform.staticNextIntFromTo(0, lowRisks.size()-1));
					}
					countHL++;
					if (sexualTransmission(ind1, ind2, MIXING_SITE.COMMON) && currentTick >= 29990) {		
						inlh++;
					};
					countTot++;
				}							
			} catch (Exception e) {}
		}

		for (Individual ind1 : hToh) {
			do {
				ind2 = (Individual) riskStates.get(RISK_STATE.HIGH).get(Uniform.staticNextIntFromTo(0, numHighRisk-1));
				//ind2 = (Individual) hToh.get(Uniform.staticNextIntFromTo(0, hToh.size()-1));
			} while (ind1.equals(ind2));
			countHigh++;
			if(sexualTransmission(ind1, ind2, MIXING_SITE.HIGH_RISK) && currentTick >= 29990) {
				inhi++;
			}
			countTot++;
		}
	}

	private boolean sexualTransmission(Individual agent1, Individual agent2, MIXING_SITE mixingSite) {
		if (agent1.equals(agent2)) {
			System.err.println("Error. ST. Agent 1 and 2 are same.");
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

	private void infect(Individual infector, Individual susceptible, MIXING_SITE mixingSite) {
		double prob = 0;
		boolean infectorAHI = infector.isAHI();
		if (infectorAHI == true) {
			prob = beta1;
		}
		else {
			prob = beta2;
		}
		if (Math.random() <= prob) {			
			ACT_TYPE actType = ACT_TYPE.NONE;			
			if (infectorAHI == true) {
				actType = ACT_TYPE.AHI;
			}
			else {
				actType = ACT_TYPE.CHI;
			}
			susceptible.setStageOfInfection(STAGE.ACUTE);
			susceptible.setInfectedTick(currentTick);
			susceptible.setInfectorID(infector.getID());
			susceptible.setInfectorStatus(infector.getStageOfInfection());						
			susceptible.setActType(actType);
			susceptible.setInfectedMixingSite(mixingSite);
			susceptible.setInfectedRiskState(susceptible.getRiskState());
			infector.setInfectionTimes(currentTick);

			/** Cluster Recorder*/
			clusterRecorder.recordTransmission(infector, susceptible);
		}
	}

	private void riskTransitions() {
		for (RISK_STATE riskState : RISK_STATE.values()) {
			riskStates.get(riskState).clear();
		}
		RISK_STATE curState;
		RISK_STATE newState;
		double rand;
		for (Individual individual : individuals) {
			curState = individual.getRiskState();
			newState = curState;
			rand = Uniform.staticNextDouble();
			if (curState.equals(RISK_STATE.HIGH)) {				
				if (rand <= probHighToLow) {
					newState = RISK_STATE.LOW;
				}
			}
			else if (curState.equals(RISK_STATE.LOW)) {
				if (rand <= probLowToHigh) {
					newState = RISK_STATE.HIGH;
				}
			}
			else {
				System.err.println("Error in risk states.");				
			}
			individual.setRiskState(newState);
			riskStates.get(newState).add(individual);
		}
	}

	private void updateIndividuals() {
		ArrayList<Individual> deads = new ArrayList<Individual>();
		for (Individual individual : individuals) {
			individual.step(currentTick);
			if (individual.isDead()) {
				deads.add(individual);
			}
			clusterRecorder.updateIndividualClusterRecord(individual);			
		}
		
		for (Individual individual : deads) {
			individual.setExitTick(currentTick);
			riskStates.get(individual.getRiskState()).remove(individual);
			individuals.remove(individual);			
		}
		for (int i=0; i<initialPopulation; i++) {
			if (Math.random() <= ((double)1/(durationLife))) {
				createIndividual();
			}
		}
	}

	private void initializeProbabilities() {
		double df1 = 1/((1/durationAHI)+(1/durationLife));
		double df2 = 1/((1/durationCHI)+(1/durationLife));
		beta2 = (1-ahiTransPotential)*(df1+df2)*baseTransProb / df2;
		beta1 = ahiTransPotential*df2*beta2/((1-ahiTransPotential)*df1);
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

	private void createIndividual() {
		Individual individual = new Individual();
		individual.setEntryTick(currentTick);
		double rand = Uniform.staticNextDouble();
		RISK_STATE riskState = RISK_STATE.LOW;
		if (rand <= fracHighRisk) {
			riskState = RISK_STATE.HIGH;
		}
		individual.setRiskState(riskState);
		riskStates.get(riskState).add(individual);
		individuals.add(individual);				
	}

	private void initializeInfection() {
		int numInitInfection = (int) (individuals.size() * initialInfection);
		Collections.shuffle(individuals);
		for (int i=0; i<numInitInfection; i++) {
			Individual individual = individuals.get(i);
			individual.setStageOfInfection(STAGE.ACUTE);
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
		for (Individual individual : individuals) {			
			if (individual.isInfected()) {
				hiv++;
				if (individual.getRiskState().equals(RISK_STATE.HIGH)) {
					highHIV++;
				}
				else {
					lowHIV++;
				}
				if (individual.getInfectorStatus().equals(STAGE.ACUTE)) {
					phi++;
				}
			}
			popSize++;
		}

		this.numHIV = (int)hiv;

		double popHigh = riskStates.get(RISK_STATE.HIGH).size();
		double popLow = riskStates.get(RISK_STATE.LOW).size();
		//		print("population: " + count + " hiv: " + hiv + " prev: " + (hiv/count) + " highprev: " + highHIV/countHigh + " lowprev: " + lowHIV/countLow
		//				+ " fracphi: " + phi/hiv + " inhi: " + inhi + " inlow: " + inlow +   " inhl: " + inlh + " ratio: " + ((double) inhi/inlow));
		//print(run + "," + popSize + "," + hiv + "," + (hiv/popSize) + "," + highHIV/popHigh + "," + lowHIV/popLow + "," + phi/hiv);
		output.add(new String(run + "," + popSize + "," + hiv + "," + (hiv/popSize) + "," + highHIV/popHigh + "," + lowHIV/popLow + "," + phi/hiv));
	}
}