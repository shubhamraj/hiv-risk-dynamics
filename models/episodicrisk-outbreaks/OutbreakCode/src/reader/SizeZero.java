package reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class SizeZero {

	public static void main(String[] args) throws FileNotFoundException {
		String path = "C:/Users/shah/LHS1000CR/LHS1000CR/";
		File directory = new File(path);
		File[] csvFiles;
		csvFiles = directory.listFiles(new FilenameFilter() {			 
			public boolean accept(File dir, String name) {								
				return name.contains("LHSCR-PHI-") 
				&& !name.contains("Plfit") 
				&& !name.contains("Outbreak")
				&& !name.contains("Incidence");				
			}
		});
		ArrayList<Double> indices = new ArrayList<Double>();
		PrintWriter writer = new PrintWriter(new File("zeroFilesNames.txt"));
		for (File file : csvFiles) {
			if (file.length() < 1.0) {
				System.out.println("File: " + file.getName() + " - " + file.getAbsolutePath());
				String str = file.getName();
				int start = str.indexOf("PHI");
				start = str.indexOf('-', start) + 1;
				int end = str.indexOf('.', start);
				str = str.substring(start, end);
				writer.println(file.getAbsolutePath());
				//System.out.println(str.substring(start, end));
				indices.add(Double.parseDouble(str));
			}
		}		
		
		writer.flush();
		writer.close();
		
/*		Collections.sort(indices);
		
		PrintWriter writer2 = new PrintWriter(new File("zeroFiles.txt"));
		
		for (Double dbl : indices) {
			System.out.println(dbl.intValue());
			writer2.println(dbl.intValue());
		}
		
		writer2.flush();
		writer2.close();*/
	}
}
