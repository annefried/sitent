package sitent.syntSemFeatures.verbs;

/**
 * @author afried (Annemarie Friedrich)
 * 
 *         Extracts verb chains from a sentence and computes the tense for the
 *         verb chains.
 * 
 *         This is a re-implementation of a part of a Python module written by
 *         Thomas Meyer and Sharid Loaiciga.
 * 
 *         English-French Verb Phrase Alignment in Europarl for Tense
 *         Translation Modeling, Loaiciga, Sharid, Meyer, Thomas and
 *         Popescu-Belis, Andrei, to appear in Proceedings of the 9th
 *         international conference on Language Resources and Evaluation (LREC),
 *         Reykjavik, Iceland, 2014.
 *         
 *         TODO: MorphologicalFeatures in dkpro?? Add them directly as feature of
 *         Token??
 * 
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import sitent.types.VerbFeatures;
import sitent.util.AnnotationComparator;

public class TenseExtraction {

	public class TenseFeatures {

		private String tense;
		private String voice;

		public TenseFeatures(String tense, String voice) {
			this.tense = tense;
			this.voice = voice;
		}

		public String getTense() {
			return tense;
		}

		public String getVoice() {
			return voice;
		}

	}

	// tense map for English + Stanford Parser
	private Map<String, Set<String>> tenseMap;

	public TenseExtraction(String tenseFile) throws IOException {
		// read tense map
		tenseMap = new HashMap<String, Set<String>>();
		BufferedReader reader = new BufferedReader(new FileReader(tenseFile));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.trim().equals("")) {
				continue;
			}
			String[] parts = line.split(",");
			String tense = parts[0].trim();
			Set<String> tensePatterns = new HashSet<String>();
			for (int i = 1; i < parts.length; i++) {
				tensePatterns.add(parts[i]);
			}
			tenseMap.put(tense, tensePatterns);
		}
		reader.close();
	}

	/**
	 * Compute tense and voice features for all verbs in the JCas.
	 * 
	 * @param jcas
	 */
	public void setTense(JCas jcas) {

		Iterator<Sentence> sentIt = JCasUtil.iterator(jcas, Sentence.class);

		while (sentIt.hasNext()) {

			Sentence sent = sentIt.next();
			Collection<Dependency> sentDeps = JCasUtil.selectCovered(Dependency.class, sent);

			// System.out.println("\n" + sent.getCoveredText());
			List<List<Token>> verbChains = identifyVerbChains(sent);
			for (List<Token> verbChain : verbChains) {
				// System.out.println("-------------------------");
				// for (Token verb : verbChain) {
				// System.out.println(verb.getPos().getPosValue() + " "
				// + verb.getCoveredText());
				// }
				TenseFeatures feat = getTenseForChain(verbChain);
				// System.out.println(feat.getTense() + " " + feat.getVoice());

				// Used to mark tense only for head of chain.
				// Problem: head is defined differently in standard vs.
				// collapsed dependencies.
				// mark tense on all tokens, handle head identification from
				// outside.
				// TODO: this head selection is just for segmentation.
				Token head = getHeadOfChain(verbChain, sentDeps);

				for (Token token : verbChain) {
					VerbFeatures vf = new VerbFeatures(jcas);
					vf.setBegin(token.getBegin());
					vf.setEnd(token.getEnd());
					vf.setTense(feat.getTense());
					vf.setVoice(feat.getVoice());
					vf.setHeadOfVerbChain(head);
					vf.addToIndexes();
				}

			}

		}
	}

	/**
	 * returns the head of the chain
	 * 
	 * @param verbChain
	 * @return
	 */
	private Token getHeadOfChain(List<Token> verbChain, Collection<Dependency> deps) {
		// simple case: only one verb in the chain
		if (verbChain.size() == 1) {
			return verbChain.get(0);
		}
		// more than one verb in the chain
		List<Token> noHeadInChain = new LinkedList<Token>();
		for (Token verb : verbChain) {
			if (!hasHeadInChain(verbChain, verb, deps)) {
				noHeadInChain.add(verb);
			}
		}
		if (noHeadInChain.size() == 1) {
			return noHeadInChain.get(0);
		} else {
			for (Token verb : noHeadInChain) {
				// used as copula / auxilary
				for (Dependency dep : deps) {
					if (dep.getDependent() == verb && dep.getDependencyType().equals("aux")) {
						// check if governor is not a verb and has a copula
						for (Dependency dep2 : deps) {
							if (dep2.getGovernor() == dep.getGovernor() && dep2.getDependencyType().equals("cop")) {
								// the 'might be best' case
								return dep2.getDependent();
							}
						}

						return verb;
					}
				}
				for (Dependency dep : deps) {
					if (dep.getDependent() == verb && dep.getDependencyType().equals("cop")) {
						return verb;
					}
				}
			}

//			if (noHeadInChain.size() >= 2) {
//				System.err.println("MULTIPLE HEADS IN CHAIN:");
//				for (Token t : noHeadInChain) {
//					System.err.println(">> " + t.getCoveredText());
//				}
//			} else {
//				System.err.println("NO HEADS IN CHAIN: (printing original chain)");
//				for (Token t : verbChain) {
//					System.err.println(">> " + t.getCoveredText());
//				}
//			}
			// for (Dependency dep : deps) {
			// System.out.println(dep.getGovernor().getCoveredText() + " --"
			// + dep.getDependencyType() + "--> "
			// + dep.getDependent().getCoveredText());
			// }
			// throw new IllegalStateException();
			return null;
		}
	}

	private boolean hasHeadInChain(List<Token> verbChain, Token token, Collection<Dependency> deps) {
		for (Dependency dep : deps) {
			if (dep.getDependent() == token && verbChain.contains(dep.getGovernor())
					&& dep.getDependencyType().matches("aux|auxpass|prt|cop|xcomp")) {
				// System.out.println("head in chain: " + token.getCoveredText()
				// + "<--" + dep.getGovernor().getCoveredText());
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the tense of a single verb chain.
	 * 
	 * @param verbChain
	 * @return
	 */
	private TenseFeatures getTenseForChain(List<Token> verbChain) {
		TenseFeatures values;
		String posChain = "";
		List<String> verbsList = new LinkedList<String>();
		for (Token verb : verbChain) {
			posChain += verb.getPos().getPosValue() + " ";
			verbsList.add(verb.getLemma().getValue());
		}
		posChain = posChain.trim();
		// System.out.println("POSCHAIN: " + posChain);

		if (tenseMap.get("PAinfinitives").contains(posChain)) {
			values = new TenseFeatures("infinitif", "passive");
		} else if (tenseMap.get("infinitives").contains(posChain)) {
			values = new TenseFeatures("infinitif", "active");
		} else if (tenseMap.get("past_perfect_cont").contains(posChain)) {
			values = new TenseFeatures("past_perf_cont", "active");
		} else if (tenseMap.get("PApast_perfect").contains(posChain)) {
			values = new TenseFeatures("past_perf", "passive");
		} else if (tenseMap.get("past_perfect").contains(posChain) && !verbsList.get(0).matches("was|were")) {
			values = new TenseFeatures("past_perf", "active");
		} else if (tenseMap.get("PApast_cont").contains(posChain)) {
			values = new TenseFeatures("past_cont", "passive");
		} else if (tenseMap.get("past_cont").contains(posChain)) {
			values = new TenseFeatures("past_cont", "active");
		} else if (tenseMap.get("PAsim_past").contains(posChain) && verbsList.get(0).matches("was|were")) {
			values = new TenseFeatures("sim_past", "passive");
		} else if (tenseMap.get("sim_past").contains(posChain)) {
			values = new TenseFeatures("sim_past", "active");
		} else if (tenseMap.get("pres_perfect_cont").contains(posChain)) {
			values = new TenseFeatures("pres_perf_cont", "active");
		} else if (tenseMap.get("PApres_perfect").contains(posChain)) {
			values = new TenseFeatures("pres_perf", "passive");
		} else if (tenseMap.get("pres_perfect").contains(posChain) && !verbsList.get(0).matches("is|are")) {
			values = new TenseFeatures("pres_perf", "active");
		} else if (tenseMap.get("PApres_cont").contains(posChain)) {
			values = new TenseFeatures("pres_cont", "passive");
		} else if (tenseMap.get("pres_cont").contains(posChain)) {
			values = new TenseFeatures("pres_cont", "active");
		} else if (tenseMap.get("PApres").contains(posChain) && verbsList.get(0).matches("is|are")) {
			values = new TenseFeatures("pres", "passive");
		} else if (tenseMap.get("pres").contains(posChain)) {
			values = new TenseFeatures("pres", "active");
		} else if (tenseMap.get("fut_perfect_cont").contains(posChain)
				&& tenseMap.get("fut_modal").contains(verbsList.get(0))) {
			values = new TenseFeatures("fut_perf_cont", "active");
		} else if (tenseMap.get("PAfut_perfect").contains(posChain)
				&& tenseMap.get("fut_modal").contains(verbsList.get(0))) {
			values = new TenseFeatures("fut_perf", "passive");
		} else if (tenseMap.get("PAfut").contains(posChain) && tenseMap.get("fut_modal").contains(verbsList.get(0))) {
			if (verbsList.contains("be")) {
				values = new TenseFeatures("fut", "passive");
			} else {
				values = new TenseFeatures("fut_perf", "active");
			}
		} else if (tenseMap.get("PAfut_cont").contains(posChain)
				&& tenseMap.get("fut_modal").contains(verbsList.get(0))) {
			values = new TenseFeatures("fut_cont", "passive");
		} else if (tenseMap.get("fut_cont").contains(posChain)
				&& tenseMap.get("fut_modal").contains(verbsList.get(0))) {
			values = new TenseFeatures("fut_cont", "active");
		} else if (tenseMap.get("fut").contains(posChain) && tenseMap.get("fut_modal").contains(verbsList.get(0))) {
			values = new TenseFeatures("fut", "active");
		} else if (tenseMap.get("fut_perfect_cont").contains(posChain)
				&& tenseMap.get("cond_modal").contains(verbsList.get(0))) {
			values = new TenseFeatures("cond_perf_cont", "active");
		} else if (tenseMap.get("PAfut_perfect").contains(posChain)
				&& tenseMap.get("cond_modal").contains(verbsList.get(0))) {
			values = new TenseFeatures("cond_perf", "passive");
		} else if (tenseMap.get("fut_perfect").contains(posChain)
				&& tenseMap.get("cond_modal").contains(verbsList.get(0))) {
			if (verbsList.contains("be")) {
				values = new TenseFeatures("cond", "passive");
			} else {
				values = new TenseFeatures("cond_perf", "active");
			}
		} else if (tenseMap.get("PAfut_cont").contains(posChain)
				&& tenseMap.get("cond_modal").contains(verbsList.get(0))) {
			values = new TenseFeatures("cond_cont", "passive");
		} else if (tenseMap.get("fut_cont").contains(posChain)
				&& tenseMap.get("cond_modal").contains(verbsList.get(0))) {
			values = new TenseFeatures("cond_cont", "active");
		} else if (tenseMap.get("fut").contains(posChain) && tenseMap.get("cond_modal").contains(verbsList.get(0))) {
			values = new TenseFeatures("cond", "active");
		} else {
			values = new TenseFeatures("other", "unknown");
		}

		return values;
	}

	private static List<List<Token>> identifyVerbChains(Sentence sent) {
		List<List<Token>> verbChains = new LinkedList<List<Token>>();

		List<Dependency> sentDeps = JCasUtil.selectCovered(Dependency.class, sent);

		// for (Dependency dep : sentDeps) {
		// System.out.println(dep.getGovernor().getCoveredText() + " --"
		// + dep.getDependencyType() + "--> "
		// + dep.getDependent().getCoveredText());
		// }

		List<Token> sentTokens = JCasUtil.selectCovered(Token.class, sent);
		List<Token> verbTokens = new LinkedList<Token>();
		List<Token> otherTokens = new LinkedList<Token>();
		for (Token token : sentTokens) {
			// System.out.println(token);
			// POS tags for English verbs
			// TODO: allow for different languages/parsers eventually
			if (token.getPos() == null) {
				// occasionally parsers messes this up??
				continue;
			}
			if (token.getPos().getPosValue().matches("MD|VB|VBD|VBG|VBN|VBP|VBZ|RP|TO")) {
				verbTokens.add(token);
			} else {
				otherTokens.add(token);
			}
		}

		for (Token verb : verbTokens) {
			List<List<Token>> matchingChains = new LinkedList<List<Token>>();
			for (List<Token> verbChain : verbChains) {
				if (isHeadOrDepInChain(verbChain, verb, sentDeps)) {
					matchingChains.add(verbChain);
				}

			}
			if (matchingChains.isEmpty()) {
				List<Token> newVerbChain = new LinkedList<Token>();
				newVerbChain.add(verb);
				verbChains.add(newVerbChain);
			} else {
				List<Token> firstChain = matchingChains.get(0);
				firstChain.add(verb);
				// if more than one matching chain, need to merge
				if (matchingChains.size() > 1) {
					for (int i = 1; i < matchingChains.size(); i++) {
						firstChain.addAll(matchingChains.get(i));
						verbChains.remove(matchingChains.get(i));
					}
				}
			}
		}

		// sort verb chains: by position of verbs in sentence
		AnnotationComparator comp = new AnnotationComparator();
		for (List<Token> chain : verbChains) {
			Collections.sort(chain, comp);
		}

		// merge chains if they are governed by some predicate
		// e.g. 'might not be a bad idea', 'had been an effort'

		boolean merged = true;
		while (merged) {
			merged = false;
			// if merged, set to true, this will break the loop
			MERGING: for (int i = 0; i < verbChains.size() - 1; i++) {
				for (int j = 1; j < verbChains.size(); j++) {
					if (i != j) {
						List<Token> chain1 = verbChains.get(i);
						List<Token> chain2 = verbChains.get(j);
						for (Token token : otherTokens) {
							if ((isHeadOrDepInChain(chain1, token, sentDeps)
									&& isHeadOrDepInChain(chain2, token, sentDeps))) {
								// merge the two chains
								// System.out.println(token.getCoveredText());
								chain1.addAll(chain2);
								verbChains.remove(chain2);
								merged = true;
								break MERGING;
							}
						}
					}
				}
			}
		}

		// remove 'chains' that only consist of 'to/TO'
		List<List<Token>> toRemove = new LinkedList<List<Token>>();
		for (List<Token> chain : verbChains) {
			if (chain.size() == 1 && chain.get(0).getLemma().getValue().equals("to")) {
				toRemove.add(chain);
			}
		}
		for (List<Token> chain : toRemove) {
			verbChains.remove(chain);
		}

		// sort verb chains: by position of verbs in sentence
		for (List<Token> chain : verbChains) {
			Collections.sort(chain, comp);
		}

		return verbChains;
	}

	private static boolean isHeadOrDepInChain(List<Token> verbChain, Token verb, List<Dependency> sentDeps) {
		// check all the heads/dependents of this verb
		List<Token> depsToToken = new LinkedList<Token>();
		for (Dependency dep : sentDeps) {
			if (dep.getDependent() == verb && dep.getDependencyType().matches("aux|auxpass|prt|cop")) {
				depsToToken.add(dep.getGovernor());
			}
			if (dep.getGovernor() == verb && dep.getDependencyType().matches("aux|auxpass|prt|cop")) {
				depsToToken.add(dep.getDependent());
			}
		}
		// form intersection
		depsToToken.retainAll(verbChain);
		if (!depsToToken.isEmpty()) {
			return true;
		}
		return false;

	}

}
