package basemodel;

import java.util.ArrayList;

import cluster.ClusterRecorder;

public interface BaseModelInterface {
	public void setup();
	public void run();
	public int returnPopulationSize();
	public void resetIndividualsOutbreakRecord();
	public ArrayList<String> getOutput();
	public ClusterRecorder getClusterRecorder();
	public int getNumHIV();
	public double getAhiTransPotential();
	public int getCurrentTick();
	public void addTransmissionToClusterRecord(AgentInteface infector, AgentInteface susceptible);
}
