package cluster;

import model.Individual;

/**
 * 
 * @author shah
 *
 */
public class Transmission extends BaseTransmission { 
	private RISK_STATE infectorRiskState = RISK_STATE.NONE;
	private RISK_STATE infectedRiskState = RISK_STATE.NONE;
	private MIXING_SITE mixingSite = MIXING_SITE.NONE;
	
	public Transmission() {
		infectorRiskState = RISK_STATE.NONE;
		infectedRiskState = RISK_STATE.NONE;
		mixingSite = MIXING_SITE.NONE;
	}
	
	public Transmission(Integer _time, Individual infector, Individual infected) {
		super(_time, infector, infected);
		infectorRiskState = infector.getRiskState();
		infectedRiskState = infected.getRiskState();
		mixingSite = infected.getInfectedMixingSite();
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