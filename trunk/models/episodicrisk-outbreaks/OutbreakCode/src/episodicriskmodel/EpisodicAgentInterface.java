package episodicriskmodel;

import interfaces.AgentInterface;

/**
 * 
 * @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
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
