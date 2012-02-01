package reader;



import java.io.File;
import java.io.FilenameFilter;

import basemodel.ParametersInterface;

/**
 * Main Reader Engine Class
 * @author shah
 *
 */
public class ReaderEngine implements ParametersInterface {
	String path = "";
	String outputString;
	public static String preFtr = "", obRecordFtr = "", endFtr = "";
	
	public ReaderEngine(String _path) {
		this.path = _path;
		outputString = "";
	}
	
	public void run(int index) {						
		preFtr = index + "-";
		obRecordFtr = OutbreakRecord.Endemic.name();
		endFtr = "AllTransmissions.csv";
		
		File directory = new File(this.path);
		File[] csvFiles = directory.listFiles(new FilenameFilter() {			
			@Override 
			public boolean accept(File dir, String name) {				
				return name.startsWith(preFtr) && name.contains(obRecordFtr) && name.endsWith(endFtr);
			}
		});		
		//sja: this needs to be checked
		int numAHICols = Output.values().length * Statistics.values().length;
		numAHICols += (SizeRanges.length + 1) + (durationRanges.length + 1);		
		//now add the chronics (see the number of times, a datum is added to the  chainsOutputs ArrayList in AHICHILink.java
		int numChronicCols = 14 + (chronicRanges.length+1);
		int numCols = numAHICols + numChronicCols;
		
		int runs = csvFiles.length;
		if (runs == 0) {
			for (int i=0; i<numCols; i++) {
				outputString += "NaN,";
			}
			return;
		}
				 		
		double[] average = new double[numCols];		
		for (int i=0; i<numCols; i++) {			
			average[i] = 0;
		}
		
		for (int run=0; run<runs; run++) {			
			AHICHILink ahichiLink = new AHICHILink(index, path + csvFiles[run].getName());
			ahichiLink.run();
			for (int i=0; i<numCols; i++) {
				try {
					if (i < numAHICols) {
						average[i] += ahichiLink.getAHIOutput().get(i);
					}
					else {
						average[i] += ahichiLink.getChainsOutput().get(i-numAHICols);
					}					 					
				} catch (Exception e) {
					System.err.println("index: " + i +  " numCols: " + numCols + " ahioutput size: " + ahichiLink.getAHIOutput().size());
					System.exit(1);
				}
			}
		}			
		for (int i=0; i<numCols; i++) {
			average[i] /= ((double)runs);
			outputString += average[i] + ",";
		}		
	}
	
	public void clear() {
		this.outputString = "";
	}
	
	public String returnOutput() {
		return this.outputString;
	}	
}