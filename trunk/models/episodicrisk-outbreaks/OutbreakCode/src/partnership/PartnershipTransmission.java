package partnership;

import interfaces.AgentInterface;
import cluster.BaseTransmission;
/**
 * 
 * Jong-Hoon Kim's model integrated with outbreaks library
 *
 */

/** Add more stuff if we want to extend the base transmission */
public class PartnershipTransmission extends BaseTransmission {
	public PartnershipTransmission(Integer _time, AgentInterface infector, AgentInterface infected) {
		super(_time, infector, infected);
	}
	
	public String toString() {
		String str = super.toString();
		str += "," + "Partnershipstub"; 
		return str;
	}
}
