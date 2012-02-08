package partnershipmodel;


import java.util.ArrayList;
import java.util.Iterator;

import cluster.Edge;
import uchicago.src.sim.network.DefaultDrawableNode;

import cern.jet.random.Uniform;
import interfaces.AgentInterface;

/**
 * 
 * Jong-Hoon Kim's model integrated with outbreaks library
 *
 */
public class PartnershipAgent extends DefaultDrawableNode implements PartnershipAgentInterface {
	static int lastID = -1;
	private int ID = -1;
	private InfectionStage stageOfInfection = InfectionStage.Susceptible;
	private InfectionStage infectorStageOfInfection = InfectionStage.Susceptible;
	private ActType actType = ActType.None;

	private int infectedTick = -1;
	private int infectorID = -1;
	private int CHITick = -1;

	/**AHI Cluster ID of the individual */
	private Integer ahiClusterID = -1;
	private int timeLastInfection = 0;
	private int timePenultimateInfection = 0;
	private int outbreakStartTime = -1;

	private boolean removedAHICluster = false;	
	private boolean dead = false;	
	private boolean root = false;

	private int entryTick = -1;
	private int exitTick = -1;
	
	private int lifeTimePartners = 0;
	private int numInfectee = 0;

	public PartnershipAgent() {
		this.ID = ++lastID;
	}

	public PartnershipAgent(int id) {
		this.ID = id;
	}

	public void step(int currentTick) {
		if (Uniform.staticNextDouble() <= ((double)1/DurationLife)) {
			dead = true;
			return;
		}
		if (stageOfInfection != InfectionStage.Susceptible) {
			updateInfectionStatus(currentTick);
		}
	}

	public void updateInfectionStatus(int currentTick) {
		double rand = Uniform.staticNextDouble();
		if (stageOfInfection.equals(InfectionStage.Acute)
				&& rand <= ((double)1/DurationAHI)) {
			stageOfInfection = InfectionStage.Chronic;
			setCHITick(currentTick);
		}

		else if (stageOfInfection.equals(InfectionStage.Chronic) 
				&& rand <= ((double)1/DurationCHI)) {
			setDead(true);		
		} 	
	}

	public void makeContactToFrom(PartnershipAgent toNode, ArrayList<Edge> list) {
		if (!hasEdgeTo(toNode)) {
			Edge e0 = new Edge(this, toNode);
			this.addOutEdge(e0);
			toNode.addInEdge(e0);
			Edge e1 = new Edge(toNode, this);
			toNode.addOutEdge(e1);
			this.addInEdge(e1);

			list.add(e0);
			
			// increase the number of life-time partners
			this.setLifeTimePartners( this.getLifeTimePartners() + 1 );
			toNode.setLifeTimePartners( toNode.getLifeTimePartners() + 1 );
		}
	}

	@Override
	public int getID() {
		return this.ID;
	}

	@Override
	public void setID(int id) {
		this.ID = id;
	}

	@Override
	public boolean isAHI() {
		return stageOfInfection.equals(InfectionStage.Acute);
	}

	@Override
	public boolean isInfected() {
		return stageOfInfection.equals(InfectionStage.Acute) || stageOfInfection.equals(InfectionStage.Chronic);
	}

	@Override
	public boolean isSusceptible() {
		return stageOfInfection.equals(InfectionStage.Susceptible);
	}

	@Override
	public void setInfectionTimes(int currentTime) {
		if (timePenultimateInfection == 0) {
			timePenultimateInfection = currentTime;
			timeLastInfection = currentTime;
			return;
		}
		else {
			timePenultimateInfection = timeLastInfection;
			timeLastInfection = currentTime;
		}
	}

	@Override
	public int returnInfectionDuration() {
		return (timeLastInfection - timePenultimateInfection + 1);
	}

	@Override
	public boolean infectedByAHI() {
		return infectorStageOfInfection.equals(InfectionStage.Acute) ? true : false;
	}

	@Override
	public boolean isDead() {
		return this.dead;
	}

	@Override
	public boolean isRemovedAHICluster() {
		return this.removedAHICluster;
	}

	@Override
	public void setRemovedAHICluster(boolean removedAHICluster) {
		this.removedAHICluster = removedAHICluster;
	}

	@Override
	public void setDead(boolean dead) {
		this.dead = dead;
	}

	@Override
	public InfectionStage getInfectorInfectionStage() {
		return this.infectorStageOfInfection;
	}

	@Override
	public void setInfectorInfectionStage(InfectionStage infectorStage) {
		this.infectorStageOfInfection = infectorStage;
	}

	@Override
	public Integer getAHIClusterID() {
		return this.ahiClusterID;
	}

	@Override
	public void setAHIClusterID(Integer ahiCluster) {
		this.ahiClusterID = ahiCluster;
	}

	@Override
	public InfectionStage getStageOfInfection() {
		return this.stageOfInfection;
	}

	@Override
	public void setStageOfInfection(InfectionStage infectionStage) {
		this.stageOfInfection = infectionStage;
	}

	@Override
	public int getOutbreakStartTime() {
		return this.outbreakStartTime;
	}

	@Override
	public void setOutbreakStartTime(int outbreakStartTime) {
		this.outbreakStartTime = outbreakStartTime;
	}

	@Override
	public int getInfectedTick() {
		return this.infectedTick;
	}

	@Override
	public void setInfectedTick(int infectedTick) {
		this.infectedTick = infectedTick;
	}

	@Override
	public int getInfectorID() {
		return this.infectorID;
	}

	@Override
	public void setInfectorID(int infectorID) {
		this.infectorID = infectorID;
	}

	@Override
	public int getCHITick() {
		return this.CHITick;
	}

	@Override
	public void setCHITick(int cHITick) {
		this.CHITick = cHITick;
	}

	@Override
	public boolean isRoot() {
		return this.root;
	}

	@Override
	public void setRoot(boolean root) {
		this.root = root;
	}

	@Override
	public ActType getActType() {
		return this.actType;
	}

	@Override
	public void setActType(ActType actType) {
		this.actType = actType;
	}

	@Override
	public boolean equals(AgentInterface agent) {
		return this.ID == agent.getID() ? true : false;
	}

	@Override
	public int getTimeLastInfection() {
		return this.timeLastInfection;
	}

	@Override
	public void setTimeLastInfection(int timeLastInfection) {
		this.timeLastInfection = timeLastInfection;
	}

	@Override
	public int getTimePenultimateInfection() {	
		return this.timePenultimateInfection;
	}

	@Override
	public void setTimePenultimateInfection(int timePenultimateInfection) {
		this.timePenultimateInfection = timePenultimateInfection;
	}

	/** This method is called to reset an agent's record of 
	 * a membership in an early infection outbreak. 
	 * An implementation of this method must call the following methods. 
	 * 		setAHIClusterID(-1);
			setRemovedAHICluster(false);
			setOutbreakStartTime(-1);
	 */
	@Override
	public void resetOutbreakRecord() {
		setAHIClusterID(-1);
		setRemovedAHICluster(false);
		setOutbreakStartTime(-1);
	}

	@Override
	public void print() {
		System.out.println("Person-" + this.ID);		
	}

	@Override
	public int getExitTick() {
		return this.exitTick;
	}

	@Override
	public void setExitTick(int exitTick) {
		this.exitTick = exitTick;
	}

	@Override
	public int getEntryTick() {
		return this.entryTick;
	}

	@Override
	public void setEntryTick(int entryTick) {
		this.entryTick = entryTick;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void removeEdgesFromList(ArrayList<Edge> edgelist) {
		ArrayList<Edge> inEdges = getInEdges();
		for (int i=0; i<inEdges.size(); i++){
			Edge e = inEdges.get( i );
			for (Iterator<Edge> eiter = edgelist.iterator(); eiter.hasNext();) {
				Edge ee = eiter.next();
				if (e == ee) {
					eiter.remove();
				}
			}
		}

		ArrayList<Edge> outEdges = getOutEdges();
		for (int i=0; i<outEdges.size(); i++) {
			Edge e = outEdges.get( i );
			for (Iterator<Edge> eiter = edgelist.iterator(); eiter.hasNext();) {
				Edge ee = eiter.next();
				if (e == ee) {
					eiter.remove();
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void dissolveEdges() {
		ArrayList<PartnershipAgent> inNodes = getInNodes();
		for (int i=0; i<inNodes.size(); i++) {
			PartnershipAgent node = inNodes.get( i );
			this.removeEdgesFrom( node );
			node.removeEdgesTo( this );
			this.removeEdgesTo( node );
			node.removeEdgesFrom( this );
		}			
	}

	@Override
	public int getLifeTimePartners() {
		return this.lifeTimePartners;
	}

	@Override
	public void setLifeTimePartners(int lifeTimePartners) {
		this.lifeTimePartners =  lifeTimePartners;
	}

	@Override
	public int getNumInfectee() {
		return this.numInfectee;
	}

	@Override
	public void setNumInfectee(int numInfectee) {
		this.numInfectee = numInfectee;
	}
}