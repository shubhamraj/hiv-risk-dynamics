package cluster;


import interfaces.AgentInterface;
import interfaces.TransmissionInterface;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * 
 * @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
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
	
	public void addRoot(AgentInterface subtreeRoot) {
		Outbreak tree = new Outbreak();
		tree.addVertex(subtreeRoot);
		addTree(tree);
	}

	public void addNode(Integer time, TransmissionInterface transRec, AgentInterface infector, AgentInterface infected) {
		if (transRec.getTimeSinceLastInfection() >= OutbreakRecord.NewChainThreshold) {
			addRoot(infected);
			newRoots++;
			return;
		}
		addEdge(new Edge(transRec), infector, infected, EdgeType.DIRECTED);
	}
}