package reader;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.StringTokenizer;

import model.Individual;
import model.Parameters;

import cluster.Edge;
import cluster.Transmission;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Tree;
import forest.InfectionForest;

/**
 * 
 * @author shah
 *
 */
public class AcuteClusterReader extends Parameters {	
	private String fname;
	private int lineNo;
	private LinkedHashMap<Integer, ArrayList<String>> inputMap;
	private LinkedHashMap<Integer, ArrayList<Transmission>> obtrans;
	private HashMap<Integer, Individual> individuals;
	private ArrayList<Individual> roots;
	private Set<Integer> rootIDs; 
	private InfectionForest ahiForest;
	private HashMap<Integer, DelegateTree<Individual, Edge>> ahiTrees;
	private OUTBREAK_RECORD outbreakRecord;

	public AcuteClusterReader(String _fname) {
		this.fname = _fname;
		inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
		obtrans = new LinkedHashMap<Integer, ArrayList<Transmission>>();
		individuals = new HashMap<Integer, Individual>();
		ahiForest = new InfectionForest(fname);
		roots = new ArrayList<Individual>();
		rootIDs = new HashSet<Integer>(); 
		lineNo = 0;
		ahiTrees = new HashMap<Integer, DelegateTree<Individual, Edge>>();
	
		for (OUTBREAK_RECORD obr : OUTBREAK_RECORD.values()) {
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
			if (obtrans.containsKey(obID) == false) {
				ArrayList<Transmission> tranList = new ArrayList<Transmission>();
				obtrans.put(obID, tranList);
				flag = true;
			}
			ArrayList<Transmission> transList = obtrans.get(obID);
			Transmission transmission = new Transmission();
			int time = Integer.parseInt(tokens.get(AHIKey.Time.ordinal()).trim());

			int infectorID = Integer.parseInt(tokens.get(AHIKey.InfectorID.ordinal()).trim());
			int infectorTick = Integer.parseInt(tokens.get(AHIKey.InfectorTick.ordinal()).trim());			
			int infectedID = Integer.parseInt(tokens.get(AHIKey.InfectedID.ordinal()).trim());

			String act = tokens.get(AHIKey.ActType.ordinal()).trim();
			ACT_TYPE actType = ACT_TYPE.NONE;
			if (act.equals("AHI")) {
				actType = ACT_TYPE.AHI;
			}
			else {
				actType = ACT_TYPE.CHI;
			}

			int timeSinceLastInfection = Integer.parseInt(tokens.get(AHIKey.TimeSinceLastInf.ordinal()).trim());

			String strInfectorStage = tokens.get(AHIKey.InfectorState.ordinal()).trim();			
			STAGE infectorStage;			
			if (strInfectorStage.equals("AHI")) {
				infectorStage = STAGE.ACUTE;
			}
			else {
				infectorStage = STAGE.CHRONIC;
			}			
			RISK_STATE infectorState, infectedState;

			String strInfectorState = tokens.get(AHIKey.InfectorState.ordinal()).trim();
			if (strInfectorState.equals("HIGH")) {
				infectorState = RISK_STATE.HIGH;
			}
			else {
				infectorState = RISK_STATE.LOW;
			}
			String strInfectedState = tokens.get(AHIKey.InfectedState.ordinal()).trim();
			if (strInfectedState.equals("HIGH")) {
				infectedState = RISK_STATE.HIGH;
			}
			else {
				infectedState = RISK_STATE.LOW;
			}

			MIXING_SITE mixingSite = MIXING_SITE.NONE;
			String site = tokens.get(AHIKey.MixingSite.ordinal()).trim();
			if (site.equals("HIGH_RISK")) {
				mixingSite = MIXING_SITE.HIGH_RISK;
			}
			else {
				mixingSite = MIXING_SITE.COMMON;
			}

			int branchTime = Integer.parseInt(tokens.get(AHIKey.BranchLength.ordinal()).trim());
			transmission.setTime(time);
			transmission.setInfectorID(infectorID);
			transmission.setInfectedID(infectedID);
			transmission.setTimeSinceLastInfection(timeSinceLastInfection);
			transmission.setActType(actType);
			transmission.setInfectorStage(infectorStage);
			transmission.setInfectorRiskState(infectorState);
			transmission.setInfectedRiskState(infectedState);
			transmission.setMixingSite(mixingSite);
			transmission.setBranchTime(branchTime);

			if (individuals.containsKey(infectorID) == false) {
				Individual infector = new Individual(infectorID);
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
				Individual infected = new Individual(infectedID);
				infected.setActType(actType);
				infected.setInfectedMixingSite(mixingSite);
				infected.setInfectedRiskState(infectedState);
				infected.setInfectorID(infectorID);
				infected.setInfectorStatus(infectorStage);
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
		for (Integer key : obtrans.keySet()) { 
			ArrayList<Transmission> transList = obtrans.get(key);
			Collections.sort(transList, new Comparator<Transmission>() {
				public int compare(Transmission t1, Transmission t2) {
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
			for (Transmission transmission : transList) {
				Individual infector = individuals.get(new Integer(transmission.getInfectorID()));
				Individual infected = individuals.get(new Integer(transmission.getInfectedID()));
				ahiForest.addNode(new Integer(transmission.getTime()), transmission, infector, infected);
			}
		}

		for (Tree<Individual, Edge> tree : ahiForest.getTrees()) {
			Individual root = (Individual) tree.getRoot();
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
		for (Individual root : roots) {
			ahiForest.addRoot(root);
			root.setRoot(true);
		}
	}

	public int getOBID(Integer vertexID) {
		if (individuals.containsKey(vertexID)) {
			Individual vertex = individuals.get(vertexID);
			return vertex.getAHIClusterID();					
		}
		return -1;
	}

	public boolean isRootID (Integer ID) {
		if (individuals.containsKey(ID)) {
			Individual individual = individuals.get(ID);
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

	public static void main(String[] args) {
		File directory = new File("./early/");
		File[] csvFiles = directory.listFiles(new FilenameFilter() {			
			@Override 
			public boolean accept(File dir, String name) {
				return name.endsWith("earlyTransmissions.csv");
			}
		});
		for (int i=0; i<csvFiles.length; i++) {
			System.out.println(directory+csvFiles[i].getName());
			AcuteClusterReader acuteClusterReader = new AcuteClusterReader(csvFiles[i].getName());
			acuteClusterReader.run();
		}
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
	
	public ArrayList<Individual> getRoots() {
		return roots;
	}

	public InfectionForest getAhiForest() {
		return ahiForest;
	}

	public DelegateTree<Individual, Edge> getAHITree(Integer rootID) {
		return getAhiTrees().get(rootID);	
	}
	
	public HashMap<Integer, DelegateTree<Individual, Edge>> getAhiTrees() {
		return ahiTrees;
	}
}