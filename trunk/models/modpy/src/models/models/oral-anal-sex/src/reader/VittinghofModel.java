package reader;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

public class VittinghofModel {
	String path = "./src/vt-summary.csv";
	Reader reader = new Reader();
	Vector<String> input = new Vector<String>();
	HashMap<String, Individual> individuals = new HashMap<String, Individual>();
	public void readData(){
		reader.read(path);
		for (int i=1; i<=reader.getLineNo(); i++) {	
			input = reader.getDataSet().get(new Integer(i));
			String strID = input.get(0);
			if (!individuals.containsKey(strID)) {
				Individual individual = new Individual();
				individual.setStringId(strID);
				if (Integer.valueOf(input.get(1)).intValue() == 1) {
					individual.setSeroconverted(true);
					double numAnal = Double.valueOf(input.get(3)).doubleValue();
					double numOral = Double.valueOf(input.get(4)).doubleValue();
					double infection = 1;
					numAnal/=2; numOral/=2;
					individual.totalVisits++;
					Double[] rec = new Double[3];
					rec[0] = numAnal;
					rec[1] = numOral;
					rec[2] = infection;
					individual.record.put(individual.totalVisits, rec);
					individual.totalVisits++;
					individual.record.put(individual.totalVisits, rec);
					individuals.put(strID, individual);
				}
				else {
					individual.totalVisits++;
					Double[] rec = new Double[3];
					rec[0] = Double.valueOf(input.get(3)).doubleValue();
					rec[1] = Double.valueOf(input.get(4)).doubleValue();
					rec[2] = Double.valueOf(input.get(1)).doubleValue();
					individual.record.put(individual.totalVisits, rec);
					individuals.put(strID, individual);
				}
			} else {
				Individual individual = individuals.get(strID);
				if (!individual.isSeroconverted()) {
					individual.totalVisits++;	
					if (Integer.valueOf(input.get(1)).intValue() == 1) {
						individual.setSeroconverted(true); 
					}
					Double[] rec = new Double[3];
					rec[0] = Double.valueOf(input.get(3)).doubleValue();
					rec[1] = Double.valueOf(input.get(4)).doubleValue();
					rec[2] = Double.valueOf(input.get(1)).doubleValue();
					individual.record.put(individual.totalVisits, rec);
					individuals.put(strID, individual);
				}
			}

		}
	}
	
	public void writeData() {
		File file = new File("./src/vt-processed-R.csv");		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (String strID : individuals.keySet()) {
				Individual individual = individuals.get(strID);
				for (Integer visit : individual.record.keySet()) { 				
					String str = strID + "," + individual.record.get(visit)[2]+","
					+individual.record.get(visit)[0]
                    +","+individual.record.get(visit)[1];
					if (!(individual.record.get(visit)[0] == 0
							&& individual.record.get(visit)[1] == 0)) {
//					if (individual.record.get(visit)[0] > 0) {
						writer.write(str);
						writer.newLine();						
					}
				}
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {}
	}

	public void print(String str) {
		System.out.println(""+str);
	}
	public static void main(String[] args) {
		VittinghofModel model = new VittinghofModel();
		model.readData();	
		model.writeData();
	}
}
