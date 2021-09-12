import java.io.*;
import java.awt.*;

public class MappingConstraint {
	// mapping constraints consist of a set of hypothetical morphemes
	char mappings[][];

	// in addition to the entire mapping strings, we also want to have a few
	// details at our fingertips:
	char A[], B[];
	char P[], Q[];
	int P_feat[][], Q_feat[][];
	boolean P_residue, Q_residue;
	boolean P_features_active = true, Q_features_active = true;
	int change_location;
	int affixType;
	final int SUFFIX = 0, PREFIX = 1, INFIX = 2, ABLAUT = 3, SUPPLETION = 4;
	final int INACTIVE = -2;

	int numberOfFeatures;

	// and for the ranking and the statistical computations:
	int scope, // the measure of scope for this constraint
			hits, // cases where constraint derives the correct output
			stratum, // which stratum he belongs to
			blots; // blots on his escutcheon
	double reliability; // (scope - blots)/scope
	double weightedConfidence; // weighted for length of A,B
	double weightedByFreq;
	double overallconfidence;

	// we'll store: the lower confidences (at all levels) for the constraint,
	// and the upper confidences of the impugned values.

	LowerConfidenceAdjustor lowerConfidence;
	UpperConfidenceAdjustor impugnedConfidence;
	LowerConfidenceAdjustor relfreqConfidence;

	int impugner;
	boolean doppelgaenger;
	boolean degenerate;
	boolean keep;
	int chronological;

	int hits_frequency;
	int scope_frequency;
	double rel_frequency;

	// for now we won't actually use the upper confidences, but maybe someday:
	// ConfidenceValues upperConfidence;

	// the constructor takes the number of morphological categories as an argument,
	// so we end up with the right number of hypothesized morphemes
	public MappingConstraint(int features) {
		mappings = new char[2][];
		scope = 0;
		blots = 0;
		reliability = 1;
		A = new char[0];
		B = new char[0];
		P = new char[0];
		Q = new char[0];
		change_location = 0;
		affixType = 0;
		P_residue = false;
		Q_residue = false;
		P_features_active = true;
		Q_features_active = true;
		numberOfFeatures = features;
		P_feat = new int[2][numberOfFeatures];
		Q_feat = new int[2][numberOfFeatures];
		lowerConfidence = new LowerConfidenceAdjustor();
		impugnedConfidence = new UpperConfidenceAdjustor();
		relfreqConfidence = new LowerConfidenceAdjustor();
		// we're going to use default impugnedConfidence values of 100,
		// and replace them as we really find out the constraint's sordid past:
		impugnedConfidence.set_p75(100);
		impugnedConfidence.set_p90(100);
		impugnedConfidence.set_p95(100);

		impugner = -1;
		weightedConfidence = 0;
		weightedByFreq = 0;
		overallconfidence = 0;
		doppelgaenger = false;
		chronological = 0;
		degenerate = false;
		keep = true;

		hits_frequency = 0;
		scope_frequency = 0;
		rel_frequency = 0;
	} // end of constructor

	public MappingConstraint(int mcats, int numberOfFeatures) {
		mappings = new char[mcats][];
		scope = 0;
		blots = 0;
		reliability = 1;
		A = new char[0];
		B = new char[0];
		P = new char[0];
		Q = new char[0];
		change_location = 0;
		affixType = 0;
		P_feat = new int[2][numberOfFeatures];
		Q_feat = new int[2][numberOfFeatures];
		P_residue = false;
		Q_residue = false;
		P_features_active = true;
		Q_features_active = true;

		lowerConfidence = new LowerConfidenceAdjustor();
		impugnedConfidence = new UpperConfidenceAdjustor();
		relfreqConfidence = new LowerConfidenceAdjustor();
		// we're going to use default impugnedConfidence values of 100,
		// and replace them as we really find out the constraint's sordid past:
		impugnedConfidence.set_p75(100);
		impugnedConfidence.set_p90(100);
		impugnedConfidence.set_p95(100);
		impugner = -1;

		overallconfidence = 0;
		weightedByFreq = 0;
		hits_frequency = 0;
		scope_frequency = 0;
		rel_frequency = 0;
		doppelgaenger = false;
		chronological = 0;
		degenerate = false;
		keep = true;
	}

	public void setP(char newP[]) {
		P = newP;
	}

	public void setQ(char newQ[]) {
		Q = newQ;
	}

	public void setChronological(int chron) {
		chronological = chron;
	}

	public void inc_scope() {
		scope++;

		// System.out.println("(scope for this constraint now = " + scope + ")");

		update_reliability();
	}

	public void setStratum(int value) {
		stratum = value;
	}

	public void addBlot() {
		blots++;
		update_reliability();
	}

	public void scoreHit() {
		hits++;

		// System.out.println("(hits for this constraint now = " + hits + ")");

		update_reliability();
	}

	public void incHitsFrequency(int newfreq) {
		hits_frequency = hits_frequency + newfreq;
	}

	public void incScopeFrequency(int newfreq) {
		scope_frequency = scope_frequency + newfreq;
	}

	public void calculateRelFrequency() {
		if (scope_frequency == 0) {
			rel_frequency = 0;
		} else {
			relfreqConfidence.calculate(hits_frequency, scope_frequency);
			rel_frequency = relfreqConfidence.p75;
		}
		if (rel_frequency > 1)
			rel_frequency = 0;
	}

	public void update_reliability() {
		reliability = (double) hits / scope;

		// OUR "REFINED RELIABILITY", which we no longer use:
		// reliability = (scope - blots)/scope;
	}

	public void setChangeLocation(char s1[], char s2[], int location) {
		A = s1;
		B = s2;
		change_location = location;
	}

	public void setImpugner(int newImpugner) {
		impugner = newImpugner;
	}

	public void diagnoseAffixType() {

		// System.out.print("(The constraint itself thinks that it is ");

		if (!P_residue && !P_features_active && P.length == 0)
		// if ( !P_residue && !P_features_active)
		{
			if (!Q_residue && !Q_features_active && Q.length == 0)
			// if ( !Q_residue && !Q_features_active )
			{ // ok, both sides are null-- either suppletion or circumfixation
				affixType = SUPPLETION;
				// System.out.println(" SUPPLETION");
			} else {
				// just prefixation
				affixType = PREFIX;
				// System.out.println(" PREFIXATION");

			}
		} else if (!Q_residue && !Q_features_active && Q.length == 0) {
			// so if we've gotten here, P is non-null but Q is. Suffixation.
			affixType = SUFFIX;
			// System.out.println(" SUFFIXATION");

		}

		else {
			affixType = INFIX;
			// System.out.println(" INFIXATION");
		}

		// System.out.print(".\r");
	}

	public void setAffixType(int type) // override diagnosis
	{
		affixType = type;
	}

	public void setP_residue(boolean res) {
		P_residue = res;
		// if (!res)
		// deactivate_P_features();
	}

	public void setQ_residue(boolean res) {
		Q_residue = res;
		// if (!res)
		// deactivate_Q_features();
	}

	public void setP_features(int p_features[][]) {
		boolean deactivate = true;
		P_feat = p_features;
		/*
		 * for (int i = 0; i < p_features.length; i++) { if (p_features[0][i] !=
		 * INACTIVE) { deactivate = false; break; } } if (deactivate) P_features_active
		 * = false;
		 */
	}

	public void deactivate_P_features() {
		P_features_active = false;
	}

	public void setQ_features(int q_features[][]) {
		boolean deactivate = true;

		Q_feat = q_features;
		/*
		 * for (int i = 0; i < q_features.length; i++) { if (q_features[0][i] !=
		 * INACTIVE) { deactivate = false; break; } } if (deactivate) Q_features_active
		 * = false;
		 */
	}

	public void deactivate_Q_features() {
		Q_features_active = false;
	}

	public void calculateLowerConfidence() {
		// the default "lower confidence" limit which we want to compute
		// is for the hits and scope of the current constraint:
		lowerConfidence.calculate(hits, scope);
		return;
	}

	public void calculateWeightedConfidence(double factor) {
		// we'll weight by length of A and B as follows (for lack of a better idea)
		// Take length of strings A & B.
		// Then multiply that by some factor (to be specified) to get a weight
		// Then multiply that weight by the lower c75 of the constraint

		// double weight = factor * (A.length + B.length);

		// wrong, actually, we want this to be sort of like "compounded interest" --
		// the longer the overlap, the better (a non-linear function)
		double weight = 1;
		int length = A.length + B.length;
		for (int i = 1; i <= length; i++) {
			weight = weight + (weight * factor);
		}
		weightedConfidence = weight * lowerConfidence.p75;
	}

	public void calculateWeightedByFreq() {
		// for now, we'll try just multiplying
		weightedByFreq = rel_frequency * lowerConfidence.p75;
	}

	public void calculateWeightedConfidence() {
		// for the moment, we can make this a default of 1.
		// (in fact this is INCREDIBLY high, I imagine we want something
		// more like .1 or something -- but I'm not really sure how to set this.
		calculateWeightedConfidence(1);
	}

	/*
	 * public void calculateOverallConfidence() { if (impugnedConfidence.p90 <
	 * lowerConfidence.p90) overallconfidence = impugnedConfidence.p90; else
	 * overallconfidence = lowerConfidence.p90; return; }
	 */
	public void calculateOverallConfidence(boolean impugn) {
		if (impugn) {
			if (impugnedConfidence.p75 < lowerConfidence.p75)
				overallconfidence = impugnedConfidence.p75;
			else
				overallconfidence = lowerConfidence.p75;
		} else {
			overallconfidence = lowerConfidence.p75;
		}
		return;
	}

	public void setDoppel(boolean d) {
		doppelgaenger = d;
		return;
	}

	public void setDegenerate(boolean d) {
		degenerate = d;
		return;
	}

	public void setKeep(boolean k) {
		keep = k;
		overallconfidence = -1;
		scope = -1;
		return;
	}

	public void read(DataInputStream file, int features) throws IOException {
		String m1String, m2String, aString, bString, pString, qString;
		int lb, ub = 0;

		byte m1[] = new byte[30];
		file.readFully(m1);
		m1String = new String(m1, 0);
		m1String = m1String.trim();

		mappings[0] = m1String.toCharArray();

		byte m2[] = new byte[30];
		file.readFully(m2);
		m2String = new String(m2, 0);
		m2String = m2String.trim();

		mappings[1] = m2String.toCharArray();

		byte a[] = new byte[30];
		file.readFully(a);
		aString = new String(a, 0);
		aString = aString.trim();
		A = aString.toCharArray();

		byte b[] = new byte[30];
		file.readFully(b);
		bString = new String(b, 0);
		bString = bString.trim();
		B = bString.toCharArray();

		byte p[] = new byte[30];
		file.readFully(p);
		pString = new String(p, 0);
		pString = pString.trim();
		P = pString.toCharArray();

		byte q[] = new byte[30];
		file.readFully(q);
		qString = new String(q, 0);
		qString = qString.trim();
		Q = qString.toCharArray();

		// System.out.println(m1String +", " + m2String+ ", " + ", " +aString +", " +
		// bString +", " + pString +", " + qString);

		for (int i = 0; i < features; i++) {
			// System.out.print( i + "");
			lb = file.readInt();
			ub = file.readInt();
			this.P_feat[0][i] = lb;
			this.P_feat[1][i] = ub;
			// System.out.print( "(" + P_feat[0][i] + "," + P_feat[1][i] + ")");

		}

		for (int i = 0; i < features; i++) {
			// System.out.print( i + "");

			lb = file.readInt();
			ub = file.readInt();
			Q_feat[0][i] = lb;
			Q_feat[1][i] = ub;
			// System.out.print( "(" + Q_feat[0][i] + "," + Q_feat[1][i] + ")");

		}

		// System.out.print("\r");
		P_residue = file.readBoolean();
		Q_residue = file.readBoolean();
		P_features_active = file.readBoolean();
		Q_features_active = file.readBoolean();
		change_location = file.readInt();
		affixType = file.readInt();
		hits = file.readInt();
		scope = file.readInt();
		impugnedConfidence.set_p75(file.readDouble());
		impugnedConfidence.set_p90(file.readDouble());
		impugnedConfidence.set_p95(file.readDouble());

		impugner = file.readInt();
		chronological = file.readInt();
		doppelgaenger = file.readBoolean();
		degenerate = file.readBoolean();

		update_reliability();
	}

	public void write(DataOutputStream file, int numberOfFeatures) throws IOException {
		String m1String = new String(mappings[0]);
		String m2String = new String(mappings[1]);
		String aString = new String(A);
		String bString = new String(B);
		String pString = new String(P);
		String qString = new String(Q);

		byte m1[] = new byte[30];
		if (m1String != null)
			m1String.getBytes(0, m1String.length(), m1, 0);

		file.write(m1);

		byte m2[] = new byte[30];
		if (m2String != null)
			m2String.getBytes(0, m2String.length(), m2, 0);
		file.write(m2);

		byte a[] = new byte[30];
		if (aString != null)
			aString.getBytes(0, aString.length(), a, 0);
		file.write(a);

		byte b[] = new byte[30];
		if (bString != null)
			bString.getBytes(0, bString.length(), b, 0);
		file.write(b);

		byte p[] = new byte[30];
		if (pString != null)
			pString.getBytes(0, pString.length(), p, 0);
		file.write(p);

		byte q[] = new byte[30];
		if (qString != null)
			qString.getBytes(0, qString.length(), q, 0);
		file.write(q);

		for (int i = 0; i < numberOfFeatures; i++) {
			file.writeInt(P_feat[0][i]);
			file.writeInt(P_feat[1][i]);
		}

		for (int i = 0; i < numberOfFeatures; i++) {
			file.writeInt(Q_feat[0][i]);
			file.writeInt(Q_feat[1][i]);
		}

		file.writeBoolean(P_residue);
		file.writeBoolean(Q_residue);
		file.writeBoolean(P_features_active);
		file.writeBoolean(Q_features_active);
		file.writeInt(change_location);
		file.writeInt(affixType);
		file.writeInt(hits);
		file.writeInt(scope);
		file.writeDouble(impugnedConfidence.p75);
		file.writeDouble(impugnedConfidence.p90);
		file.writeDouble(impugnedConfidence.p95);

		file.writeInt(impugner);
		file.writeInt(chronological);
		file.writeBoolean(doppelgaenger);
		file.writeBoolean(degenerate);

		file.flush();
	}

	// is this one right? depends on if boolean is really just 1 byte...
	// XXXXXXX NOTE: this is not right at all any more!
	public int size() {
		return 198;
	}

	public void report() {
		System.out.println("Mapping constraint: [" + mappings[0] + "] -> [" + mappings[1] + "]\r");
		System.out.print("\tP residue: ");
		if (P_residue)
			System.out.print("true\r");
		else
			System.out.print("false\r");
		System.out.println("\tP: " + P);

		System.out.print("\tP features: ");
		if (P_features_active)
			System.out.print("ACTIVE");
		else
			System.out.print("INACTIVE");
		System.out.print("\r\t\t");
		for (int i = 0; i <= numberOfFeatures; i++)
			System.out.print("(" + P_feat[0][i] + "," + P_feat[1][i] + ")  ");
		System.out.println("\r\tA: " + A);
		System.out.println("\tB: " + B);
		System.out.print("\tQ features: ");
		if (Q_features_active)
			System.out.print("ACTIVE");
		else
			System.out.print("INACTIVE");
		System.out.print("\r\t\t");
		for (int i = 0; i <= numberOfFeatures; i++)
			System.out.print("(" + Q_feat[0][i] + "," + Q_feat[1][i] + ")  ");
		System.out.println("\r\tQ: " + Q);
		System.out.print("\tQ residue: ");
		if (Q_residue)
			System.out.print("true\r");
		else
			System.out.print("false\r");
		System.out.print("\r");
	}

} // end of MappingConstraint class
