package reader;



import interfaces.ParametersInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.StringTokenizer;



import cluster.Edge;
import edu.uci.ics.jung.graph.DelegateTree;
import episodicriskmodel.EpisodicRiskTransmission;
import episodicriskmodel.Person;

/**
 * 
 * @author shah
 *
 */
public class SingleRootedTreeReader implements ParametersInterface {
	private String fname;
	private int lineNo;
	private LinkedHashMap<Integer, ArrayList<String>> inputMap;
	private LinkedHashMap<Integer, Person> individuals;
	private ArrayList<Person> roots;
	DelegateTree<Person, Edge> infectionForest;
	ArrayList<EpisodicRiskTransmission> transmissionsList;

	public static final int threshold = 92000;
	
	ArrayList<ChainsDataStructure> deadEnds;
	ArrayList<ChainsDataStructure> continuous;
	EnumMap<ActType, Set<Edge>> uniqueContinuousEdges, uniqueDeadEdges;

	public SingleRootedTreeReader(String _fname) {
		this.fname = _fname;
		this.inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
		this.individuals = new LinkedHashMap<Integer, Person>();
		this.roots = new ArrayList<Person>();
		this.infectionForest = new DelegateTree<Person, Edge>();
		this.transmissionsList = new ArrayList<EpisodicRiskTransmission>();
		this.lineNo = 0;
		this.deadEnds = new ArrayList<ChainsDataStructure>();
		this.continuous = new ArrayList<ChainsDataStructure>();

		this.uniqueContinuousEdges = new EnumMap<ActType, Set<Edge>>(ActType.class);
		this.uniqueDeadEdges = new EnumMap<ActType, Set<Edge>>(ActType.class);

		for (ActType actType : ActType.values()) {
			this.uniqueContinuousEdges.put(actType, new HashSet<Edge>()) ;
			this.uniqueDeadEdges.put(actType, new HashSet<Edge>()) ;
		}
	}

	public void pumpTransmissions() {
		HashMap<Integer, Double>infecteds = new LinkedHashMap<Integer, Double>(); 
		this.transmissionsList = new ArrayList<EpisodicRiskTransmission>();		

		for (Integer key : inputMap.keySet()) {
			ArrayList<String> tokens = (ArrayList<String>) inputMap.get(key);
			EpisodicRiskTransmission transmission = new EpisodicRiskTransmission();
			int time = Integer.parseInt(tokens.get(AHIKey.Time.ordinal()).trim());
			int infectorID = Integer.parseInt(tokens.get(AHIKey.InfectorID.ordinal()).trim());
			int infectedID = Integer.parseInt(tokens.get(AHIKey.InfectedID.ordinal()).trim());
			int infectorTick = Integer.parseInt(tokens.get(AHIKey.InfectorTick.ordinal()).trim());
			String act = tokens.get(AHIKey.ActType.ordinal()).trim();
			ActType actType = ActType.None;
			if (act.equals("AHI")) {
				actType = ActType.Acute_Susceptible;
			}
			else {
				actType = ActType.Chronic_Susceptible;
			}		
			int timeSinceLastInfection = Integer.parseInt(tokens.get(AHIKey.TimeSinceLastInf.ordinal()).trim());
			String strInfectorStage = tokens.get(AHIKey.InfectorStg.ordinal()).trim();			
			InfectionStage infectorStage;			
			if (strInfectorStage.equals("AHI")) {
				infectorStage = InfectionStage.Acute;
			}
			else {
				infectorStage = InfectionStage.Chronic;
			}			
			RISK_STATE infectorState, infectedState;

			String strInfectorState = tokens.get(AHIKey.InfectorState.ordinal()).trim();
			if (strInfectorState.equals("HIGH")) {
				infectorState = RISK_STATE.High;
			}
			else {
				infectorState = RISK_STATE.Low;
			}
			String strInfectedState = tokens.get(AHIKey.InfectedState.ordinal()).trim();
			if (strInfectedState.equals("HIGH")) {
				infectedState = RISK_STATE.High;
			}
			else {
				infectedState = RISK_STATE.Low;
			}
			MIXING_SITE mixingSite = MIXING_SITE.None;
			String site = tokens.get(AHIKey.MixingSite.ordinal()).trim();
			if (site.equals("HIGH_RISK")) {
				mixingSite = MIXING_SITE.HighRisk;
			}
			else {
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

			if (individuals.containsKey(infectorID) == false) {
				Person infector = new Person(infectorID);
				infector.setInfectedTick(infectorTick);
				individuals.put(new Integer(infectorID), infector);										
			}
			if (individuals.containsKey(infectedID) == false) {
				Person infected = new Person(infectedID);
				infected.setActType(actType);
				infected.setInfectedMixingSite(mixingSite);
				infected.setInfectedRiskState(infectedState);
				infected.setInfectorID(infectorID);
				infected.setInfectorStageOfInfection(infectorStage);
				infected.setInfectedTick(time);
				individuals.put(new Integer(infectedID), infected);
				infecteds.put(infectedID, new Double(time));
			}			
			/*			System.out.println(obID + "," + time + "," + infectorID + ","
					+ infectedID + "," + actType + "," + timeSinceLastInfection  
					+ "," + infectorStage + "," + infectorState + "," + infectedState);

			 */
			transmissionsList.add(transmission);
		}

		for (Integer indID : individuals.keySet()) {
			if (infecteds.containsKey(indID) == false) {
				roots.add(individuals.get(indID));
			}
		}
	}

	public void generateForest() {
		Person root = new Person(-1);
		infectionForest.setRoot(root);		
		for (Person actualRoot : roots) {
			infectionForest.addEdge(new Edge(new EpisodicRiskTransmission()), root, actualRoot); 
		}
		for (EpisodicRiskTransmission transmission : transmissionsList) {
			Person infector = individuals.get(new Integer(transmission.getInfectorID()));
			Person infected = individuals.get(new Integer(transmission.getInfectedID()));
			try {
				infectionForest.addEdge(new Edge(transmission), infector, infected);
			} catch (Exception e) {	
				System.out.println("ERROR generateFores()");
			}
		}	
	}

	public void processInfectionTree() {
		int deadLeafsAHI = 0, deadLeafsCHI = 0;
		int contLeafsAHI = 0, contLeafsCHI = 0;
		
		for (Person vertex : infectionForest.getVertices()) {
			if (infectionForest.isLeaf(vertex)) {
				if (vertex.getInfectedTick() <= threshold) {
					deadEnds.add(returnTreeStat(vertex));
					if (vertex.getActType().equals(ActType.Acute_Susceptible)) deadLeafsAHI++;
					else if (vertex.getActType().equals(ActType.Chronic_Susceptible)) deadLeafsCHI++;
				}
			}
			else {
				if (vertex.getInfectedTick() <= threshold) {
					for (Person successor : infectionForest.getSuccessors(vertex)) {
						if (successor.getInfectedTick() > threshold) {
							continuous.add(returnTreeStat(vertex));
							if (vertex.getActType().equals(ActType.Acute_Susceptible)) contLeafsAHI++;
							else if (vertex.getActType().equals(ActType.Chronic_Susceptible)) contLeafsCHI++;
							break;
						}
					}
				}

			}
		}

		System.out.println(deadLeafsAHI + " " + deadLeafsCHI + " " + contLeafsAHI + " " + contLeafsCHI);
				
		for (ChainsDataStructure treeStat: continuous) {
			Person leaf = treeStat.getLeaf();
			pumpUniqueEdges(leaf, uniqueContinuousEdges);
		}

		for (ChainsDataStructure treeStat: deadEnds) {
			Person leaf = treeStat.getLeaf();
			pumpUniqueEdges(leaf, uniqueDeadEdges);
		}

		double numChronicDead=0, numAcuteDead=0;
		double numChronicCont=0, numAcuteCont=0;

		numChronicDead = uniqueDeadEdges.get(ActType.Chronic_Susceptible).size();
		numAcuteDead = uniqueDeadEdges.get(ActType.Acute_Susceptible).size();
		numChronicCont = uniqueContinuousEdges.get(ActType.Chronic_Susceptible).size();
		numAcuteCont = uniqueContinuousEdges.get(ActType.Acute_Susceptible).size();

		double deadFrac = numAcuteDead/(numChronicDead+numAcuteDead);
		double contFrac = numAcuteCont/(numChronicCont+numAcuteCont);

		System.out.println(numChronicDead + " " + numAcuteDead + " " + deadFrac 
				+ " " + numChronicCont + " " + numAcuteCont + " " + contFrac);		
	}

	private void pumpUniqueEdges(Person leaf, EnumMap<ActType, Set<Edge>> map) {
		boolean rootFound = false;
		Person vertex = leaf;
		while (rootFound == false) {
			Edge edge = infectionForest.getParentEdge(vertex);
			Person parent = infectionForest.getSource(edge);
			if (infectionForest.isRoot(parent)
					|| parent.getID() == -1
			) {
				rootFound = true;
			}
			else {
				Set<Edge> edgeSet = map.get(edge.getTransmission().getActType());
				edgeSet.add(edge);				
				vertex = parent;
			}
		}								
	}


	private ChainsDataStructure returnTreeStat(Person vertex) {
		Edge edge = infectionForest.getParentEdge(vertex);
		ChainsDataStructure treeStat = new ChainsDataStructure(vertex, edge);

		/*		System.out.println(" Time: " + edge.getTransmission().getTime() + " Infector: " + edge.getTransmission().getInfectorID() + " infected id: " + vertex.getID());

		for (Person parent : infectionForest.getPath(vertex)) {
			if (parent.getID() == -1
					|| parent.getInfectedTick() == -1
					|| infectionForest.isRoot(infectionForest.getParent(parent))) {
				continue;
			}	
			//Edge inEdge = infectionForest.findEdge(parent, v2)
			//if (parent.get)
		}*/
		return treeStat;
	}

	public void read() {
		try {
			BufferedReader bufRdr = new BufferedReader(new FileReader(fname));			
			String line = null;					
			while ((line = bufRdr.readLine()) != null) {
				lineNo++;
				StringTokenizer st = new StringTokenizer(line,",");
				ArrayList<String> tokens = new ArrayList<String>(); 
				while (st.hasMoreTokens()) {
					tokens.add(st.nextToken());
				}
				inputMap.put(new Integer(lineNo), tokens);
			}
			bufRdr.close();
		} catch (IOException e) {e.printStackTrace();}
	}

	public static void main(String[] args) {
		File directory = new File("./");
		File[] csvFiles = directory.listFiles(new FilenameFilter() {			
			@Override 
			public boolean accept(File dir, String name) {
				return name.endsWith("allTransmissions.csv");
			}
		});
		for (int i=0; i<csvFiles.length; i++) {
			System.out.println(directory+csvFiles[i].getName());
			SingleRootedTreeReader r = new SingleRootedTreeReader(csvFiles[i].getName());
			r.read();
			r.pumpTransmissions();
			r.generateForest();
			r.processInfectionTree();
		}
	}
}