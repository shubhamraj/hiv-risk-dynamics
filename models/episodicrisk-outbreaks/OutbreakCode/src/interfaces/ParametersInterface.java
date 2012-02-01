package interfaces;

import org.apache.commons.math.stat.descriptive.moment.Skewness;
import org.apache.commons.math.stat.descriptive.rank.Percentile;

import flanagan.math.PsRandom;

public interface ParametersInterface {
	/** Skewness class used to calculate skewness of a given data. 
	 * It is used by the Stats class in the reader package. */
	public static Skewness skew = new Skewness();
	/** PsRandom from the Flanagan's library for calculation of descriptive statistics of a given data. 
	 * It is used by the Stats class in the reader package. */
	public static PsRandom psr = new PsRandom();
	/**  Percentile class used to calculate skewness of a given data. 
	 * It is used by the Stats class in the reader package.*/
	public static Percentile percentile = new Percentile();
	/** Maximum number of iterations for which the model shall run.*/
	public static final int MaxIterations = 105000;
	/** Initial population of agents/individuals in the model. */
	public static int InitialPopulation = 10000;
	/** Initial proportion of infection in the population. */
	public static final double InitialInfection = 0.1d;	
	/** Duration of sexually active life of a susceptible individual in the model. */
	public static final double DurationLife = 40*12*30d;	
	/** Types of infection stages. */
	public static enum InfectionStage {Susceptible, Acute, Chronic}
	/** Duration of stay in the Acute stage of infection. */
	public static final double DurationAHI = 2*30d;
	/** Duration of stay in the Chronic stage of infection. */
	public static final double DurationCHI = 120*30d;
	/** Base transmission probability from Vittinghoff et al. (1999) */
	public static final double BaseTransProb = 0.003d;
	/** Determines whether to record early infection outbreaks during simulation or not. */	
	public static final boolean RecordOutbreak = true;	
	/** Type of early infection outbreak. 
	 * Default is the AHI period, which follows the Exponential distribution with mean 2 months. 
	 * One may also have other definitions for an early infection outbreak such as Six-months or Two-years.
	 * Used by the ClusterRecorder (constructor) and ClusterEngine (constructor and outbreakCriteria method) .  
	 * */
	public static enum OutbreakType {AHI, Six_Months, Two_Years}
	/** Types of sex acts. May be extended depending upon the implemented transmission model. */
	public static enum ActType {None, Acute_Susceptible, Chronic_Susceptible}	
	/** Types of early infection outbreaks that are to be recorded by mean of period. Currently, we have the Transient and the 
	 * Endemic periods defined by the time in simulation ticks (1 tick == 1 day) in their respective constructors.
	 * This can be extended by adding further in the enum class, e.g. Middle_Period (initTick, # of years) etc. 
	 * The first argument in the constructor is the start tick from which to record this type of outbreak; 
	 * the second argument in the constructor is the number of years for which early infection transmissions
	 * from this outbreak should be recorded. */
	public static enum OutbreakRecord {EarlyPeriod(1, 30), Endemic(90000, 30);
	/** This is the threshold that allows creating of entire new transmission chains during
	 * the recording period. This means that if a person transmits infection after this threshold,
	 * we consider the newly-infected person as starting a new chain as a new root of an infection tree
	 * in the forest. Currently, the threshold is 300 years - so no new chains will be created. */
	public static final int NewChainThreshold = 300*12*30;		
		private int startRecordTick;
		private int endObserveTick;
		private int recordYears;
		OutbreakRecord(int _startTime, int _recordYears) {
			this.startRecordTick = _startTime;
			this.recordYears = _recordYears;
			this.endObserveTick = startRecordTick + (recordYears*12*30); 
		}
		public int getStartRecordTick() {
			return this.startRecordTick;
		}
		public int getEndObserveTick() {
			return this.endObserveTick;
		}
		public int getRecordYears() {
			return recordYears;
		}
	}
	/** Types of transmission chains to be considered for measuring outbreaks' statistics: 
	 * Continuous chains (ongoing transmission chains), dead-ends (dead transmission chains) or all chains.*/
	public static enum ChainsType {Continuous, DeadEnds, All}
	/** */
	public enum Output {Size, Duration, Height, IRatio, Nary, Children, Delta, Chronics};
	/** */
	public enum Statistics {Avg, Max, Var, IQR, p25, p50, p75, p90, p99}	
	/** */
	public static enum TreeStatisticsKey {ObID, HRI, HRL, LRI, LRL, Internals, Leaves, Height, Vertices, Size, Duration, RatioIL}
	/** */
	public static enum BalanceStatistics {MeanBar, StdBar, Beta1, Beta2};
	/** */
	public static final int[] SizeRanges = {1, 2, 5, 10};
	/** */
	public static final int[] durationRanges = {30, 60, 150, 600};
	/** */
	public static final int[] chronicRanges = {1, 2, 5, 7};
	/** */
	public static enum AHIKey {
		/*String header = "ObID, time, InfectorID, InfectorTick, InfectedID, ActType, TimeSinceLastInf, InfectorStg, " +
		"InfectorState, InfectedState, MixingSite, Branchlength";*/
		OBID, Time, InfectorID, InfectorTick, InfectedID, ActType, TimeSinceLastInf, InfectorStg, BranchLength,
		InfectorState, InfectedState, MixingSite
	};
	/** */
	public static int[] sizeCategories = {1, 2, 5, 10, Integer.MAX_VALUE};
	/** */
	public static int[] durationCategories = {30, 60, 150, 300, Integer.MAX_VALUE};	
	/** */
	public static final boolean SingleTreeMode = false;
	/** */
	public static final boolean RecordInfectionTree = true;
	/** */
	public static double FracLargestTrees = 0.1;
	
	/*********** File Paths *********************/
	/** Path for the input parameters file. */
	public static final String inputPath = "./input/";
	/** Path for the output directory. */
	public static final String outputPath = "./output/";
	/** Name of the parameters input file. Must be located in the input path. */
	public static final String inputFile = "Prevalence45-1000.csv";	
	/** Path to save the aggregate data files for the corresponding parameter set' index for multiple runs. */
	public static final String aggregatePath = "./data/aggregates/";
	/** Path to save the individual data files for the corresponding parameter set' index for multiple runs. 
	 * These contain all transmissions, early infection transmissions and basic outbreak trees' statistics 
	 * for all the Outbreak types (Transient and Endemic).*/
	public static final String allRunsPath = "./data/allruns/";
	/** Name of the output file containing endemic prevalence, fraction of AHI transmissions etc for all runs of a parameter set. 
	 * Must be located in the aggregatePath. */
	public static final String populationFilename = "Population.csv";
	/** Name of the output file containing joint distribution of size and duration of the early infection 
	 * outbreaks for all runs of a parameter set. Must be located in the aggregatePath. */
	public static final String incidenceFilename = "Incidence.csv";
	/** Name of the output file containing estimates from the Baek et al. method for all runs of a parameter set. 
	 * Must be located in the aggregatePath. */
	public static final String plfitFilename = "Plfit.csv";
	/** Name of the output file containing all tree statistics summaries for all runs of a parameter set. 
	 * Saved in the outputPath directory. */
	public static final String treeStatsFilename = "EarlyInfectionOutbreaksSummary.csv";	
	public static final String outbreakDataFilename = "OutbreakData.csv";
	/** Directory containing the plfit.exe (C binary) for the Powerlaw fit method from Abramnson et al.*/
	public static final String plfitExecutablePath = "./src/ext/cfg/plfit.exe";
	
	/**************** Related to the Episodic Risk Model implementation ***************/
	/** Episodic risk model's mixing sites */
	public static enum MIXING_SITE {None, Common, HighRisk};
	/** Episodic risk model's states */
	public static enum RISK_STATE {None, High, Low}
}
