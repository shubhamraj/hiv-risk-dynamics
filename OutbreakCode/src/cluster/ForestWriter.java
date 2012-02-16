package cluster;



import interfaces.AgentInterface;
import interfaces.ParametersInterface;

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;




import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Tree;
import episodicriskmodel.EpisodicRiskAgent;

/**
 * 
 * @author shah
 *
 */
public class ForestWriter implements ParametersInterface {
	BaseForest forest;
	int maxDepth;
	String prefix;		
	private int numLeaves;
	private int numInternals;
	private int numHighRiskLeaves;
	private int numHighRiskInternals;
	private int numLowRiskLeaves;
	private int numLowRiskInternals;
	private OutbreakRecord outbrekRecord;
	
	public ForestWriter(BaseForest _forest, OutbreakRecord _outbreakRecord) {
		this.forest = _forest;
		this.outbrekRecord = _outbreakRecord;
		prefix = forest.prefix;
		maxDepth = 0;		
	}
	
	public void save() throws IOException {
		try {
			writeTreeStatistics();			
/*			int rank = (int)(fracLargestTrees * forest.getTreesCount());
			onlyLargestTrees(rank);
			display();*/			
			//savePajekTrans();
			//saveSingleTree(forest, prefix+"-pajek-an-1-all.net");
		} catch (Exception e) {e.printStackTrace();}				
	}

	@SuppressWarnings({ "rawtypes" })
	private void writeTreeStatistics() throws IOException {
		PrintWriter statsWriter = new PrintWriter(new FileWriter(prefix + "-" + outbrekRecord.name() + "-TreeStats.csv"));
		if (maxDepth <= 0) {
			for (Tree tree : forest.getTrees()) {
				if (tree.getHeight() > maxDepth) {
					maxDepth = tree.getHeight();
				}
			}
		}
		
		double[] totTrans = new double[maxDepth+1];
		double[] totPHI = new double[maxDepth+1];
		for (int i=0; i<maxDepth; i++) {
			totTrans[i] = 0;
			totPHI[i] = 0;
		}				 	
						
		String header = "";
		for (TreeStatisticsKey key : TreeStatisticsKey.values()) {
			header += key.name() + ",";
		}		
		for (BalanceStatistics key : BalanceStatistics.values()) {
			header += key.name() + ",";
		}
		statsWriter.println(header);
		
		int obID = -1;
		int numVertices = 0;
		double duration = 0;
		
		for (Tree tree : forest.getTrees()) {
			obID = ((EpisodicRiskAgent)tree.getRoot()).getAHIClusterID();
			double[] balanceStatistics = returnNodesStats((DelegateTree)tree);
			String str = "";
			if (forest instanceof OutbreakForest) {				
				duration = ((OutbreakForest)forest).getDuration(obID);
			}
			numVertices = tree.getVertexCount();
			if (duration != -1) {
				//{ObID, HRI, HRL, LRI, LRL, Internals, Leaves, Height, Vertices, Size, Duration, RatioIL, MeanBar, StdBar, Beta1, Beta2}
				String valStr = "";
				for (TreeStatisticsKey key : TreeStatisticsKey.values()) {					
					valStr = "";
					switch(key) {
					case ObID:
						valStr += obID;
						break;
					case HRI:
						valStr += numHighRiskInternals;
						break;
					case HRL:
						valStr += numHighRiskLeaves;
						break;
					case LRI: 
						valStr += numLowRiskInternals;
						break;
					case LRL: 
						valStr += numLowRiskLeaves;
						break;
					case Internals:
						valStr += numInternals;
						break;
					case Leaves:
						valStr += numLeaves;
						break;
					case Height:
						valStr += tree.getHeight();
						break;
					case Vertices:
						valStr += numVertices;
						break;
					case Size:
						valStr += (numVertices-1);
						break;						
					case Duration:
						valStr += duration;
						break;
					case RatioIL:
						valStr += ((double)numInternals/numLeaves);
						break;
					default: break;						
					}
					str += valStr + ",";
				}
				for (int i=0; i<BalanceStatistics.values().length; i++) {
					str += balanceStatistics[i] + ","; 				
				}					
				statsWriter.println(str);
			}			
		}
		statsWriter.flush();
		statsWriter.close();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private double[] returnNodesStats(DelegateTree tree) {
		numLeaves=0;
		numInternals=0;
		numHighRiskLeaves=0;
		numHighRiskInternals=0;
		numLowRiskLeaves=0;
		numLowRiskInternals=0;

		ArrayList<EpisodicRiskAgent> leaves = new ArrayList<EpisodicRiskAgent>();
		ArrayList<EpisodicRiskAgent> internals = new ArrayList<EpisodicRiskAgent>();
		HashMap<EpisodicRiskAgent, ArrayList<EpisodicRiskAgent>> leafMap = new LinkedHashMap<EpisodicRiskAgent, ArrayList<EpisodicRiskAgent>>();
		double[] stats = new double[BalanceStatistics.values().length];
		EpisodicRiskAgent root = (EpisodicRiskAgent) tree.getRoot();		
		Iterator<EpisodicRiskAgent> itr = tree.getVertices().iterator();
		while (itr.hasNext()) {			
			EpisodicRiskAgent vertex = (EpisodicRiskAgent) itr.next();
			if (forest.isLeaf(vertex)) {
				numLeaves++;
				leaves.add(vertex);
				leafMap.put(vertex, (ArrayList<EpisodicRiskAgent>)tree.getPath(vertex));
				if (vertex.getInfectedRiskState().equals(RiskState.High)) {
					numHighRiskLeaves++;
				}
				else {
					numLowRiskLeaves++;
				}
			}
			else {
				if (vertex.getID() != root.getID()) {
					internals.add(vertex);
				}
				numInternals++;
				if (vertex.getInfectedRiskState().equals(RiskState.High)) {
					numHighRiskInternals++;
				}
				else {
					numLowRiskInternals++;
				}
			}			
		}
		double[] ni = new double[leaves.size()];
		int i=0;
		double nbar = 0;
		double beta2 = 0;
		for (EpisodicRiskAgent leaf : leaves) {
			if (tree.getVertexCount() == 2) {
				ni[i] = 0;
			}
			else {
				ni[i] = leafMap.get(leaf).size()-1;
			}
			nbar += ni[i];
			beta2 += ni[i]/Math.pow(2.0, ni[i]);
			i++;
		}

		nbar /= ((double)leaves.size());		
		stats[BalanceStatistics.MeanBar.ordinal()] = nbar;
		stats[BalanceStatistics.Beta2.ordinal()] = beta2;		
		double stdbar = 0;
		for (i=0; i<ni.length; i++) {
			stdbar += ((nbar-ni[i])*(nbar-ni[i]));			
		}
		stdbar /= ((double)leaves.size());
		stats[BalanceStatistics.StdBar.ordinal()] = stdbar;

		double beta1 = 0;
		//This would be the distance to the most recent common ancestor. use JHK's implementation. 	
		double max = 0;
		double dist = 0;
		double d2 = 0;
		int ipsize = 0;

		//System.out.println("Root: " + ((Person)tree.getRoot()).getID());
		for (EpisodicRiskAgent internal : internals) {
			ArrayList<EpisodicRiskAgent> internalPath = (ArrayList<EpisodicRiskAgent>)tree.getPath(internal);
			//System.out.println("Internal node: " + internal.getID());
			/*printList(internalPath);*/
			ipsize = internalPath.size();
			max = 0; 
			dist = 0;
			for (EpisodicRiskAgent leaf : leaves) {
				//System.out.println("Leaf node: " + leaf.getID());
				//lfsize = leafMap.get(leaf).size();
				/*printList(leafMap.get(leaf));*/
				d2 = 0;
				EpisodicRiskAgent parent = (EpisodicRiskAgent) tree.getParent(leaf);
				if (parent.getID() == internal.getID()) {
					//System.out.println("InternalID:" + internal.getID() + " parentID: " + parent.getID());
					dist = 1;
				}
				else {						
					for (int index=0; index<ipsize; index++) {
						EpisodicRiskAgent p1 = internalPath.get(index);
						if (leafMap.get(leaf).contains(p1)) {					
							d2 = leafMap.get(leaf).indexOf(p1);
							dist = d2 + index;
							//System.out.println("index: " + index);
							//System.out.println("D2: " + d2);
							break;
						}
					}		
				}								 
				if (dist > max) {
					max = dist;
				}
			}
			beta1 += 1/max;
		}
		stats[BalanceStatistics.Beta1.ordinal()] = beta1;

		leaves.clear();
		internals.clear();
		leafMap.clear();
		leaves = null;
		internals = null;
		leafMap = null;
		
		return stats;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected double[] processChainTree(Tree tree) {
		double[] data = new double[2];
		data[0] = 0; data[1] = 0;
		data[0] = tree.getVertexCount();
		Iterator itr = tree.getVertices().iterator();		
		while (itr.hasNext()) {
			EpisodicRiskAgent vertex = (EpisodicRiskAgent) itr.next();
			Iterator itrChld = tree.getChildren(vertex).iterator();
			if (vertex.getActType().equals(ActType.Acute_Susceptible)
					//&& vertex.getInfectedTick() >= Parameters.startRecordTick
			) {
				while (itrChld.hasNext()) {
					EpisodicRiskAgent child = (EpisodicRiskAgent) itrChld.next();
					if (child.getActType().equals(ActType.Acute_Susceptible)) {
						data[1]++;
					}
				}	
			}			
		}
		return data;
	}

	protected void printList(ArrayList<EpisodicRiskAgent> list) {
		String str = "";
		for (EpisodicRiskAgent individual : list) {
			str += individual.getID() + "-> ";
		}
		System.out.println(str);
	}

	public void display() {
/*		JFrame frame = new ForestDisplay(forest, forest.getSortedRoots());
		frame.setTitle(prefix);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);		*/
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void onlyLargestTrees(int rank) {		
		ArrayList<Tree<EpisodicRiskAgent, Edge>> trees = new ArrayList<Tree<EpisodicRiskAgent,Edge>>(forest.getTrees().size()+10);
		Iterator itr = forest.getTrees().iterator();
		while (itr.hasNext()) {
			Tree<EpisodicRiskAgent, Edge> tree = (Tree<EpisodicRiskAgent, Edge>) itr.next();
			trees.add(tree);
		}
		//sort in descending order
		Collections.sort(trees, new Comparator<Tree<EpisodicRiskAgent, Edge>>() {
			public int compare(Tree<EpisodicRiskAgent, Edge> t1, Tree<EpisodicRiskAgent, Edge> t2) {
				return t1.getVertexCount() < t2.getVertexCount() ? +1 
						: (t1.getVertexCount() == t2.getVertexCount()) ? 0 : -1;
			}
		});			 
		for (int index=0; index<trees.size(); index++) {
			if (index < rank) {
				forest.getSortedRoots().add(trees.get(index).getRoot());
				//System.out.println("i: " + index + " tree size: " + trees.get(index).getVertexCount());
			}
			else {
				forest.removeVertex((EpisodicRiskAgent)trees.get(index).getRoot(), true);				
			}
		}			
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void removeSubTrees(int minSize) {
		ArrayList<Tree<EpisodicRiskAgent, Edge>> trees = new ArrayList<Tree<EpisodicRiskAgent,Edge>>();				
		for (Tree tree : forest.getTrees()) {
//			System.out.println("Tree root: " + tree.getRoot().getID() + " and depth "+ tree.getHeight());
			if (tree.getHeight() <= minSize) {
				trees.add(tree);
			}			
		}
		for (Tree<EpisodicRiskAgent, Edge> tree : trees) {
			forest.removeVertex((EpisodicRiskAgent)tree.getRoot(), true);
		}	
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void savePajekTrans() throws IOException {
		String fname = prefix+"-pajek.net";
		int singletons = 0;
		ArrayList<Tree<EpisodicRiskAgent, Edge>> trees = new ArrayList<Tree<EpisodicRiskAgent, Edge>>();		
		maxDepth = 0;
		for (Tree tree : forest.getTrees()) {
			if (tree.getVertexCount() <= 1) {
				trees.add(tree);
				singletons++;
			}
			else {
				if (tree.getHeight() > maxDepth) {
					maxDepth = tree.getHeight();
				}
			}
		}	
		//		System.out.println("number actual trees : " + totalActualTrees);
		//		System.out.println("number singletons : " + singletons);

		if (trees.isEmpty()) {
			System.err.println("Tree is empty");
			System.exit(1);
		}

		for (Tree tree : trees) {
			forest.removeVertex((EpisodicRiskAgent)tree.getRoot(), true);
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fname)));		
		HashMap<Integer, Boolean> vertices = new HashMap<Integer, Boolean>();

		//		System.out.println("graph vertices: " + graph.getVertexCount());
		//		System.out.println("graph edges: " + graph.getEdges().size());
		//		System.out.println("singletons: " + singletons);
		//		System.out.println("new roots: " + newRoots);

		for (AgentInterface individual : forest.getVertices()) {
			if ( true
					//(!(forest.isRoot(individual) && forest.isLeaf(individual)))
					//forest.getChildCount(individual) >= 1
					//&& individual.getInfectedTick() > 1000
			){
				//			if (graph.getInEdges(individual).size() <= 0
				//					&& graph.getOutEdges(individual).size() <= 0) {
				///individuals.add(individual);
				vertices.put(new Integer(individual.getID()), true);
			}
		}

		//		System.out.println("new vertices: " + vertices.size());

		HashMap<Integer, Integer> verticesP = new HashMap<Integer, Integer>();
		int counter = 1;
		writer.write("*Vertices " + vertices.size());
		writer.newLine();
		for (Iterator<Integer> it = vertices.keySet().iterator(); it.hasNext(); ){
			Integer id = it.next();
			writer.write("" + counter + " p-" + id.intValue());
			//			writer.write("" + counter);
			writer.newLine();
			verticesP.put(id, new Integer(counter));
			counter++;
		}

		Collection<Edge> d_set = new HashSet<Edge>();
		boolean directed = forest instanceof DirectedGraph;
		// If it is strictly one or the other, no need to create extra sets
		if (directed) {
			d_set.addAll(forest.getEdges());
		}
		if (!d_set.isEmpty()) {
			writer.write("*Arcs");
			writer.newLine();
		}

		BufferedWriter transWriter = new BufferedWriter(new FileWriter(new File(prefix+"transRec.csv"))); 
		for (Edge e : d_set) {
			if (verticesP.containsKey((e.getTransmission().getInfectorID()))
					&& verticesP.containsKey((e.getTransmission().getInfectedID()))) {
				int source_id = verticesP.get(e.getTransmission().getInfectorID());
				int target_id = verticesP.get(e.getTransmission().getInfectedID()); 
				transWriter.write(source_id + "," + target_id + "," + e.getTransmission().toString());
				transWriter.newLine();
				writer.write(source_id + " " + target_id);
				writer.newLine();				
			}
		}
		transWriter.flush();
		transWriter.close();
		writer.flush();
		writer.close();		
	}

	public void saveSingleTree(DelegateForest<EpisodicRiskAgent,Edge> graph, String fname) throws IOException {
		ArrayList<EpisodicRiskAgent> singletons = new ArrayList<EpisodicRiskAgent>();	
		for (EpisodicRiskAgent vertex : graph.getVertices()) {
			if (graph.isRoot(graph.getParent(vertex)) &&
					(graph.getChildCount(vertex) == 0 || graph.getSuccessorCount(vertex) == 0
							|| graph.getOutEdges(vertex).isEmpty())) {
				singletons.add(vertex);
			}
		}

		for (EpisodicRiskAgent vertex : singletons) {
			graph.removeVertex(vertex);			
		}
		/*		for (Tree<Person, Edge> tree : graph.getTrees()) {
			if (tree.getVertexCount() <= 1) {
				trees.add(tree);
				singletons++;
			}
		}
		for (Tree tree : trees) {
			graph.removeVertex((Person)tree.getRoot(), true);
		}
		 */
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fname)));		
		HashMap<Integer, Boolean> vertices = new HashMap<Integer, Boolean>();

		//		System.out.println("graph vertices: " + graph.getVertexCount());
		//		System.out.println("graph edges: " + graph.getEdges().size());
		//		System.out.println("singletons: " + singletons);
		//		System.out.println("new roots: " + newRoots);

		for (EpisodicRiskAgent individual : graph.getVertices()) {
			if ( true
					//(!(forest.isRoot(individual) && forest.isLeaf(individual)))
					//forest.getChildCount(individual) >= 1
					//&& individual.getInfectedTick() > 1000
			){
				//			if (graph.getInEdges(individual).size() <= 0
				//					&& graph.getOutEdges(individual).size() <= 0) {
				///individuals.add(individual);
				vertices.put(new Integer(individual.getID()), true);
			}
		}

		//System.out.println("new vertices: " + vertices.size());

		HashMap<Integer, Integer> verticesP = new HashMap<Integer, Integer>();
		int counter = 1;
		writer.write("*Vertices " + vertices.size());
		writer.newLine();
		for (Iterator<Integer> it = vertices.keySet().iterator(); it.hasNext(); ){
			Integer id = it.next();
			writer.write("" + counter + " p-" + id.intValue());
			//			writer.write("" + counter);
			writer.newLine();
			verticesP.put(id, new Integer(counter));
			counter++;
		}

		Collection<Edge> d_set = new HashSet<Edge>();
		boolean directed = graph instanceof DirectedGraph;
		// If it is strictly one or the other, no need to create extra sets
		if (directed) {
			d_set.addAll(graph.getEdges());
		}
		if (!d_set.isEmpty()) {
			writer.write("*Arcs");
			writer.newLine();
		}

		BufferedWriter transWriter = new BufferedWriter(new FileWriter(new File(prefix+"transRec.csv"))); 
		for (Edge e : d_set) {
			if (verticesP.containsKey((e.getTransmission().getInfectorID()))
					&& verticesP.containsKey((e.getTransmission().getInfectedID()))) {
				int source_id = verticesP.get(e.getTransmission().getInfectorID());
				int target_id = verticesP.get(e.getTransmission().getInfectedID());
				transWriter.write(source_id + "," + target_id + "," + e.getTransmission().toString());
				transWriter.newLine();
				writer.write(source_id + " " + target_id);
				writer.newLine();				
			}
		}
		transWriter.flush();
		transWriter.close();
		writer.flush();
		writer.close();		
	}
}