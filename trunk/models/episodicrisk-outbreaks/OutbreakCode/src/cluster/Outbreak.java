package cluster;

import interfaces.AgentInteface;
import edu.uci.ics.jung.graph.DelegateTree;

/**
 * 
 * @author shah
 *
 */
public class Outbreak extends DelegateTree<AgentInteface, Edge>{
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