package forest;

import cluster.Edge;
import cluster.Outbreak;
import cluster.Transmission;
import model.Individual;
import model.Parameters;
import edu.uci.ics.jung.graph.util.EdgeType;

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
	
	public void addRoot(Individual subtreeRoot) {
		Outbreak tree = new Outbreak();
		tree.addVertex(subtreeRoot);
		addTree(tree);
	}

	public void addNode(Integer time, Transmission transRec, Individual infector, Individual infected) {
		//Currently, the threshold is 300 years - so the following If block will not be effect
		if (transRec.getTimeSinceLastInfection() >= Parameters.OUTBREAK_RECORD.TRANS_THRESHOLD) {
			addRoot(infected);
			newRoots++;
			return;
		}
		addEdge(new Edge(transRec), infector, infected, EdgeType.DIRECTED);
	}
}