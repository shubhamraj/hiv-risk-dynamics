package reader;

import interfaces.AgentInterface;
import interfaces.ParametersInterface;
import interfaces.TransmissionInterface;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.StringTokenizer;

import cluster.Edge;
import cluster.InfectionForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Tree;
import episodicriskmodel.EpisodicRiskAgent;
import episodicriskmodel.EpisodicRiskTransmission;

/**
 * 
 * @author shah
 *
 */
public class AcuteClusterReader implements ParametersInterface {	
	private String fname;
	private int lineNo;
	private LinkedHashMap<Integer, ArrayList<String>> inputMap;
	private LinkedHashMap<Integer, ArrayList<TransmissionInterface>> outbreakTransmissions;
	private HashMap<Integer, AgentInterface> individuals;
	private ArrayList<AgentInterface> roots;
	private Set<Integer> rootIDs; 
	private InfectionForest ahiForest;
	private HashMap<Integer, DelegateTree<AgentInterface, Edge>> ahiTrees;
	private OutbreakRecord outbreakRecord;

	public AcuteClusterReader(String _fname) {
		this.fname = _fname;
		inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
		outbreakTransmissions = new LinkedHashMap<Integer, ArrayList<TransmissionInterface>>();
		individuals = new HashMap<Integer, AgentInterface>();
		ahiForest = new InfectionForest(fname);
		roots = new ArrayList<AgentInterface>();
		rootIDs = new HashSet<Integer>(); 
		lineNo = 0;
		ahiTrees = new HashMap<Integer, DelegateTree<AgentInterface, Edge>>();
	
		for (OutbreakRecord obr : OutbreakRecord.values()) {
			if (fname.contains(obr.name())) {
				this.outbreakRecord = obr;
			}
		}
	}

	public void processInput() {
		boolean flag = false;
		for (Integer key : inputMap.keySet()) {
			ArrayList<String> tokens = (ArrayList<String>) inputMap.get(key);
			Integer obID = Integer.parseInt(tokens.get(AHIKey.OBID.ordinal())); 
			if (outbreakTransmissions.containsKey(obID) == false) {
				ArrayList<TransmissionInterface> tranList = new ArrayList<TransmissionInterface>();
				outbreakTransmissions.put(obID, tranList);
				flag = true;
			}
			ArrayList<TransmissionInterface> transList = outbreakTransmissions.get(obID);
			
			/* Create an instance for the EpisodicRiskTransmission class instead of the interface */
			EpisodicRiskTransmission transmission = new EpisodicRiskTransmission();
			int time = Integer.parseInt(tokens.get(AHIKey.Time.ordinal()).trim());
			int infectorID = Integer.parseInt(tokens.get(AHIKey.InfectorID.ordinal()).trim());
			int infectorTick = Integer.parseInt(tokens.get(AHIKey.InfectorTick.ordinal()).trim());			
			int infectedID = Integer.parseInt(tokens.get(AHIKey.InfectedID.ordinal()).trim());
			ActType actType = ActType.valueOf(tokens.get(AHIKey.ActType.ordinal()).trim());	
			int timeSinceLastInfection = Integer.parseInt(tokens.get(AHIKey.TimeSinceLastInf.ordinal()).trim());					
			InfectionStage infectorStageOfInfection = InfectionStage.valueOf(tokens.get(AHIKey.InfectorStageOfInfection.ordinal()).trim());						
			RiskState infectorRiskState, infectedRiskState;
			infectorRiskState = RiskState.valueOf(tokens.get(AHIKey.InfectorRiskState.ordinal()).trim());
			infectedRiskState = RiskState.valueOf(tokens.get(AHIKey.InfectedRiskState.ordinal()).trim());		
			MixingSite mixingSite = MixingSite.valueOf(tokens.get(AHIKey.MixingSite.ordinal()).trim());
			int branchTime = Integer.parseInt(tokens.get(AHIKey.BranchLength.ordinal()).trim());
			
			transmission.setTime(time);
			transmission.setInfectorID(infectorID);
			transmission.setInfectedID(infectedID);
			transmission.setTimeSinceLastInfection(timeSinceLastInfection);
			transmission.setActType(actType);
			transmission.setInfectorStage(infectorStageOfInfection);
			transmission.setInfectorRiskState(infectorRiskState);
			transmission.setInfectedRiskState(infectedRiskState);
			transmission.setMixingSite(mixingSite);
			transmission.setBranchTime(branchTime);

			if (individuals.containsKey(infectorID) == false) {
				/* Create an instance for the EpisodicRiskAgent class instead of the interface */
				EpisodicRiskAgent infector = new EpisodicRiskAgent(infectorID);
				infector.setInfectedTick(infectorTick);
				infector.setAHIClusterID(obID);
				individuals.put(new Integer(infectorID), infector);

				if (flag == true) {
					roots.add(infector);
					rootIDs.add(infector.getID());
					flag = false;
				} 
			}
			if (individuals.containsKey(infectedID) == false) {
				/* Create an instance for the EpisodicRiskAgent class instead of the interface */
				EpisodicRiskAgent infected = new EpisodicRiskAgent(infectedID);
				infected.setActType(actType);
				infected.setInfectedMixingSite(mixingSite);
				infected.setInfectedRiskState(infectedRiskState);
				infected.setInfectorID(infectorID);
				infected.setInfectorStageOfInfection(infectorStageOfInfection);
				infected.setInfectedTick(time);
				infected.setAHIClusterID(obID);
				individuals.put(new Integer(infectedID), infected);
			}			
			/*			System.out.println(obID + "," + time + "," + infectorID + ","
					+ infectedID + "," + actType + "," + timeSinceLastInfection  
					+ "," + infectorStage + "," + infectorState + "," + infectedState);

			 */
			transList.add(transmission);
		}		
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void generateForest() {
		for (Integer key : outbreakTransmissions.keySet()) { 
			ArrayList<TransmissionInterface> transList = outbreakTransmissions.get(key);
			Collections.sort(transList, new Comparator<TransmissionInterface>() {
				public int compare(TransmissionInterface t1, TransmissionInterface t2) {
					if (t1.getTime() > t2.getTime()) {
						return 1;
					}
					else if (t1.getTime() < t2.getTime()) {
						return -1;
					}
					else {
						return 0;	
					}
				}
			});
			for (TransmissionInterface transmission : transList) {
				AgentInterface infector = individuals.get(new Integer(transmission.getInfectorID()));
				AgentInterface infected = individuals.get(new Integer(transmission.getInfectedID()));
				ahiForest.addNode(new Integer(transmission.getTime()), transmission, infector, infected);
			}
		}

		for (Tree tree : ahiForest.getTrees()) {
			AgentInterface root = (AgentInterface) tree.getRoot();
			ahiTrees.put(root.getID(), (DelegateTree) tree);
		}		
	}

	public HashMap<Integer, ArrayList<String>> read() {
		try {
			BufferedReader bufRdr = new BufferedReader(new FileReader(fname));			
			String line = null;					
			while ((line = bufRdr.readLine()) != null) {
				lineNo++;
				if (lineNo > 1) {
					StringTokenizer st = new StringTokenizer(line,",");
					ArrayList<String> tokens = new ArrayList<String>(); 
					while (st.hasMoreTokens()) {
						tokens.add(st.nextToken());
					}
					inputMap.put(new Integer(lineNo), tokens);
				}				
			}
			bufRdr.close();
		} catch (IOException e) {e.printStackTrace();}
		return inputMap;
	}

	//Work around for now ... 
	public void saveForest() {
		try {
			ahiForest.save(outbreakRecord);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initializeRoots() {
		for (AgentInterface root : roots) {
			ahiForest.addRoot(root);
			root.setRoot(true);
		}
	}

	public int getOBID(Integer vertexID) {
		if (individuals.containsKey(vertexID)) {
			AgentInterface individualVertex = individuals.get(vertexID);
			return individualVertex.getAHIClusterID();					
		}
		return -1;
	}

	public boolean isRootID (Integer ID) {
		if (individuals.containsKey(ID)) {
			AgentInterface individual = individuals.get(ID);
			return individual.isRoot();
		}
		else {
			return false; 
		}
	}

	public void run() {
		read();
		processInput();
		initializeRoots();
		generateForest();
		//saveForest();
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public Set<Integer> getRootIDs() {
		return rootIDs;
	}
	
	public ArrayList<AgentInterface> getRoots() {
		return roots;
	}

	public InfectionForest getAhiForest() {
		return ahiForest;
	}

	public DelegateTree<AgentInterface, Edge> getAHITree(Integer rootID) {
		return getAhiTrees().get(rootID);	
	}
	
	public HashMap<Integer, DelegateTree<AgentInterface, Edge>> getAhiTrees() {
		return ahiTrees;
	}
}