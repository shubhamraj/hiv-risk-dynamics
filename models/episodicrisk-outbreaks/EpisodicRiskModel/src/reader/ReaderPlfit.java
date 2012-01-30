package reader;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * 
 * @author shah
 *
 */
public class ReaderPlfit {
	public static final int numColumns = 4;

	public String cleanup(File file, TreeMap<Integer, String> output) {
		String line = null;
		ArrayList<String> tokens = new ArrayList<String>();
		try {
			BufferedReader bufRdr = new BufferedReader(new FileReader(file));
			while ((line = bufRdr.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line,","); 
				while (st.hasMoreTokens()) {
					tokens.add(st.nextToken());
				}
			}
			bufRdr.close();
		} catch (Exception e) {}

		ArrayList<String> values = new ArrayList<String>();

		for (String token : tokens) {
			String oldStr = ""; String newStr = "";
			if (token.indexOf(".", 7) > 0) {
				int start = token.indexOf('.', 7) - 1;
				oldStr = token.substring(0, start);
				newStr = token.substring(start, token.length()-1);
				values.add(clean(oldStr));
				values.add(clean(newStr));
			}
			else {
				values.add(clean(token));
			}
		}
		String str = "";
		int index = 0; int counter = 0;
		for (String token : values) {
			str += token + ",";
			if (index == 3) {
				output.put(counter, str);
				counter++;
				index = 0;
				str = "";
			}
			else {
				index++;
			}
		}

		return str;
	}

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


		double[] average = new double[numColumns];
		for (int i=0; i<numColumns; i++) {
			average[i] = 0d;
		}

		String output = "";
		for (Integer inputKey : inputMap.keySet()) {			
			ArrayList<String> tokens = (ArrayList<String>) inputMap.get(inputKey);
			for (int i=0; i<numColumns; i++) {
				average[i]  += Double.parseDouble(tokens.get(i));
			}
		}		

		for (int i=0; i<numColumns; i++) {
			average[i] /= inputMap.size();
			output += average[i] + ",";
		}

		return output;
	}

	private String clean (String str) {
		String out = str;
		if (str.indexOf('-') > 0) {
			out = str.substring(0, str.length()-2);
		}
		return out;
	}
}