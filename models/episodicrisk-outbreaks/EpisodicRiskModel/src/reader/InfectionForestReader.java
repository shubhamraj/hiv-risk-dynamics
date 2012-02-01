package reader;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

import basemodel.ParametersInterface;

import cluster.Edge;
import edu.uci.ics.jung.graph.DelegateTree;
import episodicriskmodel.EpisodicRiskTransmission;
import episodicriskmodel.Person;

/**
 * 
 * @author shah
 *
 */
public class InfectionForestReader implements ParametersInterface {
	private String fname = "";
	private int lineNo;
	private LinkedHashMap<Integer, ArrayList<String>> inputMap;
	private ArrayList<EpisodicRiskTransmission> transmissionsList;
	private HashMap<Integer, Person> individualsMap;
	private ArrayList<Person> roots;
	private DelegateTree<Person, Edge> infectionForest;
	
	public InfectionForestReader(String _fname) {
		this.fname = _fname;
		this.lineNo = 0;
		this.inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
		this.transmissionsList = new ArrayList<EpisodicRiskTransmission>();
		this.individualsMap = new LinkedHashMap<Integer, Person>();
		this.roots = new ArrayList<Person>();
		this.infectionForest = new DelegateTree<Person, Edge>();
	}
	
	public void run() {
		readInfectionTree();
		pumpTransmissions();
		createForest();
	}
	
	private void createForest() {
		Person root = new Person(-1);
		this.infectionForest.setRoot(root);
		for (Person actualRoot : this.roots) {
			this.infectionForest.addEdge(new Edge(new EpisodicRiskTransmission()), root, actualRoot);
		}
		for (EpisodicRiskTransmission transmission : this.transmissionsList) {
			Person infector = individualsMap.get(new Integer(transmission.getInfectorID()));
			Person infected = individualsMap.get(new Integer(transmission.getInfectedID()));
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
		this.transmissionsList = new ArrayList<EpisodicRiskTransmission>();
		for (Integer key : this.inputMap.keySet()) {
			ArrayList<String> tokens = (ArrayList<String>) this.inputMap.get(key);
			EpisodicRiskTransmission transmission = new EpisodicRiskTransmission();
			int time = Integer.parseInt(tokens.get(AHIKey.Time.ordinal()).trim());
			int infectorID = Integer.parseInt(tokens.get(AHIKey.InfectorID.ordinal()).trim());
			int infectedID = Integer.parseInt(tokens.get(AHIKey.InfectedID.ordinal()).trim());
			int infectorTick = Integer.parseInt(tokens.get(AHIKey.InfectorTick.ordinal()).trim());

			String act = tokens.get(AHIKey.ActType.ordinal()).trim();
			ActType actType = ActType.None;
			if (act.equals("AHI")) {
				actType = ActType.Acute_Susceptible;
			} else {
				actType = ActType.Chronic_Susceptible;
			}
			int timeSinceLastInfection = Integer.parseInt(tokens.get(AHIKey.TimeSinceLastInf.ordinal()).trim());
			String strInfectorStage = tokens.get(AHIKey.InfectorStg.ordinal()).trim();
			InfectionStage infectorStage;
			if (strInfectorStage.equals("AHI")) {
				infectorStage = InfectionStage.Acute;
			} else {
				infectorStage = InfectionStage.Chronic;
			}
			RISK_STATE infectorState, infectedState;

			String strInfectorState = tokens.get(AHIKey.InfectorState.ordinal()).trim();
			if (strInfectorState.equals("HIGH")) {
				infectorState = RISK_STATE.High;
			} else {
				infectorState = RISK_STATE.Low;
			}
			String strInfectedState = tokens.get(AHIKey.InfectedState.ordinal()).trim();
			if (strInfectedState.equals("HIGH")) {
				infectedState = RISK_STATE.High;
			} else {
				infectedState = RISK_STATE.Low;
			}

			MIXING_SITE mixingSite = MIXING_SITE.None;
			String site = tokens.get(AHIKey.MixingSite.ordinal()).trim();
			if (site.equals("HIGH_RISK")) {
				mixingSite = MIXING_SITE.HighRisk;
			} else {
				mixingSite = MIXING_SITE.Common;
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
				Person infector = new Person(infectorID);
				infector.setInfectedTick(infectorTick);
				this.individualsMap.put(new Integer(infectorID), infector);
			}
			if (this.individualsMap.containsKey(infectedID) == false) {
				Person infected = new Person(infectedID);
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
	
	public Collection<Person> getVertices() {
		return infectionForest.getVertices();
	}
	
	public boolean isLeaf(Person individual) {
		return infectionForest.isLeaf(individual);
	}
	
	public Collection<Person> getSuccessors(Person individual) {
		return infectionForest.getSuccessors(individual);
	}
		
	public Person getParent(Person individual) {
		return infectionForest.getParent(individual);
	}
	
	public Edge getParentEdge(Person individual) {
		return infectionForest.getParentEdge(individual);
	}
	
	public Person getPerson(Integer ID) {
		return individualsMap.get(ID);
	}
	
	public boolean isRoot(Person individual) {
		return infectionForest.isRoot(individual); 
	}
	
	public Person getSource(Edge edge) {
		return infectionForest.getSource(edge);
	}

	public static void main(String[] args) {
	}
}
