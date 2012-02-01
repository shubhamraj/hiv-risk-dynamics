package cluster;




import interfaces.AgentInteface;
import interfaces.ParametersInterface;

import java.io.IOException;
import java.util.ArrayList;


import edu.uci.ics.jung.graph.DelegateForest;
import episodicriskmodel.EpisodicRiskTransmission;
import episodicriskmodel.Person;

/**
 * This is the forest/tree of the entire infections recorded during the observed period.
 * @author shah
 *
 */
public class BaseForest extends DelegateForest<AgentInteface, Edge> implements ParametersInterface {
	protected static final long serialVersionUID = 1L;
	protected static Person root;
	protected ArrayList<Person> sortedRoots;
	protected int newRoots;
	protected String prefix;
	protected int maxDepth;

	public BaseForest() {
		root = new Person(-1);
		newRoots = 0;
		prefix = "";
		maxDepth = 0;
		sortedRoots = new ArrayList<Person>();
		if (SingleTreeMode) {
			setRoot(root);
		}
	}

	public BaseForest(String prefix){
		this.prefix = prefix;
		root = new Person(-1);
		newRoots = 0;
		prefix = "";
		maxDepth = 0;
		sortedRoots = new ArrayList<Person>();
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

	public ArrayList<Person> getSortedRoots() {
		return sortedRoots;
	}

	public void setSortedRoots(ArrayList<Person> sortedRoots) {
		this.sortedRoots = sortedRoots;
	}
	
	public void addRoot(Person subtreeRoot) {		
	}

	public void addNode(Integer time, EpisodicRiskTransmission transmission,
			Person infector, Person infected) {	
	}
}