package sitent.syntSemFeatures.segment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class ParagraphClusterFeatures {

	private static void clusterData(String arffLocation, String outputLocation, int NUM_CLUSTERS) throws Exception {

		BufferedReader reader = new BufferedReader(new FileReader(arffLocation));
		Instances data = new Instances(reader);
		reader.close();

		PrintWriter w = new PrintWriter(new File(outputLocation + "_" + NUM_CLUSTERS));

		// remove class attribute
		Remove remove = new Remove();
		List<Integer> toRemove = new LinkedList<Integer>();
		for (int i = 0; i < data.numAttributes(); i++) {
			if (data.attribute(i).name().startsWith("class_")) {
				toRemove.add(i);
			}
		}
		int[] attributes = new int[toRemove.size()];
		for (int i = 0; i < toRemove.size(); i++) {
			attributes[i] = toRemove.get(i);
		}
		remove.setAttributeIndicesArray(attributes);
		remove.setInputFormat(data);
		Filter.useFilter(data, remove);

		// cluster data
		System.out.println("Clustering....");
		EM clusterer = new EM(); // new instance of clusterer
		clusterer.setMaxIterations(100);
		clusterer.setNumClusters(NUM_CLUSTERS);
		clusterer.buildClusterer(data); // build the clusterer
		System.out.println("Done.");

		// assign cluster IDs to paragraphs
		Attribute instIdx = data.attribute("passageId");
		for (int i = 0; i < data.numInstances(); i++) {
			int clusterId = clusterer.clusterInstance(data.get(i));
			double[] dist = clusterer.distributionForInstance(data.get(i));
			String assignments = "";
			for (double d : dist) {
				assignments += "\t" + d;
			}
			w.println(data.get(i).stringValue(instIdx.index()) + "\t" + clusterId + assignments);
		}

		w.close();
	}

	public static void main(String[] args) {
		// Command line options
		Options options = new Options();
		options.addOption("input", true, "ARFF file with the data.");
		options.addOption("output", true, "Output: cluster IDs per instance");
		options.addOption("numClusters", true, "number of clusters");
		// Parse command line and configure
		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cmd = parser.parse(options, args);
			String inputArff = cmd.getOptionValue("input");
			String output = cmd.getOptionValue("output");
			int numClusters = Integer.parseInt(cmd.getOptionValue("numClusters"));
			clusterData(inputArff, output, numClusters);

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
