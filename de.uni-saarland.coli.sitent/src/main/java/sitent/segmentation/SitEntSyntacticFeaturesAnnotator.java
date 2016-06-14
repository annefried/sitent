package sitent.segmentation;
/**
 * @author afried
 * 
 * This annotator find the main verb and the main referent (subject of the main verb)
 * for each segment.
 * 
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import sitent.types.Segment;
import sitent.util.GrammarUtils;

public class SitEntSyntacticFeaturesAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		// (STEP 2) iterate over segments: find main verbs. Iterator<Segment>
		Iterator<Segment> segIt = JCasUtil.iterator(jcas, Segment.class);

		// create map for efficient access of child nodes
		HashMap<Token, Set<Dependency>> childNodeMap = GrammarUtils.getChildNodesMap(jcas);

		Segment previousSegment = null;

		while (segIt.hasNext()) {

			if (previousSegment != null) {
				if (previousSegment.getMainVerb() == null) {
					//System.out.println("\n*** NO MAIN VERB ***\n");
				}
			}

			Segment segment = segIt.next();
			previousSegment = segment;
			//System.out.println("\nSyntFeat: " + segment.getCoveredText().trim());
			//if (segment.getMainVerb() != null)
				//System.out.println(segment.getMainVerb().getCoveredText());

			// (STEP 2a) Find the main verb of the segment.
			Token mainVerb;
			if (segment.getMainVerb() == null) {
				// find verbs of the segment
				List<Token> verbs = new LinkedList<Token>();
				for (Token token : JCasUtil.selectCovered(Token.class, segment)) {
					if (token.getPos() == null) {
						continue;
					}
					if (token.getPos().getPosValue().startsWith("V")) {
						verbs.add(token);
						// System.out.println(token.getPos().getPosValue() + " "
						// + token.getLemma().getValue());
					}
				}

				// if no verb can be found, label the segment as NO_SITUATION.
				if (verbs.isEmpty()) {
					segment.setPredictionNoSituation(true);
					// continue with next segment.
					continue;
				}

				// find the verbs that do not have a governor in the same
				// segment
				// dependencies of the segment
				List<Dependency> deps = JCasUtil.selectCovered(Dependency.class, segment);

				List<Token> remainingVerbs = new LinkedList<Token>();
				for (Token verb : verbs) {
					if (!governedByVerbInSegment(deps, verb, verbs)) {
						remainingVerbs.add(verb);
					}
				}
				verbs = remainingVerbs;

				// System.out.println("MAIN VERBS:");
				// for (Token token : verbs) {
				// System.out.println(token.getPos().getPosValue() + " "
				// + token.getLemma().getValue());
				// }
				if (verbs.size() > 1 || verbs.isEmpty()) {
					//System.out.println("\nMore than one main verb or no main verb found!");
					//System.out.println("\t" + segment.getCoveredText().trim());
					//System.out.println("\t" + verbs.size());

					// all dependencies of the entire sentence: try to find a
					// path
					// from one verb to the other via another segment (same
					// sentence)
					List<Sentence> segmentSents = JCasUtil.selectCovering(Sentence.class, segment);
					if (!segmentSents.isEmpty()) {
						Sentence sent = segmentSents.get(0);
						List<Dependency> sentDeps = JCasUtil.selectCovered(Dependency.class, sent);
						// search for path in full sentence only in this case
						// (for
						// efficiency reasons)
						remainingVerbs = new LinkedList<Token>();
						for (Token verb : verbs) {
							if (!governedByVerbInSegment(sentDeps, verb, verbs)) {
								remainingVerbs.add(verb);
							}
						}
						verbs = remainingVerbs;
//						if (verbs.size() > 1) {
							//System.out.println("\nStill more than one main verb.");
							// for (Token verb : verbs) {
							// System.out.println(verb.getPos().getPosValue()
							// + " " + verb.getCoveredText());
							// }

//							for (Dependency dep : sentDeps) {
//								System.out.println(dep.getGovernor().getCoveredText() + " --> "
//										+ dep.getDependencyType() + " --> " + dep.getDependent().getCoveredText());
//							}
//						} else {
//							System.out.println("ONE MAIN VERB (after extended search): " + verbs.size());
//						}
					}
				}

				if (verbs.size() == 0) {
					//System.out.println("Cyclic dependencies? Parsing error?");
					//System.out.println(segment.getCoveredText());
					continue;
				}

				// otherwise, it keeps the main verb configure 'manually'
				// earlier!
				if (verbs.size() > 1) {
					remainingVerbs = new LinkedList<Token>();
					// choose copula if one of them is one
					for (Token verb : verbs) {
						for (Dependency dep : JCasUtil.selectCovered(jcas, Dependency.class, verb)) {
							if (dep.getDependencyType().matches("cop")) {
								remainingVerbs.add(verb);
							}
						}
					}
					if (!remainingVerbs.isEmpty()) {
						verbs = remainingVerbs;
					}
				}
//				if (verbs.size() > 1) {
//					System.out.println("\ncandidates: ");
//					for (Token v : verbs) {
//						System.out.println(v.getLemma().getValue() + " " + v.getPos().getPosValue());
//					}
//				}
				mainVerb = verbs.get(0);
				segment.setMainVerb(mainVerb);
			} else {
				mainVerb = (Token) segment.getMainVerb();
			}
			// String lemma = mainVerb.getLemma().getValue();
			// if (!lemma.matches("get|give|have|take|hold|make")) {
			// throw new RuntimeException();
			// }

			//System.out.println("Main verb: " + mainVerb.getCoveredText());

			// Finding the main referent: subject/ vmod
			Token mr = null;

			// get sentence to which the main verb belongs
			List<Sentence> sents = JCasUtil.selectCovering(Sentence.class, mainVerb);
			Sentence sent = sents.get(0);
			// get dependencies of the sentence
			List<Dependency> sentDeps = JCasUtil.selectCovered(Dependency.class, sent);

			if (childNodeMap.containsKey(mainVerb)) {
				for (Dependency dep : childNodeMap.get(mainVerb)) {
					if (dep.getDependencyType().matches("nsubj|nsubjpass|csubjpass|csubj|xsubj")) {
						// mark this token as main referent
						// there could be multiple subjects, use the first.
						if (mr == null || mr.getBegin() > dep.getDependent().getBegin()) {
							// Other main referent annotation will stay, but
							// segment
							// only links to the first one.
							segment.setMainReferent(dep.getDependent());
							break;
						}
					}
				}
			}

			// if no main referent found, it could be a copula.
			if (mr == null) {
				for (Dependency dep : sentDeps) {
					// System.out.println("!!"
					// + dep.getGovernor().getCoveredText() + " --> "
					// + dep.getDependencyType() + " --> "
					// + dep.getDependent().getCoveredText());
					if (dep.getDependent() == mainVerb && dep.getDependencyType().matches("cop")) {
						// get the subject of the head of the copula
						// relation
						for (Dependency dep2 : childNodeMap.get(dep.getGovernor())) {
							if (dep2.getDependencyType().matches("nsubj|nsubjpass|csubjpass|csubj|xsubj|dep")) {
								segment.setMainReferent(dep2.getDependent());
								break;
							}
						}

					}
				}
			}
			// if no main referent found, use some more relaxed rules
			if (mr == null) {
				// 'dep' relation between 'subject' and main verb (instead
				// of
				// nsubj etc)
				for (Dependency dep : sentDeps) {
					if (dep.getDependent() == mainVerb && dep.getDependencyType().matches("dep")) {
						// assume this is the main referent
						segment.setMainReferent(dep.getGovernor());
						break;
					}
				}
			}
			if (mr == null) {
				// TODO: 'vmod--nsubj' relation
				// TODO: 'prepc_by--xsubj' relation
				// TODO: 'prepc_before-subj' relation
				for (Dependency dep : sentDeps) {
					if (dep.getDependent() == mainVerb
							&& dep.getDependencyType().matches("vmod|conj_and|conj_but|conj_or|prepc_.+")) {

						if (dep.getGovernor().getPos().getPosValue().startsWith("N")
								|| dep.getGovernor().getPos().getPosValue().equals("DT")) {
							// assume this is the main referent
							segment.setMainReferent(dep.getGovernor());
							break;
						}
						for (Dependency dep2 : sentDeps) {
							// System.out.println(":"
							// + dep2.getGovernor().getCoveredText()
							// + " --> " + dep2.getDependencyType()
							// + " --> "
							// + dep2.getDependent().getCoveredText());
							if (dep.getGovernor() == dep2.getGovernor()
									&& dep2.getDependencyType().matches(".*subj.*")) {
								// System.out.println("-///-> "
								// + dep2.getGovernor().getCoveredText());
								segment.setMainReferent(dep2.getDependent());
								break;
							}
							// go further here?
							/*
							 * The Office of Fair Trading will then investigate,
							 * impose an injunction Main verb: investigate Main
							 * referent: Office
							 * 
							 * or take the matter to litigation. Main verb: take
							 * :Office --> det --> The :investigate --> nsubj
							 * --> Office :Trading --> nn --> Fair :Office -->
							 * prep_of --> Trading :investigate --> aux --> will
							 * :investigate --> advmod --> then :investigate -->
							 * ccomp --> impose :injunction --> det --> an
							 * :impose --> dobj --> injunction :investigate -->
							 * ccomp --> take :impose --> conj_or --> take
							 * :matter --> det --> the :take --> dobj --> matter
							 * :take --> prep_to --> litigation
							 */
						}

					}
				}
			}

//			if (mr == null) {
//				for (Dependency dep : sentDeps) {
//					System.out.println(":" + dep.getGovernor().getCoveredText() + " --> " + dep.getDependencyType()
//							+ " --> " + dep.getDependent().getCoveredText());
//				}
//			}

		}

	}

	private static boolean governedByVerbInSegment(List<Dependency> deps, Token verb, List<Token> verbs) {
		// Step 1: for verbs in predicative constructions, add the head to the
		// 'verb' list.
		Token predicateGovernor = null;
		Set<Token> predicates = new HashSet<Token>();
		for (Token v : verbs) {
			predicates.add(v);
			// System.out.println("predicates: " + v.getCoveredText() + " " +
			// v.getBegin());
			// if (v.getLemma().getValue().equals("be")) { could also be become
			// for instance
			for (Dependency dep : deps) {
				if (dep.getDependent() == v && dep.getDependencyType().matches("aux|cop|partmod")) {
					predicates.add(dep.getGovernor());
					if (dep.getDependent() == verb) {
						// current verb should not be considered to be
						// governed by its predicate if it's "be"
						if (!verbs.contains(dep.getGovernor())) {
							predicateGovernor = dep.getGovernor();
						}
					}
				}
			}
		}

		// Step 2: find all the Tokens in the segment that govern the verb
		Set<Token> ancestors = new HashSet<Token>();
		ancestors.add(verb);
		int sizeBefore = -2;
		int sizeAfter = -1;
		while (sizeBefore < sizeAfter) {
			sizeBefore = ancestors.size();
			for (Dependency dep : deps) {
				if (ancestors.contains(dep.getDependent())) {
					ancestors.add(dep.getGovernor());
					// System.out.println(" ancestor: "
					// + dep.getGovernor().getCoveredText() + " "
					// + dep.getDependencyType() + " " +
					// dep.getGovernor().getBegin() + " " +
					// predicates.contains(dep.getGovernor()));
				}
			}
			sizeAfter = ancestors.size();
			// is size did not increase, can stop.
		}

		// Step 3: if one of the other verbs is an ancestor the verb, return
		// false
		for (Token verb2 : predicates) {
			if (verb == verb2) {
				continue;
			}
			if (ancestors.contains(verb2)) {
				// found another verb in the segment that governs the verb
				// System.out.println("governing relation: verb:" +
				// verb.getCoveredText() + " -> "
				// + verb2.getCoveredText() + " ");

				// the following 'continue' had introduced some errors.
				// would it be needed somewhere?

				// if (predicateGovernor != null) {
				// // System.out.println("pgov: "
				// // + predicateGovernor.getCoveredText());
				// }
				if (predicateGovernor == verb2) {
					// if it is the predicate governing 'be', continue
					// System.out.println("continuing");
					continue;
				}
				// System.out.println("found governing node"
				// + verb2.getCoveredText());
				return true;
			}
		}
		// System.out.println("did not find governing node");
		return false;
	}

}
