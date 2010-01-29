package reader;

public class IndividualRecord {
	private int id = -1;
	private int infected = -1;
	private int numWaves = -1;
	private int numOralSusc = -1;
	private int numOralAcute = -1;
	private int numOralChronic = -1;
	private int numAnalSusc = -1;
	private int numAnalAcute = -1;
	private int numAnalChronic = -1;

	public IndividualRecord() {		
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getInfected() {
		return infected;
	}
	public void setInfected(int infected) {
		this.infected = infected;
	}
	public int getNumWaves() {
		return numWaves;
	}
	public void setNumWaves(int numWaves) {
		this.numWaves = numWaves;
	}
	public int getNumOralSusc() {
		return numOralSusc;
	}
	public void setNumOralSusc(int numOralSusc) {
		this.numOralSusc = numOralSusc;
	}
	public int getNumOralAcute() {
		return numOralAcute;
	}
	public void setNumOralAcute(int numOralAcute) {
		this.numOralAcute = numOralAcute;
	}
	public int getNumOralChronic() {
		return numOralChronic;
	}
	public void setNumOralChronic(int numOralChronic) {
		this.numOralChronic = numOralChronic;
	}
	public int getNumAnalSusc() {
		return numAnalSusc;
	}
	public void setNumAnalSusc(int numAnalSusc) {
		this.numAnalSusc = numAnalSusc;
	}
	public int getNumAnalAcute() {
		return numAnalAcute;
	}
	public void setNumAnalAcute(int numAnalAcute) {
		this.numAnalAcute = numAnalAcute;
	}
	public int getNumAnalChronic() {
		return numAnalChronic;
	}
	public void setNumAnalChronic(int numAnalChronic) {
		this.numAnalChronic = numAnalChronic;
	}	
}