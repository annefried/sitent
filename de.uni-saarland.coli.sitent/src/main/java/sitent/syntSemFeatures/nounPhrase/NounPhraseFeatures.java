package sitent.syntSemFeatures.nounPhrase;

/**
 * This class bundles several methods for extracting syntactic-semantic features for noun phrases.
 * The feature extraction is based on POS tags and syntactic dependencies (using the Stanford parser).
 * 
 * If you use this software, please cite:
 * Annemarie Friedrich and Manfred Pinkal: Discourse-sensitive Automatic Identification of Generic Expressions.
 * August 2015. In Proceedings of the 53rd Annual Meeting of the Association for Computational Linguistics (ACL).
 * Beijing, China.
 * 
 * 
 * @author afried (Annemarie Friedrich)
 * 
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class NounPhraseFeatures {

	HashMap<String, String> countability;
	private static boolean USE_CELEX = false;
	

	/**
	 * Creates NounPhraseFeatures object.
	 * 
	 * @param celexFile
	 */
	public NounPhraseFeatures(String celexFile) {

		if (celexFile != null) {
			USE_CELEX = true;
			countability = new HashMap<String, String>();
			try {
				BufferedReader reader = new BufferedReader(new FileReader(celexFile));
				String line;

				while ((line = reader.readLine()) != null) {
					// the first tab on each line separates key from value.
					// Keys cannot contain whitespace.
					int tabPos = line.indexOf('\t');
					String key = line.substring(0, tabPos);
					String val = line.substring(tabPos + 1);
					countability.put(key, val);
				}

				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isNounOrPronoun(Token token) {
		String pos = token.getPos().getPosValue();
		// JJ: allows things like "British" / "Australian" which are marked as
		// NEs in ACE
		if (!(pos.startsWith("N") || pos.matches("PRP\\$?|CD|JJS?") || pos.matches("DT|WHNP|WP|PRP$?")
				|| (pos.matches("WDT|WP") && token.getLemma().getValue().matches("who|which|that")))) {
			return false;
		}
		return true;
	}

	/**
	 * Extracts the number (sg/pl) from the Token. To be applied only on nouns /
	 * pronouns.
	 * 
	 * @param token
	 * @return
	 */
	public String getNumber(Token token) {
		String pos = token.getPos().getPosValue();
		if (!isNounOrPronoun(token)) {
			System.err.println("Use method only for nouns / pronouns. " + pos + " " + token.getCoveredText());
			// throw new IllegalArgumentException();
			return "unknown"; // occurs e.g. for 'there' (existential)
		}
		if (pos.matches("NNP?S")) {
			return "pl";
		}
		if (pos.matches("NNP?")) {
			return "sg";
		}
		if (pos.matches("PRP\\$?|CD")) {
			String lemma = token.getLemma().getValue().toLowerCase();
			if (lemma.matches("I|me|myself|he|him|himself|she|her|herself|it|itself|one|onself|mine|thine|his|hers")) {
				return "sg";
			}
			if (lemma.matches("we|us|ourselves|ourself|yourselves|they|them|themselves|theirselves|theirs|ours")) {
				return "pl";
			}
		}
		return "unknown";
	}

	/**
	 * Extracts person from Token. To be applied only on nouns / pronouns.
	 * 
	 * @param token
	 * @return
	 */
	public String getPerson(Token token) {
		if (!isNounOrPronoun(token)) {
			if (token.getPos().getPosValue().equals("EX")) {
				return "3"; // existential 'there'
			} else {
				System.err.println("Use getPerson method only for nouns / pronouns.");
				throw new IllegalArgumentException();
			}
		}
		String lemma = token.getLemma().getValue().toLowerCase();
		String person = "3";
		if (lemma.matches("i|we|me|us|myself|ourselves|ourself")) {
			person = "1";
		} else if (lemma.matches("you|ye|thou|thee|yourself|thyself|yourselves|yourself")) {
			person = "2";
		}
		return person;
	}

	/**
	 * Returns countability information according to Celex database of English
	 * nouns.
	 * 
	 * @param token
	 * @return
	 */
	public String getCountability(Token token) {
		if (!USE_CELEX) {
			System.err.println(
					"This should never happen, don't call this function if you did not configure to use Celex!");
			throw new IllegalStateException();
		}

		if (!token.getPos().getPosValue().startsWith("N")) {
			return "NO-NOUN";
		}
		if (countability.containsKey(token.getLemma().getValue().toLowerCase())) {
			return countability.get(token.getLemma().getValue().toLowerCase());
		} else {
			return "none";
		}
	}

	/**
	 * Extracts the noun type from the POS tag. Returns proper/common/pronoun.
	 * 
	 * @param token
	 * @return
	 */
	public static String getNounType(Token token) {
		if (!isNounOrPronoun(token)) {
			if (token.getPos().getPosValue().equals("EX")) {
				return "unknown"; // existential 'there'
			} else {
				System.err.println("Use getPerson method only for nouns / pronouns.");
				throw new IllegalArgumentException();
			}
		}
		if (token.getPos().getPosValue().matches("NNPS?")) {
			return "proper";
		}
		if (token.getPos().getPosValue().matches("NNS?")) {
			return "common";
		}
		if (isPronoun(token.getPos().getPosValue(), token.getLemma().getValue())) {
			return "pronoun";
		}
		return "unknown";
	}

	/**
	 * Returns determiner type (def, indef, demon) if the Token has a
	 * determiner. TODO: what if multiple determiners, e.g. "a few..."?
	 * 
	 * @param jCas
	 * @param token
	 * @param childNodeMap
	 * @return
	 */
	public static String getDeterminerType(JCas jCas, Token token, HashMap<Token, Set<Dependency>> childNodeMap,
			boolean simplified) {
		if (!childNodeMap.containsKey(token)) {
			return "none";
		}

		for (Dependency dep : childNodeMap.get(token)) {
			// System.out.println("\nToken: " + token.getCoveredText());
			//
			// System.out.println(dep.getGovernor().getCoveredText() + " --"
			// + dep.getDependencyType() + "--> "
			// + dep.getDependent().getCoveredText());

			if (dep.getGovernor() == token && dep.getDependencyType().matches("det|poss")) {
				String detLemma = dep.getDependent().getLemma().getValue();

				if (detLemma.matches("the")) {
					return "def";
				} else if (detLemma.matches("a|an|another")) {
					return "indef";
				} else if (detLemma.matches("all|each|every")) {
					if (simplified) {
						return "def";
					}
					return "quantDef"; // "quant" instead? // Abbott (2003), see
										// Mathew
				} else if (detLemma.matches("some|no|several|none|any|many|few")) {
					if (simplified) {
						return "indef";
					}
					return "quantIndef"; // "quant" instead? -- for ACL 2015
											// paper: all quantifiers were
											// 'indef'
				} else if (detLemma.matches("this|these|that|those")) {
					return "demon";
				} else if (detLemma.matches("their|his|her|its|our|your|my")) {
					return "def"; // "poss" instead?
				} else if (dep.getDependent().getPos().getPosValue().equals("CD")) {
					if (simplified) {
						return "indef";
					}
					return "cd"; // quantified phrases??
				} else if (dep.getDependent().getPos().getPosValue().equals("JJ")
						&& dep.getDependent().getLemma().equals("last|next|previous|coming")) {
					if (simplified) {
						return "def";
					}
					return "quantSpec"; // specifically quantified temporal NP

				} else if (dep.getDependent().getPos().getPosValue().equals("JJ")) {
					return "indef"; // quantified phrases??
				}
			}
		}
		return "none";
	}

	/**
	 * Returns true if the Token is a bare plural (definition by Reiter:
	 * excludes the quantified cases -- different from Suh!!).
	 * 
	 * @param jCas
	 * @param token
	 * @return
	 */
	public static Boolean isBarePlural(JCas jCas, Token token, HashMap<Token, Set<Dependency>> childNodeMap) {
		// is it a plural?
		String pos = token.getPos().getPosValue();
		if (!pos.matches("NNP?S")) {
			return false;
		}

		if (!childNodeMap.containsKey(token)) {
			return true;
		}
		for (Dependency dep : childNodeMap.get(token)) {
			if (dep.getGovernor() == token && dep.getDependencyType().matches("det|poss")) {
				return false;
			}
		}
		return true;
	}

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
			if (!map.containsKey(dep.getGovernor())) {
				map.put(dep.getGovernor(), new HashSet<Dependency>());
			}
			map.get(dep.getGovernor()).add(dep);
		}
		return map;
	}

	public static boolean isPronoun(String headPos, String headLemma) {
		return headPos.matches("DT|WHNP|WP|PRP$?")
				|| (headPos.matches("WDT|WP") && headLemma.matches("who|which|that|whose"));
	}

}
