package partnership;

import interfaces.AgentInteface;
import interfaces.ParametersInterface;

public interface PartnershipAgentInterface extends AgentInteface, PartnershipParametersInterface {
	public int getExitTick();
	public void setExitTick(int exitTick);
	public int getEntryTick();
	public void setEntryTick(int entryTick);
}
