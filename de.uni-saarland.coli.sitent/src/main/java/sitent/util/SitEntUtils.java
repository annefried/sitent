package sitent.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SitEntUtils {

	/**
	 * from:
	 * http://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
	 * 
	 * @param passedMap
	 * @return
	 */
	public static LinkedHashMap<String, Integer> sortHashMapByValues(HashMap<String, Integer> passedMap) {
		List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
		List<Integer> mapValues = new ArrayList<Integer>(passedMap.values());
		Collections.sort(mapValues, Collections.reverseOrder());
		Collections.sort(mapKeys);

		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();

		Iterator<Integer> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Object val = valueIt.next();
			Iterator<String> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				Object key = keyIt.next();
				String comp1 = passedMap.get(key).toString();
				String comp2 = val.toString();

				if (comp1.equals(comp2)) {
					// passedMap.remove(key);
					mapKeys.remove(key);
					sortedMap.put((String) key, (Integer) val);
					break;
				}

			}

		}
		return sortedMap;
	}

	/**
	 * Increments the value of the map for the entry of 'key' by one. If key not
	 * given, this method initializes the corresponding entry.
	 * 
	 * @param map
	 *            Map with object of any type, values are Integers.
	 * @param key
	 *            The key for which the map entry will be incremented.
	 */
	public static <T> void incrementMapForKey(Map<T, Integer> map, T key) {
		if (!map.containsKey(key)) {
			map.put(key, 0);
		}
		map.put(key, map.get(key) + 1);
	}
	
	public static <T> void incrementMapForKeyDouble(Map<T, Double> map, T key) {
		if (!map.containsKey(key)) {
			map.put(key, 0.0);
		}
		map.put(key, map.get(key) + 1);
	}
	
}
