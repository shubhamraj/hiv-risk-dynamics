package reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

public class Reader {
	private  HashMap<Integer, Vector<Double>> parametersSet = new HashMap<Integer, Vector<Double>>();
	private  HashMap<Integer, Vector<String>> dataSet = new HashMap<Integer, Vector<String>>();
	private int lineNo=0;

	public HashMap<Integer, Vector<Double>> read(String path) {		
		File file = new File(path);		
		try {
			BufferedReader bufRdr = new BufferedReader(new FileReader(file));			
			String line = null;					
			while ((line = bufRdr.readLine()) != null) {
				lineNo++;
				if (lineNo >= 0) {
					StringTokenizer st = new StringTokenizer(line,",");
//					Vector<Double> d = new Vector<Double>();
					Vector<String> str = new Vector<String>();
					while (st.hasMoreTokens()) {			
//						d.add(Double.parseDouble(st.nextToken()));
						str.add(st.nextToken());
					}							
//					parametersSet.put(new Integer(lineNo), d);
					dataSet.put(new Integer(lineNo), str);
				}				
			}
		} catch (IOException e) {e.printStackTrace();}
		return parametersSet;
	}

	public static void main(String[] args) {
		Reader r = new Reader();
		String path = "./src/parameters.txt";
		r.read(path);
	}

	public HashMap<Integer, Vector<Double>> getParametersSet() {
		return parametersSet;
	}

	public void setParametersSet(HashMap<Integer, Vector<Double>> parameters) {
		this.parametersSet = parameters;
	}

	public int getLineNo() {
		return lineNo;
	}

	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}

	public HashMap<Integer, Vector<String>> getDataSet() {
		return dataSet;
	}

	public void setDataSet(HashMap<Integer, Vector<String>> dataSet) {
		this.dataSet = dataSet;
	}
}
