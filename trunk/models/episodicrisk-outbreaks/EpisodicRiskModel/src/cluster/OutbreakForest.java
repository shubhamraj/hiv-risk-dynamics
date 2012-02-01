package cluster;

import java.util.LinkedHashMap;

import basemodel.AgentInteface;




import edu.uci.ics.jung.graph.util.EdgeType;
import episodicriskmodel.EpisodicRiskTransmission;

/**
 * 
 * @author shah
 *
 */
public class OutbreakForest extends BaseForest {
	private static final long serialVersionUID = 1L;
	private LinkedHashMap<Integer, Double> obDur;

	public OutbreakForest(String prefix) {
		super(prefix);
		if (obDur != null) {
			obDur.clear();
			obDur = null;
		}
		obDur = new LinkedHashMap<Integer, Double>();
	}

	public void addObDur(int id, double duration) {
		if (!obDur.containsKey(id)) {
			obDur.put(new Integer(id), new Double(duration));
		}
	}

	public void addRoot(AgentInteface subtreeRoot) {
		Outbreak tree = new Outbreak(subtreeRoot.getAHIClusterID());
		tree.addVertex(subtreeRoot);
		addTree(tree);
	}

	public void addNode(Integer time, EpisodicRiskTransmission transRec, AgentInteface infector, AgentInteface infected) {
		addEdge(new Edge(transRec), infector, infected, EdgeType.DIRECTED);	
	}

	public LinkedHashMap<Integer, Double> getObDur() {
		return obDur;
	}

	public double getDuration(int ID) {
		double duration = -1;
		if (obDur.containsKey(ID)) {
			duration = obDur.get(ID).doubleValue();
		}
		return duration;
	}
}