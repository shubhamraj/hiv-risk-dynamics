package reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

import model.Individual;
import model.Parameters;
import cluster.Edge;
import cluster.Transmission;
import edu.uci.ics.jung.graph.DelegateTree;

/**
 * 
 * @author shah
 *
 */
public class InfectionForestReader extends Parameters {
	private String fname = "";
	private int lineNo;
	private LinkedHashMap<Integer, ArrayList<String>> inputMap;
	private ArrayList<Transmission> transmissionsList;
	private HashMap<Integer, Individual> individualsMap;
	private ArrayList<Individual> roots;
	private DelegateTree<Individual, Edge> infectionForest;
	
	public InfectionForestReader(String _fname) {
		this.fname = _fname;
		this.lineNo = 0;
		this.inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
		this.transmissionsList = new ArrayList<Transmission>();
		this.individualsMap = new LinkedHashMap<Integer, Individual>();
		this.roots = new ArrayList<Individual>();
		this.infectionForest = new DelegateTree<Individual, Edge>();
	}
	
	public void run() {
		readInfectionTree();
		pumpTransmissions();
		createForest();
	}
	
	private void createForest() {
		Individual root = new Individual(-1);
		this.infectionForest.setRoot(root);
		for (Individual actualRoot : this.roots) {
			this.infectionForest.addEdge(new Edge(new Transmission()), root, actualRoot);
		}
		for (Transmission transmission : this.transmissionsList) {
			Individual infector = individualsMap.get(new Integer(transmission.getInfectorID()));
			Individual infected = individualsMap.get(new Integer(transmission.getInfectedID()));
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
		this.transmissionsList = new ArrayList<Transmission>();
		for (Integer key : this.inputMap.keySet()) {
			ArrayList<String> tokens = (ArrayList<String>) this.inputMap.get(key);
			Transmission transmission = new Transmission();
			int time = Integer.parseInt(tokens.get(AHIKey.Time.ordinal()).trim());
			int infectorID = Integer.parseInt(tokens.get(AHIKey.InfectorID.ordinal()).trim());
			int infectedID = Integer.parseInt(tokens.get(AHIKey.InfectedID.ordinal()).trim());
			int infectorTick = Integer.parseInt(tokens.get(AHIKey.InfectorTick.ordinal()).trim());

			String act = tokens.get(AHIKey.ActType.ordinal()).trim();
			ACT_TYPE actType = ACT_TYPE.NONE;
			if (act.equals("AHI")) {
				actType = ACT_TYPE.AHI;
			} else {
				actType = ACT_TYPE.CHI;
			}
			int timeSinceLastInfection = Integer.parseInt(tokens.get(AHIKey.TimeSinceLastInf.ordinal()).trim());
			String strInfectorStage = tokens.get(AHIKey.InfectorStg.ordinal()).trim();
			STAGE infectorStage;
			if (strInfectorStage.equals("AHI")) {
				infectorStage = STAGE.ACUTE;
			} else {
				infectorStage = STAGE.CHRONIC;
			}
			RISK_STATE infectorState, infectedState;

			String strInfectorState = tokens.get(AHIKey.InfectorState.ordinal()).trim();
			if (strInfectorState.equals("HIGH")) {
				infectorState = RISK_STATE.HIGH;
			} else {
				infectorState = RISK_STATE.LOW;
			}
			String strInfectedState = tokens.get(AHIKey.InfectedState.ordinal()).trim();
			if (strInfectedState.equals("HIGH")) {
				infectedState = RISK_STATE.HIGH;
			} else {
				infectedState = RISK_STATE.LOW;
			}

			MIXING_SITE mixingSite = MIXING_SITE.NONE;
			String site = tokens.get(AHIKey.MixingSite.ordinal()).trim();
			if (site.equals("HIGH_RISK")) {
				mixingSite = MIXING_SITE.HIGH_RISK;
			} else {
				mixingSite = MIXING_SITE.COMMON;
			}
			int branchTime = Integer.parseInt(tokens.get(AHIKey.BranchLength.ordinal()).trim());
			transmission.setTime(time);
			transmission.setInfectorID(infectorID);
			transmission.setInfectedID(infectedID);
			transmission.setTimeSinceLastInfection(timeSinceLastInfection);
			transmission.setActType(actType);
			transmission.setInfectorStage(infectorStage);
			transmission.setInfectorRiskState(infectorState);
			transmission.setInfectedRiskState(infectedState);
			transmission.setMixingSite(mixingSite);
			transmission.setBranchTime(branchTime);

			if (this.individualsMap.containsKey(infectorID) == false) {
				Individual infector = new Individual(infectorID);
				infector.setInfectedTick(infectorTick);
				this.individualsMap.put(new Integer(infectorID), infector);
			}
			if (this.individualsMap.containsKey(infectedID) == false) {
				Individual infected = new Individual(infectedID);
				infected.setActType(actType);
				infected.setInfectedMixingSite(mixingSite);
				infected.setInfectedRiskState(infectedState);
				infected.setInfectorID(infectorID);
				infected.setInfectorStatus(infectorStage);
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
	
	public Collection<Individual> getVertices() {
		return infectionForest.getVertices();
	}
	
	public boolean isLeaf(Individual individual) {
		return infectionForest.isLeaf(individual);
	}
	
	public Collection<Individual> getSuccessors(Individual individual) {
		return infectionForest.getSuccessors(individual);
	}
		
	public Individual getParent(Individual individual) {
		return infectionForest.getParent(individual);
	}
	
	public Edge getParentEdge(Individual individual) {
		return infectionForest.getParentEdge(individual);
	}
	
	public Individual getIndividual(Integer ID) {
		return individualsMap.get(ID);
	}
	
	public boolean isRoot(Individual individual) {
		return infectionForest.isRoot(individual); 
	}
	
	public Individual getSource(Edge edge) {
		return infectionForest.getSource(edge);
	}

	public static void main(String[] args) {
	}
}
