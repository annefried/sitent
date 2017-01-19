package sitent.syntSemFeatures.io;

/**
 * This class contains a configuration of a UIMA pipeline that extracts the
 * syntactic-semantic features as described in the ACL 2015 paper.
 * Running this will take a while as parsing is included.
 */

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import sitent.syntSemFeatures.io.SyntSemFeaturesCSVWriter;
import sitent.syntSemFeatures.nounPhrase.NounPhraseFeaturesAnnotator;
import sitent.syntSemFeatures.nounPhrase.NounPhraseSelectorAnnotator;
import sitent.syntSemFeatures.verbs.VerbFeaturesAnnotator;
import sitent.syntSemFeatures.verbs.VerbSelectorAnnotator;
import sitent.tests.ParseWriterAnnotator;

public class FeatureExtractionPipeline {

	private static String wordNetPath = null;
	private static String countabilityPath = null;

	private static void process(String inputDir, String xmiOutputDir, String csvOutputDir, String parseDir)
			throws UIMAException, IOException {
		
		CollectionReader reader = createReader(TextReader.class, TextReader.PARAM_SOURCE_LOCATION, inputDir,
				TextReader.PARAM_LANGUAGE, "en", TextReader.PARAM_PATTERNS, new String[] { "[+]*/*" }); // suffix .txt?

		// Preprocessing with Stanford CoreNLP components
		AnalysisEngineDescription stTokenizer = AnalysisEngineFactory.createEngineDescription(StanfordSegmenter.class,
				StanfordSegmenter.PARAM_LANGUAGE, "en");

		AnalysisEngineDescription stParser = AnalysisEngineFactory.createEngineDescription(StanfordParser.class,
				StanfordParser.PARAM_LANGUAGE, "en", StanfordParser.PARAM_WRITE_POS, true,
				StanfordParser.PARAM_WRITE_PENN_TREE, true, StanfordParser.PARAM_MAX_TOKENS, 200,
				StanfordParser.PARAM_WRITE_CONSTITUENT, true, StanfordParser.PARAM_WRITE_DEPENDENCY, true,
				StanfordParser.PARAM_MODE, StanfordParser.DependenciesMode.CC_PROPAGATED);

		AnalysisEngineDescription stLemmas = AnalysisEngineFactory.createEngineDescription(StanfordLemmatizer.class);

		// NP feature extraction components: select the noun phrases for which
		// to extract features.
		// See NounPhraseSelectorAnnotator for possible argument choices.
		AnalysisEngineDescription npSelector = AnalysisEngineFactory.createEngineDescription(
				NounPhraseSelectorAnnotator.class, NounPhraseSelectorAnnotator.PARAM_TARGET, "AllNounPhrases");

		// Extract the NP-based features.
		AnalysisEngineDescription npFeatures = AnalysisEngineFactory.createEngineDescription(
				NounPhraseFeaturesAnnotator.class, NounPhraseFeaturesAnnotator.PARAM_COUNTABILITY_PATH,
				countabilityPath, NounPhraseFeaturesAnnotator.PARAM_WORDNET_PATH, wordNetPath);

		// Select the verbs for which to extract features.
		AnalysisEngineDescription verbSelector = AnalysisEngineFactory
				.createEngineDescription(VerbSelectorAnnotator.class);

		// Extract the verb-based features.
		AnalysisEngineDescription verbFeatures = AnalysisEngineFactory.createEngineDescription(
				VerbFeaturesAnnotator.class, VerbFeaturesAnnotator.PARAM_WORDNET_PATH, wordNetPath,
				VerbFeaturesAnnotator.PARAM_TENSE_FILE, "resources/tense/tense.txt");

		// Write standoff CSV file with features.
		AnalysisEngineDescription csvWriter = null;
		if (csvOutputDir != null) {
			csvWriter = AnalysisEngineFactory.createEngineDescription(SyntSemFeaturesCSVWriter.class,
					SyntSemFeaturesCSVWriter.PARAM_OUTPUT_FOLDER, csvOutputDir);
		}
		
		// write out dependency parses (for development)
		AnalysisEngineDescription parseWriter = null;
		if (parseDir != null) {
			parseWriter = AnalysisEngineFactory.createEngineDescription(ParseWriterAnnotator.class,
					ParseWriterAnnotator.PARAM_OUTPUT_FILE, parseDir);
		}

		// writes out XMIs (can then be inspected with UIMA annotation viewer,
		// or used for further processing in an UIMA pipeline)
		AnalysisEngineDescription xmiWriter = null;
		if (xmiOutputDir != null) {
			xmiWriter = AnalysisEngineFactory.createEngineDescription(XmiWriter.class, XmiWriter.PARAM_TARGET_LOCATION,
					xmiOutputDir);
		}

		if (xmiOutputDir != null && csvOutputDir != null) {
			runPipeline(reader, stTokenizer, stParser, stLemmas, npSelector, npFeatures, verbSelector, verbFeatures,
					csvWriter, xmiWriter);
		}
		if (xmiOutputDir != null && csvOutputDir == null) {
			runPipeline(reader, stTokenizer, stParser, stLemmas, npSelector, npFeatures, verbSelector, verbFeatures,
					xmiWriter);
		}

		if (xmiOutputDir == null && csvOutputDir != null) {
			// TODO: proper configuration of pipeline for parseWriter
			runPipeline(reader, stTokenizer, stParser, stLemmas, npSelector, npFeatures, verbSelector, verbFeatures,
					csvWriter, parseWriter);
		}

	}

	public static void main(String[] args) {

		Options options = new Options();
		options.addOption("input", true, "Path to directories with input texts, separated by commas (required).");
		options.addOption("xmiOutput", true, "Output path for XMI (optional).");
		options.addOption("wordnet", true, "Path to WordNet database (required).");
		options.addOption("countability", true, "Path to file with countability information (optional).");
		options.addOption("csvOutput", true, "Path to folder for CSV output (standoff format, optional).");
		options.addOption("parserOutput", true, "Path to folder where to write parses (optional)");
		
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("SitEnt SyntSem FeatureExtraction", options);

		// Parse command line and configure
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
			if (!cmd.hasOption("input")) {
				System.err.println("Please give an input directory!");
				formatter.printHelp("SitEnt SyntSem FeatureExtraction", options);
				return;
			}
			String inputDir = cmd.getOptionValue("input");
			String xmiOutputDir = cmd.getOptionValue("xmiOutput");
			String csvOutputDir = cmd.getOptionValue("csvOutput");
			String parseDir = cmd.getOptionValue("parserOutput");

			if (cmd.hasOption("wordnet")) {
				wordNetPath = cmd.getOptionValue("wordnet");
			}
			if (cmd.hasOption("countability")) {
				countabilityPath = cmd.getOptionValue("countability");
			}

			if (csvOutputDir != null && !(new File(csvOutputDir).exists())) {
				new File(csvOutputDir).mkdirs();
			}
			
			if (parseDir != null && !(new File(parseDir).exists())) {
				new File(parseDir).mkdirs();
			}
			
			if (csvOutputDir == null && xmiOutputDir == null) {
				System.out.println("Please give either a path for XMI output, or for CSV output."
						+ " Otherwise you won't be able to see the extracted features!");
				formatter.printHelp("SitEnt SyntSem FeatureExtraction", options);
				return;
			}

			process(inputDir, xmiOutputDir, csvOutputDir, parseDir);

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
