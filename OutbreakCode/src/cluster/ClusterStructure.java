package cluster;

/**
 * 
 * @author Shah Jamal Alam, Koopman Lab (Dynamic Causal Systems in Epidemiologic Analysis), University of Michigan 2011.
 *
 */
public class ClusterStructure {
	private Integer clusterID = -1;
	private int size = 0;
	private int time = 0;
	private int timeLastTransmission = 0;
	
	public ClusterStructure(Integer _clusterID, int _size) {
		clusterID = _clusterID;
		size = _size;
	}

	public Integer getClusterID() {
		return clusterID;
	}

	public void setClusterID(Integer clusterID) {
		this.clusterID = clusterID;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getTimeLastTransmission() {
		return timeLastTransmission;
	}

	public void setTimeLastTransmission(int timeLastTransmission) {
		this.timeLastTransmission = timeLastTransmission;
	}
}
