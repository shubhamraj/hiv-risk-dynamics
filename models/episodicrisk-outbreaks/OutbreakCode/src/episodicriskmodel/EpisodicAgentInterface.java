package episodicriskmodel;

import interfaces.AgentInterface;

/**
 * 
 * @author shah
 *
 */
public interface EpisodicAgentInterface extends AgentInterface {
	public RISK_STATE getRiskState();
	public void setRiskState(RISK_STATE riskState);
	public RISK_STATE getInfectedRiskState();
	public void setInfectedRiskState(RISK_STATE infectedRiskState);
	public MIXING_SITE getInfectedMixingSite();
	public void setInfectedMixingSite(MIXING_SITE infectedMixingSite);
	public int getExitTick();
	public void setExitTick(int exitTick);
	public int getEntryTick();
	public void setEntryTick(int entryTick);
}
