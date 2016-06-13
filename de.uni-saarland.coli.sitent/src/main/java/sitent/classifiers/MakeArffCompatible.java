package sitent.classifiers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

/**
 * 
 * @author afried
 * 
 *         Takes all the ARFF files in one directory and makes them compatible
 *         (by merging the headers). It assumes that all features with the same
 *         name have the same type.
 *
 */

public class MakeArffCompatible {

	private List<ArffDoc> docs;
	private Map<String, Set<String>> jointFeatures;
	private Map<String, String> jointFeatureTypes;
	private Map<String, String[]> sortedJointFeatures;
	// joint header string
	private String header;
	private Map<String, Integer> featNameToIndex;
	private Map<Integer, String> featIndextoName;
	private List<String> featNames;

	public MakeArffCompatible() {
		docs = new LinkedList<ArffDoc>();
	}

	/**
	 * Reads one document.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private ArffDoc readDocument(String path, boolean sparse, String prefix) throws IOException {
		ArffDoc doc = new ArffDoc(path, prefix);
		BufferedReader r = new BufferedReader(new FileReader(path));
		String line;
		boolean data = false;
		while ((line = r.readLine()) != null) {
			if (data) {
				// map: feature name to feature value
				Map<String, String> instance = new HashMap<String, String>();
				if (!sparse) {
					String[] values = line.trim().split(",");
					for (int i = 0; i < values.length; i++) {
						String value = values[i];
						// remove quotes if necessary
						String featName = doc.orderedFeatures.get(i);
						instance.put(featName, value);
					}
				} else {
					// remove brackets
					line = line.trim().substring(1, line.trim().length() - 1);
					String[] values = line.split(",");
					for (String value : values) {
						// remove quotes if necessary
						String[] parts = value.trim().split(" ");
						int featIndex = Integer.parseInt(parts[0]);
						String featName = doc.orderedFeatures.get(featIndex);
						instance.put(featName, parts[1]);
					}
				}
				doc.instances.add(instance);
			} else if (line.startsWith("@attribute")) {
				String[] parts = line.split(" ");
				String featureName = parts[1];
				if (parts.length < 3) {
					continue;
				}
				if (parts[2].equals("numeric")) {
					doc.featureType.put(featureName, "numeric");
				} else if (parts[2].equals("string")) {
					doc.featureType.put(featureName, "string");
				} else {
					doc.featureType.put(featureName, "nominal");
					String[] values = parts[2].substring(1, parts[2].length() - 1).split(",");
					doc.features.put(featureName, values);
				}
				doc.orderedFeatures.add(featureName);
			} else if (line.startsWith("@data")) {
				data = true;
			}
		}
		r.close();
		return doc;
	}

	/**
	 * Reads in ARFFs in input directory.
	 * 
	 * @param inputDir
	 * @throws IOException
	 */
	public void readArffs(String prefix, String inputDir, boolean sparse) throws IOException {
		System.out.println("Reading ARFFs from directory: " + prefix + "/" + inputDir);
		for (String path : new File(prefix + "/" + inputDir).list()) {
			System.out.println(path);
			docs.add(readDocument(prefix + "/" + inputDir + "/" + path, sparse, prefix));
		}
	}

	public class ArffDoc {
		String path;
		Map<String, String> featureType;
		// features as ordered in this document
		List<String> orderedFeatures;
		// features and values in the ARFF
		Map<String, String[]> features;
		// instances and their values
		List<Map<String, String>> instances;

		public ArffDoc(String path, String prefix) {
			// keep only 'local' path
			this.path = path.replace(prefix, "");
			// System.out.println("Path for this document: " + this.path);
			features = new HashMap<String, String[]>();
			instances = new LinkedList<Map<String, String>>();
			featureType = new HashMap<String, String>();
			orderedFeatures = new LinkedList<String>();
		}
	}

	/**
	 * collect info for joint header
	 * 
	 */
	private void collectJointHeader(String className) {
		System.out.println("Collecting joint header...");

		jointFeatures = new HashMap<String, Set<String>>();
		jointFeatureTypes = new HashMap<String, String>();
		sortedJointFeatures = new HashMap<String, String[]>();

		for (ArffDoc doc : docs) {
			for (String featName : doc.featureType.keySet()) {
				String featureType = doc.featureType.get(featName);
				if (jointFeatures.containsKey(featName)) {
					if (jointFeatureTypes.get(featName).equals("string")) {
						featureType = "string";
					}
				}
				jointFeatureTypes.put(featName, featureType);
				if (featureType.equals("nominal")) {
					if (!jointFeatures.containsKey(featName)) {
						jointFeatures.put(featName, new HashSet<String>());
					}
					for (String featVal : doc.features.get(featName)) {
						jointFeatures.get(featName).add(featVal);
					}
				}
			}
		}
		// sort feature values for joint header
		for (String featName : jointFeatures.keySet()) {
			List<String> featVals = new LinkedList<String>(jointFeatures.get(featName));
			Collections.sort(featVals);
			// put dummy value first
			featVals.remove("\"THE-DUMMY-VALUE\"");
			featVals.add(0, "\"THE-DUMMY-VALUE\"");
			String[] featValArray = new String[featVals.size()];
			featVals.toArray(featValArray);
			sortedJointFeatures.put(featName, featValArray);
		}

		// sorted list of feature names
		featNames = new LinkedList<String>(jointFeatureTypes.keySet());
		Collections.sort(featNames);

		// move class to end
		className = "\"" + className + "\"";
		featNames.remove(className);
		featNames.add(className);

		// feature index for each feature name
		featNameToIndex = new HashMap<String, Integer>();
		featIndextoName = new HashMap<Integer, String>();
		for (int i = 0; i < featNames.size(); i++) {
			featNameToIndex.put(featNames.get(i), i);
			featIndextoName.put(i, featNames.get(i));
		}

		System.out.println("... collecting joint header string.");

		// create header string
		header = "@relation sitent\n";
		for (String featName : featNames) {
			if (featName.equals("\"null\"")) {
				continue;
			}
			// System.out.println(featName);
			if (jointFeatureTypes.get(featName).equals("numeric")) {
				header += "@attribute " + featName + " numeric\n";
			} else if (jointFeatureTypes.get(featName).equals("string")) {
				header += "@attribute " + featName + " string\n";
			} else {
				String values = "";
				for (String value : sortedJointFeatures.get(featName)) {
					if (!value.trim().equals("")) {
						values += "" + value + ",";
					}
				}
				values = values.substring(0, values.length() - 1);
				header += "@attribute " + featName + " {" + values + "}\n";
			}
		}
		header += "\n";
		System.out.println("Done.");
	}

	private void writeCompatibleArffs(String outputDir, boolean sparse) throws IOException {

		// add one file with all instances
		//PrintWriter wAll = new PrintWriter(new FileWriter(outputDir + "/allData.arff"));
		//wAll.println(header);
		//wAll.println("@data");
		//System.out.println("opening all writer...");

		for (ArffDoc doc : docs) {
			String[] parts = doc.path.split("/");
			String filename = parts[parts.length - 1];
			System.out.println("Writing: " + filename);
			// use doc.path here instead of filename if using dev/test
			String outPath = outputDir + "/" + filename;
			// write header
			PrintWriter w = new PrintWriter(new FileWriter(outPath));
			w.println(header);

			w.println("@data");
			for (Map<String, String> instances : doc.instances) {
				if (!sparse) {
					String line = "";
					for (String featName : featNames) {
						if (instances.containsKey(featName)) {
							line += instances.get(featName) + ",";
						} else {
							line += "?,";
						}
					}
					line = line.substring(0, line.length() - 1);
					w.println(line);
					//wAll.println(line);
				} else {
					// sparse format
					String values = "";
					List<Integer> indices = new LinkedList<Integer>();
					for (String featName : instances.keySet()) {
						indices.add(featNameToIndex.get(featName));
					}
					Collections.sort(indices);

					for (int index : indices) {
						values += index + " " + instances.get(featIndextoName.get(index)) + ", ";
					}
					String line = "{" + values.substring(0, values.length() - 2) + "}";
					w.println(line);
//					wAll.println(line);
//					wAll.flush();
				}
			}

			w.close();
		}
//		wAll.close();
	}

	public static void main(String[] args) {

		Options options = new Options();
		options.addOption("input", true, "Input path with ARFFs: one or more directories.");
		options.addOption("output", true, "Output path for compatible ARFFs.");
		options.addOption("sparse", false, "Arff in sparse format?");
		options.addOption("classAttribute", true, "last attribute in ARFF");

		// Parse command line and configure
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			String inputDir = cmd.getOptionValue("input");
			String outputDir = cmd.getOptionValue("output");
			boolean sparse = cmd.hasOption("sparse");
			String classAttribute = cmd.getOptionValue("classAttribute");

			File outputDirFile = new File(outputDir);
			if (outputDirFile.exists()) {
				outputDirFile.delete();
			}
			outputDirFile.mkdirs();

			MakeArffCompatible mac = new MakeArffCompatible();
			// if input directory has subdirectories, process them all
			// assumes only one level of subdirs!
			File inputFile = new File(inputDir);
			System.out.println("input directory: " + inputFile);
			System.out.println("is dir? " + inputFile.isDirectory());

			String[] inputDirs = null;
			// are there subdirectories?
			boolean subDirs = false;
			for (String subFile : inputFile.list()) {
				if (new File(inputFile + "/" + subFile).isDirectory()) {
					subDirs = true;
					break;
				}
			}
			System.out.println("subdirs? " + subDirs);

			if (subDirs) {
				inputDirs = inputFile.list();
			} else {
				String directory = FilenameUtils.getName(inputDir);
				inputDirs = new String[] { directory };
			}
			// add the files to the list of documents.
			for (String id : inputDirs) {
				if (inputDirs.length == 1) {
					// only one input directory
					String prefix = FilenameUtils.getPath(inputDir);
					mac.readArffs(prefix, id, sparse);
				} else {
					mac.readArffs(inputDir, id, sparse);
				}
				// create the matching output directories
				File outDir = new File(outputDir);
				//File outDir = new File(outputDir + "/" + id);
				if (outDir.exists()) {
					outDir.delete();
				}
				outDir.mkdirs();
			}
			mac.collectJointHeader(classAttribute);
			mac.writeCompatibleArffs(outputDir, sparse);

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
