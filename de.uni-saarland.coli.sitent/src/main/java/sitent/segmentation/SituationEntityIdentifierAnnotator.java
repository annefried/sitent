package sitent.segmentation;

/**
 * This is an alternative to the situation entity segmentation using SPADE as
 * described in our papers. The advantage is that you don't have to install and
 * run SPADE, and results are comparable. Segments are given as references to
 * the Token annotation for their main verbs. Main verbs are selected as follows:
 * - finite verbs
 * - gerunds that are present participles; for reduced relative clauses
 * 	(e.g., Rules governing proper pronunciation is called tajwid. -- two
 *  segments with main verbs "called" and "governing").
 *  
 *  Note that this segmentation method is an APPROXIMATION of situation entity
 *  segmentation; there will be false positives and false negatives (often based
 *  on parsing errors).
 *  
 *  !!! Selection of tokens that belong to the "clause" as defined by the head verb
 *  is "beta", needs to be tested more fully.
 */

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

/**
 * 
 */

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.PP;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import sitent.syntSemFeatures.verbs.VerbFeaturesAnnotator;
import sitent.syntSemFeatures.verbs.VerbSelectorAnnotator;
import sitent.types.Segment;
import sitent.types.VerbFeatures;
import sitent.util.AnnotationComparator;
import sitent.util.GrammarUtils;
import sitent.util.SitEntUimaUtils;

public class SituationEntityIdentifierAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		// iterate over VerbFeatures annotations (heads)
		Set<Token> verbChainHeads = new HashSet<Token>();
		for (VerbFeatures vf : JCasUtil.select(jCas, VerbFeatures.class)) {
			verbChainHeads.add((Token) vf.getHeadOfVerbChain());
		}

		Set<Token> situationSegmentHeads = new HashSet<Token>();

		List<Token> sortedVerbChainHeads = new LinkedList<Token>();
		sortedVerbChainHeads.addAll(verbChainHeads);
		Collections.sort(sortedVerbChainHeads, new AnnotationComparator());

		for (Token head : sortedVerbChainHeads) {
			// skip heads that are only punctuation
			if (Pattern.matches("\\p{Punct}+", head.getCoveredText())) {
				continue;
			}

			// if some finite tense has been assigned, select as situation
			// entity
			VerbFeatures vf = JCasUtil.selectCovered(VerbFeatures.class, head).get(0);
			String tense = vf.getTense();

			if (!tense.matches("other|infinitif")) {
				// a finite tense has been assigned: identify SE
				situationSegmentHeads.add(head);

			} else if (tense.matches("other")) {
				// need to check whether it's not a gerund
				if (!head.getPos().getPosValue().matches("P")) {

					// approximate identification of reduced relative clauses
					if (head.getPos().getPosValue().matches("VBG")) {
						boolean isSe = false;
						Sentence sent = JCasUtil.selectCovering(Sentence.class, vf).get(0);
						Collection<Dependency> deps = JCasUtil.selectCovered(Dependency.class, sent);
						for (Dependency dep : deps) {
							if (dep.getDependent() == head && dep.getDependencyType().matches("vmod|xcomp|ccomp")) {
								isSe = true;
								break;
							}
						}
						// exclude other cases for now.
						if (!isSe) {
							continue;
						}
						// else {
						// System.out.println("Found VBG case: " +
						// vf.getCoveredText());
						// System.out.println(sent.getCoveredText());
						// }
					}

					// The following is not used for now.
					// if (head.getPos().getPosValue().equals("VB")) {
					// boolean isSe = false;
					// // difficult case: some are SEs, some not
					// // the ones with aux are SEs.
					// Sentence sent = JCasUtil.selectCovering(Sentence.class,
					// vf).get(0);
					// Collection<Dependency> deps =
					// JCasUtil.selectCovered(Dependency.class, sent);
					// for (Dependency dep : deps) {
					// if (dep.getGovernor() == head &&
					// dep.getDependencyType().equals("aux")) {
					// isSe = true;
					// break;
					// }
					// }
					// // exclude other cases for now.
					// if (!isSe) {
					// continue;
					// }
					// }

					if (head.getPos().equals("VBN")) {
						boolean isSe = true;
						Sentence sent = JCasUtil.selectCovering(Sentence.class, vf).get(0);
						Collection<Dependency> deps = JCasUtil.selectCovered(Dependency.class, sent);
						for (Dependency dep : deps) {
							if (dep.getDependent() == head && dep.getDependencyType().equals("amod")) {
								isSe = false;
								break;
							}
						}
						// exclude other cases for now.
						if (!isSe) {
							continue;
						}
						// else {
						// System.out.println("Found VBN case: " +
						// vf.getCoveredText());
						// System.out.println(sent.getCoveredText());
						// }
					}

					// these should be situations
					// a finite tense has been assigned: identify SE
					situationSegmentHeads.add(head);

				}

			}
		}

		// sort situation segment heads
		List<Token> sorted = new LinkedList<Token>();
		sorted.addAll(situationSegmentHeads);
		Collections.sort(sorted, new AnnotationComparator());

		// get a map for accessing the dependencies quickly (child nodes for
		// each token)
		HashMap<Token, Set<Dependency>> deps = GrammarUtils.getChildNodesMap(jCas);

		int i = 0;
		// add the actual Segments to the JCas
		for (Token se : sorted) {
			Segment segment = new Segment(jCas);
			segment.setBegin(se.getBegin());
			segment.setEnd(se.getEnd());
			segment.setMainVerb(se);
			segment.setSegid(i++);
			segment.addToIndexes();

			// identify the child nodes of the main verb as the 'segment'
			// TODO: find best method here!
			List<Token> segmentTokens = new LinkedList<Token>();
			segmentTokens.add(se);
			// if main verb is a copula, search for dependents of copula's head
			// instead
			List<Dependency> headDeps = JCasUtil.selectCovered(Dependency.class, se);
			if (!headDeps.isEmpty()) {
				Dependency headDep = headDeps.get(0);
				if (headDep.getDependencyType().equals("cop")) {
					se = headDep.getGovernor();
					// this heads clause in dependency parse
				}
			}

			if (deps.containsKey(se)) {
				for (Dependency dependency : deps.get(se)) {
					getSegmentTokens(new HashSet<Token>(sorted), dependency.getDependent(), segmentTokens, deps,
							dependency);
				}
			}
			// set the segment tokens to the FSList feature
			//System.out.println("-----------------------");
			for (Token token : segmentTokens) {
				segment.setTokens(SitEntUimaUtils.addToFSList(segment.getTokens(), token, jCas));
				//System.out.println(token.getCoveredText());
			}
		}

	}

	/**
	 * Select the set of tokens relevant for the "clause" as the relevant
	 * dependents of the main verb. Use the set of descendents of a node; stop
	 * top-down search if the child node has been identified as the main verb of
	 * another situation entity.
	 * 
	 * @param segment
	 */
	private static void getSegmentTokens(Set<Token> mainVerbs, Token token, List<Token> descendants,
			HashMap<Token, Set<Dependency>> deps, Dependency relToHead) {
		if (!mainVerbs.contains(token) && !descendants.contains(token)) {
			// need to check whether direct child of this token is 'be'
			// e.g., as in "Born in the town Krasnoturinsk, Sverdlovsk Oblast in
			// the Urals as the son of a priest, he became interested in natural
			// sciences when he was a child."
			if (deps.containsKey(token)) {
				for (Dependency dependency : deps.get(token)) {
					if (dependency.getDependencyType().equals("cop")) {
						// do not add these cases
						return;
					}
				}
			}

			descendants.add(token);

			// if relation to head is a collapsed dependency relation, e.g.,
			// prep_of, need to add the preposition as well
			if (relToHead.getDependencyType().startsWith("prep_")) {
				// select covering PP with smallest span
				List<PP> pps = JCasUtil.selectCovering(PP.class, token);
				if (!pps.isEmpty()) {
					PP pp = pps.get(0);
					for (int i = 1; i < pps.size(); i++) {
						if (SitEntUimaUtils.covers(pp, pps.get(i))) {
							pp = pps.get(i);
						}
					}
					// add all tokens of the PP (if not yet contained)
					for (Token t : JCasUtil.selectCovered(Token.class, pp)) {
						if (!descendants.contains(t)) {
							descendants.add(t);
						}
					}
				}
			}
			// TODO: are there other cases of collapsing that we need to catch?

			if (deps.containsKey(token)) {
				for (Dependency dependency : deps.get(token)) {
					getSegmentTokens(mainVerbs, dependency.getDependent(), descendants, deps, dependency);
				}
			}
		}
	}

	/**
	 * Just for testing intermediate steps.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Options options = new Options();
		options.addOption("input", true, "input directory with XMIs (e.g., gold standard)");
		options.addOption("output", true, "output directory for XMIs");

		// Parse command line and configure
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
			String xmiInputDir = cmd.getOptionValue("input");
			String xmiOutputDir = cmd.getOptionValue("output");

			// read text
			CollectionReader reader = createReader(XmiReader.class, XmiReader.PARAM_SOURCE_LOCATION, xmiInputDir,
					XmiReader.PARAM_PATTERNS, new String[] { "[+]*.xmi" });

			// tokenize, parse, add lemmas

			AnalysisEngineDescription stParser = AnalysisEngineFactory.createEngineDescription(StanfordParser.class,
					StanfordParser.PARAM_LANGUAGE, "en", StanfordParser.PARAM_WRITE_POS, true,
					StanfordParser.PARAM_WRITE_PENN_TREE, true, StanfordParser.PARAM_MAX_TOKENS, 200,
					StanfordParser.PARAM_WRITE_CONSTITUENT, true, StanfordParser.PARAM_WRITE_DEPENDENCY, true,
					StanfordParser.PARAM_MODE, StanfordParser.DependenciesMode.CC_PROPAGATED);

			AnalysisEngineDescription stLemmas = AnalysisEngineFactory
					.createEngineDescription(StanfordLemmatizer.class);

			// Verb features are used to identify the main verbs for clauses.

			// Select the verbs for which to extract features.
			AnalysisEngineDescription verbSelector = AnalysisEngineFactory
					.createEngineDescription(VerbSelectorAnnotator.class);

			// Extract the verb-based features.
			AnalysisEngineDescription verbFeatures = AnalysisEngineFactory.createEngineDescription(
					VerbFeaturesAnnotator.class, VerbFeaturesAnnotator.PARAM_WORDNET_PATH, "resources/wordnet3.0",
					VerbFeaturesAnnotator.PARAM_TENSE_FILE, "resources/tense/tense.txt");

			// find situation entities
			AnalysisEngineDescription seIdentifier = AnalysisEngineFactory
					.createEngineDescription(SituationEntityIdentifierAnnotator.class);

			// writes out XMIs (can then be inspected with UIMA annotation
			// viewer,
			// or used for further processing in an UIMA pipeline)
			AnalysisEngineDescription xmiWriter = null;
			if (xmiOutputDir != null) {
				xmiWriter = AnalysisEngineFactory.createEngineDescription(XmiWriter.class,
						XmiWriter.PARAM_TARGET_LOCATION, xmiOutputDir);
			}

			runPipeline(reader, stParser, stLemmas, verbSelector, verbFeatures, seIdentifier, xmiWriter);

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		} catch (UIMAException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
