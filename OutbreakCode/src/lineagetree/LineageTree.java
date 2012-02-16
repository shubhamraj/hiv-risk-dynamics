package lineagetree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import interfaces.AgentInterface;
import interfaces.ParametersInterface.LineageEventType;
import cluster.Edge;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Tree;

/**
 *
 * Actually, a lineage tree. 
 * @author shah
 *
 */
public class LineageTree {
	private DelegateTree<AgentInterface, Edge> infectionTree;
	private DelegateForest<LineageVertex, LineageEdge> eventForest;
	private HashMap<Integer, AgentInterface> individualsMap;
	private HashMap<Integer, DelegateTree<LineageVertex, LineageEdge>> eventTreesMap;
	private int totalEvents;
	private AgentInterface baseRootAgent;
	private boolean debug = true;

	public LineageTree(DelegateTree<AgentInterface, Edge> _infectionForest) {
		this.infectionTree = _infectionForest;
		this.eventForest = new DelegateForest<LineageVertex, LineageEdge>();
		this.individualsMap = new HashMap<Integer, AgentInterface>();
		this.eventTreesMap = new HashMap<Integer, DelegateTree<LineageVertex, LineageEdge>>();
		this.totalEvents = 0;

		if (debug == true) {
			System.out.println("Num edges infection forest: " + infectionTree.getEdgeCount());
			System.out.println("Num vertices infection forest: " + infectionTree.getVertexCount());			
		}
	}
	
	public void buildEventForest() {
		setRootEventTree();
		setIndividualEventTrees();

		for (Integer individualID : individualsMap.keySet()) {
			AgentInterface individual = individualsMap.get(individualID);
			Integer infectorID = individual.getInfectorID();
			DelegateTree<LineageVertex, LineageEdge> infectorTree = this.eventTreesMap.get(infectorID);
			for (LineageVertex event : infectorTree.getVertices()) {
				if (event.getLineageEventType().equals(LineageEventType.Transmission) && event.getInfected().getID() == individualID) {
					DelegateTree<LineageVertex, LineageEdge> infectedTree = this.eventTreesMap.get(individualID);
					LineageEdge eventEdge = new LineageEdge(individual.getInfectedTick());					
					eventForest.addEdge(eventEdge, event, infectedTree.getRoot());					
					break;
				}
			}
		}	
		
		if (debug == true) {
			System.out.println("Num individuals: " + individualsMap.values().size());
			System.out.println("Num event trees: " + eventTreesMap.values().size());
			System.out.println("Num event forest roots: " + eventForest.getRoots().size());
			System.out.println("Num trees in forest: " + eventForest.getTrees().size());			
		}
		
		Tree<LineageVertex, LineageEdge> rootTree = null; 
		for (Tree<LineageVertex, LineageEdge> tree : this.eventForest.getTrees()) {
			if (tree.getRoot().getInfector().getID() == -1) {
				rootTree = tree;
				break;
			}
		}

		if (debug == true) {
			System.out.println("-----------------------");
			System.out.println("total events: " + totalEvents);
			System.out.println("size of root tree: " + rootTree.getVertexCount());
			System.out.println("Num individuals: " + individualsMap.values().size());
			System.out.println("Num event trees: " + eventTreesMap.values().size());
			System.out.println("Num event forest roots: " + eventForest.getRoots().size());
			System.out.println("Num trees in forest: " + eventForest.getTrees().size());	
		}
	}
	
	/**
	 * Sets the root of the event tree. This is similar to the dummy root from the infection forest passed to this class.
	 * Where the root in the infection forest (infection tree) is a dummy agent, here it is a dummy lineage.
	 * This method sets the root lineage of the event tree and the transmission events related to the base-root. 
	 */
	private void setRootEventTree() {
		baseRootAgent = infectionTree.getRoot();	
		baseRootAgent.setExitTick(Integer.MAX_VALUE);
		individualsMap.put(baseRootAgent.getID(), baseRootAgent);

		ArrayList<LineageVertex> rootEventsList = new ArrayList<LineageVertex>();						
		for (AgentInterface child : infectionTree.getChildren(baseRootAgent)) {
			LineageVertex event = new LineageVertex(child.getInfectedTick(), baseRootAgent, child);		
			rootEventsList.add(event);
		}	
		LineageVertex rootEvent = new LineageVertex(Integer.MAX_VALUE, baseRootAgent);
		rootEventsList.add(rootEvent);
		sortEvents(rootEventsList);

		DelegateTree<LineageVertex, LineageEdge> rootEventTree = new DelegateTree<LineageVertex, LineageEdge>();
		rootEventTree.setRoot(rootEventsList.get(0));
		LineageVertex parent = rootEventTree.getRoot();
		try {
			for (int i=1; i<rootEventsList.size(); i++) {
				LineageVertex event = rootEventsList.get(i) ;
				LineageEdge eventEdge = new LineageEdge(event.getEventTime());
				rootEventTree.addEdge(eventEdge, parent, event);
				parent = event;
			}				
		} catch (Exception e) {
			e.printStackTrace();
		}
		eventTreesMap.put(baseRootAgent.getID(), rootEventTree);				
		eventForest.addTree(rootEventTree);			

		totalEvents += rootEventTree.getVertexCount();		
		if (debug == true) {
			System.out.println("size of root tree: " + rootEventTree.getVertexCount());	
		}
	}

	private void setIndividualEventTrees() {		
		for (AgentInterface individual : infectionTree.getVertices()) {
			if (individualsMap.containsKey(individual.getID()) == false) {
				individualsMap.put(individual.getID(), individual);
				ArrayList<LineageVertex> eventsList = new ArrayList<LineageVertex>();
				DelegateTree<LineageVertex, LineageEdge> eventTree;
				LineageVertex event;
				for (AgentInterface child : infectionTree.getChildren(individual)) {
					event = new LineageVertex(child.getInfectedTick(), individual, child);
					eventsList.add(event);				
				}
				
				int exitTick = individual.getExitTick() == -1 ? Integer.MAX_VALUE : individual.getExitTick();
				event = new LineageVertex(exitTick, individual);
				eventsList.add(event);

				sortEvents(eventsList);
				
				eventTree = new DelegateTree<LineageVertex, LineageEdge>();
				eventTree.setRoot(eventsList.get(0));
				LineageVertex parent = eventTree.getRoot();				
				try {
					for (int i=1; i<eventsList.size(); i++) {
						LineageVertex myEvent = eventsList.get(i) ;
						LineageEdge timeEdge = new LineageEdge(myEvent.getEventTime());
						eventTree.addEdge(timeEdge, parent, myEvent);
						parent = myEvent;
					}				
				} catch (Exception e) {
					e.printStackTrace();
				}
				eventTreesMap.put(individual.getID(), eventTree);				
				eventForest.addTree(eventTree);
				totalEvents += eventTree.getVertexCount();
			}
		}		
	}

	protected void sortEvents(ArrayList<LineageVertex> eventList) {
		Collections.sort(eventList, new Comparator<LineageVertex>() {
			public int compare(LineageVertex e1, LineageVertex e2) {
				if (e1.getEventTime() > e2.getEventTime()) {
					return 1;
				}
				else if (e1.getEventTime() < e2.getEventTime()) {
					return -1;
				}
				else {
					return 0;	
				}
			}
		});
	}
}