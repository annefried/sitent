package sitent.io;

/**
 * @author afried
 * 
 *         Runs the full suite of feature extraction for situation entity type classification.
 */

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;

public class GoldDataToXMI {

	static Logger log = Logger.getLogger(GoldDataToXMI.class.getName());

	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("input", true, "path to directory with raw texts");
		options.addOption("annotations", true, "path to directory with XMLs marked up with gold standard SE types");
		options.addOption("output", true, "path to directory where XMI files will be written");

		// Parse command line and configure
		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cmd = parser.parse(options, args);

			String inputDir = cmd.getOptionValue("input");
			String outputDir = cmd.getOptionValue("output");
			String annotDir = cmd.getOptionValue("annotations");

			// read text
			CollectionReader reader = createReader(TextReaderWithFilename.class,
					TextReaderWithFilename.PARAM_SOURCE_LOCATION, inputDir, TextReaderWithFilename.PARAM_PATTERNS,
					new String[] { "[+]*.txt" }, TextReaderWithFilename.PARAM_LANGUAGE, "en");

			// tokenize, parse, add lemmas
			AnalysisEngineDescription stTokenizer = AnalysisEngineFactory
					.createEngineDescription(StanfordSegmenter.class, StanfordSegmenter.PARAM_LANGUAGE, "en");

			// add annotations from XML files
			// AnalysisEngineDescription xmlReader = AnalysisEngineFactory.createEngineDescription(
			//		XmlAnnotationsReader.class, XmlAnnotationsReader.PARAM_INPUT_DIR, annotDir);

			// write to XMI format (for potential further processing)
			AnalysisEngineDescription xmiWriter = AnalysisEngineFactory.createEngineDescription(XmiWriter.class,
					XmiWriter.PARAM_TARGET_LOCATION, outputDir);

			// gold data and segmentation given
			log.info("Feature extraction: gold annotations given.");
			runPipeline(reader, stTokenizer, xmiWriter);
			

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
