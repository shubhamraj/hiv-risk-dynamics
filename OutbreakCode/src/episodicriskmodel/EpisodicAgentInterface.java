package episodicriskmodel;

import interfaces.AgentInterface;

/**
 * 
 * @author shah
 *
 */
public interface EpisodicAgentInterface extends AgentInterface {
	public RiskState getRiskState();
	public void setRiskState(RiskState riskState);
	public RiskState getInfectedRiskState();
	public void setInfectedRiskState(RiskState infectedRiskState);
	public MixingSite getInfectedMixingSite();
	public void setInfectedMixingSite(MixingSite infectedMixingSite);
}
