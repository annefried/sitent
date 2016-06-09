package sitent.io;

/**
 * @author afried
 * 
 * This annotator reads in the manually created annotations for situation entity types,
 * genericity, habituality and aspectual class from XML files. For each *.txt files,
 * the corresponding annotations should be in a *.xml file. It reads in each annotator's
 * labels as well as the gold standard labels (if contained in the XML file).
 * 
 */

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import sitent.types.Segment;
import sitent.types.Situation;
import sitent.util.FeaturesUtil;
import sitent.util.SitEntUimaUtils;

public class XmlAnnotationsReader extends JCasAnnotator_ImplBase {

	public static final String PARAM_INPUT_DIR = "inputDir";
	@ConfigurationParameter(name = PARAM_INPUT_DIR, mandatory = true, defaultValue = "null")
	private String inputDir;

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		String docid = SitEntUimaUtils.getDocid(jCas);
		System.out.println("Processing: " + docid);

		// read XML with annotations for this file and add annotations to JCas
		File xmlFile;
		try {
			xmlFile = new File(inputDir + "/" + docid.replace(".txt", ".xml"));
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Element root = document.getRootElement();

			for (@SuppressWarnings("unchecked")
			Iterator<Element> i = root.elementIterator("segment"); i.hasNext();) {
				Element segment = i.next();
				String instanceId = segment.attributeValue("instanceid");
				int begin = Integer.parseInt(segment.attributeValue("begin"));
				int end = Integer.parseInt(segment.attributeValue("end"));

				Segment segAnnot = new Segment(jCas);
				FeaturesUtil.addFeature("instanceid", instanceId, jCas, segAnnot);
				segAnnot.setBegin(begin);
				segAnnot.setEnd(end);

				Node mvNode = segment.selectSingleNode("mainVerb");
				if (mvNode != null) {
					int mvBegin = Integer.parseInt(mvNode.valueOf("@begin"));
					int mvEnd = Integer.parseInt(mvNode.valueOf("@end"));
					// select token
					List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, mvBegin, mvEnd);
					if (tokens.isEmpty()) {
						System.err.println("No token for this main verb?");
						throw new AnalysisEngineProcessException();
					} else {
						segAnnot.setMainVerb(tokens.get(0));
					}
				}
				Node mrNode = segment.selectSingleNode("mainReferent");
				if (mrNode != null) {
					int mrBegin = Integer.parseInt(mrNode.valueOf("@begin"));
					int mrEnd = Integer.parseInt(mrNode.valueOf("@end"));
					// select token
					List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, mrBegin, mrEnd);
					if (tokens.isEmpty()) {
						System.err.println("No token for this main referent?");
						throw new AnalysisEngineProcessException();
					} else {
						segAnnot.setMainReferent(tokens.get(0));
					}
				}

				for (@SuppressWarnings("unchecked")
				Iterator<Element> a = segment.elementIterator("annotation"); a.hasNext();) {
					Element annotation = a.next();
					String annotator = annotation.attributeValue("annotator");
					String seType = annotation.attributeValue("seType");
					String mainRef = annotation.attributeValue("mainReferentGenericity");
					String habituality = annotation.attributeValue("habituality");
					String lexAsp = annotation.attributeValue("mainVerbAspectualClass");
					// add gold info to segment
					if (annotator.equals("gold")) {
						FeaturesUtil.addFeature("class_sitent_type", seType, jCas, segAnnot);
						FeaturesUtil.addFeature("class_main_referent", mainRef, jCas, segAnnot);
						FeaturesUtil.addFeature("class_habituality", habituality, jCas, segAnnot);
						FeaturesUtil.addFeature("class_aspectual_class", lexAsp, jCas, segAnnot);
					} else {
						// human annotator; add Situation annotation to JCas
						Situation sitAnnot = new Situation(jCas);
						sitAnnot.setBegin(begin);
						sitAnnot.setEnd(end);
						sitAnnot.setAnnotator(annotator);
						if (seType != null) {
							String[] seTypes = seType.split(":");
							for (String st : seTypes) {
								sitAnnot.setSeType(SitEntUimaUtils.addToStringList(sitAnnot.getSeType(), st, jCas));
							}
						}
						if (mainRef != null) {
							String[] mainRefs = mainRef.split(":");
							for (String mr : mainRefs) {
								sitAnnot.setMainReferent(
										SitEntUimaUtils.addToStringList(sitAnnot.getMainReferent(), mr, jCas));
							}
						}
						if (habituality != null) {
							String[] habits = habituality.split(":");
							for (String h : habits) {
								sitAnnot.setHabituality(
										SitEntUimaUtils.addToStringList(sitAnnot.getHabituality(), h, jCas));
							}
						}
						if (lexAsp != null) {
							String[] asps = lexAsp.split(":");
							for (String asp : asps) {
								sitAnnot.setAspectualClass(
										SitEntUimaUtils.addToStringList(sitAnnot.getAspectualClass(), asp, jCas));
							}
						}
						sitAnnot.addToIndexes();
					}
				}

				segAnnot.addToIndexes();

			}

		} catch (DocumentException e) {
			e.printStackTrace();
			throw new AnalysisEngineProcessException();
		}
	}

	/**
	 * For testing intermediate steps: read in raw text, add annotations
	 * (including pre-processed gold standard if given in XML files).
	 * 
	 * @param args
	 */
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

			// read XMI
			CollectionReader reader = createReader(TextReaderWithFilename.class,
					TextReaderWithFilename.PARAM_SOURCE_LOCATION, inputDir, TextReaderWithFilename.PARAM_PATTERNS,
					new String[] { "[+]*.txt" }, TextReaderWithFilename.PARAM_LANGUAGE, "en");

			// need to run Stanford tokenizer before reading in the annotations
			// (for setting references to main verb and main referent tokens).
			AnalysisEngineDescription stTokenizer = AnalysisEngineFactory
					.createEngineDescription(StanfordSegmenter.class, StanfordSegmenter.PARAM_LANGUAGE, "en");

			// add annotations from XML files
			AnalysisEngineDescription xmlReader = AnalysisEngineFactory.createEngineDescription(
					XmlAnnotationsReader.class, XmlAnnotationsReader.PARAM_INPUT_DIR, annotDir);

			AnalysisEngineDescription xmiWriter = AnalysisEngineFactory.createEngineDescription(XmiWriter.class,
					XmiWriter.PARAM_TARGET_LOCATION, outputDir);

			runPipeline(reader, stTokenizer, xmlReader, xmiWriter);

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
