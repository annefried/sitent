package sitent.syntSemFeatures.segment;

/**
 * Sets main referent as subject of a main verb (which is already given, e.g. in the Mathew&Katz data).
 * 
 * Extracts the features used by M&K.
 * For each verb:
 * - subject: bare plural, definite, indefinite
 * - object: absent, bare plural, definite, indefinite
 * - TODO: others! (??? which others?)
 * 
 * Other features for M&K's system are: tense (coarseTense for mainVerb), progressive and perfect,
 * also for main verb. These are extracted by VerbFeatures.
 * 
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.NP;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.TMOD;
import sitent.syntSemFeatures.nounPhrase.NounPhraseFeatures;
import sitent.types.SEFeature;
import sitent.types.Segment;
import sitent.util.FeaturesUtil;
import sitent.util.SitEntUimaUtils;

public class MathewKatzFeaturesAnnotator extends JCasAnnotator_ImplBase {

	public static final String PARAM_HABIT_ADV_PATH = "habitualAdvPath";
	@ConfigurationParameter(name = PARAM_HABIT_ADV_PATH, mandatory = false, defaultValue = "null", description = "Path to file with habitual adverbs.")
	private String habitualAdvPath;

	private Set<String> habitualAdverbs;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);

		habitualAdverbs = new HashSet<String>();
		try {
			BufferedReader r = new BufferedReader(new FileReader(
					habitualAdvPath));
			String line = r.readLine();
			for (String adv : line.split(",")) {
				habitualAdverbs.add(adv.trim());
			}
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceInitializationException();
		}

	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		// create child node map for efficient access of child nodes
		HashMap<Token, Set<Dependency>> childNodeMap = NounPhraseFeatures
				.getChildNodesMap(jCas);

		// iterate over segments
		// iterate over all the situations and compare the annotations.
		Iterator<Segment> segIt = JCasUtil.iterator(jCas, Segment.class);
	
		
		String prevGold = "none";

		while (segIt.hasNext()) {

			Segment segment = segIt.next();
			
			FeaturesUtil.addFeature("previous_habituality", prevGold, jCas, segment);
			for (Annotation annot : SitEntUimaUtils.getList(segment.getFeatures())) {
				SEFeature feat = (SEFeature) annot;
				if (feat.getName().equals("class_habituality")){
					prevGold = feat.getValue();
					break;
				}
				prevGold ="none";
			}
			

			//System.out.println("\n" + segment.getCoveredText());

			List<Dependency> deps = JCasUtil.selectCovered(Dependency.class,
					segment);

			// (STEP 1) find subject ==> main referent
			Annotation mainRef = getDependent(deps, segment.getMainVerb(),
					"subj");
			if (mainRef == null) {
				// no subject found, try to find subject of a 'dep' head
				for (Dependency dep : deps) {
					if (dep.getDependent() == segment.getMainVerb()
							&& dep.getDependencyType().matches(
									"vmod|dep|conj\\_and")) {
						// does this governor have a subject?
						// System.out.println("found verb: "
						// + dep.getDependencyType() + " "
						// + dep.getGovernor().getCoveredText());
						Annotation subj = getDependent(deps, dep.getGovernor(),
								"subj");
						if (subj != null) {
							// System.out.println("found subj: "
							// + subj.getCoveredText());
							mainRef = subj;
						}
					}
				}
			}
			if (mainRef != null) {
//				// set main referent
//				MainReferent mr = new MainReferent(jCas);
//				mr.setBegin(mainRef.getBegin());
//				mr.setEnd(mainRef.getEnd());
//				mr.addToIndexes();
//				segment.setMainReferent(mr);

				addDependentFeature(mainRef, jCas, childNodeMap, segment, "subj");

			}
			
			if (segment.getMainVerb() == null) {
				// no main verb --> can't predict habituality.
				continue;
			}

			// (STEP 2) find object (if present) and add features
			// TODO: only direct objects?
			Annotation obj = getDependent(deps, segment.getMainVerb(), "obj");
			Boolean objAbsent = obj == null;
			FeaturesUtil.addFeature("main_verb_obj_absent",
					objAbsent.toString(), jCas, segment);
			if (!objAbsent) {
				addDependentFeature(obj, jCas, childNodeMap, segment, "obj");
			}

			// (STEP 3) conditional (if, when, whenever) present in sentence?
			// It's not clear to me from Mathew's thesis whether it must be in
			// the same clause as the verb annotated, or just in the same
			// sentence. (Going for the latter here.)
			// Similarly: a prepositional phrase starting with at/in/on in the
			// sentence?
			List<Sentence> sentences = JCasUtil.selectCovering(jCas,
					Sentence.class, segment.getMainVerb());
			// should always be covered by exactly one sentence
			Sentence sent = sentences.get(0);
			Boolean conditional = false;
			boolean containsIf = false;
			Boolean atPrep = false;
			Boolean inPrep = false;
			Boolean onPrep = false;
			Boolean habitualAdv = false;
			boolean containsWould = false;
			for (Token token : JCasUtil.selectCovered(Token.class, sent)) {
				if (token.getCoveredText().matches("if|when|whenever")) {
					conditional = true;
				}
				if (token.getCoveredText().equals("if")) {
					containsIf = true;
				}
				if (token.getCoveredText().equals("at")) {
					atPrep = true;
				}
				if (token.getCoveredText().equals("in")) {
					inPrep = true;
				}
				if (token.getCoveredText().equals("on")) {
					onPrep = true;
				}
				if (token.getCoveredText().equals("would")) {
					containsWould = true;
				}
				if (habitualAdverbs.contains(token.getCoveredText())) {
					habitualAdv = true;
				}
			}
			FeaturesUtil.addFeature("sent_conditional", conditional.toString(),
					jCas, segment);
			FeaturesUtil.addFeature("sent_atPrep", atPrep.toString(), jCas,
					segment);
			FeaturesUtil.addFeature("sent_inPrep", inPrep.toString(), jCas,
					segment);
			FeaturesUtil.addFeature("sent_onPrep", onPrep.toString(), jCas,
					segment);

			// (STEP 4) Temporal modifiers: quantificational or specific?
			// TODO: this is somewhat approximate for now... test etc!!
			boolean habitualPastMod = !containsIf
					&& (sent.getCoveredText().contains("used to") || containsWould);
			Boolean quantTempMod = false;
			Boolean specificTempMod = false;
			// check TMP modifiers
			List<TMOD> tmods = JCasUtil.selectCovered(TMOD.class, sent);
//			if (!tmods.isEmpty()) {
//				System.out.println("\n" + sent.getCoveredText());
//			}
			
			/**TODO: these rules are stupid.
			 * "a few days" -- specific.
			 * 
			 */
			for (TMOD tmod : tmods) {
				Token token = JCasUtil.selectCovered(Token.class, tmod).get(0);
				// is this token quantified?
				String detType = NounPhraseFeatures.getDeterminerType(jCas,
						token, childNodeMap, false);
				if (detType.matches("def|demon|none|quantSpec|cd")) {
					if (!token.getCoveredText()
							.matches("hours|minutes|seconds")) {
						specificTempMod = true;
					}
				}
				// Mathew treats indef as spc, but I think the rules have to be
				// more precise here. TODO: if followed by after/before, it's SPC.
				if (detType.matches("quantDef|quantIndef")) {
					quantTempMod = true;
				}
				if (detType.equals("indef")) {
					Token following = JCasUtil.selectFollowing(Token.class, token, 1).get(0);
					if (!following.getCoveredText().matches("after|before")) {
						quantTempMod = true;
					}
					else {
						specificTempMod = true;
					}
				}
//				System.out.println(token.getCoveredText() + " " + detType
//						+ " spc:" + specificTempMod + " hab:" + quantTempMod);
			}
			quantTempMod = habitualAdv || habitualPastMod || quantTempMod;

			FeaturesUtil.addFeature("sent_quantTempMod", quantTempMod.toString(), jCas, segment);
			FeaturesUtil.addFeature("sent_specTempMod", specificTempMod.toString(), jCas, segment);
		}
		
		// Mark whether each segment is in the same sentence as the preceding segment
		for (Sentence sent : JCasUtil.select(jCas, Sentence.class)) {
			List<Segment> segments = JCasUtil.selectCovered(Segment.class, sent);
			for (int i=0; i<segments.size(); i++) {
				if (i==0) {
					FeaturesUtil.addFeature("prevSegment_sameSent", "false", jCas, segments.get(i));
				}
				else {
					FeaturesUtil.addFeature("prevSegment_sameSent", "true", jCas, segments.get(i));
				}
			}
		}
		
		

	}

	private void addDependentFeature(Annotation mr, JCas jCas,
			HashMap<Token, Set<Dependency>> childNodeMap, Segment segment,
			String featName) {
		// extract subject-related features
		Token head = SitEntUimaUtils.getHead(mr, jCas);

		// If here, a head of the named entity mention noun phrase has
		// been
		// found.
		if (head != null && head.getPos() != null) {
			// happens rarely -- parser error / out of memory?

			// A POS tag was assigned to the head of the mention by the
			// parser.
			if (NounPhraseFeatures.isNounOrPronoun(head)) {
				String detType = NounPhraseFeatures.getDeterminerType(jCas,
						head, childNodeMap, false);
				String nounType = NounPhraseFeatures.getNounType(head);

				// System.out.println(head.getCoveredText() + " " + detType +
				// " "
				// + nounType);

				String isDefinite = ((Boolean) (detType
						.matches("def|demon|quantDef") || nounType
						.matches("proper|pronoun"))).toString();
				String isIndefinite = ((Boolean) detType
						.matches("indef|quantIndef")).toString();
				FeaturesUtil.addFeature("main_verb_" + featName + "_def",
						isDefinite, jCas, segment);
				FeaturesUtil.addFeature("main_verb_" + featName + "_indef",
						isIndefinite, jCas, segment);

				String barePlural = ((Boolean) NounPhraseFeatures.isBarePlural(
						jCas, head, childNodeMap)).toString();
				FeaturesUtil.addFeature(
						"main_verb_" + featName + "_barePlural", barePlural,
						jCas, segment);

			}
		}

	}

	/**
	 * Returns the subject of the Token verb (if any).
	 * 
	 * @param deps
	 * @param verb
	 * @return
	 */
	private Annotation getDependent(List<Dependency> deps, Annotation verb,
			String type) {
		// System.out.println("looking for: " + verb.getCoveredText());
		for (Dependency dep : deps) {
			// System.out.println("getsubj: "
			// + dep.getDependent().getCoveredText() + " "
			// + dep.getDependencyType() + " "
			// + dep.getGovernor().getCoveredText());
			// System.out.println(dep.getGovernor()== verb);
			if (dep.getGovernor() == verb
					&& dep.getDependencyType().contains(type)) { // vmod in
																	// second
																	// trial?
																	// System.out.println("in getSubj"
																	// +
																	// dep.getDependencyType()
																	// + " "
				// + dep.getDependent().getCoveredText());
				// find covering NP
				List<NP> nps = JCasUtil.selectCovering(NP.class,
						dep.getDependent());
				if (nps.isEmpty()) {
					return dep.getDependent();
				} else {
					// choose the shortest
					NP np = nps.get(0);
					for (int i = 1; i < nps.size(); i++) {
						NP np2 = nps.get(i);
						if (np2.getEnd() - np2.getBegin() < np.getEnd()
								- np.getBegin()) {
							np = np2;
						}
					}
					return np;
				}
			}
		}
		return null;
	}
	
	

	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
	}
}
