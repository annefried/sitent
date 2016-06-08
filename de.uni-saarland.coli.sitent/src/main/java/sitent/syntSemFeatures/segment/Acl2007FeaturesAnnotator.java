package sitent.syntSemFeatures.segment;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import sitent.types.Segment;
import sitent.util.FeaturesUtil;

public class Acl2007FeaturesAnnotator extends JCasAnnotator_ImplBase {

	// Alexis' predicate lists.
	String[] gen_preds = { "invent", "Invent", "invented", "Invented", "used to", "extinct", "Extinct" };
	String[] force_preds = { "force", "Force", "convince", "Convince", "persuade", "Persuade" };
	String[] prop_subj_preds = { "seem", "Seem", "appear", "Appear" };
	String[] prop_obj_preds = { "believe", "Believe", "fear", "Fear", "hope", "Hope", "want", "Want", "affirm",
			"Affirm", "deny", "Deny", "assume", "Assume" };
	String[] fact_subj_preds = { "seem", "Seem", "appear", "Appear" };
	String[] fact_obj_preds = { "realize", "Realize", "remember", "Remember", "discover", "Discover", "know", "Know",
			"hear", "Hear", "forget", "Forget", "regret", "Regret", "resent", "Resent", "deplore", "Deplore" };

	Set<String> gen_preds_set = new HashSet<String>();
	Set<String> force_preds_set = new HashSet<String>();
	Set<String> prop_subj_preds_set = new HashSet<String>();
	Set<String> prop_obj_preds_set = new HashSet<String>();
	Set<String> fact_subj_preds_set = new HashSet<String>();
	Set<String> fact_obj_preds_set = new HashSet<String>();

	// Alexis' adverb lists.
	String[] volitional_adverbs = { "deliberately" };
	String[] frequency_adverbs = { "always", "usually", "daily", "monthly", "weekly", "never", "annually" };
	String[] modal_adverbs = {"probably", "likely", "truly", "possibly"};

	Set<String> volitional_adverbs_set = new HashSet<String>();
	Set<String> frequency_adverbs_set = new HashSet<String>();
	Set<String> modal_adverbs_set = new HashSet<String>();

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		initSet(gen_preds, gen_preds_set);
		initSet(force_preds, force_preds_set);
		initSet(prop_subj_preds, prop_subj_preds_set);
		initSet(prop_obj_preds, prop_obj_preds_set);
		initSet(fact_subj_preds, fact_subj_preds_set);
		initSet(fact_obj_preds, fact_obj_preds_set);
		initSet(volitional_adverbs, volitional_adverbs_set);
		initSet(frequency_adverbs, frequency_adverbs_set);
		initSet(modal_adverbs, modal_adverbs_set);
	}

	public void initSet(String[] preds, Set<String> set) {
		for (String pred : preds) {
			set.add(pred);
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		Collection<Segment> segments = JCasUtil.select(jCas, Segment.class);
		List<Segment> segmentList = new LinkedList<Segment>(segments);
		for (int i = 0; i < segmentList.size(); i++) {
			Boolean gen_pred = false;
			Boolean force_pred = false;
			Boolean prop_pred = false;
			Boolean fact_pred = false;

			Segment segment = segmentList.get(i);
			force_pred = containsPred(segment, force_preds_set);
			prop_pred = containsPred(segment, prop_subj_preds_set);
			prop_pred = prop_pred || containsPred(segment, prop_obj_preds_set);
			fact_pred = containsPred(segment, fact_subj_preds_set);
			fact_pred = fact_pred || containsPred(segment, fact_obj_preds_set);
			gen_pred = containsPred(segment, gen_preds_set);

			if (i > 0) {
				// can check previous segment as well
				Segment prevSegment = segmentList.get(i - 1);
				force_pred = force_pred || containsPred(prevSegment, force_preds_set);
				prop_pred = prop_pred || containsPred(prevSegment, prop_subj_preds_set);
				prop_pred = prop_pred || containsPred(prevSegment, prop_obj_preds_set);

			}

			FeaturesUtil.addFeature("segment_acl2007_L_forcePred", force_pred.toString(), jCas, segment);
			FeaturesUtil.addFeature("segment_acl2007_L_factPred", fact_pred.toString(), jCas, segment);
			FeaturesUtil.addFeature("segment_acl2007_L_propPred", prop_pred.toString(), jCas, segment);
			FeaturesUtil.addFeature("segment_acl2007_L_genPred", gen_pred.toString(), jCas, segment);

			Collection<Token> tokens = JCasUtil.selectCovered(Token.class, segment);

			Boolean hasFiniteVerb = false;
			Boolean hasModal = false;
			Token firstVerb = null;

			Boolean hasFrequencyAdverb = false;
			Boolean hasModalAdverb = false;
			Boolean hasVolitionalAdverb = false;

			for (Token token : tokens) {
				// Penn treebank POS tags
				if (token.getPos() == null) {
					continue;
				}
				String posValue = token.getPos().getPosValue();
				if (posValue.matches("VB[DPZ]")) {
					hasFiniteVerb = true;
				}
				if (posValue.equals("MD")) {
					hasModal = true;
				}
				if (firstVerb == null && posValue.startsWith("V")) {
					firstVerb = token;
				}
				// features for all verbs in clause
				// isn't this redundant to the words features??
				if (posValue.startsWith("V")) {
					FeaturesUtil.addFeature("segment_acl2007_G_verbLemma_" + token.getLemma().getValue(), "1", jCas,
							segment);
					FeaturesUtil.addFeature("segment_acl2007_G_verbSurface_" + token.getCoveredText(), "1", jCas,
							segment);
					FeaturesUtil.addFeature("segment_acl2007_G_verbPos_" + token.getPos().getPosValue(), "1", jCas,
							segment);
				}

				// adverb features
				if (volitional_adverbs_set.contains(token.getLemma().getValue())) {
					hasVolitionalAdverb = true;
				}
				if (frequency_adverbs_set.contains(token.getLemma().getValue())) {
					hasFrequencyAdverb = true;
				}
				if (modal_adverbs_set.contains(token.getLemma().getValue())) {
					hasModalAdverb = true;
				}

			}

			FeaturesUtil.addFeature("segment_acl2007_L_hasFiniteVerb", hasFiniteVerb.toString(), jCas, segment);
			FeaturesUtil.addFeature("segment_acl2007_L_hasModal", hasModal.toString(), jCas, segment);
			if (firstVerb != null) {
				FeaturesUtil.addFeature("segment_acl2007_L_firstVerb", firstVerb.getCoveredText(), jCas, segment);
				FeaturesUtil.addFeature("segment_acl2007_L_firstVerbPos", firstVerb.getPos().getPosValue(), jCas,
						segment);
			}

			FeaturesUtil.addFeature("segment_acl2007_L_hasModalAdverb", hasModalAdverb.toString(), jCas, segment);
			FeaturesUtil.addFeature("segment_acl2007_L_hasFreqAdverb", hasFrequencyAdverb.toString(), jCas, segment);
			FeaturesUtil.addFeature("segment_acl2007_L_hasVolitionalAdverb", hasVolitionalAdverb.toString(), jCas,
					segment);

			// main verb is main_verb_verb_lemma
			// subject is main_referent_mentionLemma (lemma ok?)

		}

	}

	/**
	 * Returns true if the segment contains one of the predicates.
	 * 
	 * @param segment
	 * @param preds
	 * @return
	 */
	private boolean containsPred(Segment segment, Set<String> preds) {
		Collection<Token> tokens = JCasUtil.selectCovered(Token.class, segment);
		for (Token token : tokens) {
			String lemma = token.getLemma().getValue();
			if (preds.contains(lemma)) {
				return true;
			}
		}
		return false;
	}

}
