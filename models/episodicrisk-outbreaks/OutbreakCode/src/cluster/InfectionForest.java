package cluster;

import edu.uci.ics.jung.graph.util.EdgeType;
import episodicriskmodel.EpisodicRiskTransmission;
import episodicriskmodel.EpisodicRiskAgent;

/**
 * 
 * @author shah
 *
 */
public class InfectionForest extends BaseForest {
	private static final long serialVersionUID = 1L;

	public InfectionForest() {
		super();
	}
	
	public InfectionForest(String prefix) {
		super(prefix);
	}
	
	public void addRoot(EpisodicRiskAgent subtreeRoot) {
		Outbreak tree = new Outbreak();
		tree.addVertex(subtreeRoot);
		addTree(tree);
	}

	public void addNode(Integer time, EpisodicRiskTransmission transRec, EpisodicRiskAgent infector, EpisodicRiskAgent infected) {
		if (transRec.getTimeSinceLastInfection() >= OutbreakRecord.NewChainThreshold) {
			addRoot(infected);
			newRoots++;
			return;
		}
		addEdge(new Edge(transRec), infector, infected, EdgeType.DIRECTED);
	}
}