package model;

/**
 * 
 * @author shah
 *
 */
public class Individual extends Person {
	private int entryTick = -1;
	private int exitTick = -1;
	
	private RISK_STATE riskState = RISK_STATE.NONE;		
	private RISK_STATE infectedRiskState = RISK_STATE.NONE;
	private MIXING_SITE infectedMixingSite = MIXING_SITE.NONE;
		
	public Individual() {
	}
	
	public Individual(int _roodID) {
		this.ID = _roodID;
	}
	
	public void step(int currentTick) {	
		super.step(currentTick);
	}
		
	public ACT_TYPE getActType() {
		return actType;
	}

	public void setActType(ACT_TYPE actType) {
		this.actType = actType;
	}

	public RISK_STATE getRiskState() {
		return riskState;
	}

	public void setRiskState(RISK_STATE riskState) {
		this.riskState = riskState;
	}
	
	public RISK_STATE getInfectedRiskState() {
		return infectedRiskState;
	}

	public void setInfectedRiskState(RISK_STATE infectedRiskState) {
		this.infectedRiskState = infectedRiskState;
	}

	public MIXING_SITE getInfectedMixingSite() {
		return infectedMixingSite;
	}

	public void setInfectedMixingSite(MIXING_SITE infectedMixingSite) {
		this.infectedMixingSite = infectedMixingSite;
	}

	public int getExitTick() {
		return exitTick;
	}

	public void setExitTick(int exitTick) {
		this.exitTick = exitTick;
	}

	public int getEntryTick() {
		return entryTick;
	}

	public void setEntryTick(int entryTick) {
		this.entryTick = entryTick;
	}
}