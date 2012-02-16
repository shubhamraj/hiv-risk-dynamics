package episodicriskmodel;

import cluster.BaseTransmission;
import interfaces.AgentInterface;

/**
 * @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 *
 */
public class EpisodicRiskTransmission extends BaseTransmission { 
	private RiskState infectorRiskState = RiskState.None;
	private RiskState infectedRiskState = RiskState.None;
	private MixingSite mixingSite = MixingSite.None;
	
	public EpisodicRiskTransmission() {
		infectorRiskState = RiskState.None;
		infectedRiskState = RiskState.None;
		mixingSite = MixingSite.None;
	}

	public EpisodicRiskTransmission(Integer _time, AgentInterface infector, AgentInterface infected) {
		super(_time, infector, infected);
		/** Here type-casting to allow more features to be recorded in the transmissions output. */
		infectorRiskState = ((EpisodicRiskAgent) infector).getRiskState();
		infectedRiskState = ((EpisodicRiskAgent) infected).getRiskState();
		mixingSite = ((EpisodicRiskAgent) infected).getInfectedMixingSite();
	}
	
	/** add further data to be written. */
	public String toString() {
		String str = super.toString();
		str += "," + getInfectorRiskState() + "," + infectedRiskState + "," + mixingSite; 
		return str;
	}

	public RiskState getInfectorRiskState() {
		return infectorRiskState;
	}

	public void setInfectorRiskState(RiskState infectorState) {
		this.infectorRiskState = infectorState;
	}

	public RiskState getInfectedRiskState() {
		return infectedRiskState;
	}

	public void setInfectedRiskState(RiskState infectedState) {
		this.infectedRiskState = infectedState;
	}

	public MixingSite getMixingSite() {
		return mixingSite;
	}

	public void setMixingSite(MixingSite mixingSite) {
		this.mixingSite = mixingSite;
	}
}