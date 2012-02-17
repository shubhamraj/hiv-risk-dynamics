package cluster;

import interfaces.AgentInterface;
import edu.uci.ics.jung.graph.DelegateTree;

/**
 * 
 * @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 *
 */
public class Outbreak extends DelegateTree<AgentInterface, Edge>{
	private static final long serialVersionUID = 1L;
	private int obID;
	
	public Outbreak() {
		obID = -1;
	}
	
	public Outbreak(int _obID) {
		this.obID = _obID;
	}

	public int getObID() {
		return obID;
	}

	public void setObID(int obID) {
		this.obID = obID;
	}
}