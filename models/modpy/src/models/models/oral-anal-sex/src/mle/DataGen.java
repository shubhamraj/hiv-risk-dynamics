package mle;

import cern.jet.random.Normal;

public class DataGen {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double prob = 0.102;
		int[] x = {1,
     		   4,
    		   1,
    		   1,
    		   5,
    		   3,
    		   5,
    		   3,
    		   5,
    		   3};
		double[] y = new double[10];
		for (int i=0; i<10; i++) {
//			x[i] = Uniform.staticNextIntFromTo(1, 5);
			y[i] = 1-(Math.pow(1-prob,x[i]));
			
			if (Normal.staticNextDouble(0, 1) <= Math.random()) {
				y[i] = 1;
			}
			else {
				y[i] = 0;
			}

		}
//		for (int i=0; i<10; i++) {
//			System.out.println(x[i]+",");
//		}
//		for (int i=0; i<10; i++) {
//			System.out.println((int)y[i]+",");
//		}
		
		for (int i=0; i<10; i++) {
			double d = 1-(Math.pow(1-0.2476399,x[i]));
			if (Math.random() <= d) {
				y[i] = 1;
			}
			else {
				y[i] = 0;
			}
			System.out.println((int)y[i]);
		}
	}

}
