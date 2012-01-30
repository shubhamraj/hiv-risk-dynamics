package reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

/**
 * 
 * @author shah
 *
 */
public class OutbreakStatsReader extends TreeOutput{
	private String path = "";
	private ArrayList<Double> output;
	private boolean fileFound = false;	
	public static String ftr;
	private LinkedHashMap<Integer, ArrayList<String>> inputMap;
	private int runs = 0;	
	private File directory;
	private File[] csvFiles;
	private int lineNo;
	private double[][] outputData;	
	
	public OutbreakStatsReader(String _path, int index) {
		super();
		this.path = _path;		
		this.output = new ArrayList<Double>();
		this.inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
		this.directory = new File(this.path);
		this.lineNo = 0;

		ftr = ""+ index + "-";
		this.csvFiles = directory.listFiles(new FilenameFilter() {			
			@Override 
			public boolean accept(File dir, String name) {				
				return name.startsWith(ftr);
			}
		});

		this.runs = csvFiles.length;
		if (runs == 0) {
			for (int i=0; i<Outputs.values().length*Statistics.values().length; i++) {
				output.add(Double.NaN);
			}
		}
		else {
			fileFound = true;
			initializeOutputData();
		}
	}

	public void run() {
		for (int run=0; run<runs; run++) { 
			inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
			readFile(run);
			initializeOutputMap(this.lineNo);			
			pumpDataArrays();
			double[] statisticsArray = new double[Statistics.values().length];
			for (Outputs output : Outputs.values()) {
				statisticsArray = returnDoubleArray(calculateStats(output));
				for (Statistics statistic : Statistics.values()) {
					outputData[output.ordinal()][statistic.ordinal()] += statisticsArray[statistic.ordinal()];
				}
			}
		}
		
		//now take the average
		for (Outputs output : Outputs.values()) {
			for (Statistics statistic : Statistics.values()) {
/*				if (output == Outputs.IRatio && statistic.ordinal() > Statistics.Var.ordinal()) {
					continue;
				}*/
				outputData[output.ordinal()][statistic.ordinal()] /= this.runs;
				this.output.add(outputData[output.ordinal()][statistic.ordinal()]);
			}
		}
	}

	private void pumpDataArrays() {		
		int row = 0;
		for (Integer inputKey : inputMap.keySet()) {
			ArrayList<String> tokens = (ArrayList<String>) inputMap.get(inputKey);
			try {
				outputsMap.get(Outputs.Height)[row] = Double.parseDouble(tokens.get(7));
				outputsMap.get(Outputs.Size)[row] = Double.parseDouble(tokens.get(9));
				outputsMap.get(Outputs.Duration)[row] = Double.parseDouble(tokens.get(10));
				outputsMap.get(Outputs.IRatio)[row] = Double.parseDouble(tokens.get(11));
				row++;
			} catch (Exception e) {
				System.err.println("I have an error.");
			}
		}
	}

	protected void initializeOutputData() {
		outputData = new double[Outputs.values().length][Statistics.values().length];
		for (Outputs output : Outputs.values()) {
			for (Statistics stat : Statistics.values()) {
				outputData[output.ordinal()][stat.ordinal()] = 0;
			}			
		}					
	}

	private void readFile(int run) {
		this.lineNo = 0;
		try {
			BufferedReader bufRdr = new BufferedReader(new FileReader(csvFiles[run]));
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
		} catch (IOException e) {}
		lineNo--;
	}

	public ArrayList<Double> getOutput() {
		return output;
	}
	
	public boolean isFileFound() {
		return fileFound;
	}
}