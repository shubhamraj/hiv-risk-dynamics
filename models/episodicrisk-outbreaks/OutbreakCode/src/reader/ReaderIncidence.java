package reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import reader.ReaderPopulation.IncidenceKey;

/**
 * 
 * @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 *
 */
public class ReaderIncidence {
	public enum Duration {LessOneMonth, TwoMonths, FiveMonths, GreaterFiveMonths};
	public enum Size {Two, Five, Ten, GreaterTen};

	public String processIncidence (File file) {
		int lineNo = 0;
		LinkedHashMap<Integer, ArrayList<String>>inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
		LinkedHashMap<Integer, ArrayList<String>> jointDistMap = new LinkedHashMap<Integer, ArrayList<String>>();	
		try {
			BufferedReader bufRdr = new BufferedReader(new FileReader(file));			
			String line = null;					
			while ((line = bufRdr.readLine()) != null) {
				lineNo++;
				StringTokenizer st = new StringTokenizer(line,",");
				ArrayList<String> tokens = new ArrayList<String>(); 
				while (st.hasMoreTokens()) {
					tokens.add(st.nextToken());
				}
				if (lineNo > 1 && lineNo % 8 == 0) {
					inputMap.put(new Integer(lineNo), tokens);
				}
				else if ((lineNo-1) % 7 >=1 && (lineNo-1) % 7 <=4){
					tokens.remove(tokens.size()-1);					
					jointDistMap.put(new Integer(lineNo), tokens);
				}
			}
			bufRdr.close();
		} catch (IOException e) {e.printStackTrace();}

		//Process incidence data
		EnumMap<IncidenceKey, ArrayList<Double>> keyMap = new EnumMap<IncidenceKey, ArrayList<Double>>(IncidenceKey.class);
		for (IncidenceKey key : IncidenceKey.values()) {
			keyMap.put(key, new ArrayList<Double>());
		}

		for (Integer inputKey : inputMap.keySet()) {			
			ArrayList<String> tokens = (ArrayList<String>) inputMap.get(inputKey);
			for (IncidenceKey key : IncidenceKey.values()) {
				keyMap.get(key).add(Double.parseDouble(tokens.get(key.ordinal())));
			}
		}
		double[] average = new double[IncidenceKey.values().length];
		for (int i=0; i<IncidenceKey.values().length; i++) {
			average[i] = 0d;
		}
		int size = keyMap.get(IncidenceKey.NewCases).size();
		for (IncidenceKey key : IncidenceKey.values()) {
			for (int i=0; i<size; i++) {
				average[key.ordinal()] += keyMap.get(key).get(i);	
			}			
		}

		String output = "";
		for (int i=IncidenceKey.NewCases.ordinal(); i<IncidenceKey.values().length; i++) {
			average[i] /= size;
			output += average[i] + ",";
		}		

		//Process joint distribution data
		EnumMap<Duration, ArrayList<Double>> durMap = new EnumMap<Duration, ArrayList<Double>>(Duration.class);
		for (Duration key : Duration.values()) {
			durMap.put(key, new ArrayList<Double>());
		}

		int s = 0;		
		for (Integer inputKey : jointDistMap.keySet()) {
			//System.out.println("s mod 4 : " + s%4);
			Duration key = Duration.values()[s%4];
			ArrayList<String> tokens = (ArrayList<String>) jointDistMap.get(inputKey);
			String str = "";
			for (String token : tokens) {
				if (! token.isEmpty()) {
					//System.out.println("Key: " + key + " tokens: " + token);
					durMap.get(key).add(Double.parseDouble(token.trim()));
				}
				str += durMap.get(key) + ",";
			}			
			s++;
		}
		
		double[][] out = new double[4][4];
		for (int i=0; i<4; i++) {
			for (int j=0; j<4; j++) {
				out[i][j] = 0d;
			}
		}		
		
		for (Duration key : Duration.values()) {			
			ArrayList<Double> rows = durMap.get(key);
			for (int i=0; i<12; i++) {
				try {
					out[key.ordinal()][i%4] += rows.get(i); 
				} catch (Exception e) {break;}
			}
		}
		
		for (int i=0; i<4; i++) {
			for (int j=0; j<4; j++) {				
				out[i][j] /= jointDistMap.size()/4;
				String str = "" + out[i][j] + ",";
				output += str;
				//System.out.println("i: " + i + " j: " + j + " out: " + out[i][j]);			
			}
		}

		return output;
	}


	public static void main(String[] args) {
//		String path = "./src/reader/";
		String path = "./Six-Months-P2-Tester/summary/";
		File directory = new File(path);
		File[] csvFiles = directory.listFiles(new FilenameFilter() {			
			@Override 
			public boolean accept(File dir, String name) {
				return name.endsWith(".csv");
			}
		});
		TreeMap<Integer, String> output = new TreeMap<Integer, String>();
		for (int i=0; i<csvFiles.length; i++) {
			ReaderIncidence r = new ReaderIncidence();
			String name = csvFiles[i].getName();
			if (name.indexOf("Outbreak") > 0) {
				int start = name.indexOf('k');
				int last = name.indexOf('.');
				name = name.substring(start+1, last);
				int index = Integer.parseInt(name);
				String str = r.processIncidence(csvFiles[i]);
				output.put(index, str);				
				//System.out.println(index + "," + output.get(index));
			}			
		}

		try {
			PrintWriter writer = new PrintWriter(new File(path+"Incidence.csv"));
			for (Integer key : output.keySet()) {
				//System.out.println(key + "," + output.get(key));
				writer.println(key + "," + output.get(key));
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {}
	}
}