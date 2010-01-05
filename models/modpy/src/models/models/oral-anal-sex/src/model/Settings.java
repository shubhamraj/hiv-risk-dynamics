package model;

public class Settings {
	public static final boolean RECORD_DATABASE = false;
	public static final int NUMBER_AGENTS =  20000;
	public static final int MAXIMUM_STEPS = 45000;
	public static final int NUM_ENROLLED_AGENTS = 0;
	public static final int ENROLLED_TIME = 40000;
	public static final int STUDY_DURATION = 720;
	public static final int NUMBER_STUDY_PERIODS = 4;
	//6 months
	public static final int STUDY_PERIOD_LENGTH = 180;
	public static final double CHANCE_FOR_GETTING_ENROLLED = 0.10;
	
	public static final double ANAL_SEX_PROPORTION = 0.5;
	public static final double ANAL_ORAL_RISK_RATIO = 100;		

	public static class INFECTION_STATUS {
		public static final int SUSCEPTIBLE = 0;
		public static final int PHI = 1;
		public static final int POST_PHI = 2;
	}
	
	public static final int SEXUAL_ACTIVE_AGE = 30;
	public static final double BASELINE_PROBABILITY =  0.001;
	public static final double PHI_INFECTIVITY_FACTOR =  38;

	public static class STAGE_DURATION {
		public static final double PHI_DURATION = 60;
		public static final double POST_PHI_DURATION = 2520;
		//1/60 DAYS (2 months)
		public static final double PROB_STAY_PHI = 0.016667;
		//1/12 years Or 1/4320 days
		public static final double PROB_STAY_POSTPHI = 2.3148E-4;
	}
	
	public static final double DT = 0.0175;
	public static final double PARTNERSHIP_PROBABILITY = 0.6;			
	public static class CONTACT_TYPE {
		public static final int ORAL = 0;
		public static final int ANAL = 1;
	}			
	public static final double PERCENTAGE_INITIAL_INFECTIONS = 0.01;	
}