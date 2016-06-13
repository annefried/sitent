package sitent.classifiers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import sitent.types.ClassificationAnnotation;
import sitent.types.SEFeature;
import sitent.types.Segment;
import sitent.util.FeaturesUtil;
import sitent.util.SitEntUimaUtils;

/**
 * @author afried
 * 
 *         This annotator takes the syntactic-semantic features labeled on the
 *         nouns and verbs and maps them to the Segments for the situation
 *         entity classification task.
 *
 */

public class SitEntFeaturesAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		DocumentMetaData dm = JCasUtil.selectSingle(jCas, DocumentMetaData.class);

		Collection<Segment> segments = JCasUtil.select(jCas, Segment.class);

		for (Segment segment : segments) {

			//System.out.println("\n" + segment.getCoveredText());

			// add the features for main verb
			if (segment.getMainVerb() != null) {
				Token mainVerb = (Token) segment.getMainVerb();
				//System.out.println("> main verb: " + mainVerb.getCoveredText() + " " + mainVerb.getBegin() + " " + mainVerb.getEnd());

				Collection<ClassificationAnnotation> classAnnots = JCasUtil
						.selectCovering(ClassificationAnnotation.class, mainVerb);
				for (ClassificationAnnotation classAnnot : classAnnots) {
					if (classAnnot.getTask() != null && classAnnot.getTask().equals("VERB")) {
						//System.out.println(">> Features found.");
						// use these features
						for (Annotation annot : SitEntUimaUtils.getList(classAnnot.getFeatures())) {
							SEFeature feature = (SEFeature) annot;
							FeaturesUtil.addFeature("main_verb_" + feature.getName(), feature.getValue(), jCas,
									segment);
							//System.out.println(feature.getName() + " " + feature.getValue());
						}
						break;
					}
				}
			} else {
				//System.out.println("> No main verb found.");
			}

			// add the features for main referent
			if (segment.getMainReferent() != null) {
				Token mainReferent = (Token) segment.getMainReferent();
				//System.out.println("> main referent: " + mainReferent.getCoveredText());
				Collection<ClassificationAnnotation> classAnnots = JCasUtil
						.selectCovering(ClassificationAnnotation.class, mainReferent);
				
				List<ClassificationAnnotation> npClassAnnots = new LinkedList<ClassificationAnnotation>();
				for (ClassificationAnnotation classAnnot : classAnnots) {
					if (classAnnot.getTask()!= null && classAnnot.getTask().equals("NP")) {
						npClassAnnots.add(classAnnot);
					}
				}
				classAnnots = npClassAnnots;
				
				// find the 'smallest' one (covering the smallest span)
				if (classAnnots.size() > 1) {
					List<ClassificationAnnotation> largerAnnots = new LinkedList<ClassificationAnnotation>();
					for (ClassificationAnnotation classAnnot1 : classAnnots) {
						for (ClassificationAnnotation classAnnot2 : classAnnots) {
							if (classAnnot1 != classAnnot2) {
								if (SitEntUimaUtils.coversLarger(classAnnot1, classAnnot2)) {
									largerAnnots.add(classAnnot1);
								}
							}
						}
					}
					List<ClassificationAnnotation> reducedClassAnnots = new LinkedList<ClassificationAnnotation>();
					reducedClassAnnots.addAll(classAnnots);
					for (ClassificationAnnotation classAnnot : largerAnnots) {
						reducedClassAnnots.remove(classAnnot);
						//System.out.println("removing: " + classAnnot.getCoveredText());
					}
					classAnnots = reducedClassAnnots;
				}
							
				
				// only use info for the head
				for (ClassificationAnnotation classAnnot : classAnnots) {
					if (classAnnot.getTask() != null && classAnnot.getTask().equals("NP")) {
//						System.out.println(">> features found.");
//						System.out.println("   classannot: " + classAnnot.getCoveredText());
						// use these features
						for (Annotation annot : SitEntUimaUtils.getList(classAnnot.getFeatures())) {
							SEFeature feature = (SEFeature) annot;
							FeaturesUtil.addFeature("main_referent_" + feature.getName(), feature.getValue(), jCas,
									segment);
							//System.out.println(feature.getName() + " " + feature.getValue());
						}
						break;
					}
				}
			} else {
				//System.out.println("> No main referent found.");
			}
			
			// add features for object of the main verb (e.g., for habituality classification)
			

			// add instance id feature (for mapping predictions back to segments
			// later)
			String instanceId = dm.getDocumentId() + "_" + segment.getSegid();
			FeaturesUtil.addFeature("instanceid", instanceId, jCas, segment);

		}

	}

}
