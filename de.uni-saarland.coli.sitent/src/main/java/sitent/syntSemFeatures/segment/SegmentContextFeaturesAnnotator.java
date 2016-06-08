package sitent.syntSemFeatures.segment;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Collect features for somewhat wider local context of a segment, sentence or gliding window.
 */

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import sitent.types.Segment;
import sitent.util.FeaturesUtil;
import sitent.util.SitEntUtils;

public class SegmentContextFeaturesAnnotator extends JCasAnnotator_ImplBase {

	// segments to the left & right of the Segment
	private static final int WINDOW_SIZE = 5;

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		List<Segment> segments = new LinkedList<Segment>(JCasUtil.select(jCas, Segment.class));
		// collect statistics per Segment
		// TODO: could be unified with the annotator for the segment-based
		// stats!

		// features for each segment (counts)
		Map<Integer, Map<String, Double>> featureMap = new HashMap<Integer, Map<String, Double>>();
		// number of tokens for each segment, needed for normalization later on
		Map<Integer, Integer> numTokensPerSegment = new HashMap<Integer, Integer>();

		System.out.println("Collecting features");
		for (int i = 0; i < segments.size(); i++) {
			Collection<Token> tokens = JCasUtil.selectCovered(Token.class, segments.get(i));
			numTokensPerSegment.put(i, tokens.size());
			Map<String, Double> features = new HashMap<String, Double>();
			// count words, pos tags, lemmas per segment
			for (Token token : tokens) {
				if (token.getCoveredText().matches("[a-z0-9]*")) {
					// use only alphanumeric cases
					// word
					SitEntUtils.incrementMapForKeyDouble(features, "word_" + token.getCoveredText());
					// lemma
					SitEntUtils.incrementMapForKeyDouble(features, "lemma_" + token.getLemma().getValue());
				}
				if (token.getPos() != null) {
					SitEntUtils.incrementMapForKeyDouble(features, "pos_" + token.getPos().getPosValue());
				}
			}
			featureMap.put(i, features);
		}

		System.out.println("creating counts");
		// add features for wider context of each segment
		for (int i = 0; i < segments.size(); i++) {
			int sum = 0;
			Map<String, Double> aggregatedFeatures = new HashMap<String, Double>();
			for (int x = Math.max(0, i - WINDOW_SIZE); x < Math.min(i + WINDOW_SIZE, segments.size()); x++) {
				System.out.println(x + " / " + segments.size());
				sum += numTokensPerSegment.get(x);
				for (String key : featureMap.get(x).keySet()) {
					for (int k = 0; k < featureMap.get(x).get(key); k++) {
						SitEntUtils.incrementMapForKeyDouble(aggregatedFeatures, key);
					}
				}
			}
			// normalize
			for (String key : aggregatedFeatures.keySet()) {
				String featVal = new Double(aggregatedFeatures.get(key) / (double) sum).toString();
				FeaturesUtil.addFeature("context_" + key, featVal, jCas, segments.get(i));
			}

		}

	}

}
