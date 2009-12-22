package reader;

import java.util.HashMap;

public class Individual {
	private int id = -1;
	private String stringID = "";	
	private boolean seroconverted = false;
	public int totalVisits = 0; 		
	public HashMap<Integer, Double[]> record = new HashMap<Integer, Double[]>();
	
	Individual() {}

	public String getStringId() {
		return stringID;
	}

	public void setStringId(String stringID) {
		this.stringID = stringID;
	}

	public boolean isSeroconverted() {
		return seroconverted;
	}

	public void setSeroconverted(boolean seroconverted) {
		this.seroconverted = seroconverted;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTotalVisits() {
		return totalVisits;
	}

	public void setTotalVisits(int totalVisits) {
		this.totalVisits = totalVisits;
	}
}