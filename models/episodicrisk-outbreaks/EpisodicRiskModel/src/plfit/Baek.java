package plfit;

import cern.colt.Timer;

/** Original code: Baek et al. (2011). Thanks to Seung Baek. */

public class Baek {
	
	private int M, N, kmax, kmin; 
	
	public Baek(int _M, int _N, int _kmax, int _kmin) {
		this.M = _M;
		this.N = _N;
		this.kmax = _kmax;
		this.kmin = _kmin;
	}

	private double li(double x, double g, int k, int prec) {		
		double f1;
		double crit;
		double sum;

		f1 = Math.exp(-x*k)*Math.pow(k,-g);
		crit = f1*Math.pow(10,-prec);
		sum = 0.0;
		do{
			sum += f1;
			k++;
			f1 = Math.exp(-x*k)*Math.pow(k,-g);
		}while(f1>crit);
		return sum;
	}

	private double get_b(double g) {
		double b, db;
		double kavg;
		double upper, lower, center;
		double r;
		int prec;
		double pb;
		kavg = (double)M/N;

		pb = -1.0;
		prec = 6;
		while(true){
			// search from big b
			b = M;
			db = b * 0.5;
			while(true){
				r = li(b/M, g-1, kmin, prec) / li(b/M, g, kmin, prec);
				if(r > kavg) {
					break;
				}
				b -= db;
				db *= 0.5;
			}
			// binary search between b and 2b
			upper = 2*b;
			lower = b;
			while(true) {
				center = (upper + lower)/2;
				r = li(center/M, g-1, kmin, prec) / li(center/M, g, kmin, prec);
				if(Math.abs(r-kavg) < Math.pow(10,-6)) {
					break;
				}
				else if(r > kavg) lower = center;
				else upper = center;
			}			
			if(pb>0.0 && Math.abs(center-pb)<Math.pow(10,-3)) {
				break;
			}
			pb = center;
			prec++;
		}
		return center;
	}

	private int get_kc(double g, double b) {
		double f1;
		double sum;
		double crit;
		double A;
		int k, pk;
		int prec;

		prec = 6;
		// largely dependent on precision
		pk = 2*M;
		while(true) {
			A = li(b/M, g, kmin, prec);
			k = kmin;

			f1 = Math.exp(-b/M*k)*Math.pow(k,-g);
			crit = A*(N-1)/N;
			sum = 0.0;
			do{
				sum += f1;
				k++;
				f1 = Math.exp(-b/M*k)*Math.pow(k,-g);
			} while(sum<crit);

			if (Math.abs(k-pk)<M*Math.pow(10,-3)) {
				break;
			}
			pk = k;
			++prec;
		}

		return k;
	}

	private double get_ekmax(double g, double b, int kc) {
		double r, pr;
		int prec;
		pr = -1.0;
		prec = 6;
		while(true) {
			r = li(b/M, g-1, kc, prec) / li(b/M, g, kc, prec);
			if(pr>0.0 && Math.abs(r-pr) < Math.pow(10,-3)) {
				break;
			}
			pr = r;
			prec++;
		}
		return r;
	}

	private double get_A(double g, double b, int kc) {
		double A, pA;
		int prec;

		pA = -1.0;
		prec = 6;
		while(true){
			A = li(b/M, g, kc, prec);
			if(pA>0.0 && Math.abs(A-pA) < Math.pow(10,-6)) {
				break;
			}
			pA = A;
			prec++;
		}
		return A;
	}

	public double[] run() {
		double g, dg, b, ekmax, A;
		double upper, lower, center;
		int kc;

		int repeat = 0;
		double pekmax = 0.0;

		g = 0.0;
		dg = 0.02;

		while(true){
			b = get_b(g);
			kc = get_kc(g, b);
			ekmax = get_ekmax(g, b, kc)/kmax - 1.0;
			if(ekmax > 0.0) break;
			g += dg;
		}

		upper = g;
		lower = g - dg;
		while(true){
			center = (upper+lower)/2;
			b = get_b(center);
			kc = get_kc(center, b);
			ekmax = get_ekmax(center, b, kc)/kmax - 1.0;

			if(Math.abs(ekmax)<Math.pow(10,-3)) {
				break;
			}
			else if(ekmax < 0.0) lower = center;
			else upper = center;

			if(Math.abs(ekmax-pekmax)<Math.pow(10,-6)) repeat++;
			else repeat = 0;
			pekmax = ekmax;
			if(repeat>10) {
				break;
			}
		}

		A = get_A(center, b, kmin);

/*		System.out.println("gamma: " + center);
		System.out.println("b0: " + b);
		System.out.println("kc: " + kc);
		System.out.println("b0: " + A);*/
		
		double[] output = new double[4];
		output[0] = center;
		output[1] = b;
		output[2] = (double)kc;
		output[3] = A;
		
		return output;
	}

	public static void main(String[] args) {
		Timer timer = new Timer();
		timer.start();
		Baek comp = new Baek(263937, 29759, 14924, 1);
		comp.run();
		timer.stop();
		System.out.println(timer.seconds());

	}
}