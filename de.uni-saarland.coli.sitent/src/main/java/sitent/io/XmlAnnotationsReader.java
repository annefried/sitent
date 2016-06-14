package sitent.io;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
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
			if (!xmlFile.exists()) {
				System.err.println("There are no gold annotations for : " + docid);
				return;
			}

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
				System.out.println(segAnnot.getCoveredText());

				Node mvNode = segment.selectSingleNode("mainVerb");
				if (mvNode != null) {
					int mvBegin = Integer.parseInt(mvNode.valueOf("@begin"));
					int mvEnd = Integer.parseInt(mvNode.valueOf("@end"));
					// select token
					List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, mvBegin, mvEnd);
					if (tokens.isEmpty()) {
						System.err.println("No token for this main verb? " + mvBegin + " " + mvEnd);
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
						System.err.println("No token for this main referent? " + mrBegin + " " + mrEnd);
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
						if (seType != null)
							FeaturesUtil.addFeature("class_sitent_type", seType, jCas, segAnnot);
						if (mainRef != null)
							FeaturesUtil.addFeature("class_main_referent", mainRef, jCas, segAnnot);
						if (habituality != null)
							FeaturesUtil.addFeature("class_habituality", habituality, jCas, segAnnot);
						if (lexAsp != null)
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

				// add the tokens of the Segment to the list of segment tokens
				for (Token token : JCasUtil.selectCovered(Token.class, segAnnot)) {
					segAnnot.setTokens(SitEntUimaUtils.addToFSList(segAnnot.getTokens(), token, jCas));
				}

				segAnnot.addToIndexes();
			}

		} catch (DocumentException e) {
			e.printStackTrace();
			throw new AnalysisEngineProcessException();
		}
	}

}
