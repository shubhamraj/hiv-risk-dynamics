package ibm_alt;

import flanagan.math.PsRandom;

public class Test {

	public PsRandom psr = new PsRandom();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Test test = new Test();
		for (int i=0; i<5000; i++) {
			System.out.println("" + test.psr.nextErlang(0.83, 3)*10);
		}

	}

}
