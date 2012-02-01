package cluster;


import java.io.FileWriter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import basemodel.AgentInteface;
import basemodel.ParametersInterface;

import plfit.Baek;
import plfit.ExtStats;

import reader.Stats;

import episodicriskmodel.EpisodicRiskTransmission;

/**
 * 
 * @author shah
 *
 */
public class ClusterEngine implements ParametersInterface {
	private OutbreakRecord outbreakRecord;
	private int startRecordTick;
	private int endObserveTick;	
	private Map<Integer, ArrayList<Cluster>> outbreakRecMap;
	private Map<Integer, ArrayList<EpisodicRiskTransmission>> earlyTransmissions;
	private Map<Integer, ArrayList<EpisodicRiskTransmission>> allTransmissions;
	private OutbreakForest outbreakForest;
	private ExtStats extStats;
	
	private int maxRecords;
	private int[] numInfected; 
	private int[] population;
	private int newCases;
	private int newAHICases;
	private int newOutbreakAHICases;

	private OutbreakType outbreakType;
	private boolean started;
	private boolean stopped;
	private String prefix;
	
	private int clusterLastID; 

	private double[][] obIncidence;
	private double[][] outbreakDist;

	public ClusterEngine(String _prefix, OutbreakType _outbreakType, OutbreakRecord _outbreakRecord, int maxIterations) {
		this.prefix = _prefix;
		this.outbreakType = _outbreakType;
		this.outbreakRecord = _outbreakRecord;
		this.startRecordTick = outbreakRecord.getStartRecordTick();
		this.endObserveTick = outbreakRecord.getEndObserveTick();		
		this.clusterLastID = -1;		
		if (outbreakRecMap != null) {
			outbreakRecMap.clear();
			outbreakRecMap = null;
		}
		if (earlyTransmissions != null) {
			earlyTransmissions.clear();
			earlyTransmissions = null;
		}		
		this.started = false;
		this.stopped = false;		
		this.outbreakForest = new OutbreakForest(prefix);		
		this.extStats = new ExtStats();		
		this.earlyTransmissions = Collections.synchronizedMap(new TreeMap<Integer, ArrayList<EpisodicRiskTransmission>>());
		this.allTransmissions = Collections.synchronizedMap(new TreeMap<Integer, ArrayList<EpisodicRiskTransmission>>());		
		this.outbreakRecMap = Collections.synchronizedMap(new HashMap<Integer, ArrayList<Cluster>>());		
		this.maxRecords = maxIterations + 1;				
		this.numInfected = new int[maxRecords];
		this.population = new int[maxRecords];
		this.newCases = 0;
		this.newAHICases = 0;
		this.newOutbreakAHICases = 0;		
		for (int i=0; i<this.maxRecords; i++) {
			ArrayList<Cluster> clusterList = new ArrayList<Cluster>();
			this.outbreakRecMap.put(new Integer(i), clusterList);
			this.numInfected[i] = 0;
			this.population[i] = 0;
		}		
		if (Stats.checkRanges(sizeCategories) == false) {
			System.err.println("Size categories array not properly defined.");
		}		
		if (Stats.checkRanges(durationCategories) == false) {
			System.err.println("Duration categories array not properly defined.");
		}			
		this.obIncidence = new double[sizeCategories.length][durationCategories.length];
		this.outbreakDist = new double[sizeCategories.length][durationCategories.length];		
		for (int i=0; i<sizeCategories.length; i++) {
			for (int j=0; j<durationCategories.length; j++) {
				this.obIncidence[i][j]=0;
				this.outbreakDist[i][j]=0;
			}
		}
	}

	public void addTransmission(Integer time, AgentInteface infector, AgentInteface infected) {
		EpisodicRiskTransmission transmission = new EpisodicRiskTransmission(time, infector, infected);		
		if (outbreakCriteria(time, infector)) {
			if (addToOutbreak(transmission, infector, infected)) {
				int clusterTransmissions = transmission.returnCurrentClusterTransmissions(); 
				int clusterAge = transmission.returnCurrentClusterAge();				
				int row = returnCumulativeCategoriesIndex(Output.Size, clusterTransmissions);
				int col = returnCumulativeCategoriesIndex(Output.Duration, clusterAge);
				obIncidence[row][col]++;						
				newOutbreakAHICases++;
			}
			newAHICases++;
		}
		if (RecordInfectionTree) {
			if (allTransmissions.containsKey(transmission.getTime()) == false) {
				allTransmissions.put(transmission.getTime(), new ArrayList<EpisodicRiskTransmission>());
			}
			allTransmissions.get(transmission.getTime()).add(transmission);
		}
		newCases++;
	}

	private boolean addToOutbreak(EpisodicRiskTransmission transmission, AgentInteface infector, AgentInteface infected) {
		boolean addedTranmission = false;
		Integer time = transmission.getTime();
		Cluster cluster;
		Integer key = new Integer(0);
		if (infector.getAHIClusterID() == -1) {
			if (time <= endObserveTick) {
				cluster = new Cluster(time, ++clusterLastID);
				cluster.step(time, infector, infected);					
				ArrayList<Cluster> clusterList = (ArrayList<Cluster>) outbreakRecMap.get(time);
				clusterList.add(cluster);
				key = cluster.getId();
				transmission.setCurrentClusterTransmissions(cluster.returnTotalTransmissions());
				transmission.setCurrentClusterAge(cluster.returnCurrentAge());
				//creating new cluster
				outbreakForest.addRoot(infector);
				addedTranmission = true;
			}
		}
		else {
			key = (Integer) infector.getAHIClusterID();
			ArrayList<Cluster> clusterList = (ArrayList<Cluster>) outbreakRecMap.get(time);		
			for (int index=clusterList.size()-1; index>=0; index--) {
				cluster = clusterList.get(index);
				if (cluster.getId() == key) {
					cluster.step(time, infected);
					transmission.setCurrentClusterTransmissions(cluster.returnTotalTransmissions());
					transmission.setCurrentClusterAge(cluster.returnCurrentAge());
					addedTranmission = true;
					break;
				}
			}
		}
		ArrayList<EpisodicRiskTransmission> transList = new ArrayList<EpisodicRiskTransmission>();
		if (addedTranmission) {
			if (earlyTransmissions.containsKey(key) == false) {
				earlyTransmissions.put(key, transList);
			}
			else {
				transList = earlyTransmissions.get(key);
			}
			transList.add(transmission);
			outbreakForest.addNode(time, transmission, infector, infected);
		}
		return addedTranmission;
	}

	public void step(Integer time) {
		if (time.intValue() <= 1) {
			return;
		}
		ArrayList<Cluster> clusterList = outbreakRecMap.get(time-1);
		int numClusters = clusterList.size();
		for (Cluster cluster : clusterList) {
			if (cluster.returnMembersSize() > 0) {
				ClusterStructure pair = new ClusterStructure(new Integer(cluster.getId()), cluster.returnMembersSize());
				pair.setTime(time);
				pair.setTimeLastTransmission(cluster.getTimeLastTransmission());
			}
			else {
				numClusters--;
			}
		}
		outbreakRecMap.put(time, clusterList);
		if (numClusters == 0
				&& time > endObserveTick) {
			stopped = true;
		}
	}

	public void removeTimeRecord (Integer time, AgentInteface individual) {
		if (individual.getAHIClusterID() == -1) {
			return;
		}
		ArrayList<Cluster> clusterList = (ArrayList<Cluster>) outbreakRecMap.get(time);
		for (Cluster cluster : clusterList) {
			if (cluster.getId() == individual.getAHIClusterID()) {
				cluster.removeMember(individual);
				break;
			}
		}		
	}

	public boolean outbreakCriteria(Integer time, AgentInteface individual) {
		boolean flag = false; 
		switch (outbreakType) {
		case AHI:
			if (individual.isAHI()) {
				flag = true;
			}
			break;
		case Six_Months: 
			if (time - individual.getInfectedTick() <= 6 * 30) {
				flag = true;
			}
			break;
		case Two_Years:
			if (time - individual.getInfectedTick() <= 2 * 12 * 30) {
				flag = true;
			}
			break;
		//case NONE: default: flag = false; break;
		}
		return !individual.isRemovedAHICluster() && flag;
	}

	public void outputRecord() {
		try {
			writeAcuteTransmissions();
			writeAllTransmissions();
			outbreakForest.save(outbreakRecord);
		} catch (IOException e) {e.printStackTrace();}			
	}
	
	public void saveExtStats(PrintWriter[] plfitWriter) {
		int index = this.outbreakRecord.ordinal();
		Baek baek = new Baek(
				(int) extStats.getM(), 
				(int) extStats.getN(), 
				(int) extStats.getKmax(), 
				(int) extStats.getKmin()
				);
		double[] output = baek.run();
		String str = "";
		for (double val : output) {
			str += val + ",";
		}
		plfitWriter[index].println(str);		 		 
		
/*		PowerLawFit plfit = new PowerLawFit(extStats.getOutbreakSizes());
		output = plfit.run();
		str = "";
		for (double val : output) {
			str += val + ",";
		}
		plfitWriter[index].println(str);*/
	}

	private void writeAllTransmissions() throws IOException {
		PrintWriter transwriter = new PrintWriter(new FileWriter(prefix+"-" + outbreakRecord.name() + "-AllTransmissions.csv"));
		int obIDDummy = -1;
		for (Integer time : allTransmissions.keySet()) {
			for (EpisodicRiskTransmission transRec : allTransmissions.get(time)) {
				transwriter.println(obIDDummy + "," + transRec.toString());
			}
			transwriter.flush();
		}
		transwriter.flush();
		transwriter.close();		
	}
	
	private void writeAcuteTransmissions() throws IOException {
		PrintWriter acuteTransmissionsWriter = new PrintWriter(new FileWriter(prefix+"-" + outbreakRecord.name() + "-AcuteTransmissions.csv"));
		//String header = "ObID, time, InfectorID, InfectorTick, InfectedID, ActType, TimeSinceLastInf, InfectorStg, InfectorState, InfectedState, MixingSite, Branchlength";
		String header = "";
		for (AHIKey key : AHIKey.values()) {
			header += key.name() + ",";
		}
		acuteTransmissionsWriter.println(header);
		String rec = "";
		int totalTrans = 0;
		int lastOB = 0;
		int startTime = 0;
		int endTime = 0;
		int duration = 0;
			
		double maxSize = 0;
		double minSize = Double.MAX_VALUE;
		double totalOutbreaks = 0;
		
		ArrayList<EpisodicRiskTransmission> transmissionsList = new ArrayList<EpisodicRiskTransmission>();
		int numOutbreaks = earlyTransmissions.keySet().size();

		for (int outbreakIndex=0; outbreakIndex<numOutbreaks; outbreakIndex++) {
			if (outbreakIndex == (lastOB + 1)) {
				int c = outbreakIndex - 1;
				if (outbreakIndex < numOutbreaks) {
					outbreakForest.addObDur(c, duration);
					if (maxSize < totalTrans) {
						maxSize = totalTrans;
					}
					if (minSize > totalTrans) {
						minSize = totalTrans;
					}
					totalOutbreaks += totalTrans;
					extStats.getOutbreakSizes().add(new Double(totalTrans));
				}								
				lastOB = (short) outbreakIndex;
				totalTrans = 0;
			}
			transmissionsList = (ArrayList<EpisodicRiskTransmission>)earlyTransmissions.get(outbreakIndex);
			if (transmissionsList != null) {
				totalTrans = transmissionsList.size();
				startTime = transmissionsList.get(0).getTime();
				endTime = transmissionsList.get(totalTrans-1).getTime();
				duration = endTime - startTime;				
				for (EpisodicRiskTransmission transRec : transmissionsList) {
					rec = outbreakIndex + "," + transRec.toString();
					acuteTransmissionsWriter.println(rec);
					if (outbreakIndex%100 == 0) {
						acuteTransmissionsWriter.flush();
					}
				}			
				//pump outbreak distributions array here
				int row = returnCumulativeCategoriesIndex(Output.Size,	totalTrans);
				int col = returnCumulativeCategoriesIndex(Output.Duration, duration);
				outbreakDist[row][col]++;
			}			
			
			extStats.setKmax(maxSize);
			extStats.setKmin(minSize);
			extStats.setM(totalOutbreaks);
			extStats.setN((double) numOutbreaks);
	
		}
		acuteTransmissionsWriter.flush();		
		acuteTransmissionsWriter.close();
		transmissionsList = null;
	}

	public void writeJointDistSummary(int run, PrintWriter[] incidenceWriter, PrintWriter[] outbreakWriter)	
	throws IOException {		
		int index = this.outbreakRecord.ordinal();	
		incidenceWriter[index].println(prefix);
		outbreakWriter[index].println(prefix);
		for (int i=0; i<sizeCategories.length; i++) {
			String str = "", str2 = "";
			for (int j=0; j<durationCategories.length; j++) {
				str += obIncidence[i][j] + ", ";
				str2 += outbreakDist[i][j] + ", ";
			}
			incidenceWriter[index].println(str);
			outbreakWriter[index].println(str2);
		}
		incidenceWriter[index].println("newcases, numAHI, numObAHI, prev");
		outbreakWriter[index].println("newcases, numAHI, numObAHI, prev");
		int arrayIndex = numInfected.length-1;
		double prevalence = ((double)numInfected[arrayIndex])/((double) population[arrayIndex]);
		incidenceWriter[index].println(newCases + ", " + newAHICases + "," + newOutbreakAHICases + ", " + prevalence + "," +((double)newAHICases/newCases));
		outbreakWriter[index].println(newCases + ", " + newAHICases + "," + newOutbreakAHICases + ", " + prevalence + "," +((double)newAHICases/newCases));
		incidenceWriter[index].flush();
		outbreakWriter[index].flush();
	}

	public boolean shouldBeginRecording(Integer time) {
		if (RecordOutbreak
			&& started == false
			&& startRecordTick == time) {
			started = true;
			return true;
		}
		else {
			return false;
		}
	}

	public boolean shouldRecordTransmission(Integer time) {		
		if (RecordOutbreak
			&& started == true
			&& stopped == false
			&& startRecordTick <= time) {
			return true;
		}
		else {
			return false;
		}
	}

	public void setPopulationStats (Integer time, int numHIV, int popsize) {
		numInfected[time] = numHIV;
		population[time] = popsize;
	}

	private int returnCumulativeCategoriesIndex(Output output, int cumulativeTransmissions) {
		int[] categories;
		switch(output) {
		case Size:
			categories = sizeCategories;
			break;
		case Duration:
			categories = durationCategories;
			break;
		default:
			System.err.println("Cumulaitve categories not defined for output: " + output);
			return -1;
		}
		int index = 0;
		for (int i=0; i<categories.length; i++) {
			int category = categories[i];
			if (cumulativeTransmissions <= category) {
				index = i;
				break;
			}
		}
		return index;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public ExtStats getExtStats() {
		return extStats;
	}

	public void setExtStats(ExtStats extStats) {
		this.extStats = extStats;
	}
	
	public OutbreakRecord getOutbreakRecord() {
		return outbreakRecord;
	}

	public boolean isStopped() {
		return stopped;
	}
}