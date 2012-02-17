package reader;


import interfaces.AgentInterface;
import interfaces.ParametersInterface;
import interfaces.TransmissionInterface;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;


import cluster.Edge;
import edu.uci.ics.jung.graph.DelegateTree;
import episodicriskmodel.EpisodicRiskAgent;
import episodicriskmodel.EpisodicRiskTransmission;

/**
 * 
 *  @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 *
 */
public class InfectionForestReader implements ParametersInterface {
	private String fname = "";
	private int lineNo;
	private LinkedHashMap<Integer, ArrayList<String>> inputMap;
	private ArrayList<TransmissionInterface> transmissionsList;
	private HashMap<Integer, AgentInterface> individualsMap;
	private ArrayList<AgentInterface> roots;
	private DelegateTree<AgentInterface, Edge> infectionForest;
	
	public InfectionForestReader(String _fname) {
		this.fname = _fname;
		this.lineNo = 0;
		this.inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
		this.transmissionsList = new ArrayList<TransmissionInterface>();
		this.individualsMap = new LinkedHashMap<Integer, AgentInterface>();
		this.roots = new ArrayList<AgentInterface>();
		this.infectionForest = new DelegateTree<AgentInterface, Edge>();
	}
	
	public void run() {
		readInfectionTree();
		pumpTransmissions();
		createForest();
	}
	
	private void createForest() {
		/* Here is actually using EpisodicRiskAgent class instead of the interface*/
		EpisodicRiskAgent root = new EpisodicRiskAgent(-1);
		this.infectionForest.setRoot(root);
		for (AgentInterface actualRoot : this.roots) {
			/* Here is actually using EpisodicRiskTransmission class instead of the interface*/
			this.infectionForest.addEdge(new Edge(new EpisodicRiskTransmission()), root, actualRoot);
		}
		for (TransmissionInterface transmission : this.transmissionsList) {
			AgentInterface infector = individualsMap.get(new Integer(transmission.getInfectorID()));
			AgentInterface infected = individualsMap.get(new Integer(transmission.getInfectedID()));
			try {
				this.infectionForest.addEdge(new Edge(transmission), infector, infected);
			} catch (Exception e) {
				System.err.println("ERROR in generateInfectionForest()");
				e.printStackTrace();
			}
		}
	}
	
	private void pumpTransmissions() {
		HashMap<Integer, Double> infecteds = new LinkedHashMap<Integer, Double>();
		this.transmissionsList = new ArrayList<TransmissionInterface>();
		for (Integer key : this.inputMap.keySet()) {
			ArrayList<String> tokens = (ArrayList<String>) this.inputMap.get(key);
			/* Here actually using the EpisodicRiskTransmission class instead of the TransmissionInterface or the BaseTransmission class*/
			EpisodicRiskTransmission transmission = new EpisodicRiskTransmission();
			int time = Integer.parseInt(tokens.get(AHIKey.Time.ordinal()).trim());
			int infectorID = Integer.parseInt(tokens.get(AHIKey.InfectorID.ordinal()).trim());
			int infectedID = Integer.parseInt(tokens.get(AHIKey.InfectedID.ordinal()).trim());
			int infectorTick = Integer.parseInt(tokens.get(AHIKey.InfectorTick.ordinal()).trim());
			ActType actType = ActType.valueOf(tokens.get(AHIKey.ActType.ordinal()).trim()); 
			int timeSinceLastInfection = Integer.parseInt(tokens.get(AHIKey.TimeSinceLastInf.ordinal()).trim());
			InfectionStage infectorStage = InfectionStage.valueOf(tokens.get(AHIKey.InfectorStageOfInfection.ordinal()).trim());
			RiskState infectorRiskState = RiskState.valueOf(tokens.get(AHIKey.InfectorRiskState.ordinal()).trim());
			RiskState infectedRiskState = RiskState.valueOf(tokens.get(AHIKey.InfectedRiskState.ordinal()).trim());
			MixingSite mixingSite = MixingSite.valueOf(tokens.get(AHIKey.MixingSite.ordinal()).trim());
			int branchTime = Integer.parseInt(tokens.get(AHIKey.BranchLength.ordinal()).trim());

			transmission.setTime(time);
			transmission.setInfectorID(infectorID);
			transmission.setInfectedID(infectedID);
			transmission.setTimeSinceLastInfection(timeSinceLastInfection);
			transmission.setActType(actType);
			transmission.setInfectorStage(infectorStage);
			transmission.setInfectorRiskState(infectorRiskState);
			transmission.setInfectedRiskState(infectedRiskState);
			transmission.setMixingSite(mixingSite);
			transmission.setBranchTime(branchTime);

			if (this.individualsMap.containsKey(infectorID) == false) {
				/* Here is actually using EpisodicRiskAgent class instead of the interface*/
				EpisodicRiskAgent infector = new EpisodicRiskAgent(infectorID);
				infector.setInfectedTick(infectorTick);
				this.individualsMap.put(new Integer(infectorID), infector);
			}
			if (this.individualsMap.containsKey(infectedID) == false) {
				/* Here is actually using EpisodicRiskAgent class instead of the interface*/
				EpisodicRiskAgent infected = new EpisodicRiskAgent(infectedID);
				infected.setActType(actType);
				infected.setInfectedMixingSite(mixingSite);
				infected.setInfectedRiskState(infectedRiskState);
				infected.setInfectorID(infectorID);
				infected.setInfectorStageOfInfection(infectorStage);
				infected.setInfectedTick(time);
				this.individualsMap.put(new Integer(infectedID), infected);
				infecteds.put(infectedID, new Double(time));
			}
			this.transmissionsList.add(transmission);
		}
		for (Integer indID : this.individualsMap.keySet()) {
			if (infecteds.containsKey(indID) == false) {
				this.roots.add(this.individualsMap.get(indID));
			}
		}
	}

	private void readInfectionTree() {
		try {
			BufferedReader bufRdr = new BufferedReader(new FileReader(fname));
			String line = null;
			while ((line = bufRdr.readLine()) != null) {
				this.lineNo++;
				StringTokenizer st = new StringTokenizer(line, ",");
				ArrayList<String> tokens = new ArrayList<String>();
				while (st.hasMoreTokens()) {
					tokens.add(st.nextToken());
				}
				this.inputMap.put(new Integer(this.lineNo), tokens);
			}
			bufRdr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Collection<AgentInterface> getVertices() {
		return infectionForest.getVertices();
	}
	
	public boolean isLeaf(AgentInterface individual) {
		return infectionForest.isLeaf(individual);
	}
	
	public Collection<AgentInterface> getSuccessors(AgentInterface individual) {
		return infectionForest.getSuccessors(individual);
	}
		
	public AgentInterface getParent(AgentInterface individual) {
		return infectionForest.getParent(individual);
	}
	
	public Edge getParentEdge(AgentInterface individual) {
		return infectionForest.getParentEdge(individual);
	}
	
	public AgentInterface getPerson(Integer ID) {
		return individualsMap.get(ID);
	}
	
	public boolean isRoot(AgentInterface individual) {
		return infectionForest.isRoot(individual); 
	}
	
	public AgentInterface getSource(Edge edge) {
		return infectionForest.getSource(edge);
	}

	public DelegateTree<AgentInterface, Edge> getInfectionForest() {
		return infectionForest;
	}
}