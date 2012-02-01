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

/**
 * 
 * @author shah
 *
 */
public class ReaderPopulation {
	public enum GeneralKey {ID, Population, Infecteds, Prevalence, PrevHigh, PrevLow, FrPHI};
	public enum IncidenceKey {NewCases, NewAHI};
	
	public String processGeneral (File file) {
		int lineNo = 0;
		LinkedHashMap<Integer, ArrayList<String>>inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
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
				inputMap.put(new Integer(lineNo), tokens);
			}
			bufRdr.close();
		} catch (IOException e) {e.printStackTrace();}

		EnumMap<GeneralKey, ArrayList<Double>> keyMap = new EnumMap<GeneralKey, ArrayList<Double>>(GeneralKey.class);
		for (GeneralKey key : GeneralKey.values()) {
			keyMap.put(key, new ArrayList<Double>());
		}
		for (Integer inputKey : inputMap.keySet()) {
			ArrayList<String> tokens = (ArrayList<String>) inputMap.get(inputKey);
			for (GeneralKey key : GeneralKey.values()) {
				keyMap.get(key).add(Double.parseDouble(tokens.get(key.ordinal())));
			}
		}
		double[] average = new double[GeneralKey.values().length];
		for (int i=0; i<GeneralKey.values().length; i++) {
			average[i] = 0d;
		}
		int size = keyMap.get(GeneralKey.Population).size();
		for (GeneralKey key : GeneralKey.values()) {
			for (int i=0; i<size; i++) {
				average[key.ordinal()] += keyMap.get(key).get(i);	
			}			
		}
		String output = "";
		for (int i=GeneralKey.Population.ordinal(); i<GeneralKey.values().length; i++) {
			average[i] /= size;
			output += average[i] + ",";
		}		
		return output;
	}
	
	public String processIncidence (File file) {
		int lineNo = 0;
		LinkedHashMap<Integer, ArrayList<String>>inputMap = new LinkedHashMap<Integer, ArrayList<String>>();	
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
				if (lineNo > 1 && lineNo % 7 == 0) {
					inputMap.put(new Integer(lineNo), tokens);
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
		return output;
	}

	public static void main(String[] args) {
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
			ReaderPopulation r = new ReaderPopulation();
			String name = csvFiles[i].getName();
			if (name.indexOf("Outbreak") == -1
					&& name.indexOf("Plfit") == -1
					&& name.indexOf("Incidence") == -1
				) {
				int start = name.indexOf('-');
				int last = name.indexOf('.');
				name = name.substring(start+1, last);
				int index = Integer.parseInt(name);			
				output.put(index, r.processGeneral(csvFiles[i]));				
			}			
		}
		//Do that for incidence cases
		for (int i=0; i<csvFiles.length; i++) {
			ReaderPopulation r = new ReaderPopulation();
			String name = csvFiles[i].getName();
			if (name.indexOf("Outbreak") > 0) {
				int start = name.indexOf('k');
				int last = name.indexOf('.');
				name = name.substring(start+1, last);
				int index = Integer.parseInt(name);
				String str = output.get(index).concat(r.processIncidence(csvFiles[i]));
				output.put(index, str);				
				//System.out.println(index + "," + output.get(index));
			}			
		}
		
		try {
			PrintWriter writer = new PrintWriter(new File(path+"Population.csv"));
			for (Integer key : output.keySet()) {
				//System.out.println(key + "," + output.get(key));
				writer.println(key + "," + output.get(key));
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {}
	}
}