package main;



import interfaces.ParametersInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;


import episodicriskmodel.EpisodicModel;


/**
 * 
 * @author shah
 *
 */
public class Controller implements ParametersInterface {
	public static void main(String[] args) throws IOException {
		OutbreakType outbreakType = OutbreakType.AHI;
		int maxRuns = 3;		
		int startParam = 2;
		int endParam = 3;
		
		for (int index=startParam; index<=endParam; index++) {
			int lineNo = 1;
			ArrayList<String> tokens = new ArrayList<String>();
			try {			
				BufferedReader bufRdr = new BufferedReader(new FileReader(inputPath + inputFile));
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
			PrintWriter[] incidenceWriter = new PrintWriter[OutbreakRecord.values().length];
			PrintWriter[] outbreakWriter = new PrintWriter[OutbreakRecord.values().length];
			PrintWriter[] plfitWriter = new PrintWriter[OutbreakRecord.values().length];			
			for (OutbreakRecord obRecord : OutbreakRecord.values()) {
				incidenceWriter[obRecord.ordinal()] = new PrintWriter(new File(aggregatePath + index+"-Incidence-"+obRecord.name()+".csv"));
				outbreakWriter[obRecord.ordinal()] = new PrintWriter(new File(aggregatePath + index+"-Outbreak-"+obRecord.name()+".csv"));
				plfitWriter[obRecord.ordinal()] = new PrintWriter(new File(aggregatePath + index+"-Plfit-"+obRecord.name()+".csv"));
			}						
			populationWriter = new PrintWriter(new File(aggregatePath + index+"-PopulationStats.csv"));			
			for (int run=0; run<maxRuns; run++) {
				String individualRunPath = allRunsPath + index+"-"+run;
				//model instantiation and initialization
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
			for (OutbreakRecord obRecord : OutbreakRecord.values()) {
				incidenceWriter[obRecord.ordinal()].flush(); incidenceWriter[obRecord.ordinal()].close(); 
				outbreakWriter[obRecord.ordinal()].flush(); outbreakWriter[obRecord.ordinal()].close();
				plfitWriter[obRecord.ordinal()].flush(); plfitWriter[obRecord.ordinal()].close();
			}										
			populationWriter.close();
		}			
	}
}