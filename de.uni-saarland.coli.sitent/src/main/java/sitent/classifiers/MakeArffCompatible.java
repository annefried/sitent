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
import org.apache.log4j.Logger;

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

	static Logger log = Logger.getLogger(MakeArffCompatible.class.getName());

	private List<ArffDoc> docs;
	private Map<String, Set<String>> jointFeatures;
	private Map<String, String> jointFeatureTypes;
	private Map<String, String[]> sortedJointFeatures;
	// joint header string
	private StringBuffer header;
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
					if (line.trim().equals("")) {
						continue;
					}
					String[] values = line.split(",");
					for (String value : values) {
						// remove quotes if necessary
						String[] parts = value.trim().split(" ");
						int featIndex = Integer.parseInt(parts[0]);
						String featName = doc.orderedFeatures.get(featIndex);
						if (parts[1].equals("QUOTE")) {
							// this was wrong in Intercorp features?!
							parts[1] = "\"QUOTE\"";
						}
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
				// quote feature names
				if (!featureName.startsWith("\"")) {
					featureName = "\""
							+ featureName.replaceAll("\"|``", "QUOTE").replaceAll(",", "COMMA").replaceAll(" ", "SPACE")
							+ "\"";
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
		log.info("Reading ARFFs from directory: " + prefix + "/" + inputDir);
		int num = 0;
		int total = new File(prefix + "/" + inputDir).list().length;
		for (String path : new File(prefix + "/" + inputDir).list()) {
			log.info("... " + path + " " + num++ + "/" + total);
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
		log.info("Collecting joint header... (this may take a while)");

		jointFeatures = new HashMap<String, Set<String>>();
		jointFeatureTypes = new HashMap<String, String>();
		sortedJointFeatures = new HashMap<String, String[]>();

		for (ArffDoc doc : docs) {
			System.out.println(doc.path);

			for (String featName : doc.featureType.keySet()) {
				String featureType = doc.featureType.get(featName);
				if (jointFeatures.containsKey(featName)) {
					if (jointFeatureTypes.get(featName).equals("string")) {
						featureType = "string";
					}
				}
				if (!jointFeatureTypes.containsKey(featName)) {
					jointFeatureTypes.put(featName, featureType);
				}
				if (featureType.equals("numeric") && jointFeatureTypes.get(featName).equals("nominal")) {
					// keep it that way
				} else if (featureType.equals("nominal") && jointFeatureTypes.get(featName).equals("numeric")) {
					jointFeatureTypes.put(featName, featureType);
				}
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
		log.info("sorting features ... ");
		for (String featName : jointFeatures.keySet()) {
			List<String> featVals = new LinkedList<String>(jointFeatures.get(featName));
			// add all values from document
			Set<String> featSet = new HashSet<String>(featVals);
			for (ArffDoc doc : docs) {
				for (Map<String, String> inst : doc.instances) {
					if (inst.get(featName) != null) {
						featSet.add(inst.get(featName));
					}
				}
			}
			featVals = new LinkedList<String>(featSet);
			// ... done
			Collections.sort(featVals);
			// put dummy value first, making sure there is no second dummy value
			// due to quotation
			featVals.remove("\"THE-DUMMY-VALUE\"");
			featVals.remove("THE-DUMMY-VALUE");
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

		log.info("Done collecting joint header string.");

		// create header string
		header = new StringBuffer("@relation sitent\n");
		for (String featName : featNames) {
			if (featName.equals("\"null\"")) {
				continue;
			}
			if (jointFeatureTypes.get(featName).equals("numeric")) {
				header.append("@attribute " + featName + " numeric\n");
			} else if (jointFeatureTypes.get(featName).equals("string")) {
				header.append("@attribute " + featName + " string\n");
			} else {
				StringBuffer values = new StringBuffer("");
				for (String value : sortedJointFeatures.get(featName)) {
					if (!value.trim().equals("")) {
						values.append(value + ",");
					}
				}
				values = new StringBuffer(values.substring(0, values.length() - 1));
				header.append("@attribute " + featName + " {" + values + "}\n");
			}
		}
		header.append("\n");
		log.info("Done creating new header string.");
	}

	private void writeCompatibleArffs(String outputDir, boolean sparse, boolean keepDirectoryStructure)
			throws IOException {

		// add one file with all instances
		// PrintWriter wAll = new PrintWriter(new FileWriter(outputDir +
		// "/allData.arff"));
		// wAll.println(header);
		// wAll.println("@data");
		// System.out.println("opening all writer...");
		int num = 0;
		int total = docs.size();

		for (ArffDoc doc : docs) {
			String[] parts = doc.path.split("/");
			String filename = parts[parts.length - 1];
			System.out.println("Writing: " + filename + " " + num++ + "/" + total);
			String outPath = outputDir + "/" + filename;
			if (keepDirectoryStructure) {
				// use doc.path here instead of filename if using dev/test
				outPath = outputDir + "/" + doc.path;
			}
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
					// wAll.println(line);
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
					// wAll.println(line);
					// wAll.flush();
				}
			}

			w.close();
		}
		// wAll.close();
	}

	public static void main(String[] args) {

		Options options = new Options();
		options.addOption("input", true, "Input path with ARFFs: one or more directories.");
		options.addOption("output", true, "Output path for compatible ARFFs.");
		options.addOption("sparse", false, "Arff in sparse format?");
		options.addOption("classAttribute", true, "last attribute in ARFF");
		options.addOption("keepDirs", false,
				"if given, keep directory structure for output (otherwise all ARFFs are written into one directory)");

		// Parse command line and configure
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			String inputDir = cmd.getOptionValue("input");
			String outputDir = cmd.getOptionValue("output");
			boolean sparse = cmd.hasOption("sparse");
			String classAttribute = cmd.getOptionValue("classAttribute");
			boolean keepDirectoryStructure = cmd.hasOption("keepDirs");

			File outputDirFile = new File(outputDir);
			if (outputDirFile.exists()) {
				outputDirFile.delete();
			}
			outputDirFile.mkdirs();

			MakeArffCompatible mac = new MakeArffCompatible();
			// if input directory has subdirectories, process them all
			// assumes only one level of subdirs!
			File inputFile = new File(inputDir);
			log.info("input directory: " + inputFile);
			log.info("is directory? " + inputFile.isDirectory());

			String[] inputDirs = null;
			// are there subdirectories?
			boolean subDirs = false;
			for (String subFile : inputFile.list()) {
				if (new File(inputFile + "/" + subFile).isDirectory()) {
					subDirs = true;
					break;
				}
			}
			log.info("has subdirectories? " + subDirs);

			if (subDirs) {
				inputDirs = inputFile.list();
			} else {
				String directory = FilenameUtils.getName(inputDir);
				log.info("the directory: " + directory);
				inputDirs = new String[] { directory };
			}
			// add the files to the list of documents.
			for (String id : inputDirs) {
				if (inputDirs.length == 1) {
					// only one input directory
					String prefix = FilenameUtils.getPath(inputDir);
					if (inputDir.startsWith("/")) {
						// absolute path
						prefix = "/" + prefix;
					}
					log.info("prefix: " + prefix);
					mac.readArffs(prefix, id, sparse);
				} else {
					mac.readArffs(inputDir, id, sparse);
				}
				// create the matching output directories
				File outDir = new File(outputDir);
				if (outDir.exists()) {
					outDir.delete();
				}
				outDir.mkdirs();
				if (keepDirectoryStructure) {
					outDir = new File (outputDir + "/" + id);
					outDir.mkdirs();
				}
			}
			log.info("now starting to collect the joint header.");
			mac.collectJointHeader(classAttribute);
			log.info("done collecting joint header.");
			mac.writeCompatibleArffs(outputDir, sparse, keepDirectoryStructure);
			log.info("done writing ARFFs");

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
