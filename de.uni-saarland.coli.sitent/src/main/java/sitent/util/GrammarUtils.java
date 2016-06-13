package sitent.util;

import java.util.Collection;
import java.util.HashMap;

/**
 * This class contains utility functions for grammar-related tasks.
 * 
 * @author afried (Annemarie Friedrich)
 */

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
//import org.cleartk.ne.type.NamedEntityMention;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.VP;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import sitent.syntSemFeatures.nounPhrase.NounPhraseFeatures;
import sitent.types.ClassificationAnnotation;

public class GrammarUtils {

	/**
	 * Collects all the child nodes of the Tokens.
	 * 
	 * @param jCas
	 * @return
	 */
	public static HashMap<Token, Set<Dependency>> getChildNodesMap(JCas jCas) {
		HashMap<Token, Set<Dependency>> map = new HashMap<Token, Set<Dependency>>();
		Collection<Dependency> deps = JCasUtil.select(jCas, Dependency.class);
		for (Dependency dep : deps) {
//			System.out.println(dep.getGovernor().getCoveredText() + " " + dep.getDependencyType() + " "
//					+ dep.getDependent().getCoveredText());
			if (!map.containsKey(dep.getGovernor())) {
				map.put(dep.getGovernor(), new HashSet<Dependency>());
			}
			map.get(dep.getGovernor()).add(dep);
		}
		return map;
	}

	/**
	 * Returns a {@link Token} annotation which is the grammatical head (in
	 * syntactic dependencies) of the phrase covered by the {@link Annotation}
	 * annotation.
	 * 
	 * 
	 * @param annotation
	 *            a UIMA annotation, e.g. a named entity mention
	 * @param jcas
	 * @return the Token annotation covering the head of this phrase, if any
	 *         (null otherwise)
	 */
	public static Token getHeadNoun(Annotation annotation, JCas jcas) {

		// find covered tokens and dependencies
		List<Token> tokens = JCasUtil.selectCovered(Token.class, annotation);

		// only one token, this should be the head
		if (tokens.size() == 1) {
			return tokens.get(0);
		}

		if (tokens.size() == 0) {
			// in some rare cases, the named entity mention does not span an
			// entire Token,
			// e.g. annotation of "California" in "California-based"
			tokens = JCasUtil.selectCovering(Token.class, annotation);
		}

		List<Dependency> deps = JCasUtil.selectCovered(Dependency.class, annotation);
		Set<Token> tokenSet = new HashSet<Token>(tokens);
		// find the head of the tokens
		// remove tokens not in the dependencies: have been collapsed.
		for (Token token : tokens) {
			boolean foundDep = false;
			for (Dependency dep : deps) {

				if (dep.getDependent() == dep.getGovernor()) {
					// occurred for conj_and
					continue;
				}

				// relative clause modifier creates cycles
				if (dep.getDependencyType().equals("rcmod")) {
					continue;
				}

				if (dep.getGovernor() == token) {
					foundDep = true;
				}
				if (dep.getDependent() == token) {
					foundDep = true;
					if (tokens.contains(dep.getGovernor())) {
						tokenSet.remove(token);
					}
				}
			}
			if (!foundDep && deps.size() > 0) {
				tokenSet.remove(token);
			}
		}
		// if (tokenSet.size() > 1) {
		// System.err.println("could not identify head: " +
		// annotation.getCoveredText());
		// System.err.println(tokenSet.size());
		// for (Token t : tokenSet) {
		// System.err.println("token: " + t.getCoveredText());
		// }
		// for (Dependency dep : deps) {
		// System.err.println(dep.getGovernor().getCoveredText() + " --> " +
		// dep.getDependent().getCoveredText());
		// }
		// throw new RuntimeException();
		// }
		if (tokenSet.isEmpty()) {
			// System.err.println("SitEntUimaUtils: No head found.");
			// for (Dependency dep : deps) {
			// System.err.println(dep.getGovernor().getCoveredText() + "(" +
			// dep.getGovernor().getBegin() + ") --> "
			// + dep.getDependencyType() + "-->" +
			// dep.getDependent().getCoveredText() + " ("
			// + dep.getDependent().getBegin() + ")");
			// }
			return null;
		}
		Iterator<Token> it = tokenSet.iterator();
		Token head = it.next();
		while (!NounPhraseFeatures.isNounOrPronoun(head) && it.hasNext()) {
			head = it.next();
		}
		// if in doubt, returns last token.
		return head;
	}

	/**
	 * Returns a {@link Token} annotation which is the grammatical head (in
	 * syntactic dependencies) of the phrase covered by the {@link Annotation}
	 * annotation.
	 * 
	 * 
	 * @param annotation
	 *            a UIMA annotation, e.g. verb phrase (VP)
	 * @param jcas
	 * @return the Token annotation covering the head of this phrase, if any
	 *         (null otherwise)
	 */
	public static Token getHeadVerb(Annotation annotation, JCas jcas, boolean useCopula) {

		// System.out.println("getHeadVerb " + annotation.getCoveredText());

		// find covered tokens and dependencies
		List<Token> tokens = new LinkedList<Token>();
		tokens.addAll(JCasUtil.selectCovered(Token.class, annotation));

		// remove tokens covered by an included VP
		if (annotation instanceof VP) {
			Collection<VP> includedVPs = JCasUtil.selectCovered(VP.class, annotation);
			for (VP vp : includedVPs) {
				if (vp != annotation) {
					List<Token> includedTokens = JCasUtil.selectCovered(Token.class, vp);
					for (Token t : includedTokens) {
						tokens.remove(t);
						// System.out.println("removing: " +
						// t.getCoveredText());
					}
				}
			}
		}

		if (tokens.size() == 1) {
			// only one token, this should be the head.
			return tokens.get(0);
		}

		List<Dependency> deps = JCasUtil.selectCovered(Dependency.class, annotation);
		Set<Token> tokenSet = new HashSet<Token>(tokens);

//		for (Dependency dep : deps) {
//			System.err.println(dep.getGovernor().getCoveredText() + " --> " + dep.getDependencyType() + " "
//					+ dep.getDependent().getCoveredText());
//		}

		// find the head of the tokens
		// remove tokens not in the dependencies: have been collapsed.
		for (Token token : tokens) {
			boolean foundDep = false;
			for (Dependency dep : deps) {

				if (dep.getDependent() == dep.getGovernor()) {
					// occurred for conj_and
					continue;
				}

				// relative clause modifier creates cycles
				if (dep.getDependencyType().equals("rcmod")) {
					continue;
				}

				if (dep.getGovernor() == token) {
					foundDep = true;
				}
				if (dep.getDependent() == token) {
					foundDep = true;
					if (tokens.contains(dep.getGovernor())) {
						tokenSet.remove(token);
					}
				}
			}
			if (!foundDep && deps.size() > 0) {
				tokenSet.remove(token);
			}
		}
		// if (tokenSet.size() > 1) {
		// System.err.println("could not identify head: " +
		// annotation.getCoveredText());
		// System.err.println(tokenSet.size());
		// for (Token t : tokenSet) {
		// System.err.println("token: " + t.getCoveredText());
		// }
		// for (Dependency dep : deps) {
		// System.err.println(dep.getGovernor().getCoveredText() + " --> " +
		// dep.getDependent().getCoveredText());
		// }
		// throw new RuntimeException();
		// }

		if (tokenSet.isEmpty()) {
			return null;
		}
		Iterator<Token> it = tokenSet.iterator();
		Token head = it.next();

		if (useCopula && !head.getPos().getPosValue().startsWith("V")) {
			// adjectives and nouns can be heads in predicative constructions
			for (Dependency dep : deps) {
				if (dep.getGovernor() == head && dep.getDependencyType().equals("cop")) {
					// mark form of 'is' as the head
					head = dep.getDependent();
				}
			}
		}

		// if in doubt, returns last token.
		return head;
	}

	/**
	 * Relation between entity and its governor, relation between governor and
	 * governor and so forth. [0-4]
	 * 
	 * @param token
	 * @param jCas
	 * @param fn
	 */
	public static void setDependencyRelationFeatures(Token token, JCas jCas, ClassificationAnnotation classAnnot,
			String featurePrefix) {

		int path = 0;
		// This selects the dependencies where the token is the dependent.
		List<Dependency> deps = JCasUtil.selectCovered(Dependency.class, token);
		while (!deps.isEmpty()) {
			Dependency dep = deps.get(0); // simply choose first one. there
											// should almost never be more than
											// one.
			if (path == 0) {
				FeaturesUtil.addFeature(featurePrefix + "depRel0", dep.getDependencyType(), jCas, classAnnot);
			}
			if (path == 1) {
				FeaturesUtil.addFeature(featurePrefix + "depRel1", dep.getDependencyType(), jCas, classAnnot);
			}
			if (path == 2) {
				FeaturesUtil.addFeature(featurePrefix + "depRel2", dep.getDependencyType(), jCas, classAnnot);
			}
			if (path == 3) {
				FeaturesUtil.addFeature(featurePrefix + "depRel3", dep.getDependencyType(), jCas, classAnnot);
			}
			if (path == 4) {
				FeaturesUtil.addFeature(featurePrefix + "depRel4", dep.getDependencyType(), jCas, classAnnot);
			}

			token = dep.getGovernor();
			deps = JCasUtil.selectCovered(Dependency.class, token);
			path++;
			if (path > 4) {
				break;
			}
		}

	}

}
