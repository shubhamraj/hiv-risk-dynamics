package partnership;

import interfaces.ParametersInterface;

/**
 * 
 * Jong-Hoon Kim's model integrated with outbreaks library
 *
 */

public interface PartnershipParametersInterface extends ParametersInterface {
	public static boolean debug = false; 	
	/** Default PHI transmission potential*/	
	public static final double AcuteTransPotential = 0.35;
	public static enum PartnershipScheme {Monogamous, Random, RandomFixedNumberPartnership, Assortative, Dissassortative, MonogamousII}
	/** fixed frequency of sex acts per partnership, fixed frequency of sex acts per individual, 
	 * fixed frequency of sex acts per population by taking the fraction of singles into account */
	public static enum SexFrequencyScheme {FixedPerPartnership, FixedPerIndividual, FixedByPopulationBySingles}
	public static class Partnership {		
		public static double DurationPerDay = 10.0;
		/** "theta" in the Kim et al. (2010) paper*/
		public static double PartnershipCoefficient = 1.0;
	    public static double FrequencySexPerDay = 0.3333; 		
		public static double AverageNeighbor = 1.5d;	    
		/** partnershipProb is reset by durPartnership*avgNbor */
		public static double PartnershipProb = 0.001;
		/** maximum number of partnerships that an individual can have */
		public static int MaxDegree = 10;
		/** observed proportion of individuals without partners */
		public static double PropSingle = 0.2;
	}
	public static double stepReportFrequency = 100.0;
}
