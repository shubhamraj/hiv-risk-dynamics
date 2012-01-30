package reader;

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

import model.Individual;
import model.Parameters;
import model.Parameters.ACT_TYPE;
import model.Parameters.AHIKey;
import model.Parameters.MIXING_SITE;
import model.Parameters.RISK_STATE;
import model.Parameters.STAGE;

import cluster.Edge;
import cluster.Transmission;
import edu.uci.ics.jung.graph.DelegateTree;

/**
 * 
 * @author shah
 *
 */
public class SingleRootedTreeReader {
	private String fname;
	private int lineNo;
	private LinkedHashMap<Integer, ArrayList<String>> inputMap;
	private LinkedHashMap<Integer, Individual> individuals;
	private ArrayList<Individual> roots;
	DelegateTree<Individual, Edge> infectionForest;
	ArrayList<Transmission> transmissionsList;

	public static final int threshold = 92000;
	
	ArrayList<ChainsDataStructure> deadEnds;
	ArrayList<ChainsDataStructure> continuous;
	EnumMap<ACT_TYPE, Set<Edge>> uniqueContinuousEdges, uniqueDeadEdges;

	public SingleRootedTreeReader(String _fname) {
		this.fname = _fname;
		this.inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
		this.individuals = new LinkedHashMap<Integer, Individual>();
		this.roots = new ArrayList<Individual>();
		this.infectionForest = new DelegateTree<Individual, Edge>();
		this.transmissionsList = new ArrayList<Transmission>();
		this.lineNo = 0;
		this.deadEnds = new ArrayList<ChainsDataStructure>();
		this.continuous = new ArrayList<ChainsDataStructure>();

		this.uniqueContinuousEdges = new EnumMap<Parameters.ACT_TYPE, Set<Edge>>(ACT_TYPE.class);
		this.uniqueDeadEdges = new EnumMap<Parameters.ACT_TYPE, Set<Edge>>(ACT_TYPE.class);

		for (ACT_TYPE actType : ACT_TYPE.values()) {
			this.uniqueContinuousEdges.put(actType, new HashSet<Edge>()) ;
			this.uniqueDeadEdges.put(actType, new HashSet<Edge>()) ;
		}
	}

	public void pumpTransmissions() {
		HashMap<Integer, Double>infecteds = new LinkedHashMap<Integer, Double>(); 
		this.transmissionsList = new ArrayList<Transmission>();		

		for (Integer key : inputMap.keySet()) {
			ArrayList<String> tokens = (ArrayList<String>) inputMap.get(key);
			Transmission transmission = new Transmission();
			int time = Integer.parseInt(tokens.get(AHIKey.Time.ordinal()).trim());
			int infectorID = Integer.parseInt(tokens.get(AHIKey.InfectorID.ordinal()).trim());
			int infectedID = Integer.parseInt(tokens.get(AHIKey.InfectedID.ordinal()).trim());
			int infectorTick = Integer.parseInt(tokens.get(AHIKey.InfectorTick.ordinal()).trim());
			String act = tokens.get(AHIKey.ActType.ordinal()).trim();
			ACT_TYPE actType = ACT_TYPE.NONE;
			if (act.equals("AHI")) {
				actType = ACT_TYPE.AHI;
			}
			else {
				actType = ACT_TYPE.CHI;
			}		
			int timeSinceLastInfection = Integer.parseInt(tokens.get(AHIKey.TimeSinceLastInf.ordinal()).trim());
			String strInfectorStage = tokens.get(AHIKey.InfectorStg.ordinal()).trim();			
			STAGE infectorStage;			
			if (strInfectorStage.equals("AHI")) {
				infectorStage = STAGE.ACUTE;
			}
			else {
				infectorStage = STAGE.CHRONIC;
			}			
			RISK_STATE infectorState, infectedState;

			String strInfectorState = tokens.get(AHIKey.InfectorState.ordinal()).trim();
			if (strInfectorState.equals("HIGH")) {
				infectorState = RISK_STATE.HIGH;
			}
			else {
				infectorState = RISK_STATE.LOW;
			}
			String strInfectedState = tokens.get(AHIKey.InfectedState.ordinal()).trim();
			if (strInfectedState.equals("HIGH")) {
				infectedState = RISK_STATE.HIGH;
			}
			else {
				infectedState = RISK_STATE.LOW;
			}
			MIXING_SITE mixingSite = MIXING_SITE.NONE;
			String site = tokens.get(AHIKey.MixingSite.ordinal()).trim();
			if (site.equals("HIGH_RISK")) {
				mixingSite = MIXING_SITE.HIGH_RISK;
			}
			else {
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

			if (individuals.containsKey(infectorID) == false) {
				Individual infector = new Individual(infectorID);
				infector.setInfectedTick(infectorTick);
				individuals.put(new Integer(infectorID), infector);										
			}
			if (individuals.containsKey(infectedID) == false) {
				Individual infected = new Individual(infectedID);
				infected.setActType(actType);
				infected.setInfectedMixingSite(mixingSite);
				infected.setInfectedRiskState(infectedState);
				infected.setInfectorID(infectorID);
				infected.setInfectorStatus(infectorStage);
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
		Individual root = new Individual(-1);
		infectionForest.setRoot(root);		
		for (Individual actualRoot : roots) {
			infectionForest.addEdge(new Edge(new Transmission()), root, actualRoot); 
		}
		for (Transmission transmission : transmissionsList) {
			Individual infector = individuals.get(new Integer(transmission.getInfectorID()));
			Individual infected = individuals.get(new Integer(transmission.getInfectedID()));
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
		
		for (Individual vertex : infectionForest.getVertices()) {
			if (infectionForest.isLeaf(vertex)) {
				if (vertex.getInfectedTick() <= threshold) {
					deadEnds.add(returnTreeStat(vertex));
					if (vertex.getActType().equals(ACT_TYPE.AHI)) deadLeafsAHI++;
					else if (vertex.getActType().equals(ACT_TYPE.CHI)) deadLeafsCHI++;
				}
			}
			else {
				if (vertex.getInfectedTick() <= threshold) {
					for (Individual successor : infectionForest.getSuccessors(vertex)) {
						if (successor.getInfectedTick() > threshold) {
							continuous.add(returnTreeStat(vertex));
							if (vertex.getActType().equals(ACT_TYPE.AHI)) contLeafsAHI++;
							else if (vertex.getActType().equals(ACT_TYPE.CHI)) contLeafsCHI++;
							break;
						}
					}
				}

			}
		}

		System.out.println(deadLeafsAHI + " " + deadLeafsCHI + " " + contLeafsAHI + " " + contLeafsCHI);
				
		for (ChainsDataStructure treeStat: continuous) {
			Individual leaf = treeStat.getLeaf();
			pumpUniqueEdges(leaf, uniqueContinuousEdges);
		}

		for (ChainsDataStructure treeStat: deadEnds) {
			Individual leaf = treeStat.getLeaf();
			pumpUniqueEdges(leaf, uniqueDeadEdges);
		}

		double numChronicDead=0, numAcuteDead=0;
		double numChronicCont=0, numAcuteCont=0;

		numChronicDead = uniqueDeadEdges.get(ACT_TYPE.CHI).size();
		numAcuteDead = uniqueDeadEdges.get(ACT_TYPE.AHI).size();
		numChronicCont = uniqueContinuousEdges.get(ACT_TYPE.CHI).size();
		numAcuteCont = uniqueContinuousEdges.get(ACT_TYPE.AHI).size();

		double deadFrac = numAcuteDead/(numChronicDead+numAcuteDead);
		double contFrac = numAcuteCont/(numChronicCont+numAcuteCont);

		System.out.println(numChronicDead + " " + numAcuteDead + " " + deadFrac 
				+ " " + numChronicCont + " " + numAcuteCont + " " + contFrac);		
	}

	private void pumpUniqueEdges(Individual leaf, EnumMap<ACT_TYPE, Set<Edge>> map) {
		boolean rootFound = false;
		Individual vertex = leaf;
		while (rootFound == false) {
			Edge edge = infectionForest.getParentEdge(vertex);
			Individual parent = infectionForest.getSource(edge);
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


	private ChainsDataStructure returnTreeStat(Individual vertex) {
		Edge edge = infectionForest.getParentEdge(vertex);
		ChainsDataStructure treeStat = new ChainsDataStructure(vertex, edge);

		/*		System.out.println(" Time: " + edge.getTransmission().getTime() + " Infector: " + edge.getTransmission().getInfectorID() + " infected id: " + vertex.getID());

		for (Individual parent : infectionForest.getPath(vertex)) {
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