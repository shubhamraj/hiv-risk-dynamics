package main;


import interfaces.ParametersInterface;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeMap;


import reader.AHICHILink;
import reader.OutbreakStatsReader;
import reader.ReaderEngine;
import reader.ReaderIncidence;
import reader.ReaderPlfit;
import reader.ReaderPopulation;


/**
 * 
 * @author shah
 *
 */
public class Summarizer implements ParametersInterface {	
	public static OutbreakRecord outbreakRecord = OutbreakRecord.Endemic;
	/** Threshold used by the {@link AHICHILink} class. More details to follow. */
	public static final int threshold = 100000;
	private String prefix = "";
	private int paramSize = 1;
	
	public void setOutbreakRecord(OutbreakRecord _outbreakRecord, int _paramSize) {
		outbreakRecord = _outbreakRecord;
		this.prefix = outputPath + outbreakRecord.name() + "-";
		this.paramSize = _paramSize;
	}

	public void readerPopulation() {
		File directory = new File(aggregatePath);
		File[] csvFiles = directory.listFiles(new FilenameFilter() {			
			@Override 
			public boolean accept(File dir, String name) {
				return name.endsWith("PopulationStats.csv");
			}
		});		
		TreeMap<Integer, String> output = new TreeMap<Integer, String>();
		printStr("Population-level outputs. Processing...");
		for (int i=0; i<csvFiles.length; i++) {
			ReaderPopulation reader = new ReaderPopulation();
			int index = returnFileIndex(csvFiles[i].getName());
			output.put(index, reader.processGeneral(csvFiles[i]));				
		}
		try {
			PrintWriter writer = new PrintWriter(new File(prefix + populationFilename));
			for (Integer key : output.keySet()) {
				writer.println(key + "," + output.get(key));
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {}
	}

	public void readerIncidence() {
		File directory = new File(aggregatePath);
		File[] csvFiles = directory.listFiles(new FilenameFilter() {			
			@Override 
			public boolean accept(File dir, String name) {
				return name.endsWith(outbreakRecord.name() + ".csv")
						&& name.indexOf("Outbreak") > 0;
			}
		});
		TreeMap<Integer, String> output = new TreeMap<Integer, String>();
		printStr("Outbreaks joint distribution outputs. Processing...");
		for (int i=0; i<csvFiles.length; i++) {
			ReaderIncidence reader = new ReaderIncidence();
			int index = returnFileIndex(csvFiles[i].getName());
			String str = reader.processIncidence(csvFiles[i]);
			output.put(index, str);				
		}
		try {
			PrintWriter writer = new PrintWriter(prefix + incidenceFilename);
			for (Integer key : output.keySet()) {
				writer.println(key + "," + output.get(key));
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {}
	}

	public void readerPlfit() {
		File directory = new File(aggregatePath);
		File[] csvFiles = directory.listFiles(new FilenameFilter() {			
			@Override 
			public boolean accept(File dir, String name) {
				return name.endsWith(outbreakRecord.name() + ".csv")
				&& name.indexOf("Plfit") > 0;
			}
		});
		TreeMap<Integer, String> output = new TreeMap<Integer, String>();
		printStr("Plfit aggregate outputs. Processing...");
		for (int i=0; i<csvFiles.length; i++) {
			ReaderPlfit plfitReader = new ReaderPlfit();
			int index = returnFileIndex(csvFiles[i].getName());
			output.put(index, plfitReader.processGeneral(csvFiles[i]));
		}
		try {
			PrintWriter writer = new PrintWriter(new File(prefix + plfitFilename));
			for (Integer key : output.keySet()) {
				writer.println(key + "," + output.get(key));
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {}
	}

	public void readOutbreaksFromTreeStats() {
		TreeMap<Integer, ArrayList<Double>> aggregate = new TreeMap<Integer, ArrayList<Double>>();		
		for (int index=1; index<=paramSize; index++) {
			OutbreakStatsReader obReader = new OutbreakStatsReader(allRunsPath, index);
			if (obReader.isFileFound()) {
				obReader.run();
			}
			aggregate.put(new Integer(index), obReader.getOutput());
		}
		try {
			PrintWriter writer = new PrintWriter(new File(prefix + outbreakDataFilename));
			for (Integer key : aggregate.keySet()) {
				String str = "";
				for (Double dbl : aggregate.get(key)) {
					str += dbl + ",";
				}
				writer.println(key + "," + str);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {}
	}
	
	public void readTreeStatistics(int startParam, int endParam) {
		String path = allRunsPath;
		TreeMap<Integer, String> aggregate = new TreeMap<Integer, String>();
		ReaderEngine engine = new ReaderEngine(path, outbreakRecord);		
		printStr("Tree statistics data outputs. Processing...");
		for (int index=startParam; index<=endParam; index++) {
			System.out.println(index);
			engine.run(index);
			aggregate.put(new Integer(index), engine.returnOutput());
			engine.clear();
		}		
/*		try {
			PrintWriter writer = new PrintWriter(new File(prefix + treeStatsFilename));
			for (Integer key : aggregate.keySet()) {
				String str = aggregate.get(key);
				System.out.println(key + "," + str);
				writer.println(key + "," + str);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {e.printStackTrace();}			
*/
	}
	
	private int returnFileIndex(String fname) {
		int start = fname.indexOf('-');
		fname.substring(0, start);
		return Integer.parseInt(fname.substring(0, start));		
	}
	
	private void printStr(String str) {
		System.out.println(str);
	}

	public static void main(String[] args) {
		Summarizer summarizer = new Summarizer();
		int paramSize = 3;
		for (OutbreakRecord outbreakRecord : OutbreakRecord.values()) {
			summarizer.setOutbreakRecord(outbreakRecord, paramSize);
//			summarizer.readerPopulation();
//			summarizer.readerIncidence();
//			summarizer.readerPlfit();
			summarizer.readTreeStatistics(4, 4);
			break;
		}
	}
}