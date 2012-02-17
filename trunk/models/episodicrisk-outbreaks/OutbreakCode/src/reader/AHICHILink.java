package reader;


import interfaces.AgentInterface;
import interfaces.ParametersInterface;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import lineagetree.LineageTree;
import main.Summarizer;

import cluster.Edge;
import edu.uci.ics.jung.graph.DelegateTree;
/**
 * 
 * @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 *
 */
public class AHICHILink implements ParametersInterface {

	private int index;
	private String fname;
	private OutbreakRecord outbreakRecord = OutbreakRecord.Endemic;
	//
	private HashMap<Integer, AHIDataStructure> continuousClusterMap, deadClusterMap;
	//
	private Set<Integer> continuousAHIRoots, deadAHIRoots;
	private HashMap<Integer, AgentInterface> deadEnds;
	private HashMap<Integer, AgentInterface> continuousLeafs;
	private EnumMap<ActType, Set<Edge>> uniqueContinuousEdges, uniqueDeadEdges;
	private ArrayList<Double> dataCountChronics, ahiOutput, chainsOutput;

	private InfectionForestReader infectionForestReader;
	private AcuteClusterReader ahiClusterReader;
	private EnumMap<Output, ArrayList<Double>> outputsMap;	

	private ChainsType chainsType = ChainsType.All;

	public AHICHILink(int _index, String _fname) {
		this.index = _index;
		this.fname = _fname;
		
		this.outputsMap = new EnumMap<Output, ArrayList<Double>>(Output.class);
		
		this.deadEnds = new HashMap<Integer, AgentInterface>();
		this.continuousLeafs = new HashMap<Integer, AgentInterface>();
		this.uniqueContinuousEdges = new EnumMap<ActType, Set<Edge>>(ActType.class);
		this.uniqueDeadEdges = new EnumMap<ActType, Set<Edge>>(ActType.class);

		this.continuousAHIRoots = new HashSet<Integer>();
		this.deadAHIRoots = new HashSet<Integer>();		
		this.dataCountChronics = new ArrayList<Double>();

		for (ActType actType : ActType.values()) {
			this.uniqueContinuousEdges.put(actType, new HashSet<Edge>());
			this.uniqueDeadEdges.put(actType, new HashSet<Edge>());
		}

		this.ahiOutput = new ArrayList<Double>();
		this.chainsOutput = new ArrayList<Double>();
		this.continuousClusterMap = new HashMap<Integer, AHIDataStructure>();
		this.deadClusterMap = new HashMap<Integer, AHIDataStructure>();
		
		for (OutbreakRecord obr : OutbreakRecord.values()) {
			if (this.fname.contains(obr.name())) {
				this.outbreakRecord = obr;
			}
		}
	}

	public void run() {
		/* AHI Cluster generated */
		generateAHITree();
		/* infection tree */
		generateInfectionForest();
		/* Collects & processes continuous and dead-end chains */
		processInfectionForestChains();
		/* Calculate AHI outbreak statistics given the ChainsType argument */
		calculateAcuteOutbreakChains(ChainsType.All);
		/*
		 * calculateAcuteOutbreakChains(ChainsType.Continuous);
		 * calculateAcuteOutbreakChains(ChainsType.DeadEnds);
		 */

		/*
		 * /Continuous writeStructureData(true); //Dead ends:
		 * writeStructureData(false);
		 */

		/* */
		calculateAHIStatistics();
		/* */
		writeChronicsCountStats();
		
		/*Build the lineage tree */
		LineageTree lineageTree = new LineageTree(infectionForestReader.getInfectionForest());
		lineageTree.buildEventForest();
	}
	
	private void generateAHITree() { 
		String filename = this.fname;
		int obIndex = filename.indexOf(this.outbreakRecord.name());
		int hyphenIndex = filename.indexOf('-', obIndex);
		filename = filename.substring(0, hyphenIndex);		
		filename += "-AcuteTransmissions.csv";
		printStr(filename);
		ahiClusterReader = new AcuteClusterReader(filename);
		ahiClusterReader.run();
	}
	
	private void generateInfectionForest() {
		String filename = this.fname;
		infectionForestReader = new InfectionForestReader(filename);
		infectionForestReader.run();
	}

	public void processInfectionForestChains() {
		double deadLeafsAHI = 0, deadLeafsCHI = 0;
		double contLeafsAHI = 0, contLeafsCHI = 0;
		
		for (AgentInterface vertex : infectionForestReader.getVertices()) {
			if (infectionForestReader.isLeaf(vertex)) {
				if (vertex.getInfectedTick() <= Summarizer.threshold) {
					deadEnds.put(new Integer(vertex.getID()), vertex);
					if (vertex.getActType().equals(ActType.Acute_Susceptible))
						deadLeafsAHI++;
					else if (vertex.getActType().equals(ActType.Chronic_Susceptible))
						deadLeafsCHI++;
				}
			} else {
				if (vertex.getInfectedTick() <= Summarizer.threshold
						&& vertex.getActType() != ActType.None) {
					for (AgentInterface successor : infectionForestReader.getSuccessors(vertex)) {
						if (successor.getInfectedTick() > Summarizer.threshold) {
							continuousLeafs.put(new Integer(vertex.getID()),
									vertex);
							if (vertex.getActType().equals(ActType.Acute_Susceptible))
								contLeafsAHI++;
							else if (vertex.getActType().equals(ActType.Chronic_Susceptible))
								contLeafsCHI++;
							break;
						}
					}
				}
			}
		}
		for (Integer ID : continuousLeafs.keySet()) {
			AgentInterface leafNode = continuousLeafs.get(ID);
			pumpUniqueEdges(leafNode, uniqueContinuousEdges, true);
		}
		for (Integer ID : deadEnds.keySet()) {
			AgentInterface leafNode = deadEnds.get(ID);
			pumpUniqueEdges(leafNode, uniqueDeadEdges, false);
		}

		double numChronicDead = 0, numAcuteDead = 0;
		double numChronicContinuous = 0, numAcuteContinuous = 0;

		numChronicDead = uniqueDeadEdges.get(ActType.Chronic_Susceptible).size();
		numAcuteDead = uniqueDeadEdges.get(ActType.Acute_Susceptible).size();
		numChronicContinuous = uniqueContinuousEdges.get(ActType.Chronic_Susceptible).size();
		numAcuteContinuous = uniqueContinuousEdges.get(ActType.Acute_Susceptible).size();

		// double deadFrac = numAcuteDead/(numChronicDead+numAcuteDead);
		// double contFrac = numAcuteCont/(numChronicCont+numAcuteCont);
		// String str1 = deadLeafsAHI + " " + deadLeafsCHI + " " +
		// (deadLeafsAHI/(deadLeafsAHI+deadLeafsCHI)) + " " + contLeafsAHI + " "
		// + contLeafsCHI + " " + (contLeafsAHI/(contLeafsAHI+contLeafsCHI));
		// String str2 = numChronicDead + " " + numAcuteDead + " " + deadFrac +
		// " " + numChronicCont + " " + numAcuteCont + " " + contFrac;
		// System.out.println(str1 + "," + str2);
		
		chainsOutput.add(deadLeafsAHI);
		chainsOutput.add(deadLeafsCHI);
		chainsOutput.add(numAcuteDead);
		chainsOutput.add(numChronicDead);
		chainsOutput.add(numAcuteContinuous);
		chainsOutput.add(numChronicContinuous);
	}

	private void pumpUniqueEdges(AgentInterface leafNode, EnumMap<ActType, Set<Edge>> map, boolean continuous) {		
		boolean rootFound = false;
		AgentInterface vertex = leafNode;
		while (rootFound == false) {
			AgentInterface parentNode = infectionForestReader.getParent(vertex);
			if (infectionForestReader.isRoot(parentNode) || parentNode.getID() == -1) {
				rootFound = true;
			} else {
				Edge edge = infectionForestReader.getParentEdge(vertex);
				Set<Edge> edgeSet = map.get(edge.getTransmission().getActType());
				edgeSet.add(edge);
				if (ahiClusterReader.isRootID(vertex.getID())) {
					if (continuous == true) {
						continuousAHIRoots.add(vertex.getID());
					} else {
						deadAHIRoots.add(vertex.getID());
					}
				}
				vertex = parentNode;
			}
		}
	}

	private void calculateAHIStatistics() {
		TreeOutput treeOutput = new TreeOutput();
		for (Output output : Output.values()) {
			treeOutput.setOutputMapDataArray(output, outputsMap.get(output));
			ArrayList<Double> statistics = treeOutput.calculateStats(output);
			addToOutputArray(statistics);
		}
	}

	protected void addToOutputArray(ArrayList<Double> statistics) {
		this.ahiOutput.addAll(statistics);
	}

	protected void writeStructureData(boolean continuous) {
		HashMap<Integer, AHIDataStructure> map;
		String prefix = "Dead-";
		if (continuous == true) {
			map = continuousClusterMap;
			prefix = "Cont-";
		} else {
			map = deadClusterMap;
		}
		try {
			PrintWriter writer = new PrintWriter(prefix + "LHS_AHIStrcutre.csv");
			for (Integer ID : map.keySet()) {
				AHIDataStructure struc = map.get(ID);
				writer.println(struc.returnOutput());
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
		}
	}

	protected void calculateAcuteOutbreakChains(ChainsType type) {
		int size = 0;
		Set<Integer> rootIDs;
		HashMap<Integer, AHIDataStructure> clusterMap;
		boolean isContinuous;
		switch (type) {
		case All:
			size = ahiClusterReader.getRoots().size();
			rootIDs = ahiClusterReader.getRootIDs();
			clusterMap = continuousClusterMap;
			isContinuous = true;
			break;
		case DeadEnds:
			size = deadAHIRoots.size();
			rootIDs = deadAHIRoots;
			clusterMap = deadClusterMap;
			isContinuous = false;
			break;
		case Continuous:
			size = continuousAHIRoots.size();
			rootIDs = continuousAHIRoots;
			clusterMap = continuousClusterMap;
			isContinuous = true;
			break;
		default:
			System.err.println("Error in Chainstype: " + type);
			return;
		}

		initializeOutputsMap(size);

		for (Integer ahirootID : rootIDs) {
			AHIDataStructure ahiStruc = new AHIDataStructure(ahirootID);
			clusterMap.put(ahirootID, ahiStruc);
			ahiStruc.setObID(returnOBID(ahirootID));
			/* */
			processAHITree(ahirootID, isContinuous);
			/* */
			processChronicLinks(ahirootID, isContinuous);
			/* */
			assignDataToOutputsMap(ahiStruc);
		}
	}

	private void processChronicLinks(Integer vertexID, boolean continuous) {
		AgentInterface vertex = infectionForestReader.getPerson(vertexID);
		boolean rootFound = false;
		double countChronics = 0;
		while (rootFound == false) {
			if (infectionForestReader.isRoot(vertex) || vertex.getID() == -1
					|| vertex.getInfectedTick() == -1
					|| infectionForestReader.getParent(vertex) == null) {
				rootFound = true;
			} else {
				Edge edge = infectionForestReader.getParentEdge(vertex);
				AgentInterface parentNode = infectionForestReader.getSource(edge);
				if (edge != null
						&& edge.getTransmission().getActType().equals(ActType.Acute_Susceptible)) {
					break;
				}
				countChronics++;
				vertex = parentNode;
			}
		}
		if (countChronics > 0) {
			try {
				dataCountChronics.add(countChronics);
			} catch (Exception e) {
				System.err.println("Error in adding count chronics: " + countChronics);
			}
			if (continuous == true) {
				if (continuousClusterMap.containsKey(vertexID)) {
					continuousClusterMap.get(vertexID).addOutput(Output.Chronics, countChronics);
					continuousClusterMap.get(vertexID).setLinkedOBID(returnOBID(vertex.getID()));
				}
			} else {
				if (deadClusterMap.containsKey(vertexID)) {
					deadClusterMap.get(vertexID).addOutput(Output.Chronics, countChronics);
					deadClusterMap.get(vertexID).setLinkedOBID(returnOBID(vertex.getID()));
				}
			}
		}
	}

	private void processAHITree(Integer ahiRootID, boolean continuous) {
		try {
			DelegateTree<AgentInterface, Edge> tree = ahiClusterReader.getAHITree(ahiRootID);
			if (tree == null) {
				System.err.println("Tree is null. " + tree);
			}
			AHIDataStructure ahiStruc;
			if (continuous == true) {
				ahiStruc = continuousClusterMap.get(ahiRootID);
			} else {
				ahiStruc = deadClusterMap.get(ahiRootID);
			}
			double nary = 0;
			double numInternals = 0;
			double numLeaves = 0;
			double children = 0;
			double duration = TreeOutput.returnDuration(tree);
			double height = tree.getHeight();
			double size = tree.getVertexCount() - 1;

			for (AgentInterface vertex : tree.getVertices()) {
				if (tree.isInternal(vertex)) {
					numInternals++;
				} else if (tree.isLeaf(vertex)) {
					numLeaves++;
				}
				int childCount = tree.getChildCount(vertex);
				children += childCount;
				if (childCount > nary) {
					nary = childCount;
				}
			}
			// take the average.
			children /= ((double) tree.getVertexCount());
			// root added
			numInternals++;
			double iratio = numLeaves > 0 ? numInternals / numLeaves : 0;
			double delta = nary > 0 ? height / nary : 0;
			ahiStruc.addOutput(Output.Size, size);
			ahiStruc.addOutput(Output.Duration, duration);
			ahiStruc.addOutput(Output.Height, height);
			ahiStruc.addOutput(Output.Children, children);
			ahiStruc.addOutput(Output.Nary, nary);
			ahiStruc.addOutput(Output.IRatio, iratio);
			ahiStruc.addOutput(Output.Delta, delta);
			
		} catch (Exception e) {
			System.err.println("ahiRoot: " + ahiRootID + " continuous: " + continuous);
			e.printStackTrace();
		}
	}

	public void writeChronicsCountStats() {
		double[] dataArray = new double[dataCountChronics.size()];
		for (int i = 0; i < dataArray.length; i++) {
			dataArray[i] = dataCountChronics.get(i);
		}				
		try {
			double[] propUntil = Stats.returnCumulativeFractions(dataArray, chronicRanges);
			if (propUntil != null) {
				for (double prop : propUntil) {
					chainsOutput.add(prop);
				}				
			}
			else {
				System.err.println("propUntil array is null.");	
			}
		} catch (Exception e) {
			System.err.println("Error in write chronics.");
			e.printStackTrace();			
		}
		
		double average = Stats.returnMean(dataArray);
		double median = Stats.returnMedian(dataArray);
		double skewness = Stats.returnSkewnewss(dataArray);
		double max = Stats.returnMaximum(dataArray);
		double p25 = Stats.returnPercentile(dataArray, 25);
		double p75 = Stats.returnPercentile(dataArray, 75);
		double p90 = Stats.returnPercentile(dataArray, 90);
		double p99 = Stats.returnPercentile(dataArray, 99);

		chainsOutput.add(average);
		chainsOutput.add(median);
		chainsOutput.add(skewness);
		chainsOutput.add(max);
		chainsOutput.add(p25);
		chainsOutput.add(p75);
		chainsOutput.add(p90);
		chainsOutput.add(p99);
	}

	private void initializeOutputsMap(int size) {
		for (Output output : Output.values()) {
			ArrayList<Double> dataArrayList = new ArrayList<Double>();
			outputsMap.put(output, dataArrayList);
		}
	}

	private void assignDataToOutputsMap(AHIDataStructure struc) {
		for (Output output : Output.values()) {
			outputsMap.get(output).add(struc.getOutput(output));
		}
	}
	
	private void printStr(String str) {
		System.out.println(str);
	}

	private int returnOBID(Integer vertexID) {
		return ahiClusterReader.getOBID(vertexID);
	}

	public int getIndex() {
		return index;
	}

	public ArrayList<Double> getAHIOutput() {
		return ahiOutput;
	}

	public ChainsType getChainsType() {
		return chainsType;
	}

	public void setChainsType(ChainsType chainsType) {
		this.chainsType = chainsType;
	}

	public ArrayList<Double> getChainsOutput() {
		return chainsOutput;
	}
}