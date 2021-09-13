// the LowerConfidenceAdjustor is a type of confidence adjustor --
// it calculates lower confidences by SUBTRACTING the uncertainty

public class LowerConfidenceAdjustor extends ConfidenceAdjustor {

	public LowerConfidenceAdjustor() {
		super();

	}

	public void calculate(int x, int y) {
		// The idea here is that, say, a 1000-0 disparity gives you more confidence
		// in a generalization than a 10-0 disparity.

		// This code calculates the fudge in the manner used by Andrei Mikheev
		// in his article in _Computatational Linguistics_ (1997).

		// There are three different fudges made available here, one based on
		// a 75% confidence interval, one on 90%, and one on 95%. It's
		// not clear to me which is appropriate for our purposes. But the
		// outcomes do indeed produce fudged numbers that are at least
		// qualitatively correct.

		// Intermediate results used in calculation:
		double intermediateResult = 0;
		double pHatStar = 0;
		// Calculate the adjusted confidence. See Mikheev, p. 413.

		// Add Mikheev's fudge factors to denominator and numerator, and
		// divide, to obtain pHatStar.
		// (the fudge factor ensures no 0's, but it does it in a particular way)

		pHatStar = (double) (x + .5) / (y + 1);

		// We're quite ambivalent about this fudge, and are considering
		// not having it.

		// pHatStar = ( (double) x ) / ( (double) y);

		// Compute an intermediate result for the final formula.
		// This is the expression appearing as a square root, in
		// Mikheev's formula.

		intermediateResult = (double) Math.sqrt(pHatStar * (1 - pHatStar) / y);

		// for lower confidences, we subtract the uncertainty
		p75 = (double) pHatStar - t75(y - 1) * intermediateResult;
		p90 = (double) pHatStar - t90(y - 1) * intermediateResult;
		p95 = (double) pHatStar - t95(y - 1) * intermediateResult;

		return;
	} // end of calculate() method
}