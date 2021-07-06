/* The ConstraintBatch class is a wrapper class to contain integer arrays -- this allows them
to be stored in a hash table without having to put each little one in its own Integer wrapper */

public class ConstraintBatch implements Cloneable {
	int constraints[];

	public ConstraintBatch(int size) {
		constraints = new int[size];
		constraints[0] = 0;

		// for (int j = 1; j < constraints.length; j++)
		// constraints[j] = -1;
	}

	public void add_element(int new_element) {
		constraints[0]++;
		if (constraints[0] >= constraints.length)
			grow();
		this.constraints[constraints[0]] = new_element;

	}

	public int size() {
		return constraints[0] + 1;
	}

	public void grow() {
		int new_constraints[] = new int[constraints.length * 2];
		System.arraycopy(constraints, 0, new_constraints, 0, constraints.length);
		this.constraints = new_constraints;
	}

	public Object clone() {
		ConstraintBatch newBatch = new ConstraintBatch(this.size());
		for (int i = 0; i < this.constraints[0]; i++) {
			newBatch.constraints[i] = this.constraints[i];
		}
		return newBatch;
	}
}