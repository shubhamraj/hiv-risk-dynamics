package model;


public class Individual {
	int id = -1;
	int infectedTick = -1;
	DirectionalSexModel model;
	//insertive; receptive; dual-role
	String behavior = "";
	String infectionStatus = "susceptible";
	//insertive-only, receptive-only, dual-role, no-sex
	String category = "no-sex";
	boolean death = false;
	//0->PHI; 1->PostPHI
	int infectorID = -1;
	String infectorStatus = "";
	public boolean contacted = false;
	double phiduration = 0;
	public double phiProbInsertive = 0, phiProbReceptive = 0;
	
	public Individual(int _id, String _category, DirectionalSexModel _model) {
		this.id = _id;
		this.category = _category;
		this.model = _model;
		setPHI();
//		model.print("Phi duration: " + phiduration);
	}
	
	public void setPHI() {
//		this.phiduration = 1/model.psr.nextGaussian(35, 5);
		this.phiduration = model.d1;
//		phiduration = 1/(model.psr.nextErlang(0.81, 3)*10);
		phiProbInsertive = model.psr.nextGaussian(model.aaI, 0.15*model.aaI);
		phiProbInsertive = model.psr.nextGaussian(model.aaR, 0.15*model.aaR);
//		
//		do {
//			this.phiduration = model.psr.nextGaussian(35, 5);
//		} while (phiduration >= 45 && phiduration <= 25);
//		phiduration = 1/phiduration;
	}

	public void step(int currentTick) {
		if (Math.random() <= (6.94E-5)) {
//		if (Math.random() <= (1.11E-4)) {
			death = true;
			return;
		}
		if (infectionStatus != "susceptible") {
			updateInfectionStatus(currentTick);
		}
	}
	
	public void updateInfectionStatus(int currentTick) {
		double rand = Math.random(); 
		if (infectionStatus == "PHI"
			&& rand <= this.phiduration) {
			infectionStatus = "Post-PHI";
		}
		else if (infectionStatus == "Post-PHI" 
			&& rand <= model.d2){
			death = true;		
		} 	
	}

/*	public void updateInfectionStatus(int currentTick) {
		double rand = Math.random(); 
		if (infectionStatus == "PHI"
			&& rand <= model.d1) {
			infectionStatus = "Post-PHI";
		}
		else if (infectionStatus == "Post-PHI" 
			&& rand <= model.d2){
			death = true;		
		} 	
	}*/
	
	public boolean isDeath() {
		return death;
	}

	public void setDeath(boolean exit) {
		this.death = exit;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getInfectionStatus() {
		return infectionStatus;
	}

	public void setInfectionStatus(String infectionStatus) {
		this.infectionStatus = infectionStatus;		
	}

	public String getBehavior() {
		return behavior;
	}

	public void setBehavior(String behavior) {
		this.behavior = behavior;
		contacted = false;
	}

	public int getInfectedTick() {
		return infectedTick;
	}

	public void setInfectedTick(int infectedTick) {
		this.infectedTick = infectedTick;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getInfectorID() {
		return infectorID;
	}

	public void setInfectorID(int infectorID) {
		this.infectorID = infectorID;
	}

	public String getInfectorStatus() {
		return infectorStatus;
	}

	public void setInfectorStatus(String infectorStatus) {
		this.infectorStatus = infectorStatus;
	}
	
	
	
}