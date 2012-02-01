package interfaces;

import java.util.ArrayList;

import cluster.ClusterRecorder;

public interface BaseModelInterface {
	public void createClusterRecorder();
	public void run();
	public void addTransmissionToClusterRecord(AgentInteface infector, AgentInteface susceptible);
	public int returnPopulationSize();
	public void resetIndividualsOutbreakRecord();
	public ArrayList<String> getOutput();
	public ClusterRecorder getClusterRecorder();
	public int getNumHIV();
	public double getAhiTransPotential();
	public int getCurrentTick();
	public int getMaximumIterations();
}
