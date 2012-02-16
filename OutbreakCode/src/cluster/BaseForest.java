package cluster;




import interfaces.AgentInterface;
import interfaces.ParametersInterface;

import java.io.IOException;
import java.util.ArrayList;


import edu.uci.ics.jung.graph.DelegateForest;
import episodicriskmodel.EpisodicRiskAgent;
import episodicriskmodel.EpisodicRiskTransmission;

/**
 * This is the forest/tree of the entire infections recorded during the observed period.
 * @author shah
 *
 */
public class BaseForest extends DelegateForest<AgentInterface, Edge> implements ParametersInterface {
	protected static final long serialVersionUID = 1L;
	protected static EpisodicRiskAgent root;
	protected ArrayList<EpisodicRiskAgent> sortedRoots;
	protected int newRoots;
	protected String prefix;
	protected int maxDepth;

	public BaseForest() {
		root = new EpisodicRiskAgent(-1);
		newRoots = 0;
		prefix = "";
		maxDepth = 0;
		sortedRoots = new ArrayList<EpisodicRiskAgent>();
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
		sortedRoots = new ArrayList<EpisodicRiskAgent>();
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

	public ArrayList<EpisodicRiskAgent> getSortedRoots() {
		return sortedRoots;
	}

	public void setSortedRoots(ArrayList<EpisodicRiskAgent> sortedRoots) {
		this.sortedRoots = sortedRoots;
	}
	
	public void addRoot(EpisodicRiskAgent subtreeRoot) {		
	}

	public void addNode(Integer time, EpisodicRiskTransmission transmission,
			EpisodicRiskAgent infector, EpisodicRiskAgent infected) {	
	}
}