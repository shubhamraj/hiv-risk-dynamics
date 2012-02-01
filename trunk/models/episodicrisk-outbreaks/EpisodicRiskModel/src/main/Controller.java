package main;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import basemodel.Parameters;

import episodicriskmodel.EpisodicModel;


/**
 * 
 * @author shah
 *
 */
public class Controller extends Parameters {
	public static void main(String[] args) throws IOException {
		String aggregatePath = Parameters.aggregatePath;
		String allRunsPath = Parameters.allRunsPath;
		OUTBREAK_TYPE outbreakType = OUTBREAK_TYPE.AHI;
		int maxRuns = 3;		
		int startParam = 2;
		int endParam = 3;
		
		for (int index=startParam; index<=endParam; index++) {
			int lineNo = 1;
			ArrayList<String> tokens = new ArrayList<String>();
			try {			
				BufferedReader bufRdr = new BufferedReader(new FileReader(Parameters.inputPath+Parameters.inputFile));
				String line = null;					
				while ((line = bufRdr.readLine()) != null) {
					if (lineNo == index) {
						StringTokenizer st = new StringTokenizer(line,",");
						tokens = new ArrayList<String>(); 
						while (st.hasMoreTokens()) {
							tokens.add(st.nextToken());
						}
						break;
					}
					lineNo++;
				}
				bufRdr.close();
			} catch (IOException e) {e.printStackTrace();}

			double[] input = new double[tokens.size()];
			for (int i=0; i<tokens.size(); i++) {
				input[i] = Double.parseDouble(tokens.get(i).trim());
			}

			PrintWriter populationWriter;			
			PrintWriter[] incidenceWriter = new PrintWriter[OUTBREAK_RECORD.values().length];
			PrintWriter[] outbreakWriter = new PrintWriter[OUTBREAK_RECORD.values().length];
			PrintWriter[] plfitWriter = new PrintWriter[OUTBREAK_RECORD.values().length];			
			for (OUTBREAK_RECORD obRecord : OUTBREAK_RECORD.values()) {
				incidenceWriter[obRecord.ordinal()] = new PrintWriter(new File(aggregatePath + index+"-Incidence-"+obRecord.name()+".csv"));
				outbreakWriter[obRecord.ordinal()] = new PrintWriter(new File(aggregatePath + index+"-Outbreak-"+obRecord.name()+".csv"));
				plfitWriter[obRecord.ordinal()] = new PrintWriter(new File(aggregatePath + index+"-Plfit-"+obRecord.name()+".csv"));
			}						
			populationWriter = new PrintWriter(new File(aggregatePath + index+"-PopulationStats.csv"));			
			for (int run=0; run<maxRuns; run++) {
				String individualRunPath = allRunsPath + index+"-"+run;
				EpisodicModel model = new EpisodicModel(input, run, individualRunPath, outbreakType);
				model.setup();
				model.run();		
				for (String output : model.getOutput()) {
					populationWriter.println(output);						
				}				
				populationWriter.flush();				
				/* Cluster Recorder*/
				model.getClusterRecorder().recordOutput(run, incidenceWriter, outbreakWriter, plfitWriter);
				model = null;
			}
			for (OUTBREAK_RECORD obRecord : OUTBREAK_RECORD.values()) {
				incidenceWriter[obRecord.ordinal()].flush(); incidenceWriter[obRecord.ordinal()].close(); 
				outbreakWriter[obRecord.ordinal()].flush(); outbreakWriter[obRecord.ordinal()].close();
				plfitWriter[obRecord.ordinal()].flush(); plfitWriter[obRecord.ordinal()].close();
			}										
			populationWriter.close();
		}			
	}
}