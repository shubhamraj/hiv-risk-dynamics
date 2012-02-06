package cluster;

import interfaces.AgentInterface;
import interfaces.BaseModelInterface;
import interfaces.ParametersInterface;

import java.io.PrintWriter;

/**
 * 
 * @author shah
 *
 */
public class ClusterRecorder implements ParametersInterface {
	private BaseModelInterface model;
	private ClusterEngine[] clusterEngine;

	@SuppressWarnings("rawtypes")
	public ClusterRecorder(BaseModelInterface _model, String prefix, OutbreakType outbreakType, Class transmissionClass) {
		String transmissionClassName = transmissionClass.getCanonicalName();
		this.model = _model; 
		int numEngines = OutbreakRecord.values().length;
		clusterEngine = new ClusterEngine[numEngines];		
		clusterEngine = new ClusterEngine[OutbreakRecord.values().length];		
		for (OutbreakRecord outbreakRecord : OutbreakRecord.values()) {
			clusterEngine[outbreakRecord.ordinal()] = new ClusterEngine(prefix, outbreakType, 
					outbreakRecord, model.getMaximumIterations(), transmissionClassName);
		}			
	}

	/** */
	public void step() {
		startClusterEngine();
		checkClusterEngine();
		recordPopulationVariables();
	}

	public void recordOutput(int run, PrintWriter[] incidenceWriter, PrintWriter[] outbreakWriter, PrintWriter[] plfitWriter) {
		if (RecordOutbreak) {
			try {
				for (OutbreakRecord obRecord : OutbreakRecord.values()) {
					clusterEngine[obRecord.ordinal()].writeJointDistSummary(run, incidenceWriter, outbreakWriter);
					clusterEngine[obRecord.ordinal()].saveExtStats(plfitWriter);
				}					
			} catch (Exception e) {
				System.err.println("Error in ClusterRecorder::recordOutput.");
				e.printStackTrace();
			}			
		}
	}

	public void updateEpisodicEpisodicAgentClusterRecord(AgentInterface agent) {
		if (RecordOutbreak) {
			int currentTick = model.getCurrentTick();
			for (OutbreakRecord outbreakRecord : OutbreakRecord.values()) {
				ClusterEngine engine = clusterEngine[outbreakRecord.ordinal()];
				if (engine.isStopped() == false) {
					if (agent.isRemovedAHICluster() == false
							&& (agent.isDead()
									|| (agent.getAHIClusterID() != -1
											&& agent.isInfected() == true
											&& engine.outbreakCriteria(new Integer(currentTick), agent) == false))) {				
						engine.removeTimeRecord(new Integer(currentTick), agent);
					}
				}
			}
		}	
	}

	public void recordTransmission(AgentInterface infector, AgentInterface susceptible) {
		if (RecordOutbreak) {
			int currentTick = model.getCurrentTick();			
			for (OutbreakRecord outbreakRecord : OutbreakRecord.values()) {
				ClusterEngine engine = clusterEngine[outbreakRecord.ordinal()];
				if (engine.shouldRecordTransmission(currentTick)) {
					engine.addTransmission(currentTick, infector, susceptible);
				}
			}
		}

	}

	private void startClusterEngine() {
		if (RecordOutbreak) {
			for (OutbreakRecord outbreakRecord : OutbreakRecord.values()) {
				ClusterEngine engine = clusterEngine[outbreakRecord.ordinal()];
				if (engine.isStopped() == false) {			
					engine.shouldBeginRecording(model.getCurrentTick());
				}
			}
		}
	}	

	private void checkClusterEngine() {
		if (RecordOutbreak) {
			for (OutbreakRecord outbreakRecord : OutbreakRecord.values()) {
				ClusterEngine engine = clusterEngine[outbreakRecord.ordinal()];
				if (engine.isStopped() == false) {
					engine.step(new Integer(model.getCurrentTick()));
					if (engine.isStopped()) {
						//Output all and early infection transmissions
						engine.outputRecord();
						model.resetIndividualsOutbreakRecord();
					}
				}
			}
		}		
	}

	private void recordPopulationVariables() {
		if (RecordOutbreak) {
			for (OutbreakRecord outbreakRecord : OutbreakRecord.values()) {
				ClusterEngine recorder = clusterEngine[outbreakRecord.ordinal()];
				if (recorder.isStopped() == false) {
					recorder.setPopulationStats(model.getCurrentTick(), (int)model.getNumHIV(), model.returnPopulationSize());
				}
			}
		}
	}

	public ClusterEngine[] getClusterEngine() {
		return clusterEngine;
	}

	public void setClusterEngine(ClusterEngine[] clusterEngine) {
		this.clusterEngine = clusterEngine;
	}	
}