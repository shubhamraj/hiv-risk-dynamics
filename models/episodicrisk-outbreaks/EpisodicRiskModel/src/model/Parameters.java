package model;

import org.apache.commons.math.stat.descriptive.moment.Skewness;
import org.apache.commons.math.stat.descriptive.rank.Percentile;

import flanagan.math.PsRandom;

/**
 * 
 * @author shah
 *
 */
public class Parameters {
	/** */
	public static PsRandom psr = new PsRandom();
	/** */
	public static Percentile percentile = new Percentile();
	/** */
	public static Skewness skew = new Skewness();
	/** */
	public static final boolean RecordOutbreak = true;
	/** Maximum number of iterations */
	public static final int MAX_ITERATIONS = 105000;
	/** Initial population */
	public static int initialPopulation = 10000;
	/** Type of early infection outbreak*/
	public static enum OUTBREAK_TYPE {AHI, SIX_MONTHS, TWO_YEARS}
	/** Types of sex act */
	public static enum ACT_TYPE {NONE, AHI, CHI}
	/** Types of infection stages */
	public static enum STAGE {SUSCEPTIBLE, ACUTE, CHRONIC}
	/** Initial proportion of infection in the population (1%) */
	public static final double initialInfection = 0.1d;	
	/** Duration of sexually active life */
	public static final double durationLife = 40*12*30d;
	/** Duration of stay in primary HIV */
	public static final double durationAHI = 2*30d;
	/** Duration of stay in post-primary HIV */
	public static final double durationCHI = 120*30d;
	/** Base transmission probability from Vittinghoff et al. (1999) */
	public static final double baseTransProb = 0.003d;
	/** */
	public static enum ChainsType {Continuous, DeadEnds, All}
	/** */
	public static enum MIXING_SITE {NONE, COMMON, HIGH_RISK};
	/** Episodic risk states */
	public static enum RISK_STATE {NONE, HIGH, LOW}
	/** */
	public enum Outputs {Size, Duration, Height, IRatio, Nary, Children, Delta, Chronics};
	/** */
	public enum Statistics {Avg, Max, Var, IQR, p25, p50, p75, p90, p99}	
	public static int[] sizeRanges = {1, 2, 5, 10};
	public static int[] durationRanges = {30, 60, 150, 600};
	public static int[] chronicRanges = { 1, 2, 5, 7 };
	/** */
	public static enum AHIKey {
		/*String header = "ObID, time, InfectorID, InfectorTick, InfectedID, ActType, TimeSinceLastInf, InfectorStg, " +
		"InfectorState, InfectedState, MixingSite, Branchlength";*/
		OBID, Time, InfectorID, InfectorTick, InfectedID, ActType, TimeSinceLastInf, InfectorStg, BranchLength,
		InfectorState, InfectedState, MixingSite
	};
	/** */
	public static enum TreeStatisticsKey {ObID, HRI, HRL, LRI, LRL, Internals, Leaves, Height, Vertices, Size, Duration, RatioIL}
	/** */
	public static int[] sizeCategories = {1, 2, 5, 10, Integer.MAX_VALUE};
	public static int[] durationCategories = {30, 60, 150, 300, Integer.MAX_VALUE};	
	/** */
	public static enum BalanceStatistics {MeanBar, StdBar, Beta1, Beta2};
	/** */
	public static final boolean singleTreeMode = false;
	/** */
	public static final boolean recordInfectionTree = true;
	/** */
	public static double fracLargestTrees = 0.1;
	/** */
	public static enum OUTBREAK_RECORD {TRANSIENT(1, 30), ENDEMIC(90000, 30);
		public static final int TRANS_THRESHOLD = 300*12*30;		
		private int startRecordTick;
		private int endObserveTick;
		private int recordYears;
		
		OUTBREAK_RECORD(int _startTime, int _recordYears) {
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
	
	public static final String inputPath = "./input/";
	public static final String inputFile = "Prevalence45-1000.csv";	
	public static final String aggregatePath = "./data/aggregates/";
	public static final String allRunsPath = "./data/allruns/";	
	public static final String populationFilename = "./output/Population.csv";
	public static final String incidenceFilename = "./output/Incidence.csv";
	public static final String plfitFilename = "./output/Plfit.csv";
	public static final String treeStatsFilename = "./output/AllAcuteAHIData.csv";	
	public static final String outbreakDataFilename = "./output/OutbreakData.csv";
		
	public static String plfitExecutablePath = "./src/ext/cfg/plfit.exe";
	
	/** Returns the probability given the rate*/
	public static double returnProbability(double rate) {
		double dt = 0.028571428571;
		double d =(1-Math.exp(-rate*dt)); 
		return d;
	}
	/** */
	public static double returnAHIDuration() {
		return psr.nextErlang(2, 4); 
	}
	/** */
	public static double returnCHIDuration() {
		//KURT: 0.503767378
		//SKEW: 0.561183659
		//		return psr.nextErlang(1, 7); 
		//		return psr.nextErlang(3, 25); cappa
		return psr.nextErlang(3, 24);
	}
	
	public void printStr(String str) {
		System.out.println(str);
	}
}