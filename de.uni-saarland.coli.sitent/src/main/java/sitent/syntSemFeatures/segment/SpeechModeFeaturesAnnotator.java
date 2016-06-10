package sitent.syntSemFeatures.segment;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;


import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.ISynset;
import sitent.types.Segment;
import sitent.util.FeaturesUtil;
import sitent.util.FileReadersUtil;
import sitent.util.GrammarUtils;
import sitent.util.WordNetUtils;

/**
 * Selection of features which I hope to be useful for the identification of
 * QUESTIONS and IMPERATIVES.
 * 
 * TODO: contains some additional new features: split into different files.
 * 
 * @author afried
 *
 */

public class SpeechModeFeaturesAnnotator extends JCasAnnotator_ImplBase {

	public static final String PARAM_QUESTION_WORDS_FILE = "qWordsFile";
	@ConfigurationParameter(name = PARAM_QUESTION_WORDS_FILE, mandatory = true, defaultValue = "null", description = "File with set of question words.")
	private String qWordsFile;

	private Set<String> questionWords;

	/***********************/

	public static final String PARAM_WORDNET_PATH = "wordnetPath";
	@ConfigurationParameter(name = PARAM_WORDNET_PATH, mandatory = true, defaultValue = "null", description = "Path to WordNet database.")
	private String wordnetPath;

	private IDictionary wordnet = null;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		try {
			questionWords = FileReadersUtil.readSet(qWordsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			wordnet = WordNetUtils.getDictionary(wordnetPath);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceInitializationException();
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		// genre feature
		DocumentMetaData dm = JCasUtil.selectSingle(jCas, DocumentMetaData.class);
		String docid = dm.getDocumentId();
		String genre = docid.split("_")[0];
		// System.out.println("GENRE: " + genre);

		Collection<Segment> segments = JCasUtil.select(jCas, Segment.class);

		for (Segment segment : segments) {
			
			FeaturesUtil.addFeature("document_genre", genre, jCas, segment);

			String seclass = null;
			try {
				seclass = FeaturesUtil.getFeatureValue("class_sitent_type", segment);
			} catch (IllegalStateException e) {

			}

			// question word contained in segment?
			// punctuation features: ?, !
			Collection<Token> tokens = JCasUtil.selectCovered(Token.class, segment);
			for (Token token : tokens) {
				String tokenText = token.getCoveredText();
				if (questionWords.contains(tokenText)) {
					FeaturesUtil.addFeature("segment_questionWord_" + tokenText, "1", jCas, segment);
				}
				if (tokenText.contains("?")) {
					FeaturesUtil.addFeature("segment_speechMode_?", "1", jCas, segment);
				}
				if (tokenText.contains("!")) {
					FeaturesUtil.addFeature("segment_speechMode_!", "1", jCas, segment);
				}
				if (tokenText.matches("please|Please|PLEASE|let|Let|LET")) {
					FeaturesUtil.addFeature("segment_speechMode_" + tokenText.toLowerCase(), "1", jCas, segment);
				}
			}

			// if (seclass != null && seclass.matches("IMPERATIVE|QUESTION")) {
			// System.out.println(seclass + "\t" + segment.getCoveredText());
			// }

			// some features capturing relevant word order
			Token mainVerb = (Token) segment.getMainVerb();
			if (mainVerb != null) {
				// does the main verb have a subject?
				// dependency relations

				Token origMainVerb = null;

				// if (seclass != null &&
				// seclass.matches("IMPERATIVE|QUESTION")) {
				//
				// System.out.println("MAIN VERB: " +
				// mainVerb.getCoveredText());
				// }

				Collection<Sentence> sents = JCasUtil.selectCovering(Sentence.class, mainVerb);
				for (Sentence sent : sents) {
					List<Dependency> deps = JCasUtil.selectCovered(Dependency.class, sent);
					Token aux = null;

					// change main verb if copula (only for computing this)
					for (Dependency dep : deps) {
						if (dep.getDependent() == mainVerb && dep.getDependencyType().matches("cop")) {
							origMainVerb = mainVerb;
							mainVerb = dep.getGovernor();
						}
					}

					for (Dependency dep : deps) {
						if (dep.getGovernor() == mainVerb && dep.getDependencyType().matches("aux")) {
							FeaturesUtil.addFeature("main_verb_hasAux", "1", jCas, segment);
							aux = dep.getDependent();
						}
					}

					for (Dependency dep : deps) {
						// if (seclass != null &&
						// seclass.matches("IMPERATIVE|QUESTION")) {
						// System.out.println(dep.getGovernor().getCoveredText()
						// + " --> " + dep.getDependencyType()
						// + " --> " + dep.getDependent().getCoveredText());
						// }

						// TODO: do we need more rules for copula?
						if (dep.getGovernor() == mainVerb && dep.getDependencyType().matches(".*subj")) {
							FeaturesUtil.addFeature("main_verb_hasSubj", "1", jCas, segment);
							// subject after main verb?
							Token relevantToken = origMainVerb != null ? origMainVerb : aux != null ? aux : mainVerb;
							//
							// if (seclass != null &&
							// seclass.matches("IMPERATIVE|QUESTION")) {
							// System.out.println("subj: " +
							// dep.getDependent().getCoveredText() + " "
							// + dep.getDependent().getBegin());
							// System.out.println(
							// "verb: " + relevantToken.getCoveredText() + " " +
							// relevantToken.getBegin());
							// }

							if (relevantToken.getBegin() < dep.getDependent().getBegin()) {
								FeaturesUtil.addFeature("main_verb_subjAfterVerb", "true", jCas, segment);
							} else {
								FeaturesUtil.addFeature("main_verb_subjAfterVerb", "false", jCas, segment);
							}

						}

					}
				}
			}
			try {
				FeaturesUtil.getFeatureValue("main_verb_hasSubj", segment);
			} catch (IllegalStateException e) {
				FeaturesUtil.addFeature("main_verb_hasSubj", "0", jCas, segment);
			}
			try {
				FeaturesUtil.getFeatureValue("main_verb_hasAux", segment);
			} catch (IllegalStateException e) {
				FeaturesUtil.addFeature("main_verb_hasAux", "0", jCas, segment);
			}
			try {
				FeaturesUtil.getFeatureValue("main_verb_subjAfterVerb", segment);
			} catch (IllegalStateException e) {
				FeaturesUtil.addFeature("main_verb_subjAfterVerb", "none", jCas, segment);
			}

			// if (seclass != null && seclass.matches("IMPERATIVE|QUESTION")) {
			// System.out.println("hasSubj: " +
			// FeaturesUtil.getFeatureValue("main_verb_hasSubj", segment));
			// System.out.println("hasAux: " +
			// FeaturesUtil.getFeatureValue("main_verb_hasAux", segment));
			// System.out.println("subjAfterverb " +
			// FeaturesUtil.getFeatureValue("main_verb_subjAfterVerb",
			// segment));
			// }

		}

		// features about embedding verb (if any): for ABSTRACT ENTITIES
		for (Segment segment : segments) {
			
			if (segment.getCoveredText().trim().startsWith("like")) {
				FeaturesUtil.addFeature("segment_startsWithLike", "true", jCas, segment);
			}
			else {
				FeaturesUtil.addFeature("segment_startsWithLike", "false", jCas, segment);
			}
			
			Token mainVerb = (Token) segment.getMainVerb();
			if (mainVerb != null) {
				// use head of predicate construction (if copula is used)
				List<Dependency> deps = JCasUtil.selectCovered(Dependency.class, mainVerb);
				boolean copula = false;
				for (Dependency dep : deps) {
					if (dep.getDependent() == mainVerb && dep.getDependencyType().equals("cop")) {
//						System.out.println("COPULA: " + segment.getCoveredText());
						mainVerb = dep.getGovernor();
//						System.out.println("main verb: " + mainVerb.getCoveredText());
						copula = true;
						break;
					}
				}
				deps = JCasUtil.selectCovered(Dependency.class, mainVerb);
				for (Dependency dep : deps) {
					// TODO: advcl is a parsing error: does this occur often?
					if (dep.getDependent() == mainVerb && dep.getDependencyType().matches("ccomp|advcl")) {
						FeaturesUtil.addFeature("main_verb_ccompEmbedded", "true", jCas, segment);
						String embeddingVerb = dep.getGovernor().getLemma().getValue();
						FeaturesUtil.addFeature("main_verb_ccompHeadLemma", embeddingVerb, jCas, segment);
						if (copula)
						// some WordNet features for this embedding verb
						WordNetUtils.setWordNetFeatures(dep.getGovernor(), segment, jCas, "main_verb_ccompHeadWordnet_",
								wordnet);
					}
				}
			}
		}

		// some features which need to be moved to VerbFeaturesAnnotator

		HashMap<Token, Set<Dependency>> childNodeMap = GrammarUtils.getChildNodesMap(jCas);
		for (Segment segment : segments) {

			Token verb = (Token) segment.getMainVerb();
			if (verb == null) {
				continue;
			}
			// get the dependents of interest as features
			// particle of main verb
			String particle = "none";
			String prep = "none";
			String xcomp = "none";
			String modal = "none";

			// record the dependents of the main verb
			Map<String, String> depFeatures = new HashMap<String, String>();

			// some more dependencies of interest
			String[] depsOfInterest = new String[] { "acomp", "advmod", "advcl", "nsubj", "dobj", "nsubjpass", "neg",
					"csubjpass", "csubj", "xsubj" };
			Set<String> depsSet = new HashSet<String>();
			for (String dep : depsOfInterest) {
				depsSet.add(dep);
			}

			// Iterate over all children of the main verb

			if (childNodeMap.containsKey(verb)) {
				for (Dependency dep : childNodeMap.get(verb)) {
					if (dep.getDependencyType().equals("prt")) {
						particle = dep.getDependent().getCoveredText();
					}
					if (dep.getDependencyType().equals("xcomp")) {
						xcomp = dep.getDependent().getPos().getPosValue();
					}
					if (dep.getDependencyType().startsWith("prep_")) {
						prep = dep.getDependencyType().split("_")[1];
						// TODO: are there any cases where we want the lemma
						// instead?
					} else if (dep.getDependencyType().startsWith("prep")) {
						prep = dep.getDependent().getLemma().getValue();
					}
					if (depsSet.contains(dep.getDependencyType())) {
						// TODO: simplify subjects?
						depFeatures.put(dep.getDependencyType(), getLexNameOrPOS(dep.getDependent()));
						depFeatures.put(dep.getDependencyType() + "_lemma", dep.getDependent().getLemma().getValue());
					}
					if (dep.getDependencyType().equals("aux")) {
						if (dep.getDependent().getPos().getPosValue().equals("MD")) {
							modal = dep.getDependent().getLemma().getValue();
						}
					}

				}

				FeaturesUtil.addFeature("main_verb_prt", particle, jCas, segment);
				FeaturesUtil.addFeature("main_verb_prep", prep, jCas, segment);
				FeaturesUtil.addFeature("main_verb_xcomp", xcomp, jCas, segment);
				FeaturesUtil.addFeature("main_verb_modal", modal, jCas, segment);

				for (String dep : depFeatures.keySet()) {
					String value = depFeatures.get(dep);
					FeaturesUtil.addFeature("main_verb_dep_" + dep, value, jCas, segment);
				}
			}
		}

	}

	/**
	 * Returns WordNet lexical filename for the lemma of the token, or POS if
	 * none is available. No word sense disambiguation applied here, simply uses
	 * most frequent sense.
	 * 
	 * TODO: used to return lemma in more cases?
	 * 
	 * @param token
	 * @return
	 */
	private String getLexNameOrPOS(Token token) {
		if (token.getPos().getPosValue().equals("PRP")) {
			if (token.getLemma().getValue().matches("you|I|we|he|she|they")) {
				return "noun.person";
			}
			return token.getPos().getPosValue();
		}
		if (token.getPos().getPosValue().equals("DT")) {
			return token.getLemma().getValue();
		}

		// uses first sense only
		List<ISynset> synsets = WordNetUtils.getSynsets(wordnet, token.getLemma().getValue(),
				token.getPos().getPosValue(), true);
		if (synsets.isEmpty()) {
			if (token.getLemma().getValue().startsWith("NN")) {
				// assume person for all names (approximation)
				return "noun.person";
			}
			return token.getPos().getPosValue();
		}
		ISynset synset = synsets.get(0);

		String lexName = synset.getLexicalFile().getName();

		return lexName;
	}

}
