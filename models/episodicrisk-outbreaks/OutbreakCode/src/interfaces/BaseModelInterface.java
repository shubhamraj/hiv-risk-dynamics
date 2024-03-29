package interfaces;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import cluster.ClusterRecorder;

/**
 * 
 * @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 *
 */
public interface BaseModelInterface extends ParametersInterface {
	/** Method to be called from the implemented class' constructor
	 * or a setup function to create the member instance of the 
	 * {@link ClusterRecorder} class. */
	@SuppressWarnings("rawtypes")
	public void createClusterRecorder(String _prefix, OutbreakType _outbreakType, Class transmissionClass);
	/** This method must be called in a function (e.g. run) or 
	 * called separately such that it is scheduled at every
	 * time step. The body of this function must call the 
	 * step function of the ClusterRecorder. */
	public void callClusterRecorderStep();
	/** This function must be called at the time of a transmission event. 
	 * The body of this method must call the ClusterRecorder's method
	 * clusterRecorder.recordTransmission(infector, susceptible), passing 
	 * the infector and susceptible agents as arguments 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException */
	public void addTransmissionToClusterRecord(AgentInterface infector, AgentInterface susceptible);
	/** Updates agents early infection outbreak record, when the agent is dead. */
	public void updateClusterRecord(AgentInterface agent);
	/** This function must return the current size of agent's population. */
	public int returnPopulationSize();
	/** This body of this function must iterate over all agents
	 * in the model and for each agent must call the {@link Method} resetOutbreakRecord */
	public void resetIndividualsOutbreakRecord();
	/** This method must add to the ArrayList<String> output, the 
	 * string which contains population-level outputs such as
	 * endemic prevalence, fraction of transmissions from HIV etc.*/
	public void addToOutput(String strOutput);
	/** This method sends the output ArrayList<String> to the {@link PrintWriter} 
	 * in the respective controller class to output PopulationStats.csv */	
	public ArrayList<String> getOutput();
	/** Returns the member instance {@link ClusterRecorder} */
	public ClusterRecorder getClusterRecorder();
	/** Returns the number of agents who are currently infected. */
	public int getNumHIV();
	/** Returns the AHI transmission potential that is used to
	 * calculate the transmission probabilities betaAHI and betaCHI */
	public double getAhiTransPotential();
	/** Returns the current time step (tick) in the simulation schedule. */
	public int getCurrentTick();
	/** Returns the maximum iterations (ticks) for this simulation is setup. */
	public int getMaximumIterations();
}