package reader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import cern.jet.random.Normal;

public class CopyOfLevelsProducer {	

	private HashMap<Integer, IndividualRecord> simulationRecord;
	private String inPath = "./src/ERSInput/oa_data.";
	private String outPath = "./src/ERS_Output/";
	private Reader reader;

	File fLevel0, fLevel1, fLevel2, fLevel3;
	BufferedWriter writer0, writer1, writer2, writer3;

	public void run(int index) {		
		reader = null;
		simulationRecord = null;
		String activePath = inPath+index;
		reader = new Reader();
		simulationRecord = new HashMap<Integer, IndividualRecord>();
		reader.read(activePath);		
		recordHeaders(index);	
		for (int i=2; i<=reader.getLineNo(); i++) {
			Vector<String >inputVector = reader.getDataSet().get(new Integer(i));
			IndividualRecord individualRecord = addRecord(inputVector);					
			simulationRecord.put(individualRecord.getId(), individualRecord);			
		}		
		writeData();
	}

	public void writeData() {
		try {
			String str0="",str1="",str2="",str3="";
			for (Integer ID : simulationRecord.keySet()) {
				IndividualRecord ir = simulationRecord.get(ID);
				if (ir.getNumOralSusc() > 0
						&& ir.getNumOralAcute() > 0
						&& ir.getNumOralChronic() > 0
						&& ir.getNumAnalSusc() > 0
						&& ir.getNumAnalAcute() > 0
						&& ir.getNumAnalChronic() > 0) {
					str0 = ir.getInfected()+","+ir.getNumOralSusc()+","+ir.getNumOralAcute()
					+","+ir.getNumOralChronic()+","+ir.getNumAnalSusc()+","+ir.getNumAnalAcute()
					+","+ir.getNumAnalChronic();					
				}
				int n1=1, n2=1;
				int oralInf = ir.getNumOralAcute()+ir.getNumOralChronic();
				int analInf = ir.getNumAnalAcute()+ir.getNumAnalChronic();
				int totalOral = ir.getNumOralSusc()+oralInf;
				int totalAnal = ir.getNumAnalSusc()+analInf;
				if (oralInf > 0
						&& analInf > 0) {
					str1 = ir.getInfected()+","+oralInf+","+analInf;
				}				
				if (totalAnal > 0 
						&& totalOral > 0) {
					str2 = ir.getInfected()+","+totalOral+","+totalAnal;
					do {
						n1 = (int) Normal.staticNextDouble(totalAnal, 0.1*totalAnal);
					} while (n1 <= 0);
					do {
						n2 = (int) Normal.staticNextDouble(totalOral, 0.1*totalOral);
					} while (n2 <= 0);
					str3 = ir.getInfected()+","+n1+","+n2;
				}		
				if (str0 != "") {
					writer0.write(str0);
					writer0.newLine();	
				}
				if (str1 != "") {
					writer1.write(str1);
					writer1.newLine();
				}
				if (str2 != "") {
					writer2.write(str2);
					writer2.newLine();					
				}
				if (str3 != "") {
					writer3.write(str3);
					writer3.newLine();						
				}				
			}		
			closeRecords();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	public void recordHeaders(int index) {
		String activePath = outPath+index+"_oa_";
		Vector<String> paramaterList = reader.getDataSet().get(new Integer(1));
		print(paramaterList.toString());
		fLevel0 = new File(activePath+0+".csv");
		fLevel1 = new File(activePath+1+".csv");
		fLevel2 = new File(activePath+2+".csv");
		fLevel3 = new File(activePath+3+".csv");		
		String str0 = "\"infected\",\"oralSusc\",\"oralAcute\",\"oralChronic\"," +
		"\"analSusc\",\"analAcute\",\"analChronic\""; 
		String str1 = "\"infected\",\"oral\",\"anal\"";
		String str2 = "\"infected\",\"oral\",\"anal\"";
		String str3 = "\"infected\",\"oral\",\"anal\"";				
		try {
			writer0 = new BufferedWriter(new FileWriter(fLevel0));
			writer1 = new BufferedWriter(new FileWriter(fLevel1));
			writer2 = new BufferedWriter(new FileWriter(fLevel2));
			writer3 = new BufferedWriter(new FileWriter(fLevel3));

			writer0.write(paramaterList.toString());
			writer0.newLine();
			writer0.write(str0);
			writer0.newLine();
		
			writer1.write(paramaterList.toString());
			writer1.newLine();
			writer1.write(str1);
			writer1.newLine();
					
			writer2.write(paramaterList.toString());
			writer2.newLine();
			writer2.write(str2);
			writer2.newLine();
			
			writer3.write(paramaterList.toString());
			writer3.newLine();
			writer3.write(str3);
			writer3.newLine();
		} catch(IOException e) {e.printStackTrace();}				
	}

	public void closeRecords() {
		try {
			writer0.flush();
			writer1.flush();
			writer2.flush();
			writer3.flush();			
			writer0.close();
			writer1.close();
			writer2.close();
			writer3.close();
		} catch (IOException e) {e.printStackTrace();}		
	}

	public IndividualRecord addRecord(Vector<String> inputVector) {
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
		CopyOfLevelsProducer levelsProducer = new CopyOfLevelsProducer();
		int numFiles = 433;
		for (int i=1; i<=1;i++) {
			levelsProducer.run(i);
		}		
	}
}