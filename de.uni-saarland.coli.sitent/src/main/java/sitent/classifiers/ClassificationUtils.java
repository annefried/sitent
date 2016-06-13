package sitent.classifiers;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class ClassificationUtils {

	/**
	 * How often does attribute occur in the data? (Is greater than zero, for
	 * numeric attributes.)
	 * 
	 * @param data
	 * @param attr
	 * @return
	 */
	public static boolean frequencyHigherEqualThan(Instances data, Attribute attr, int threshold) {
		if (!attr.isNumeric()) {
			System.err.println("getAttributeFrequency is only implemented for numeric attributes, " + attr.name()
					+ " is not numeric.");
			throw new RuntimeException();
		}
		int occurrences = 0;
		for (int i = 0; i < data.numInstances(); i++) {
			Instance inst = data.instance(i);
			if (inst.value(attr) != 0) {
				occurrences++;
				if (occurrences >= threshold) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * How often does attribute occur in the data? (Is greater than zero, for
	 * numeric attributes.)
	 * 
	 * @param data
	 * @param attr
	 * @return
	 */
	public static boolean frequencyHigherEqualThan(Instances[] data, Attribute attr, int threshold) {
		if (!attr.isNumeric()) {
			System.err.println("getAttributeFrequency is only implemented for numeric attributes, " + attr.name()
					+ " is not numeric.");
			throw new RuntimeException();
		}
		int occurrences = 0;
		for (Instances instances : data) {
			for (int i = 0; i < instances.numInstances(); i++) {
				Instance inst = instances.instance(i);
				if (inst.value(attr) != 0) {
					occurrences++;
					if (occurrences >= threshold) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
