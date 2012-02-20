package lineagetree;

import edu.uci.ics.jung.graph.DelegateTree;
import reader.InfectionForestReader;

/**
 * 
 *  @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 *
 */
public class LineageTreeTester {
	String fname;
	InfectionForestReader infectionForestReader;
	LineageTree lineageTree;
	public LineageTreeTester(String fname) {
		this.fname = fname;
		this.infectionForestReader = new InfectionForestReader(this.fname);
		infectionForestReader.run();
		/*Builds the lineage tree */
		this.lineageTree = new LineageTree(infectionForestReader.getInfectionForest());
	}
	
	public DelegateTree<LineageVertex, LineageEdge> returnLineageTree() {
		return this.lineageTree.returnLineageTree();
	}
	
	public static void main(String[] args) {
		try {
			LineageTreeTester loader = new LineageTreeTester("./data/allruns/2-0-EarlyPeriod-AllTransmissions.csv");
			DelegateTree<LineageVertex, LineageEdge> lineageTree = loader.returnLineageTree();
			System.out.println(lineageTree.getVertexCount());
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
}


