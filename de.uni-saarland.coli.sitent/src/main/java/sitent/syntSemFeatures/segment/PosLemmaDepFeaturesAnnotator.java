package sitent.syntSemFeatures.segment;

/**
 * Extracts POS, words and lemma features (as a reimplementation of Alexis' 2007 ACL paper).
 */

import java.util.Collection;
import java.util.LinkedList;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import sitent.types.Segment;
import sitent.util.FeaturesUtil;
import sitent.util.SitEntUimaUtils;

public class PosLemmaDepFeaturesAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		Collection<Segment> segments = JCasUtil.select(jCas, Segment.class);

		for (Segment segment : segments) {

			//Collection<Token> tokens = JCasUtil.selectCovered(Token.class, segment);
			Collection<Annotation> tokenAnnots = SitEntUimaUtils.getList(segment.getTokens());
			Collection<Token> tokens = new LinkedList<Token>();
			for (Annotation annot : tokenAnnots) {
				tokens.add((Token) annot);
			}
			
			for (Token token : tokens) {

				//String word = token.getCoveredText().replaceAll(" ", "").replaceAll("\\\\", "BACKSLASH");

				// Word and lemma features proved to be impractical
				//FeaturesUtil.addFeature("segment_word_" + word, "1", jCas, segment);
				if (token.getPos() != null) {
					//String lemma = token.getLemma().getValue().replaceAll(" ", "").replaceAll("\\\\", "BACKSLASH");
					String pos = token.getPos().getPosValue().replaceAll(" ", "");
					FeaturesUtil.addFeature("segment_pos_" + pos, "1", jCas, segment);
					//FeaturesUtil.addFeature("segment_lemma_pos", lemma + "_" + pos, jCas, segment);
					//FeaturesUtil.addFeature("segment_lemma_" + lemma, "1", jCas, segment);
					//FeaturesUtil.addFeature("segment_word_pos_" + word + "_" + pos, "1", jCas, segment);
				}

				// TODO: count POS tags instead of binary feature?
			}

			// dependency relations
//			Collection<Dependency> deps = JCasUtil.selectCovered(Dependency.class, segment);
//			for (Dependency dep : deps) {
//				FeaturesUtil.addFeature("segment_depRel_" + dep.getDependencyType(), "1", jCas, segment);
//			}

		}

	}

}
