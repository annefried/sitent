package sitent.syntSemFeatures.verbs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class ClauseFeatures {

	// The dependency relations defined to be modifiers (from Nils)
	private static HashSet<String> depRelModifiers;
	static {
		depRelModifiers = new HashSet<String>();
		for (String s : new String[] { "advcl", "purpcl", "tmod", "rcmod",
				"amod", "infmod", "partmod", "num", "number", "appos", "nn",
				"abbrev", "advmod", "neg", "poss", "possessive", "prt", "det",
				"prep" }) {
			depRelModifiers.add(s);
		}
	}

	/**
	 * If the current clauseHead points to an auxiliary verb via 'cop', this
	 * method returns the 'lexical' head (may be noun predicate etc) which has
	 * the connections to the other dependents, e.g. nsubj etc
	 * 
	 * @param clauseHead
	 * @param childNodeMap
	 * @return
	 */
	public static Token getLexicalHead(Token clauseHead,
			Map<Token, Set<Dependency>> childNodeMap) {
		if (childNodeMap.containsKey(clauseHead)) {
			return clauseHead;
		}
		// token has no children: this happens in the case of auxiliary
		// verbs which are attached as child nodes to the 'lexical head',
		// e.g. in 'he is a member', the lexical head is 'member', 'he' is
		// the subject of 'member'
		// for the case of copula, modify clause head
		Token lexicalHead = clauseHead;
		if (!childNodeMap.containsKey(clauseHead)) {
			// if head is e.g. copula, then it has no further children.
			// find the 'main predicate'
			Collection<Dependency> deps = JCasUtil.selectCovered(
					Dependency.class, clauseHead);
			if (!deps.isEmpty()) {
				lexicalHead = deps.iterator().next().getGovernor();
			}
		}
		return lexicalHead;
	}

	/**
	 * Does the clause (token is the clauses' head) have a temporal modifier?
	 * 
	 * @param token
	 * @param childNodeMap
	 * @return
	 */
	public static boolean hasTemporalModifier(Token token,
			Map<Token, Set<Dependency>> childNodeMap) {
		// if token is a modal, need to check its head.
		token = getLexicalHead(token, childNodeMap);
		if (!childNodeMap.containsKey(token)) {
			return false;
		}
		// check children: any tmod?
		for (Dependency dep : childNodeMap.get(token)) {
			if (dep.getDependencyType().equals("tmod")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Counts the number of modifiers of Token.
	 * 
	 * @param token
	 * @param childNodeMap
	 * @return
	 */
	public static int getNumberOfModifiers(Token token,
			Map<Token, Set<Dependency>> childNodeMap) {
		// if token is a modal, need to check its head.
		token = getLexicalHead(token, childNodeMap);
		if (!childNodeMap.containsKey(token)) {
			return 0;
		}
		int number = 0;
		for (Dependency dep : childNodeMap.get(token)) {
			if (depRelModifiers.contains(dep.getDependencyType())) {
				number++;
			}
		}
		return number;

	}
	
	/**
	 * Get the lemma of the predicate of the head of the adverbial clause (if any).
	 * @param token
	 * @param childNodeMap
	 * @return
	 */
	public static String getLemmaOfAdvClause(Token token,
			Map<Token, Set<Dependency>> childNodeMap) {
		// check lexical head in case of copula
		token = getLexicalHead(token, childNodeMap);
		if (!childNodeMap.containsKey(token)) {
			return "none";
		}
		// is there any advclause?
		for (Dependency dep : childNodeMap.get(token)) {
			if (dep.getDependencyType().matches("advcl|advmod|npadvmod")) {
				return dep.getDependent().getLemma().getValue();
			}
		}
		return "none";
	}

	/**
	 * Returns the degree of any adverbial, if present.
	 * 
	 * @param token
	 * @param childNodeMap
	 * @return
	 */
	public static String getAdverbialDegree(Token token,
			Map<Token, Set<Dependency>> childNodeMap) {
		
		// check lexical head in case of copula
		token = getLexicalHead(token, childNodeMap);
		if (!childNodeMap.containsKey(token)) {
			return "none";
		}
		for (Dependency dep : childNodeMap.get(token)) {
			if (dep.getDependent().getPos().getPosValue().equals("RBS")) {
				return "superlative";
			}
		}
		for (Dependency dep : childNodeMap.get(token)) {
			if (dep.getDependent().getPos().getPosValue().equals("RBR")) {
				return "comparative";
			}
		}
		for (Dependency dep : childNodeMap.get(token)) {
			if (dep.getDependent().getPos().getPosValue().equals("RB")) {
				return "positive";
			}
		}
		return "none";

	}


}
