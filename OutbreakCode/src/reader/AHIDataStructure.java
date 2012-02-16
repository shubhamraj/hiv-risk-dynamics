package reader;

import interfaces.ParametersInterface;


/**
 * 
 * @author shah
 *
 */
public class AHIDataStructure implements ParametersInterface {
	private Integer rootID = -1;
	private Integer obID = -1; 
	private Integer linkedOBID = -1;
	private double[] data;
	
	public AHIDataStructure(Integer _rootID) {
		rootID = _rootID;
		data = new double[Output.values().length];
	}
	
	public String returnOutput() {
		String str = obID + "," + rootID + "," + linkedOBID + ",";
		for (Output output : Output.values()) {
			str += data[output.ordinal()] + ",";
		}		
		return str;
	}
	
	public void addOutput(Output output, double val) {
		data[output.ordinal()] = val;
	}
	
	public double getOutput(Output output) {
		return data[output.ordinal()]; 
	}
	
	public void print() {
		String str = obID + "," + rootID + "," + linkedOBID + ",";
		for (Output output : Output.values()) {
			str += data[output.ordinal()] + ",";
		}		
		System.out.println(str);
	}

	public double[] getData() {
		return data;
	}

	public void setData(double[] data) {
		this.data = data;
	}

	public Integer getRootID() {
		return rootID;
	}

	public void setRootID(Integer rootID) {
		this.rootID = rootID;
	}

	public Integer getObID() {
		return obID;
	}

	public void setObID(Integer obID) {
		this.obID = obID;
	}

	public Integer getLinkedOBID() {
		return linkedOBID;
	}

	public void setLinkedOBID(Integer linkedOBID) {
		this.linkedOBID = linkedOBID;
	}
}