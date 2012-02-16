package cluster;

import interfaces.AgentInterface;

import java.util.LinkedHashMap;

import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * 
 * @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 *
 */
public class OutbreakForest extends BaseForest {
	private static final long serialVersionUID = 1L;
	private LinkedHashMap<Integer, Double> outbreakDurationMap;

	public OutbreakForest(String prefix) {
		super(prefix);
		if (outbreakDurationMap != null) {
			outbreakDurationMap.clear();
			outbreakDurationMap = null;
		}
		outbreakDurationMap = new LinkedHashMap<Integer, Double>();
	}

	public void addObDur(int id, double duration) {
		if (!outbreakDurationMap.containsKey(id)) {
			outbreakDurationMap.put(new Integer(id), new Double(duration));
		}
	}

	public void addRoot(AgentInterface subtreeRoot) {
		Outbreak tree = new Outbreak(subtreeRoot.getAHIClusterID());
		tree.addVertex(subtreeRoot);
		addTree(tree);
	}

	public void addNode(Integer time, BaseTransmission transRec, AgentInterface infector, AgentInterface infected) {
		addEdge(new Edge(transRec), infector, infected, EdgeType.DIRECTED);	
	}

	public LinkedHashMap<Integer, Double> getOutbreakDurationMap() {
		return outbreakDurationMap;
	}

	public double getDuration(int ID) {
		double duration = -1;
		if (outbreakDurationMap.containsKey(ID)) {
			duration = outbreakDurationMap.get(ID).doubleValue();
		}
		return duration;
	}
}