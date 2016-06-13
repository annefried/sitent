package sitent.classifiers;

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
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import sitent.io.TextReaderWithFilename;
import sitent.io.XmlAnnotationsReader;
import sitent.segmentation.SituationEntityIdentifierAnnotator;
import sitent.syntSemFeatures.nounPhrase.NounPhraseFeaturesAnnotator;
import sitent.syntSemFeatures.nounPhrase.NounPhraseSelectorAnnotator;
import sitent.syntSemFeatures.segment.Acl2007FeaturesAnnotator;
import sitent.syntSemFeatures.segment.BrownClusterFeaturesAnnotator;
import sitent.syntSemFeatures.segment.MathewKatzFeaturesAnnotator;
import sitent.syntSemFeatures.segment.PosLemmaDepFeaturesAnnotator;
import sitent.syntSemFeatures.segment.SpeechModeFeaturesAnnotator;
import sitent.syntSemFeatures.verbs.LinguisticIndicatorsAnnotator;
import sitent.syntSemFeatures.verbs.VerbFeaturesAnnotator;
import sitent.syntSemFeatures.verbs.VerbSelectorAnnotator;

public class SitEntFeatureExtraction {

	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("input", true, "path to directory with raw texts");
		options.addOption("annotations", true, "path to directory with XMLs marked up with gold standard SE types");
		options.addOption("output", true, "path to directory where XMI files will be written");
		options.addOption("segment", false, "if given, apply situation entity segmenter");
		options.addOption("wordnet", true, "Path to WordNet database (required).");
		options.addOption("countability", true, "Path to file with countability information (optional).");
		options.addOption("arff", true, "Path to directory where ARFF files will be written");
		options.addOption("task", true,
				"classification task to be carried out: class_sitent_type, class_aspectual_class, class_habituality or class_main_referent");

		// Parse command line and configure
		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cmd = parser.parse(options, args);

			String inputDir = cmd.getOptionValue("input");
			String outputDir = cmd.getOptionValue("output");
			String annotDir = cmd.getOptionValue("annotations");
			// segment texts if no gold annotations are given
			boolean segment = cmd.hasOption("annotDir");
			String countabilityPath = cmd.getOptionValue("countability");
			String arffPath = cmd.getOptionValue("arff");
			String task = cmd.getOptionValue("task");

			// read XMI
			CollectionReader reader = createReader(TextReaderWithFilename.class,
					TextReaderWithFilename.PARAM_SOURCE_LOCATION, inputDir, TextReaderWithFilename.PARAM_PATTERNS,
					new String[] { "[+]*.txt" }, TextReaderWithFilename.PARAM_LANGUAGE, "en");

			// tokenize, parse, add lemmas
			AnalysisEngineDescription stTokenizer = AnalysisEngineFactory
					.createEngineDescription(StanfordSegmenter.class, StanfordSegmenter.PARAM_LANGUAGE, "en");

			AnalysisEngineDescription stParser = AnalysisEngineFactory.createEngineDescription(StanfordParser.class,
					StanfordParser.PARAM_LANGUAGE, "en", StanfordParser.PARAM_WRITE_POS, true,
					StanfordParser.PARAM_WRITE_PENN_TREE, true, StanfordParser.PARAM_MAX_TOKENS, 200,
					StanfordParser.PARAM_WRITE_CONSTITUENT, true, StanfordParser.PARAM_WRITE_DEPENDENCY, true,
					StanfordParser.PARAM_MODE, StanfordParser.DependenciesMode.CC_PROPAGATED);

			AnalysisEngineDescription stLemmas = AnalysisEngineFactory
					.createEngineDescription(StanfordLemmatizer.class);

			// add annotations from XML files
			AnalysisEngineDescription xmlReader = AnalysisEngineFactory.createEngineDescription(
					XmlAnnotationsReader.class, XmlAnnotationsReader.PARAM_INPUT_DIR, annotDir);

			// segmenter
			AnalysisEngineDescription segmenter = AnalysisEngineFactory
					.createEngineDescription(SituationEntityIdentifierAnnotator.class);

			// extract syntactic-semantic features
			AnalysisEngineDescription npSelector = AnalysisEngineFactory.createEngineDescription(
					NounPhraseSelectorAnnotator.class, NounPhraseSelectorAnnotator.PARAM_TARGET, "AllNounPhrases");
			AnalysisEngineDescription npFeatures = AnalysisEngineFactory.createEngineDescription(
					NounPhraseFeaturesAnnotator.class, NounPhraseFeaturesAnnotator.PARAM_COUNTABILITY_PATH,
					countabilityPath, NounPhraseFeaturesAnnotator.PARAM_WORDNET_PATH, "resources/wordnet3.0");
			AnalysisEngineDescription verbSelector = AnalysisEngineFactory
					.createEngineDescription(VerbSelectorAnnotator.class);
			AnalysisEngineDescription verbFeatures = AnalysisEngineFactory.createEngineDescription(
					VerbFeaturesAnnotator.class, VerbFeaturesAnnotator.PARAM_WORDNET_PATH, "resources/wordnet3.0",
					VerbFeaturesAnnotator.PARAM_TENSE_FILE, "resources/tense/tense.txt");
			AnalysisEngineDescription lingInd = AnalysisEngineFactory.createEngineDescription(
					LinguisticIndicatorsAnnotator.class, LinguisticIndicatorsAnnotator.PARAM_LING_IND_FILE,
					"resources/linguistic_indicators/linguistic-indicators-Gigagword-AFE-XIE.csv");

			// add features for classification to the Segment annotations for
			// the above syntactic-semantic features
			AnalysisEngineDescription sitEntFeatureMapper = AnalysisEngineFactory
					.createEngineDescription(SitEntFeaturesAnnotator.class);

			// add some more features directly to the Segment annotations
			// extract segment-based POS/lemma/word features
			AnalysisEngineDescription posLemma = AnalysisEngineFactory
					.createEngineDescription(PosLemmaDepFeaturesAnnotator.class);

			// extract features designed for recognition of QUESTIONS +
			// IMPERATIVES
			AnalysisEngineDescription speechModeFeatures = AnalysisEngineFactory.createEngineDescription(
					SpeechModeFeaturesAnnotator.class, SpeechModeFeaturesAnnotator.PARAM_QUESTION_WORDS_FILE,
					"resources/word_lists/question_words.txt", SpeechModeFeaturesAnnotator.PARAM_WORDNET_PATH,
					"resources/wordnet3.0");

			AnalysisEngineDescription mkFeatures = AnalysisEngineFactory.createEngineDescription(
					MathewKatzFeaturesAnnotator.class, MathewKatzFeaturesAnnotator.PARAM_HABIT_ADV_PATH,
					"resources/mathew_katz_lists/habitual-adverbs.txt");

			AnalysisEngineDescription acl2007Features = AnalysisEngineFactory
					.createEngineDescription(Acl2007FeaturesAnnotator.class);

			AnalysisEngineDescription brownFeatures = AnalysisEngineFactory.createEngineDescription(
					BrownClusterFeaturesAnnotator.class, BrownClusterFeaturesAnnotator.PARAM_BROWN_CLUSTER_DIR,
					"resources/brown_clusters");

			// write ARFF files (for classification toolkit Weka, also used to
			// generate CRFPP input files)
			AnalysisEngineDescription arffWriter = AnalysisEngineFactory.createEngineDescription(
					WekaArffWriterAnnotator.class, WekaArffWriterAnnotator.PARAM_RESET_SEGIDS, false,
					WekaArffWriterAnnotator.PARAM_ARFF_LOCATION, arffPath,
					WekaArffWriterAnnotator.PARAM_CLASS_ATTRIBUTE, task, WekaArffWriterAnnotator.PARAM_SPARSE_FORMAT,
					true, WekaArffWriterAnnotator.PARAM_OMIT_FEATURES, "segment_acl2007_G_verbLemma_.*",
					WekaArffWriterAnnotator.PARAM_TARGET_TYPE, "Segment");
			// can add more features to be omitted here if necessary
			// (comma-separated list)

			// write to XMI format (for potential further processing)
			AnalysisEngineDescription xmiWriter = AnalysisEngineFactory.createEngineDescription(XmiWriter.class,
					XmiWriter.PARAM_TARGET_LOCATION, outputDir);

			if (!segment) {
				// gold data and segmentation given

				runPipeline(reader, stTokenizer, stParser, stLemmas, xmlReader, npSelector, npFeatures, verbSelector,
						verbFeatures, lingInd, sitEntFeatureMapper, posLemma, speechModeFeatures, mkFeatures,
						acl2007Features, brownFeatures, arffWriter, xmiWriter);
			} else {
				// unlabeled text data, need to identify a situation entity
				// segmentation
				runPipeline(reader, stTokenizer, stParser, stLemmas, segmenter, npSelector, npFeatures, verbSelector,
						verbFeatures, lingInd, sitEntFeatureMapper, posLemma, speechModeFeatures, mkFeatures,
						acl2007Features, brownFeatures, arffWriter, xmiWriter);
			}

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
