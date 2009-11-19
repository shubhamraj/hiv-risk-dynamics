package simple;

public class Settings {
	
	public static final boolean RECORD_DATABASE = false;
	public static final int NUMBER_AGENTS =  10000;
	public static final int MAXIMUM_STEPS = 27000;
	public static final int NUM_ENROLLED_AGENTS = 1200;
	public static final int ENROLLED_TIME = 20000;
	
	public static final int STUDY_DURATION = 720;
	public static final int RECALL_PERIOD = 180;
	
	public static final double ANAL_SEX_PROPORTION = 0.5;
	public static final double ANAL_ORAL_RISK_RATIO = 100;
		
	public static class INFECTION_STATUS {
		public static final int SUSCEPTIBLE = 0;
		public static final int PRIMARY_INFECTION = 1;
		public static final int ASYMPTOMATIC_INFECTION = 2;
		public static final int LATE_INFECTION = 3;
		public static final int DEATH = 4;		
		public static final int CHRONIC = 5;
	}	
	public static final int SEXUAL_ACTIVE_AGE = 30;
	
	public static class TRANSMISSION_PROBABILITIES {
		public static final double PRIMARY_STAGE = 0.03604;
		public static final double ASYMPTOMATIC_STAGE = 0.00084;
		public static final double LATE_STAGE = 0.00421;
	}
	
	public static final double BASELINE_PROBABILITY =  0.001;
	public static final double FACTOR =  35;
	

	public static class STAGE_DURATION {
		public static final double PRIMARY_STAGE_DURATION = 60;
		public static final double ASYMPTOMATIC_STAGE_DURATION = 2520;
		public static final double LATE_STAGE_DURATION = 360;
		public static final double CHRONIC_DURATION = 3000;
	}	
	public static final double PARTNERSHIP_PROBABILITY = 0.6;			
	public static final double PARTNERSHIP_DISSOLUTION_PROBABILITY = 1;
	
	public static class CONTACT_TYPE {
		public static final int ORAL = 0;
		public static final int ANAL = 1;
		public static final int ORAL_AND_ANAL = 2;
	}		
	public static final double PERCENTAGE_INITIAL_INFECTIONS = 0.01;	
}
 