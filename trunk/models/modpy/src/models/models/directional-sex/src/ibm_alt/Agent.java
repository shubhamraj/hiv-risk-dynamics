package ibm_alt;


public class Agent {
	int id = -1;
	int infectedTick = -1;

	Model model;
	String infectionStatus = "susceptible";
	//0->NoSex; 1->Insertives; 2->Receptives; 3->Duals
	int cohort = -1;
	String behavior = "no-sex";
	boolean death = false;
	//0->PHI; 1->PostPHI
	int infector = -1;
	
	double phiDuration = 0;

	public Agent(int _id, int _cohort, Model _model) {		 
		id = _id;
		cohort = _cohort;
		model = _model;
//		phiDuration = model.getPsr().nextErlang(0.83, 3);
//		phiDuration = Uniform.staticNextDoubleFromTo(30, 40);
//		phiDuration = Uniform.staticNextDoubleFromTo(25, 35);
//		do {
//			phiDuration = Normal.staticNextDouble(35, 5);
//		} while (phiDuration > 40 || phiDuration < 30);
		//try log normal distribution
		//try erlang distribution
		//introduce mid stage
		//introduce infectiousness for asymptomatic stage
		//
		
	}
	
	public void step(int currentTick) {
		double d = 9000;
		if (Math.random()<= 1/d) {
			death = true;
			return;
		}
		if (infectionStatus != "susceptible") {
			updateInfectionStatus(currentTick);
		}
	}
	

/*	public void updateInfectionStatus(int currentTick) {
		double d0 = 3240;
		double d2 = (double) (1/d0);		
		if (infectionStatus == "PHI") {
			if (currentTick - this.infectedTick >= this.phiDuration) {
				infectionStatus = "Post-PHI";
			}
		}
		else if (infectionStatus == "Post-PHI"){
			if (Math.random() <= d2) {
				death = true;
			}			
		}
	} */

	/*public void updateInfectionStatus(int currentTick) {
		double rand = Math.random(); 
//		double d1 = (1-Math.exp(-model.PPR))/30;
		double denom = 30/(phiDuration);
		double d1 = (1-Math.exp(-denom))/30;
		double d2 = (1-Math.exp(-model.APR))/30;
//		d2 = model.returnProbability(model.APR)/30;
		double d0 = 3240;
//		d1 = (double)(1/d);
		d2 = (double) (1/d0);
//		model.print("----------------------------------d1: " + d2);
		if (infectionStatus == "PHI"
//			&& Math.random() <= 1/model.S			
			&& rand <= 1/this.phiDuration
//			&& rand <= d1
			) {
			infectionStatus = "Post-PHI";
		}
		else if (infectionStatus == "Post-PHI" 
//					&& Math.random()<= 1/model.T
			&& rand <= d2
					) {
			death = true;		
			} 	
	}
	*/

/*	public void updateInfectionStatus(int currentTick) {
		double rand = Math.random(); 
		double d1 = (1-Math.exp(-model.PPR))/30;
		double d2 = (1-Math.exp(-model.APR))/30;
//		d2 = model.returnProbability(model.APR)/30;
		double d0 = 3240;
//		d1 = (double)(1/d);
		d2 = (double) (1/d0);
//		model.print("----------------------------------d1: " + d2);
		if (infectionStatus == "PHI"
//			&& Math.random() <= 1/model.S			
			&& rand <= d1
			) {
			infectionStatus = "Post-PHI";
		}
		else if (infectionStatus == "Post-PHI" 
//					&& Math.random()<= 1/model.T
			&& rand <= d2
					) {
			death = true;		
			} 	
	}*/
		
	
	public void updateInfectionStatus(int currentTick) {
		double rand = Math.random(); 
		double d1 = (1-Math.exp(-model.PPR))/30;
		double d2 = (1-Math.exp(-model.APR))/30;
//		d2 = model.returnProbability(model.APR)/30;
		double d = 36, d0 = 3240;
//		d1 = (double)(1/d);
		d2 = (double) (1/d0);
//		model.print("----------------------------------d1: " + d2);
		if (infectionStatus == "PHI"
//			&& Math.random() <= 1/model.S			
			&& rand <= d1
			) {
			infectionStatus = "Post-PHI";
		}
		else if (infectionStatus == "Post-PHI" 
//					&& Math.random()<= 1/model.T
			&& rand <= d2
					) {
			death = true;		
			} 	
	}

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

	public int getCohort() {
		return cohort;
	}

	public void setCohort(int cohort) {
		this.cohort = cohort;
	}

	public String getBehavior() {
		return behavior;
	}

	public void setBehavior(String behavior) {
		this.behavior = behavior;
	}

	public int getInfector() {
		return infector;
	}

	public void setInfector(int infector) {
		this.infector = infector;
	}
	
	public int getInfectedTick() {
		return infectedTick;
	}


	public void setInfectedTick(int infectedTick) {
		this.infectedTick = infectedTick;
	}

}
