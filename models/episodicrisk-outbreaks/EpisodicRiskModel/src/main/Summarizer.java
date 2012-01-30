package main;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeMap;

import reader.OutbreakStatsReader;
import reader.ReaderEngine;
import reader.ReaderIncidence;
import reader.ReaderPlfit;
import reader.ReaderPopulation;

import model.Parameters;
import model.Parameters.OUTBREAK_RECORD;

/**
 * 
 * @author shah
 *
 */
public class Summarizer extends Parameters {
	public static final int paramSize = 3;	
	public static OUTBREAK_RECORD outbreakRecord = OUTBREAK_RECORD.ENDEMIC;
	
	public Summarizer(OUTBREAK_RECORD _outbreakRecord) {
		outbreakRecord = _outbreakRecord;
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
			PrintWriter writer = new PrintWriter(new File(populationFilename));
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
			PrintWriter writer = new PrintWriter(incidenceFilename);
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
			PrintWriter writer = new PrintWriter(new File(plfitFilename));
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
			PrintWriter writer = new PrintWriter(new File(outbreakDataFilename));
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
	
	public void readTreeStatistics() {
		String path = allRunsPath;
		int startParam = 2;
		int endParam = 3;
		TreeMap<Integer, String> aggregate = new TreeMap<Integer, String>();
		ReaderEngine engine = new ReaderEngine(path);		
		printStr("Tree statistics data outputs. Processing...");
		for (int index=startParam; index<=endParam; index++) {
			System.out.println(index);
			engine.run(index);
			aggregate.put(new Integer(index), engine.returnOutput());
			engine.clear();
		}		
		try {
			PrintWriter writer = new PrintWriter(new File(treeStatsFilename));
			for (Integer key : aggregate.keySet()) {
				String str = aggregate.get(key);
				/*System.out.println(key + "," + str);*/
				writer.println(key + "," + str);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {e.printStackTrace();}			

	}
	
	private int returnFileIndex(String fname) {
		int start = fname.indexOf('-');
		fname.substring(0, start);
		return Integer.parseInt(fname.substring(0, start));		
	}

	public static void main(String[] args) {
		Summarizer summarizer = new Summarizer(OUTBREAK_RECORD.ENDEMIC);
		summarizer.readerPopulation();
		summarizer.readerIncidence();
		summarizer.readerPlfit();
		summarizer.readTreeStatistics();
	}
}