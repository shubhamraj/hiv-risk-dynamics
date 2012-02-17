package partnershipmodel;

import interfaces.AgentInterface;
import cluster.BaseTransmission;
/**
 * 
 * Jong-Hoon Kim's model integrated with outbreaks library (c.f. Kim et al. 2010)
 * @author Jong-Hoon Kim (original)
 * @author Modified by: Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
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
