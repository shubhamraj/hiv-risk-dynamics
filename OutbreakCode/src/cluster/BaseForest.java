package cluster;

import interfaces.AgentInterface;
import interfaces.ParametersInterface;
import interfaces.TransmissionInterface;

import java.io.IOException;
import java.util.ArrayList;


import edu.uci.ics.jung.graph.DelegateForest;
import episodicriskmodel.EpisodicRiskAgent;

/**
 * This is the forest/tree of the entire infections recorded during the observed period.
 * @author shah
 *
 */
public class BaseForest extends DelegateForest<AgentInterface, Edge> implements ParametersInterface {
	protected static final long serialVersionUID = 1L;
	protected static EpisodicRiskAgent root;
	protected ArrayList<AgentInterface> sortedRoots;
	protected int newRoots;
	protected String prefix;
	protected int maxDepth;

	public BaseForest() {
		/* Here an instance of the EpisodicRiskAgent class is created and not the interface. */
		root = new EpisodicRiskAgent(-1);
		newRoots = 0;
		prefix = "";
		maxDepth = 0;
		sortedRoots = new ArrayList<AgentInterface>();
		if (SingleTreeMode) {
			setRoot(root);
		}
	}

	public BaseForest(String prefix){
		this.prefix = prefix;
		root = new EpisodicRiskAgent(-1);
		newRoots = 0;
		prefix = "";
		maxDepth = 0;
		sortedRoots = new ArrayList<AgentInterface>();
		if (SingleTreeMode) {
			setRoot(root);
		}
	}

	public void save(OutbreakRecord outbreakRecord) throws IOException {
		ForestWriter forestWriter = new ForestWriter(this, outbreakRecord);
		forestWriter.save();
	}
		
	public int getTreesCount() {
		return getTrees().size();
	}

	public ArrayList<AgentInterface> getSortedRoots() {
		return sortedRoots;
	}

	public void setSortedRoots(ArrayList<AgentInterface> sortedRoots) {
		this.sortedRoots = sortedRoots;
	}
	
	public void addRoot(AgentInterface subtreeRoot) {		
	}

	public void addNode(Integer time, TransmissionInterface transmission,
			AgentInterface infector, AgentInterface infected) {	
	}
}