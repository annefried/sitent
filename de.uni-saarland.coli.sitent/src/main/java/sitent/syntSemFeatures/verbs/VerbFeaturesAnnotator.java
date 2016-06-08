package sitent.syntSemFeatures.verbs;

/**
 * @author afried (Annemarie Friedrich)
 * 
 * Sets the tense and voice of all verbs in the JCas. Adds tense, voice, perfect, progressive
 * and a coarse tense feature.
 * Tense extraction follows:
 * 
 * Loaiciga, Sharid, Thomas Meyer, and Andrei Popescu-Belis. "English-French Verb Phrase Alignment
 * in Europarl for Tense Translation Modeling." The Ninth Language Resources and Evaluation Conference.
 * LREC 2014.
 * 
 */

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.ISynset;
import sitent.types.ClassificationAnnotation;
import sitent.types.VerbFeatures;
import sitent.util.FeaturesUtil;
import sitent.util.GrammarUtils;
import sitent.util.WordNetUtils;

public class VerbFeaturesAnnotator extends JCasAnnotator_ImplBase {

	public static final String PARAM_TENSE_FILE = "tenseFile";
	@ConfigurationParameter(name = PARAM_TENSE_FILE, mandatory = true, defaultValue = "null", description = "File with tense patterns by Thomas Meyer and Sharid Loaiciga.")
	private String tenseFile;

	public static final String PARAM_WORDNET_PATH = "wordnetPath";
	@ConfigurationParameter(name = PARAM_WORDNET_PATH, mandatory = true, defaultValue = "null", description = "Path to WordNet database.")
	private String wordnetPath;

	private IDictionary wordnet = null;

	private static final int CONTEXT_WINDOW_SIZE = 10;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {

		// initalize the configuration parameters
		super.initialize(context);

		try {
			wordnet = WordNetUtils.getDictionary(wordnetPath);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceInitializationException();
		}

	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		// create map for efficient access of child nodes
		HashMap<Token, Set<Dependency>> childNodeMap = GrammarUtils.getChildNodesMap(jcas);

		// (STEP 1) set tense and voice of the verbs
		TenseExtraction te;
		try {
			te = new TenseExtraction(tenseFile);
			te.setTense(jcas);
		} catch (IOException e) {
			System.err.println("Failure instantiating tense extraction module.");
			e.printStackTrace();
			throw new RuntimeException();
		}

		// (STEP 2) add the features for the verbs in question
		Iterator<ClassificationAnnotation> annots = JCasUtil.select(jcas, ClassificationAnnotation.class).iterator();

		while (annots.hasNext()) {

			ClassificationAnnotation annot = annots.next();

			if (annot.getTask() == null) {
				continue;
			}

			if (!annot.getTask().equals("VERB")) {
				continue;
			}

			List<VerbFeatures> vfList = JCasUtil.selectCovered(VerbFeatures.class, annot);

			if (vfList.isEmpty()) {
				System.out.println("No features found: " + annot.getCoveredText());
				// no verb features found here
				continue;
			}

			// found features for the main verb: prepare them as features for
			// the classification task. They can be extracted for copula.
			VerbFeatures verbFeat = vfList.get(0);

			FeaturesUtil.addFeature("verb_tense", verbFeat.getTense(), jcas, annot);
			FeaturesUtil.addFeature("verb_voice", verbFeat.getVoice(), jcas, annot);

			Boolean perfect = verbFeat.getTense().contains("perf");
			FeaturesUtil.addFeature("verb_perfect", perfect.toString(), jcas, annot);

			Boolean progressive = verbFeat.getTense().contains("cont");
			FeaturesUtil.addFeature("verb_progressive", progressive.toString(), jcas, annot);

			// coarse tense: present, past, infinitive
			String coarseTense = "none";
			if (verbFeat.getTense().contains("past")) {
				coarseTense = "past";
			} else if (verbFeat.getTense().contains("pres")) {
				coarseTense = "present";
			} else if (verbFeat.getTense().contains("fut")) {
				coarseTense = "future";
			} else if (verbFeat.getTense().contains("infinitives")) {
				coarseTense = "infinitive";
			} else if (verbFeat.getTense().contains("modal")) {
				coarseTense = "modal";
				// TODO: might be interesting to distinguish them
			}
			FeaturesUtil.addFeature("verb_coarseTense", coarseTense, jcas, annot);

			Collection<Token> tokens = JCasUtil.selectCovered(Token.class, annot);
			// should be exactly one
			Token verb = tokens.iterator().next();

			// This is the actual verb in constructions like 'be great' / 'be a
			// person'.
			List<Dependency> deps = JCasUtil.selectCovered(Dependency.class, verb);
			Token predicate = verb;
			for (Dependency dep : deps) {
				if (dep.getDependencyType().equals("cop")) {
					predicate = dep.getGovernor();
				}
			}

			// WordNet features: set for predicate
			WordNetUtils.setWordNetFeatures(predicate, annot, jcas, "predicate_", wordnet);

			// This main verb will be the copula in such cases!
			FeaturesUtil.addFeature("verb_lemma", verb.getLemma().getValue(), jcas, annot);
			FeaturesUtil.addFeature("verb_pos", verb.getPos().getPosValue(), jcas, annot);

			// the noun/adjective is used in copula constructions
			FeaturesUtil.addFeature("predicate_lemma", predicate.getLemma().getValue(), jcas, annot);
			FeaturesUtil.addFeature("predicate_pos", predicate.getPos().getPosValue(), jcas, annot);

			// ==> use 'verb' or 'predicate' version of the two above, depending
			// on the classification task!

			// Some more clause-based features from Nils Reiter
			// clause-based features
			Boolean hasTemporalModifier = ClauseFeatures.hasTemporalModifier(predicate, childNodeMap);
			Integer numModifiers = ClauseFeatures.getNumberOfModifiers(predicate, childNodeMap);
			// "Adjunct" features (or at least something similar)
			String advDegree = ClauseFeatures.getAdverbialDegree(predicate, childNodeMap);
			String advLemma = ClauseFeatures.getLemmaOfAdvClause(predicate, childNodeMap);

			FeaturesUtil.addFeature("clauseHasTmod", hasTemporalModifier.toString(), jcas, annot);
			FeaturesUtil.addFeature("clauseNumMod", numModifiers.toString(), jcas, annot);
			FeaturesUtil.addFeature("clauseAdverbDegree", advDegree, jcas, annot);
			FeaturesUtil.addFeature("clauseAdverbPred", advLemma, jcas, annot);

			// add dependency relation features
			// these might not be very meaningful for verbs (verbs are often the
			// head)
			GrammarUtils.setDependencyRelationFeatures(verb, jcas, annot, "");

			// add context lemmas
			// List<Token> contextLeft = JCasUtil.selectPreceding(Token.class, annot, CONTEXT_WINDOW_SIZE);
			// List<Token> contextRight = JCasUtil.selectFollowing(Token.class, annot, CONTEXT_WINDOW_SIZE);
			// FeaturesUtil.addFeature("contextLeft", concatValues(contextLeft), jcas, annot);
			// FeaturesUtil.addFeature("contextRight", concatValues(contextRight), jcas, annot);

		}

	}

	private String concatValues(List<Token> tokens) {
		String retVal = "";
		for (Token token : tokens) {
			if (!token.getCoveredText().matches("\\p{Punct}+")) {
				retVal += token.getLemma().getValue() + ":";
			}
		}
		if (!retVal.equals("")) {
			retVal = retVal.substring(0, retVal.length() - 1);
		}
		return retVal;
	}
	
	

}
