package sitent.io;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;

/**
 * Adds the predictions made by CRF++ to the JCas.
 * 
 */

import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import au.com.bytecode.opencsv.CSVReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import sitent.types.Segment;
import sitent.util.FeaturesUtil;

public class AddCrfppPredictions extends JCasAnnotator_ImplBase {
	
	static Logger log = Logger.getLogger(AddCrfppPredictions.class.getName());

	public static final String PARAM_FEATURE_NAME = "featureName";
	@ConfigurationParameter(name = PARAM_FEATURE_NAME, mandatory = true, defaultValue = "null", description = "Name of this predicted feature.")
	private String featureName;

	public static final String PARAM_PREDICTIONS_FILE = "predictionsFile";
	@ConfigurationParameter(name = PARAM_PREDICTIONS_FILE, mandatory = true, defaultValue = "null", description = "Path to CSV file with predictions.")
	private String predictionsFile;

	Map<String, String> predictions;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		// read in predictions from CSV (that is output by CRF++)
		try {
			CSVReader reader = new CSVReader(new FileReader(predictionsFile), '\t');
			String[] line;
			predictions = new HashMap<String, String>();
			while ((line = reader.readNext()) != null) {
				String instanceId = line[0];
				String prediction = line[line.length - 1];
				predictions.put(instanceId, prediction);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		for (Segment segment : JCasUtil.select(jCas, Segment.class)) {
			String instanceId = FeaturesUtil.getFeatureValue("instanceid", segment);
			// prediction as feature (if a prediction has been made for this
			// segment)
			if (instanceId.startsWith("\"") && instanceId.endsWith("\"")) {
				instanceId = instanceId.substring(1, instanceId.length()-1);
			}
			if (predictions.containsKey(instanceId)) {
				FeaturesUtil.addFeature(featureName, predictions.get(instanceId), jCas, segment);
			}
		}

	}

	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("input", true,
				"path to directory with XMIs marked up with gold standard / predicted SE types");
		options.addOption("outputXmi", true, "path to directory where XMI files will be written");
		options.addOption("featureName", true, "name to be used in SEFeature list");
		options.addOption("predictions", true, "path to file with CRF predictions");
		// TODO: several predictions

		// Parse command line and configure
		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cmd = parser.parse(options, args);

			String inputDir = cmd.getOptionValue("input");
			String outputXmiDir = cmd.getOptionValue("outputXmi");
			String featureName = cmd.getOptionValue("featureName");
			String predFile = cmd.getOptionValue("predictions");

			// read XMI
			CollectionReader reader = createReader(XmiReader.class, XmiReader.PARAM_SOURCE_LOCATION, inputDir,
					XmiReader.PARAM_PATTERNS, new String[] { "[+]*.xmi" });

			AnalysisEngineDescription addPredictions = AnalysisEngineFactory.createEngineDescription(
					AddCrfppPredictions.class, AddCrfppPredictions.PARAM_FEATURE_NAME, featureName,
					AddCrfppPredictions.PARAM_PREDICTIONS_FILE, predFile);

			AnalysisEngineDescription xmiWriter = AnalysisEngineFactory.createEngineDescription(XmiWriter.class,
					XmiWriter.PARAM_TARGET_LOCATION, outputXmiDir);

			runPipeline(reader, addPredictions, xmiWriter);

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		} catch (UIMAException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
