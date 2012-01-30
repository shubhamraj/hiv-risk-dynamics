package plfit;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import model.Parameters;

public class PowerLawFit extends Parameters {
	private ArrayList<String> command;
	private String fname = "./src/ext/cfg/temp.txt";
	private InputStream stdout;
	private InputStream stderr;
	private ProcessBuilder builder;
	private BufferedReader bufferedReader;
	private File file;
	private double[] output;
	
	public PowerLawFit(ArrayList<Double> input) {
		if (command != null) {
			command = null;
		}
		if (stdout != null) {
			stdout = null;
		}
		if (stderr != null) {
			stderr = null;
		}
		if (builder != null) {
			builder = null;
		}		
		if (bufferedReader != null) {
			bufferedReader = null;
		}
		if (output != null) {
			output = null;
		}
		command = new ArrayList<String>();
		command.add(plfitExecutablePath);		
		command.add(fname);
		builder = new ProcessBuilder(command);
		output = new double[5];
		output[0] = 0.0;output[1] = 0.0;output[2] = 0.0;output[3] = 0.0;output[4] = 0.0;
		
		try {
			buildInputFile(input);
		} catch (Exception e) {}
	}
	
	private void buildInputFile(ArrayList<Double> input) throws FileNotFoundException {
		file = new File(fname);
		if (file.exists()) {
			file.delete();
		}
		PrintWriter writer = new PrintWriter(new File(fname));
		for (Double val : input) {
			writer.println(val);
		}
		writer.flush();
		writer.close();		
	}
	
	public double[] run() {
		try {			
			final Process p = builder.start();
			// Or one may use getInputStream to get the actual output of process
			stdout = p.getInputStream() ; 
			stderr = p.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stdout);
			bufferedReader = new BufferedReader(isr);
			p.waitFor();
			p.destroy();
			parseOutput();
			//print();
			for (int i=0; i<5; i++) {
				System.out.println(output[i]);
			}
			file.delete();
		}
		catch (Exception err) {
			err.printStackTrace();
		}		
		return output;
	}
	
	private void parseOutput() throws IOException {
		//LinkedHashMap<Integer, ArrayList<String>> inputMap = new LinkedHashMap<Integer, ArrayList<String>>();
		int lineNo = 0;
		String line; 
		int index = 0;
		while ((line = bufferedReader.readLine()) != null) {
			lineNo++;
			//We start from 2 
			/*The output comes like: 
			 * ./src/ext/cfg/temp.txt:
				Discrete MLE
				alpha =      3.34524
				xmin  =     20.00000
				L     =   -191.46372
				D     =      0.05629
				p     =      0.99552 
			 */
			if (lineNo > 2 && lineNo < 8) {				
				StringTokenizer st = new StringTokenizer(line," ");
				ArrayList<String> tokens = new ArrayList<String>(); 
				while (st.hasMoreTokens()) {
					tokens.add(st.nextToken().trim());				
				}
/*				String ptr = "";
				for (String str : tokens) {
					ptr += str + "";
				}
				System.out.println(ptr);
				*/
				
				//hardcoded
				output[index] = Double.parseDouble(tokens.get(2));
				index++;
//				inputMap.put(new Integer(lineNo), tokens);
			}			
		}		
	}
	
	protected void print() throws Exception {
		System.out.println("Plfit output.");
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			System.out.println(line);			
		}
	}
}