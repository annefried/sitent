package sitent.io;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
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
import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import sitent.types.Segment;
import sitent.types.Situation;
import sitent.util.AnnotationComparator;
import sitent.util.FeaturesUtil;
import sitent.util.SitEntUimaUtils;

/**
 * Writes situation entity types corpus to XML format: assumes Segments, finds
 * situations and writes this info to a readable XML format. TODO: create
 * similar format with standoff annotations where situation entities are
 * indicated only by the verbs in the text.
 * 
 * @author afried
 *
 */
public class CorpusToCsvWriterAnnotator extends JCasAnnotator_ImplBase {

	public static final String PARAM_OUTPUT_DIR = "outputDir";
	@ConfigurationParameter(name = PARAM_OUTPUT_DIR, mandatory = true, defaultValue = "null", description = "path to directory where XML files will be written")
	private String outputDir;

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		DocumentMetaData dm = JCasUtil.selectSingle(jCas, DocumentMetaData.class);
		String documentId = dm.getDocumentId().replace(".txt", ".xml");
		System.out.println("document id: " + documentId);

		// create XML document
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("document");

		// add segments
		Collection<Segment> segments = JCasUtil.select(jCas, Segment.class);
		int idx = 1;
		for (Segment segment : segments) {
			Element segEl = root.addElement("segment");
			segEl.addAttribute("instanceid", dm.getDocumentId() + "_" + idx);
			segEl.addAttribute("begin", Integer.toString(segment.getBegin()));
			segEl.addAttribute("end", Integer.toString(segment.getEnd()));

			Element text = segEl.addElement("text");
			text.addText(segment.getCoveredText());
			if (segment.getMainVerb() != null) {
				Element mainVerbEl = segEl.addElement("mainVerb");
				mainVerbEl.addText(segment.getMainVerb().getCoveredText());
				mainVerbEl.addAttribute("begin", Integer.toString(segment.getMainVerb().getBegin()));
				mainVerbEl.addAttribute("end", Integer.toString(segment.getMainVerb().getEnd()));
			}
			if (segment.getMainReferent() != null) {
				Element mainRefEl = segEl.addElement("mainReferent");
				mainRefEl.addText(segment.getMainReferent().getCoveredText());
				mainRefEl.addAttribute("begin", Integer.toString(segment.getMainReferent().getBegin()));
				mainRefEl.addAttribute("end", Integer.toString(segment.getMainReferent().getEnd()));
			}
			
			// tokens (approximation if segmentation was based on verbs, not on SPADE)
			Element tokens = segEl.addElement("tokens");
			List<Annotation> tokenList = new LinkedList<Annotation>();
			for (Annotation annot : SitEntUimaUtils.getList(segment.getTokens())) {
				tokenList.add(annot);
			}
			Collections.sort(tokenList, new AnnotationComparator());
			String tokenString = "";
			for (Annotation token : tokenList) {
				tokenString +=  token.getCoveredText() + " ";
			}
			tokenString = tokenString.substring(0,tokenString.length()-1);
			tokens.addText(tokenString);

			// gold standard created via majority voting
			String goldSeType = FeaturesUtil.getFeatureValue("class_sitent_type", segment);
			String goldMainRef = FeaturesUtil.getFeatureValue("class_main_referent", segment);
			String goldHabit = FeaturesUtil.getFeatureValue("class_habituality", segment);
			String goldAspClass = FeaturesUtil.getFeatureValue("class_aspectual_class", segment);

			if (goldSeType != null || goldMainRef != null || goldHabit != null || goldAspClass != null) {
				Element annotEl = segEl.addElement("annotation");
				annotEl.addAttribute("annotator", "gold");

				if (goldSeType != null)
					annotEl.addAttribute("seType", goldSeType);

				if (goldMainRef != null)
					annotEl.addAttribute("mainReferentGenericity", goldMainRef);

				if (goldHabit != null)
					annotEl.addAttribute("habituality", goldHabit);

				if (goldAspClass != null)
					annotEl.addAttribute("mainVerbAspectualClass", goldAspClass);
			}

			String predSeType = FeaturesUtil.getFeatureValue("predicted_class_sitent_type", segment);
			String predMainRef = FeaturesUtil.getFeatureValue("predicted_class_main_referent", segment);
			String predHabit = FeaturesUtil.getFeatureValue("predicted_class_habituality", segment);
			String predAspClass = FeaturesUtil.getFeatureValue("predicted_class_aspectual_class", segment);

			if (predSeType != null || predMainRef != null || predHabit != null || predAspClass != null) {

				Element annotEl = segEl.addElement("annotation");
				annotEl.addAttribute("annotator", "predicted");

				if (predSeType != null)
					annotEl.addAttribute("seType", predSeType);

				if (predMainRef != null)
					annotEl.addAttribute("mainReferentGenericity", predMainRef);

				if (predHabit != null)
					annotEl.addAttribute("habituality", predHabit);

				if (predAspClass != null)
					annotEl.addAttribute("mainVerbAspectualClass", predAspClass);
			}

			// automatically predicted

			// annotations of each annotator
			Collection<Situation> situations = JCasUtil.selectCovered(Situation.class, segment);
			for (Situation sit : situations) {
				Element annotEl = segEl.addElement("annotation");
				annotEl.addAttribute("annotator", sit.getAnnotator());
				String seType = concatVals(sit.getSeType());
				annotEl.addAttribute("seType", seType);

				String mainRef = concatVals(sit.getMainReferent());
				annotEl.addAttribute("mainReferentGenericity", mainRef);

				String habit = concatVals(sit.getHabituality());
				annotEl.addAttribute("habituality", habit);

				String aspClass = concatVals(sit.getAspectualClass());
				annotEl.addAttribute("mainVerbAspectualClass", aspClass);
			}
			idx++;
		}

		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer;

		try {
			writer = new XMLWriter(new FileOutputStream(outputDir + File.separator + documentId), format);
			writer.write(document);
			writer.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

	}

	public static String concatVals(StringList list) {
		String seType = "";
		for (String st : SitEntUimaUtils.getList(list)) {
			if (st.equals("SPEECH_ACT")) {
				continue;
			}
			seType += st + ":";
		}
		if (seType.length() > 0) {
			seType = seType.substring(0, seType.length() - 1);
		}
		return seType;
	}

	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("input", true,
				"path to directory with XMIs marked up with gold standard / predicted SE types");
		options.addOption("output", true, "path to directory where XML files will be written");

		// Parse command line and configure
		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cmd = parser.parse(options, args);

			String inputDir = cmd.getOptionValue("input");
			String outputDir = cmd.getOptionValue("output");

			// read XMI
			CollectionReader reader = createReader(XmiReader.class, XmiReader.PARAM_SOURCE_LOCATION, inputDir,
					XmiReader.PARAM_PATTERNS, new String[] { "[+]*.xmi" });

			AnalysisEngineDescription xmlWriter = AnalysisEngineFactory.createEngineDescription(
					CorpusToCsvWriterAnnotator.class, CorpusToCsvWriterAnnotator.PARAM_OUTPUT_DIR, outputDir);

			runPipeline(reader, xmlWriter);

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
