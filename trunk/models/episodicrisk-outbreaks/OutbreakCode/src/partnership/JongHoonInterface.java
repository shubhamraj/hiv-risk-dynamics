package partnership;

import interfaces.AgentInteface;
import interfaces.ParametersInterface;

public interface JongHoonInterface extends AgentInteface, ParametersInterface {
	public int getExitTick();
	public void setExitTick(int exitTick);
	public int getEntryTick();
	public void setEntryTick(int entryTick);
}
