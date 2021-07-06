public class FeatureDecomposition {
	int features[];
	int numberOfFeatures;

	public FeatureDecomposition(int number) {
		numberOfFeatures = number;
		features = new int[numberOfFeatures];
	}

	public boolean setFeature(int feature, int value) {
		if (feature < numberOfFeatures) {
			features[feature] = value;
			return true;
		} else
			return false;
	}

	public boolean setFeatures(int newFeatures[]) {
		if (newFeatures.length == numberOfFeatures) {
			features = newFeatures;
			return true;
		} else
			return false;
	}

	public int[] getFeatures() {
		return features;
	}

	public int numberOfFeatures() {
		return numberOfFeatures;
	}
}