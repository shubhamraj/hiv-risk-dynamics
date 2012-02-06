package episodicriskmodel;

import cluster.BaseTransmission;
import interfaces.AgentInterface;

/**
 * 
 * @author shah
 *
 */
public class EpisodicRiskTransmission extends BaseTransmission { 
	private RISK_STATE infectorRiskState = RISK_STATE.None;
	private RISK_STATE infectedRiskState = RISK_STATE.None;
	private MIXING_SITE mixingSite = MIXING_SITE.None;
	
	public EpisodicRiskTransmission() {
		infectorRiskState = RISK_STATE.None;
		infectedRiskState = RISK_STATE.None;
		mixingSite = MIXING_SITE.None;
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

	public RISK_STATE getInfectorRiskState() {
		return infectorRiskState;
	}

	public void setInfectorRiskState(RISK_STATE infectorState) {
		this.infectorRiskState = infectorState;
	}

	public RISK_STATE getInfectedRiskState() {
		return infectedRiskState;
	}

	public void setInfectedRiskState(RISK_STATE infectedState) {
		this.infectedRiskState = infectedState;
	}

	public MIXING_SITE getMixingSite() {
		return mixingSite;
	}

	public void setMixingSite(MIXING_SITE mixingSite) {
		this.mixingSite = mixingSite;
	}
}