package partnershipmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import cern.jet.random.Uniform;

import cluster.BaseTransmission;
import cluster.ClusterRecorder;
import cluster.Edge;
import interfaces.AgentInterface;
import interfaces.BaseModelInterface;

/**
 * 
 * Jong-Hoon Kim's model integrated with outbreaks library (c.f. Kim et al. 2010)
 * @author Jong-Hoon Kim (original)
 * @author Modified by: Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 *
 */

public class PartnershipModel implements BaseModelInterface, PartnershipParametersInterface {
	private double ahiTransPotential = AcuteTransPotential;
	private int maxIterations;
	private PartnershipScheme partnershipScheme = PartnershipScheme.Random;
	private SexFrequencyScheme freqSexScheme = SexFrequencyScheme.FixedPerPartnership;	
	/** 'theta' in the paper */
	private double partnershipCoefficient = Partnership.PartnershipCoefficient; 	
	private double freqSex = Partnership.FrequencySexPerDay;
	private double avgNbor = Partnership.AverageNeighbor;
	/** partnershipProb is reset by durPartnership*avgNbor */
	private double partnershipProb = Partnership.AverageNeighbor * (1/Partnership.DurationPerDay + 2/DurationLife); ;
	/** Dissolution probability*/ 
	private double dissolutioinProbability = 1/Partnership.DurationPerDay;
	/** maximum number of partnerships that an individual can have */
	private int maxDegree = Partnership.MaxDegree;
	/** Observed proportion of individuals without partners */
	private double propSingle = Partnership.PropSingle;
	private double[] propSingleArray = new double [5000];

	private ArrayList<PartnershipAgent> individuals = new ArrayList<PartnershipAgent>();
	private ArrayList<Edge> edgelist = new ArrayList<Edge>();

	private int currentTick = 0;
	/** Cluster recorder*/
	private ClusterRecorder clusterRecorder;

	private OutbreakType outbreakType = OutbreakType.AHI;
	private String prefix = "";
	private int run;
	private double prevalence = 0;
	private double frPHI = 0;
	private int numHIV = 0;
	private double beta1 = 0;
	private double beta2 = 0;
	private ArrayList<String> output = new ArrayList<String>();

	public PartnershipModel(double[] inputParams, int _run, String _prefix, OutbreakType _outbreakType) {		
		this.prefix = _prefix;		
		this.run = _run;
		this.outbreakType = _outbreakType;
		
		if (inputParams != null) {
			this.ahiTransPotential = inputParams[0];
			this.freqSex = inputParams[1];
			this.dissolutioinProbability = inputParams[2];
			this.partnershipCoefficient = inputParams[3];
			this.avgNbor = inputParams[4];
			this.partnershipProb = this.avgNbor * (dissolutioinProbability + 2/DurationLife); ;
		}
	}

	/** Set defaults after a run start or restart */
	public void setup() {		
		this.maxIterations = MaxIterations;
		double df1 = 1/((1/DurationAHI)+(1/DurationLife));
		double df2 = 1/((1/DurationCHI)+(1/DurationLife));
		beta2 = (1-ahiTransPotential)*(df1+df2)*BaseTransProb/df2;
		beta1 = ahiTransPotential*df2*beta2/((1-ahiTransPotential)*df1);
		/*System.out.println("prob: " + partnershipProb);*/
		if (debug) {
			System.out.println("beta1: " + beta1 + " and beta2: " + beta2);
		}
		//System.gc();
		PartnershipAgent.lastID = 0; 
		/* Create cluster recorder*/
		createClusterRecorder(this.prefix, this.outbreakType, BaseTransmission.class);		
	}

	public void buildModel() {
		createAgents(InitialPopulation);
		stepReport();
	}


	private void updateIndividuals() {
		ArrayList<PartnershipAgent> deads = new ArrayList<PartnershipAgent>();
		for (PartnershipAgent individual : individuals) {
			individual.step((int) currentTick);
			if (individual.isDead()) {
				deads.add(individual);
			}			
			/* Cluster Recorder update Episodic Agent record of outbreak clusters */
			updateClusterRecord(individual);
		}		
		for (PartnershipAgent individual : deads) {
			individual.setExitTick((int)currentTick);
			individual.removeEdgesFromList(edgelist);     
			individual.dissolveEdges();			
			individual.setDead(true);
			individuals.remove(individual);
		}		
//		deads = null;		
		for (int i=0; i<InitialPopulation; i++) {
			double flowProb = 1/DurationLife;
			if (flowProb > getUniformDoubleFromTo(0,1))
				createAgents(1);
		}
	}

	public void run() {
		for (int i=0; i<this.maxIterations; i++) {
			step(i);
		}
	}

	public void step(int iter) {
		currentTick = iter;
		Collections.shuffle(individuals);
		if (currentTick < InfectionStart) {
			updateIndividuals();			
			partnerFormation(); 			
			stepReport();
			/*			if (freqSexScheme.equals(SexFrequencyScheme.FixedByPopulationBySingles)){ // fixed sex frequency per population by adjusting the fraction of singles
				if( currentTick >= calPropSingleStart && currentTick <= calPropSingleEnd){
					int[] degDist = calDegreeDist(individuals);
					propSingleArray[(int)(currentTick-calPropSingleStart) ] = 
						(double) degDist[0] / individuals.size();
				}
			}*/
			partnerSeparation();

		} 
		else if (currentTick == InfectionStart) {
			if (freqSexScheme.equals(SexFrequencyScheme.FixedByPopulationBySingles)){ // fixed sex frequency per population by adjusting the fraction of singles
				double sum = 0;
				int length = propSingleArray.length;
				for( int i = 0; i < length; i++ ){
					sum += propSingleArray[i];
				}
				propSingle = (double) sum/length; 
			}
			initializeInfection();
		}	

		else {					
			if (RecordOutbreak) {
				callClusterRecorderStep();
			}
			updateIndividuals();			
			partnerFormation(); 
			infectPartners();	
			partnerSeparation();
			stepReport();
		}				
	}

	private void partnerFormation() { 
		switch (partnershipScheme) {
		case Monogamous: 
			monogamousPartnership(); 
			break;
		case Random:
			randomPartnership();
			break;
		case RandomFixedNumberPartnership:
			randomFixedNumberPartnership();
			break;
		case Dissassortative: 
			disassortativePartnership(); 
			break;
		case Assortative: 
			assortativePartnership(); 
			break;
		case MonogamousII: 
			monogamousPartnership2(); 
			break;
		default: break;
		}
	}

	/** monogamousPartnership form partnerships between individuals who don't have partners */
	public void monogamousPartnership() {
		int size = individuals.size();
		double numSinglesAtEq = size /(avgNbor+1);
		// rho, partnership formation rate; sigma, partnership dissolution rate
		// 1. singles*rho = 2*pairs*sigma 
		// 2. singles + 2*pairs = total pop
		for (int i=0; i<numSinglesAtEq/2; i++){	
			if( partnershipProb > getUniformDoubleFromTo(0,1) ) {
				PartnershipAgent fromNode = individuals.get(getUniformIntFromTo(0,size-1)); 
				PartnershipAgent toNode = individuals.get(getUniformIntFromTo(0,size-1));
				while( fromNode.getOutDegree() > 0 ) {
					fromNode = individuals.get( getUniformIntFromTo(0,size-1) );
				}
				while( fromNode == toNode || toNode.getOutDegree() > 0 ){
					toNode = individuals.get( getUniformIntFromTo(0,size-1) );
				}
				fromNode.makeContactToFrom(toNode, edgelist);	
			}
		}
	}

	/** monogamousPartnership2  maintain fixed number of partnerships */ 
	public void monogamousPartnership2() {
		int size = individuals.size();
		int expectedEdges = (int) avgNbor * (size/2);
		int numEdgesToBeMade = expectedEdges - edgelist.size();
		if( numEdgesToBeMade > 0 ){
			ArrayList<PartnershipAgent> singles = new ArrayList<PartnershipAgent>();
			// make a list of singles
			for( int i = 0; i < size; i++ ){
				PartnershipAgent node = individuals.get( i );
				if( node.getOutDegree() == 0 ){
					singles.add( node );
				}
			}
			// form partnerships between singles
			for( int i = 0; i < numEdgesToBeMade; i++ ){
				PartnershipAgent fromNode = singles.get( i );
				PartnershipAgent toNode = singles.get( singles.size() - 1 - i );
				fromNode.makeContactToFrom(toNode, edgelist);	
			}
		}
	}


	public void makePairs2() {		
		double numDrawn = 0;
		int numAgents = individuals.size();
		double expectedEdges = ( avgNbor * (numAgents / 2.0d) );
		do {
			PartnershipAgent agent1 = individuals.get(Uniform.staticNextIntFromTo(0, numAgents-1));
			PartnershipAgent agent2 = individuals.get(Uniform.staticNextIntFromTo(0, numAgents-1));			
			if (agent1.getID() != agent2.getID()
					&& !agent1.hasEdgeToOrFrom(agent2)) {
				if (Math.random() <= Partnership.PartnershipProb) {
					//if (sexualMixingSchemeHighConcurrency(agent1, agent2)) {						
					if (sexualMixingSchemeLowConcurrency(agent1, agent2)) {
						agent1.makeContactToFrom(agent2, edgelist);
					}					
				}	
				numDrawn++;
			}			
			//		} while (numDrawn <= (numAgents/2.0d));
		} while (numDrawn <= expectedEdges);

		//		System.out.println(" Average degree: "  + ((double)(pairsList.size()*2)/numAgents));
	}

	public boolean sexualMixingSchemeLowConcurrency(PartnershipAgent agent1, PartnershipAgent agent2) {
		if (agent1.getOutDegree() == 0
				&& agent2.getOutDegree() == 0) {
			return true;
		}
		else {
			return Math.random() < partnershipCoefficient ? true : false;
		}
	}

	/**  randomPartnership */
	public void randomPartnership() {
		int size = individuals.size();
		// iteration occurs half the number of agents 
		for (int i=0; i<size/2; i++) {
			if (partnershipProb > getUniformDoubleFromTo(0,1)) {  	
				PartnershipAgent fromIndividual = individuals.get(getUniformIntFromTo(0,size-1)); 
				PartnershipAgent toIndividual = individuals.get(getUniformIntFromTo(0,size-1));
				while (fromIndividual.hasEdgeTo(toIndividual) || fromIndividual == toIndividual) {
					toIndividual = individuals.get(getUniformIntFromTo(0,size-1));
				}	
				int fromDeg = fromIndividual.getOutDegree();
				int toDeg = toIndividual.getOutDegree();
				if (1-partnershipCoefficient > getUniformDoubleFromTo(0,1-1e-6) && (fromDeg + toDeg) > 0) {
					while (fromIndividual.hasEdgeTo(toIndividual) 
							|| fromIndividual.getID() == toIndividual.getID() 
							|| (toDeg + fromDeg) > 0) {
						fromIndividual = individuals.get(getUniformIntFromTo(0,size-1));
						toIndividual = individuals.get(getUniformIntFromTo(0,size-1));
						fromDeg = fromIndividual.getOutDegree();
						toDeg = toIndividual.getOutDegree();
					}
				}
				fromIndividual.makeContactToFrom(toIndividual, edgelist);			    		
			}
		}
	}

	/** randomFixedNumberPartnership - maintain the fixed number of partnerships
	 * When the infecteds die of AIDS the average number of partners slightly decreases because the dying individuals 
	 * are likely to have more partnerships than average and because death removes partnerships that have been connected to
	 * dying individuals.  Therefore, in order to maintain the fixed number of partnerships we end up increasing
	 * the partnership formation rate at a given partnership duration 
	 */ 
	public void randomFixedNumberPartnership() {
		int size = individuals.size();
		int expectedEdges = (int) ( avgNbor * (size / 2) );
		int numEdgesToBeMade = expectedEdges - edgelist.size();
		for( int i = 0; i < numEdgesToBeMade; i++ ){
			PartnershipAgent fromNode = individuals.get( getUniformIntFromTo(0,size-1) ); 
			PartnershipAgent toNode = individuals.get( getUniformIntFromTo(0,size-1) );
			while( fromNode.hasEdgeTo(toNode) || fromNode == toNode ){
				toNode = individuals.get( getUniformIntFromTo(0,size-1) );
			}	
			int fromDeg = fromNode.getOutDegree();
			int toDeg = toNode.getOutDegree();
			// pcoeff = 0 -> monogamy. 
			// pcoeff = 1 -> random partnership
			if ((1-partnershipCoefficient) > getUniformDoubleFromTo(0,1-1e-6) && (fromDeg + toDeg) > 0) {
				while (fromNode.hasEdgeTo(toNode) || fromNode == toNode || (toDeg + fromDeg) > 0) {
					fromNode = individuals.get( getUniformIntFromTo(0,size-1) );
					toNode = individuals.get( getUniformIntFromTo(0,size-1) );
					fromDeg = fromNode.getOutDegree();
					toDeg = toNode.getOutDegree();
				}
			}
			fromNode.makeContactToFrom( toNode, edgelist);			    		
		}
	}

	/** assortativePartnership form partnerships assortatively
	 * if the parameter xi is equal to zero, the resulting partnerships are random
	 * if it's bigger than 0, then individuals with higher degree have higher
	 * probability of forming partnerships.
	 */
	public void assortativePartnership() {
		int size = individuals.size();
		for( int i = 0; i < size/2; i++ ) {
			if( partnershipProb > getUniformDoubleFromTo(0,1) && currentTick > InfectionStart) {
				PartnershipAgent fromNode = individuals.get( getUniformIntFromTo(0,size-1) ); 
				PartnershipAgent toNode = individuals.get( getUniformIntFromTo(0,size-1) );
				int degFrom = fromNode.getInDegree();
				int degTo = toNode.getInDegree();
				double prob = 1 - partnershipCoefficient 
				+ partnershipCoefficient*degFrom*degTo/(maxDegree*maxDegree);
				while( fromNode.hasEdgeTo(toNode) || fromNode == toNode ||						 
						prob < getUniformDoubleFromTo(0,1) ){
					fromNode = individuals.get( getUniformIntFromTo(0,size-1) );
					toNode = individuals.get( getUniformIntFromTo(0,size-1) );
					degFrom = fromNode.getInDegree();
					degTo = toNode.getInDegree();
					prob = 1 - partnershipCoefficient
					+ partnershipCoefficient*degFrom*degTo/(maxDegree*maxDegree);
				}
				fromNode.makeContactToFrom(toNode, edgelist);
			}
		}
	}


	/** assortativePartnership form partnerships assortatively
	 * if the parameter xi is equal to zero, the resulting partnerships are random
	 * if it's bigger than 0, then individuals with higher degree have higher
	 * probability of forming partnerships.
	 */
	public void assortativePartnership2( ArrayList<PartnershipAgent> nodelist, 
			ArrayList<Edge> edgelist, double partnershipProb, double pcoeff ) {
		int size = nodelist.size();
		for( int i = 0; i < size/2; i++ ) {
			if( partnershipProb > getUniformDoubleFromTo(0,1) ) {
				PartnershipAgent fromNode = nodelist.get( getUniformIntFromTo(0,size-1) ); 
				PartnershipAgent toNode = nodelist.get( getUniformIntFromTo(0,size-1) );
				int degFrom = fromNode.getInDegree();
				int degTo = toNode.getInDegree();
				int diff = Math.abs( degFrom - degTo );
				while( fromNode.hasEdgeTo(toNode) || fromNode == toNode ||						 
						diff != 0 ){
					fromNode = nodelist.get( getUniformIntFromTo(0,size-1) );
					toNode = nodelist.get( getUniformIntFromTo(0,size-1) );
					degFrom = fromNode.getInDegree();
					degTo = toNode.getInDegree();
					diff = Math.abs( degFrom - degTo );
				}
				fromNode.makeContactToFrom(toNode, edgelist);	
			}
		}
	}

	/** disassortativePartnership form partnerships disassortatively
	 * if the parameter xi is equal to zero, the resulting partnerships are random
	 * if it's bigger than 0, partnership occur more frequently when 
	 * the difference of the number of partnerships between two individuals is big
	 */
	public void disassortativePartnership() {
		int size = individuals.size();
		for( int i = 0; i < size/2; i++ ) {
			if (partnershipProb > getUniformDoubleFromTo(0,1)) {
				PartnershipAgent fromIndividual = individuals.get(getUniformIntFromTo(0,size-1)); 
				PartnershipAgent toIndividual = individuals.get(getUniformIntFromTo(0,size-1));
				int degFrom = fromIndividual.getInDegree();
				int degTo = toIndividual.getInDegree();

				double pp = 1 - partnershipCoefficient + 
				partnershipCoefficient*Math.pow(((degFrom-degTo)/maxDegree),2);
				//double prob = getUniformDoubleFromTo(0,1);
				while (fromIndividual.hasEdgeTo(toIndividual) 
						|| fromIndividual == toIndividual 
						|| pp < getUniformDoubleFromTo(0,1)){
					fromIndividual = individuals.get(getUniformIntFromTo(0,size-1));
					toIndividual = individuals.get(getUniformIntFromTo(0,size-1));
					degFrom = fromIndividual.getInDegree();
					degTo = toIndividual.getInDegree();
					pp = 1 - partnershipCoefficient + 
					partnershipCoefficient*Math.pow(((degFrom-degTo)/maxDegree),2);
				}
				fromIndividual.makeContactToFrom(toIndividual, edgelist);	
			}
		}
	}

	/** partnerSepartion */
	public void partnerSeparation() {
		for (Iterator<Edge> iter = edgelist.iterator(); iter.hasNext();) {
			Edge edge = (Edge) iter.next();
			if (dissolutioinProbability >= getUniformDoubleFromTo(0,1)) {
				dissolveNodes(edge);
				iter.remove(); // removes "edge" from the list
			}
		}	
	}

	@SuppressWarnings("unchecked")
	public void infectPartners() {
		for (int i=0; i<individuals.size(); i++) {
			PartnershipAgent toIndividual = individuals.get(i);
			InfectionStage toInfectionStage = toIndividual.getStageOfInfection();
			if (toInfectionStage.equals(InfectionStage.Susceptible) && toIndividual.getInDegree() > 0 ) {	
				ArrayList<PartnershipAgent> fromList = toIndividual.getInNodes();
				int numNbors = fromList.size();
				for (int j=0; j<numNbors; j++) {
					PartnershipAgent fromIndividual = fromList.get(j);
					InfectionStage fromInfectionStage = fromIndividual.getStageOfInfection();
					double infectionProb = 0;
					switch (fromInfectionStage) {
					case Acute: 
						infectionProb = beta1;
						break;
					case Chronic: 
						infectionProb = beta2;
						break;
					case Susceptible: 
						infectionProb = 0;
						break;
					default: break; 
					}

					switch (freqSexScheme) {
					// fixed frequency per partnership
					case FixedPerPartnership: infectionProb = infectionProb*freqSex; break;  
					// fixed frequency per individual
					case FixedPerIndividual: infectionProb = infectionProb*freqSex/numNbors; break;
					// fixed frequency per population by adjusting the fraction of singles
					case FixedByPopulationBySingles: infectionProb = infectionProb*freqSex/(1-propSingle)/numNbors; break;
					default: System.err.println("Invalid Sex Frequency."); break;
					}
					if (infectionProb > getUniformDoubleFromTo(0, 1)) {
						infect(fromIndividual, toIndividual);
						break;
					}
				}
			}
		}
	}	

	private void infect(PartnershipAgent infector, PartnershipAgent susceptible) {			
		susceptible.setStageOfInfection(InfectionStage.Acute);
		susceptible.setInfectedTick((int)currentTick);
		susceptible.setInfectorID(infector.getID());
		susceptible.setInfectorInfectionStage(infector.getStageOfInfection());						
		susceptible.setActType(infector.isAHI() == true ? ActType.Acute_Susceptible : ActType.Chronic_Susceptible);

		infector.setInfectionTimes((int)currentTick);		
		infector.setNumInfectee(infector.getNumInfectee() + 1);

		/* Add transmission to the cluster recorder*/
		addTransmissionToClusterRecord(infector, susceptible);

		if (currentTick > InfectionStart) {
			if (debug) {
				System.out.println("Time: " + currentTick + " Individual:" + infector.getID() + " infects " + susceptible.getID() + ". Infector in: " + infector.getStageOfInfection() + " in cluster: " + infector.getAHIClusterID());
			}
		}
	}

	public void dissolveNodes(Edge edge) {
		//find out nodes that this edge connects 
		PartnershipAgent from = (PartnershipAgent) edge.getFrom();
		PartnershipAgent to = (PartnershipAgent) edge.getTo();
		//dissociates edges from these nodes.  
		to.removeEdgesFrom(from);
		to.removeEdgesTo(from);
		from.removeEdgesFrom(to);
		from.removeEdgesTo(to);		
	}

	public void initializeInfection() {
		int size = individuals.size();
		int initiallyInfected = (int)(size*InitialInfection);
		Collections.shuffle(individuals);
		for (int i=0; i<initiallyInfected; i++) {
			PartnershipAgent individual = individuals.get(i);
			individual.setStageOfInfection(InfectionStage.Acute);
			individual.setInfectedTick((int)currentTick);
			if (debug) {
				System.out.println("Time: " + currentTick + " Individual:" + individual.getID() + " is infected.");
			}
		}		
	}

	private void createAgents(double numAgents) {
		for (int i=0; i<numAgents; i++) {
			PartnershipAgent individual = new PartnershipAgent();
			individual.setEntryTick((int)currentTick);
			individuals.add(individual);
			if (currentTick > InfectionStart) {
				if (debug) {
					System.out.println("Time: " + currentTick + " Individual:" + individual.getID() + " is born.");
				}
			}
		}
	}

	public void stepReport() {	
		if( currentTick % stepReportFrequency == 0
				&& currentTick > (this.maxIterations-1000)
		){ //report at every denominator steps
			int numNodes = individuals.size();
			double[] prev = getPrevalenceOverStages();
			double infTot = numNodes - prev[InfectionStage.Susceptible.ordinal()];
			this.numHIV = (int) infTot;
			prevalence = (double) infTot/numNodes;			
			double degree = 0; 
			degree = getMeanDegree();
			/*System.out.println("time: " + currentTick + " population: " + numNodes + " prev: " + prevalence + " frPHI: " + (frPHI/infTot) + " meandegree: " + degree);*/
//			System.out.println(run + "," + numNodes + "," + numHIV + "," + prevalence + "," + frPHI/infTot);
			addToOutput(new String(run + "," + numNodes + "," + numHIV + "," + prevalence + "," + frPHI/infTot + "," + degree));
		}
	}

	/** gets density of the individuals in each of the infection stage including susceptible stage */
	public double[] getPrevalenceOverStages() {
		frPHI = 0;
		double[] prevOverStages = {0, 0, 0};
		InfectionStage infStatus; 
		for (PartnershipAgent individual : individuals) {
			infStatus = individual.getStageOfInfection();
			try {
				for (InfectionStage stage : InfectionStage.values()) {
					if (infStatus.equals(stage)) {
						prevOverStages[stage.ordinal()]++;
						break;
					}
				}
			} catch (Exception e) {
				System.err.println(infStatus);
			}

			if (individual.isInfected()) {
				if (individual.infectedByAHI()) {
					frPHI++;
				}
			}
		} 
		return prevOverStages;
	}

	public static int getUniformIntFromTo (int low, int high) {
		int randNum = Uniform.staticNextIntFromTo(low, high);
		return randNum;
	}

	public static double getUniformDoubleFromTo(double low, double high) {
		double randNum = Uniform.staticNextDoubleFromTo(low, high);
		return randNum;
	}

	public double getMeanDegree() {
		double totalEdges = 0;
		double meanDegree = 0;
		for (int i=0; i<individuals.size(); i++) {
			PartnershipAgent node = individuals.get(i);
			totalEdges += node.getOutDegree(); // gets out degree of a node
		}
		meanDegree = totalEdges / individuals.size();

		return meanDegree;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void createClusterRecorder(String _prefix, OutbreakType _outbreakType, Class transmissionClass) {
		if (clusterRecorder != null) {
			clusterRecorder = null;
		}
		clusterRecorder = new ClusterRecorder(this, _prefix, _outbreakType, transmissionClass);		
	}

	@Override
	public void callClusterRecorderStep() {
		clusterRecorder.step();
	}

	@Override
	public void addTransmissionToClusterRecord(AgentInterface infector, AgentInterface susceptible) {
		try {
			clusterRecorder.recordTransmission(infector, susceptible);
		} catch (Exception e) {e.printStackTrace();}
	}

	@Override
	public void updateClusterRecord(AgentInterface agent) {
		clusterRecorder.updateEpisodicEpisodicAgentClusterRecord(agent);	
	}

	@Override
	public int returnPopulationSize() {
		return this.individuals.size();
	}

	@Override
	public void resetIndividualsOutbreakRecord() {
		for (PartnershipAgent individual : this.individuals) {
			individual.resetOutbreakRecord();
		}				
	}

	@Override
	public void addToOutput(String strOutput) {
		this.output.add(strOutput);
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
	public int getMaximumIterations() {
		return this.maxIterations;
	}
}