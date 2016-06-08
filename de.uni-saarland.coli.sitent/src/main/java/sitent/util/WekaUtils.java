package sitent.util;

import java.util.LinkedList;
import java.util.List;

import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveWithValues;

public class WekaUtils {

	/**
	 * Removes all instances that have one of the given values.
	 * 
	 * @param data
	 *            Array of Instances (e.g., folds).
	 * @param attribute The name of the attribute according to which is filtered.
	 * @param values Instances where the attribute takes one of these values are to be removed.
	 * @return The filtered set of Instances, again an array of Instances.
	 * @throws Exception
	 */
	public static Instances[] removeWithValues(Instances[] data, String attribute, String[] values) throws Exception {

		String valueRegex = "";
		for (String v : values) {
			valueRegex += v + "|";
		}
		valueRegex = valueRegex.substring(0, valueRegex.length() - 1);

		List<Integer> indicesToRemove = new LinkedList<Integer>();
		// find indices of values that should be removed
		Attribute classAttr = data[0].attribute(attribute);
		int classAttrIndex = classAttr.index();
		
		for (int i = 0; i < classAttr.numValues(); i++) {
			String value = classAttr.value(i);
			if (value.matches(valueRegex)) {
				indicesToRemove.add(i);
			}
		}
		if (!indicesToRemove.isEmpty()) {
			int[] indices = new int[indicesToRemove.size()];
			for (int i = 0; i < indicesToRemove.size(); i++) {
				indices[i] = indicesToRemove.get(i);
			}

			RemoveWithValues removeValuesFilter = new RemoveWithValues();
			// at RemoveWithValuesFilter for whatever reason the indices
			// need to be increased by 1. But that's not the case for the
			// other filters.
			removeValuesFilter.setAttributeIndex(new Integer(classAttrIndex + 1).toString());
			removeValuesFilter.setNominalIndicesArr(indices);
			removeValuesFilter.setInputFormat(data[0]);
			for (int i = 0; i < data.length; i++) {
				data[i] = Filter.useFilter(data[i], removeValuesFilter);
			}
		}

		return data;

	}

}
