package reader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import cern.jet.random.Normal;

public class LevelsProducer {	
	private int numLevels = 4;
	private HashMap<Integer, IndividualRecord> simulationRecord;
	private String inPath = "./ERSInput/oa_data.";
	private String outPath = "./ERSOutput/";
	private Reader reader;
	private String paramsList = "";
	private File[] file;
	private BufferedWriter[] writer;

	public LevelsProducer() {
		file = new File[numLevels]; 
		writer = new BufferedWriter[numLevels];	
	}

	public void setup(int index) {		
		String activePath = outPath+index+"_oa_";
		String strI = "\"id\",\"infected\",\"oralSusc\",\"oralAcute\",\"oralChronic\"," +
		"\"analSusc\",\"analAcute\",\"analChronic\""; 
		String strII = "\"id\",\"infected\",\"oral\",\"anal\"";

		try {
			for (int i=0; i<numLevels; i++) {
				file[i] = new File(activePath+i+".csv");
				writer[i] = new BufferedWriter(new FileWriter(file[i]));
				writer[i].write(paramsList);
				writer[i].newLine();
				if (i>0) {
					writer[i].write(strII);
					writer[i].newLine();					
				}
				else {
					writer[i].write(strI);
					writer[i].newLine();
				}
			}			
		} catch(IOException e) {e.printStackTrace();}
	}

	public void run(int index) {		
		reader = null;
		simulationRecord = null;
		String activePath = inPath+index;
		reader = new Reader();
		simulationRecord = new HashMap<Integer, IndividualRecord>();
		reader.read(activePath);
		paramsList = reader.getDataSet().get(new Integer(1)).toString();
		setup(index);	
		for (int i=2; i<=reader.getLineNo(); i++) {
			Vector<String >inputVector = reader.getDataSet().get(new Integer(i));
			IndividualRecord individualRecord = addRecord(inputVector);					
			simulationRecord.put(individualRecord.getId(), individualRecord);			
		}		
		writeData();
	}

	public void writeData() {
		try {
			String[] str = new String[numLevels];
			for (Integer ID : simulationRecord.keySet()) {
				IndividualRecord ir = simulationRecord.get(ID);
				int n1=0, n2=0;
				int oralInf = ir.getNumOralAcute()+ir.getNumOralChronic();
				int analInf = ir.getNumAnalAcute()+ir.getNumAnalChronic();
				int totalOral = ir.getNumOralSusc()+oralInf;
				int totalAnal = ir.getNumAnalSusc()+analInf;
				str[0] = ir.getId()+","+ir.getInfected()+","+ir.getNumOralSusc()+","+ir.getNumOralAcute()
				+","+ir.getNumOralChronic()+","+ir.getNumAnalSusc()+","+ir.getNumAnalAcute()
				+","+ir.getNumAnalChronic();

				str[1] = ir.getId()+","+ir.getInfected()+","+oralInf+","+analInf;
				str[2] = ir.getId()+","+ir.getInfected()+","+totalOral+","+totalAnal;

				do {
					n1 = (int) Normal.staticNextDouble(oralInf, 0.1*oralInf);
				} while (n1 < 0);
				do {
					n2 = (int) Normal.staticNextDouble(analInf, 0.1*analInf);
				} while (n2 < 0);
				n1 += oralInf;
				n2 += analInf;
				str[3] = ir.getId()+","+ir.getInfected()+","+n1+","+n2;

				if (totalOral == 0 && totalAnal == 0) {
					str[0] = "";
					str[1] = "";
					str[2] = "";
					str[3] = "";
				}
				if (oralInf == 0 && analInf == 0) {
					str[2] = "";
					str[3] = "";
				}
				for (int i=0; i<numLevels; i++) {
					if (str[i] != "") {
						writer[i].write(str[i]);
						writer[i].newLine();						
					}
				}
			}		
			closeRecords();
		} catch (IOException e) {e.printStackTrace();}
	}	


	public void closeRecords() {
		try {
			for (int i=0; i<numLevels; i++) {
				writer[i].flush();
				writer[i].close();
			}
		} catch (IOException e) {e.printStackTrace();}		
	}

	public IndividualRecord addRecord(Vector<String> inputVector) {
		//15923,1,2,62,0,5,198,2,36,
		IndividualRecord individualRecord = new IndividualRecord();
		individualRecord.setId(Integer.valueOf(inputVector.get(0)).intValue());
		individualRecord.setInfected(Integer.valueOf(inputVector.get(1)).intValue());
		individualRecord.setNumWaves(Integer.valueOf(inputVector.get(2)).intValue());
		individualRecord.setNumOralSusc(Integer.valueOf(inputVector.get(3)).intValue());
		individualRecord.setNumOralAcute(Integer.valueOf(inputVector.get(4)).intValue());
		individualRecord.setNumOralChronic(Integer.valueOf(inputVector.get(5)).intValue());
		individualRecord.setNumAnalSusc(Integer.valueOf(inputVector.get(6)).intValue());
		individualRecord.setNumAnalAcute(Integer.valueOf(inputVector.get(7)).intValue());
		individualRecord.setNumAnalChronic(Integer.valueOf(inputVector.get(8)).intValue());
		return individualRecord;
	}

	public void print(String str) {
		System.out.println(""+str);
	}

	public static void main(String[] args) {
		LevelsProducer levelsProducer = new LevelsProducer();
		int numFiles = 433;
		for (int i=0; i<numFiles;i++) {
			levelsProducer.run(i);
			levelsProducer.print(""+i);
		}		
	}
}