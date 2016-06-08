package sitent.syntSemFeatures.nounPhrase;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
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
import sitent.types.ClassificationAnnotation;
import sitent.util.FeaturesUtil;
import sitent.util.GrammarUtils;
import sitent.util.WordNetUtils;

/**
 * This annotator extracts features for noun phrases: which ones, should be
 * determined before running this annotator in a different annotator by marking
 * those NPs as ClassificationAnnotations. The reason for this is efficiency:
 * you can determine the NPs for which you want to extract features in another
 * annotator, and then extract features only for those (rather than for all NPs
 * in the text).
 * 
 * Parts of this annotator are a re-implementation of Nils Reiter's system
 * (Identifying Generic Noun Phrases, Reiter & Frank, ACL 2010) based on
 * Stanford CoreNLP. Original system used ParGram.
 * 
 * Please see README for information on Celex countability files.
 * 
 * 
 * If you use this software, please cite: Annemarie Friedrich and Manfred
 * Pinkal: Discourse-sensitive Automatic Identification of Generic Expressions.
 * August 2015. In Proceedings of the 53rd Annual Meeting of the Association for
 * Computational Linguistics (ACL). Beijing, China.
 * 
 * 
 * @author afried (Annemarie Friedrich)
 * 
 */
public class NounPhraseFeaturesAnnotator extends JCasAnnotator_ImplBase {

	// See README for information on file format for this resource.
	public static final String PARAM_COUNTABILITY_PATH = "countabilityPath";
	@ConfigurationParameter(name = PARAM_COUNTABILITY_PATH, mandatory = false, defaultValue = "null", description = "Path to countability file (e.g. extracted from Celex).")
	private String countabilityPath;


	public static final String PARAM_WORDNET_PATH = "wordNetPath";
	@ConfigurationParameter(name = PARAM_WORDNET_PATH, mandatory = true, defaultValue = "null", description = "Path to WordNet database.")
	private String wordNetPath;

	private IDictionary wordnet = null;
	private NounPhraseFeatures npFeatureExtractor = null;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		try {

			System.out.println(new File(wordNetPath).getAbsolutePath());

			wordnet = WordNetUtils.getDictionary(wordNetPath);
			if (!countabilityPath.equals("null")) {
				npFeatureExtractor = new NounPhraseFeatures(countabilityPath);
			} else {
				npFeatureExtractor = new NounPhraseFeatures(null);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceInitializationException();
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		// Create map for efficient access of child nodes.
		HashMap<Token, Set<Dependency>> childNodeMap = NounPhraseFeatures.getChildNodesMap(jCas);

		// collect all pre-marked NPs of interested and iterate over them.
		Collection<ClassificationAnnotation> nounPhrases = JCasUtil.select(jCas, ClassificationAnnotation.class);
		for (ClassificationAnnotation nounPhrase : nounPhrases) {

			if (nounPhrase.getTask() == null) {
				continue;
			}

			if (!nounPhrase.getTask().equals("NP")) {
				continue;
			}

			// identify the syntactic head of the NP
			Token head = GrammarUtils.getHeadNoun(nounPhrase, jCas);
			if (head == null) {
				// skip if no head could be identified
				continue;
			}
			// extract features for this head (no feature prefix, simple np
			// features.
			setNounPhraseFeaturesForToken(head, jCas, nounPhrase, childNodeMap, "");
			WordNetUtils.setWordNetFeatures(head, nounPhrase, jCas, "", wordnet);
			GrammarUtils.setDependencyRelationFeatures(head, jCas, nounPhrase, "");
		}

	}

	/**
	 * Sets the noun phrase based features for the head token given.
	 * 
	 * @param head
	 * @param jCas
	 * @param classAnnot
	 * @param childNodeMap
	 */
	private void setNounPhraseFeaturesForToken(Token head, JCas jCas, ClassificationAnnotation classAnnot,
			HashMap<Token, Set<Dependency>> childNodeMap, String featurePrefix) {

		String headPos = head.getPos().getPosValue();
		if (!NounPhraseFeatures.isNounOrPronoun(head)) {
			// head POS of this NP is not a noun
			return;
		}

		String number = npFeatureExtractor.getNumber(head);
		String person = npFeatureExtractor.getPerson(head);

		String nounType = NounPhraseFeatures.getNounType(head);
		String detType = NounPhraseFeatures.getDeterminerType(jCas, head, childNodeMap, true);
		Boolean barePlural = NounPhraseFeatures.isBarePlural(jCas, head, childNodeMap);

		// Noun-based features
		FeaturesUtil.addFeature(featurePrefix + "number", number, jCas, classAnnot);
		FeaturesUtil.addFeature(featurePrefix + "person", person, jCas, classAnnot);
		FeaturesUtil.addFeature(featurePrefix + "nounType", nounType, jCas, classAnnot);
		FeaturesUtil.addFeature(featurePrefix + "determinerType", detType, jCas, classAnnot);
		FeaturesUtil.addFeature(featurePrefix + "mentionPos", headPos, jCas, classAnnot);
		FeaturesUtil.addFeature(featurePrefix + "barePlural", barePlural.toString(), jCas, classAnnot);
		FeaturesUtil.addFeature(featurePrefix + "mentionLemma", head.getLemma().getValue(), jCas, classAnnot);

		// Countability features, e.g. from Celex
		if (!countabilityPath.equals("null")) {
			String countability = npFeatureExtractor.getCountability(head);
			FeaturesUtil.addFeature(featurePrefix + "countability", countability, jCas, classAnnot);
		}

	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		wordnet.close();
		super.collectionProcessComplete();
	}

}
