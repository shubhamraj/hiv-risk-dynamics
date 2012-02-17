package reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 *  @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 *
 */
public class CumulaitveFrequencyGenerator { 
	LinkedHashMap<Integer, ArrayList<String>>inputMap;
	TreeMap<Double, Double> treeMap;
	String path;
	int index;
	public static String ftr;
	int lineNo;
	double[] frPHI; 
	double sumDataSize;
	double[] dataSize;
	double[] fraction;
	double[] cumSum;
	double[] obKey;	

	public CumulaitveFrequencyGenerator(String _path, double[] _frPHI) {
		this.path = _path;
		this.frPHI = _frPHI;
		inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
		treeMap = new TreeMap<Double, Double>();
		ftr = "";
		lineNo = 0;
		sumDataSize = 0;
	}

	public void run(int _index) {
		this.index = _index;
		if (frPHI[index] == 0) {
			return;
		}
		boolean fileRead = readFile();
		if (fileRead == false) {
			return;
		}
		processFile();
		pumpTreeMap();
		calcECDF();
		writeECDF();
		cleanup();
	}

	private void cleanup() {
		inputMap.clear();
		inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
		treeMap.clear();
		treeMap = new TreeMap<Double, Double>();
		ftr = "";
		lineNo = 0;
		sumDataSize = 0;
		dataSize = null;
		fraction = null;
		cumSum = null;
		obKey = null;	
	}

	private void writeECDF() {
		try {
			String writePath = "./ECDF-III/";
			PrintWriter writer = new PrintWriter(new File(writePath+"ECDF-"+index+".csv"));
			for (int i=0; i<fraction.length; i++) {
				String str = obKey[i] + "," + fraction[i] + "," + cumSum[i];
				writer.println(str);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {}
	}

	private void calcECDF() {
		int numData = treeMap.size() + 1;
		obKey = new double[numData];
		fraction = new double[numData];
		cumSum = new double[numData];

		double frPHI = returnFRPHI();
		double totalTrans = sumDataSize/frPHI;
		double chronicTrans = totalTrans * (1-frPHI);
		fraction[0] = chronicTrans/totalTrans;
		cumSum[0] = chronicTrans/totalTrans;		
		//System.out.println("------------- total trans: " + totalTrans + " sumdata: " + sumDataSize);
		int i=1;
		for (Double key : treeMap.keySet()) {
			double frequency = treeMap.get(key);
			double mult = key * frequency;
			//fraction[i] = frequency/totalTrans;
			fraction[i] = mult/totalTrans;
			obKey[i] = key;
			//System.out.println(key + " " + frequency + " " + fraction[i]);
			i++;
		}		
		for (i=1; i<numData; i++) {
			for (int j=0; j<=i; j++) {
				cumSum[i] += fraction[j];
			}
		}

	}

	private double returnFRPHI() {
		return frPHI[index];
	}

	private void pumpTreeMap() {
		for (int i=0; i<dataSize.length; i++) {
			double val = 0;
			Double key = new Double(dataSize[i]);
			if (treeMap.containsKey(key)) {
				val = treeMap.get(key);				
			}
			treeMap.put(key, ++val);
		}	
		/*		for (Double key : treeMap.keySet()) {
			System.out.println(key + " " + treeMap.get(key));
		}*/
	}

	private void processFile() {
		lineNo--;		
		dataSize = new double[lineNo];
		for (int i=0; i<lineNo; i++) {
			dataSize[i] = 0;
		}
		int row = 0;
		for (Integer inputKey : inputMap.keySet()) {
			ArrayList<String> tokens = (ArrayList<String>) inputMap.get(inputKey);				
			dataSize[row] = Double.parseDouble(tokens.get(9));
			sumDataSize += dataSize[row];
			row++;
		}
	}

	private boolean readFile() {
		ftr = path + index + "-0.csv";
		try {			
			File file = new File(ftr); 
			if (file.exists()) {
				BufferedReader bufRdr = new BufferedReader(new FileReader(file));
				String line = null;					
				while ((line = bufRdr.readLine()) != null) {			
					lineNo++;
					StringTokenizer st = new StringTokenizer(line,",");
					ArrayList<String> tokens = new ArrayList<String>(); 
					while (st.hasMoreTokens()) {
						tokens.add(st.nextToken());
					}
					if (lineNo > 1) {
						inputMap.put(new Integer(lineNo), tokens);
					}
				}
				bufRdr.close();
			}
			else {
				return false;
			}
		} catch (IOException e) {}					
		return true;
	}

	public static void main(String[] args) {
		String masterPath = "./LHS/input/Population.csv";
		int paramSets = 1000;
		LinkedHashMap<Integer, ArrayList<String>>inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
		double[] frPHI = new double[paramSets+1];
		int lineNo = 1;
		try {
			BufferedReader bufRdr = new BufferedReader(new FileReader(masterPath));
			String line = null;					
			while ((line = bufRdr.readLine()) != null) {			
				StringTokenizer st = new StringTokenizer(line,",");
				ArrayList<String> tokens = new ArrayList<String>(); 
				while (st.hasMoreTokens()) {
					tokens.add(st.nextToken());
				}
				inputMap.put(new Integer(lineNo), tokens);
				lineNo++;
			}
			bufRdr.close();			
		} catch (Exception e) {}

		frPHI[0] = 0;
		int row = 1;
		for (Integer inputKey : inputMap.keySet()) {
			ArrayList<String> tokens = (ArrayList<String>) inputMap.get(inputKey);
			if (tokens.get(6) == "NaN") {
				frPHI[row] = 0;
			}
			else {
				frPHI[row] = Double.parseDouble(tokens.get(6));
			}
			row++;
		}			
		String path = "C:/Users/shah/LHS1000CR/LHS1000CR/dataLHS1000/";
		CumulaitveFrequencyGenerator cumfreq = new CumulaitveFrequencyGenerator(path, frPHI);
		for (int i=1; i<=paramSets; i++) {
			cumfreq.run(i);
		}
	}	
}