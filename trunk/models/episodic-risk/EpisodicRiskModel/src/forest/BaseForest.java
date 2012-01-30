package forest;


import java.io.IOException;
import java.util.ArrayList;

import cluster.Edge;
import cluster.Transmission;

import model.Individual;
import model.Parameters;
import model.Parameters.OUTBREAK_RECORD;

import edu.uci.ics.jung.graph.DelegateForest;

/**
 * This is the forest/tree of the entire infections recorded during the observed period.
 * @author shah
 *
 */
public class BaseForest extends DelegateForest<Individual, Edge>{
	protected static final long serialVersionUID = 1L;
	protected static Individual root;
	protected ArrayList<Individual> sortedRoots;
	protected int newRoots;
	protected String prefix;
	protected int maxDepth;

	public BaseForest() {
		root = new Individual(-1);
		newRoots = 0;
		prefix = "";
		maxDepth = 0;
		sortedRoots = new ArrayList<Individual>();
		if (Parameters.singleTreeMode) {
			setRoot(root);
		}
	}

	public BaseForest(String prefix){
		this.prefix = prefix;
		root = new Individual(-1);
		newRoots = 0;
		prefix = "";
		maxDepth = 0;
		sortedRoots = new ArrayList<Individual>();
		if (Parameters.singleTreeMode) {
			setRoot(root);
		}
	}

	public void save(OUTBREAK_RECORD outbreakRecord) throws IOException {
		ForestWriter forestWriter = new ForestWriter(this, outbreakRecord);
		forestWriter.save();
	}
		
	public int getTreesCount() {
		return getTrees().size();
	}

	public ArrayList<Individual> getSortedRoots() {
		return sortedRoots;
	}

	public void setSortedRoots(ArrayList<Individual> sortedRoots) {
		this.sortedRoots = sortedRoots;
	}
	
	public void addRoot(Individual subtreeRoot) {		
	}

	public void addNode(Integer time, Transmission transmission,
			Individual infector, Individual infected) {	
	}
}