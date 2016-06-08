package sitent.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

public class FileReadersUtil {

	/**
	 * This function expects a file with two columns separated by tab (\t). The
	 * first column is the key for the HashMap, the second column is the value.
	 * TODO: this is for two Strings at the moment, create for different data
	 * types?
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> readMap(String path) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		CSVReader reader = new CSVReader(new FileReader(path), '\t');
		String[] line = null;
		while ((line = reader.readNext()) != null) {
			if (map.containsKey(line[0])) {
				reader.close();
				throw new IllegalStateException("Duplicate key: " + line[0]);
			}
			map.put(line[0], line[1]);
		}
		reader.close();
		return map;
	}

	/**
	 * This function expects a file which has one item per line, which is read
	 * into a set.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static Set<String> readSet(String path) throws IOException {
		Set<String> set = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (!line.trim().equals("")) {
				set.add(line.trim());
			}
		}
		reader.close();
		return set;
	}

	/**
	 * This function expects a file which has one item per line, which is read
	 * into a list.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static List<String> readList(String path) throws IOException {
		List<String> list = new LinkedList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (!line.trim().equals("")) {
				list.add(line.trim());
			}
		}
		reader.close();
		return list;
	}

	/**
	 * This function expects a file which has one item per line, which is read
	 * into a list.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static List<String> readListIncludingNewlines(String path) throws IOException {
		List<String> list = new LinkedList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = null;
		while ((line = reader.readLine()) != null) {
			list.add(line.trim());
		}
		reader.close();
		return list;
	}

}
