// i really wish i could think of a better way to do this: we often
// need to pass around little sets of three confidence values (the 
// 75, the 90, and the 95)  we could invoke separate methods for each,
// or the same method three times with different parameters, but i guess
// the most efficient thing to do is just to bundle them in a class:
public abstract class ConfidenceAdjustor {
	double p75;
	double p90;
	double p95;

	public void ConfidenceValues() {
		// i don't think we want to initialize these yet

		// (actually, initializing them to 0 might be a good idea because
		// it could avoid nullPointerException's in writing constraints
		// to disk, in case we ever end up writing the constraints without
		// first calculating the impugned confidence values)

		// 6/10/99 yes: i think we need this now that there is a separate
		// confidence adjustor for relative TOKEN frequencies, because we often
		// have no need to explicitly calculate them (and in fact they cause
		// divide by 0 errors if we do!)
		p75 = 0;
		p90 = 0;
		p95 = 0;
	}

	// we'll use different calculate() methods for upper and lower confidence
	// adjustors -- one adds, the other subtracts
	// (actually, these will share a lot of code, so it would really be more
	// elegant to farm some of that out to other methods which they can share--
	// however, i looked quickly over the calculations and couldn't see a way
	// to do it without also introducing a certain degree of inefficiency, so
	// i'll leave this for now.
	abstract void calculate(int x, int y);

	public void set_p75(double newval) {
		p75 = newval;
	}

	public void set_p90(double newval) {
		p90 = newval;
	}

	public void set_p95(double newval) {
		p95 = newval;
	}

	// here are the t-tables for different degrees of freedom:
	public double t95(int df) {

		if (df == 0) {
			return 100;
		} else if (df == 1) {
			return 6.314;
		} else if (df == 2) {
			return 2.92;
		} else if (df == 3) {
			return 2.353;
		} else if (df == 4) {
			return 2.132;
		} else if (df == 5) {
			return 2.015;
		} else if (df == 6) {
			return 1.94;
		} else if (df == 7) {
			return 1.89;
		} else if (df == 8) {
			return 1.86;
		} else if (df == 9) {
			return 1.83;
		} else if (df == 10) {
			return 1.81;
		} else if (df == 11) {
			return 1.79;
		} else if (df == 12) {
			return 1.78;
		} else if (df == 13) {
			return 1.77;
		} else if (df == 14) {
			return 1.76;
		} else if (df == 15) {
			return 1.75;
		} else if (df <= 18) {
			return 1.74;
		} else if (df <= 21) {
			return 1.73;
		} else if (df <= 24) {
			return 1.72;
		} else if (df <= 27) {
			return 1.71;
		} else if (df <= 30) {
			return 1.7;
		} else if (df <= 40) {
			return 1.69;
		} else if (df <= 50) {
			return 1.68;
		} else if (df <= 75) {
			return 1.67;
		} else if (df <= 100) {
			return 1.66;
		} else if (df <= 1000) {
			return 1.65;
		} else {
			return 1.64;
		}
	}

	public double t90(int df) {

		if (df == 0) {
			return 100;
		} else if (df == 1) {
			return 3.08;
		} else if (df == 2) {
			return 1.89;
		} else if (df == 3) {
			return 1.64;
		} else if (df == 4) {
			return 1.53;
		} else if (df == 5) {
			return 1.48;
		} else if (df == 6) {
			return 1.44;
		} else if (df == 7) {
			return 1.41;
		} else if (df == 8) {
			return 1.4;
		} else if (df == 9) {
			return 1.38;
		} else if (df == 10) {
			return 1.37;
		} else if (df == 11) {
			return 1.36;
		} else if (df == 12) {
			return 1.36;
		} else if (df == 13) {
			return 1.35;
		} else if (df == 14) {
			return 1.35;
		} else if (df == 15) {
			return 1.34;
		} else if (df <= 18) {
			return 1.34;
		} else if (df <= 21) {
			return 1.33;
		} else if (df <= 24) {
			return 1.32;
		} else if (df <= 27) {
			return 1.31;
		} else if (df <= 30) {
			return 1.31;
		} else if (df <= 40) {
			return 1.3;
		} else if (df <= 50) {
			return 1.3;
		} else if (df <= 75) {
			return 1.3;
		} else if (df <= 100) {
			return 1.29;
		} else if (df <= 1000) {
			return 1.28;
		} else {
			return 1.28;
		}
	}

	public double t75(int df) {
		if (df == 0) {
			return 100;
		} else if (df == 1) {
			return 1;
		} else if (df == 2) {
			return .82;
		} else if (df == 3) {
			return .76;
		} else if (df == 4) {
			return .74;
		} else if (df == 5) {
			return .73;
		} else if (df == 6) {
			return .72;
		} else if (df == 7) {
			return .71;
		} else if (df == 8) {
			return .71;
		} else if (df == 9) {
			return .7;
		} else if (df == 10) {
			return .7;
		} else if (df == 11) {
			return .7;
		} else if (df == 12) {
			return .7;
		} else if (df == 13) {
			return .69;
		} else if (df == 14) {
			return .69;
		} else if (df == 15) {
			return .69;
		} else if (df <= 18) {
			return .69;
		} else if (df <= 21) {
			return .69;
		} else if (df <= 24) {
			return .68;
		} else if (df <= 27) {
			return .68;
		} else if (df <= 30) {
			return .68;
		} else if (df <= 40) {
			return .68;
		} else if (df <= 50) {
			return .68;
		} else if (df <= 75) {
			return .68;
		} else if (df <= 100) {
			return .68;
		} else if (df <= 1000) {
			return .67;
		} else {
			return .67;
		}
	}

}
