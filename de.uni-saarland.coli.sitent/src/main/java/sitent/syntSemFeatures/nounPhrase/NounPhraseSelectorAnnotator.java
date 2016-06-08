package sitent.syntSemFeatures.nounPhrase;

import java.util.Collection;

import org.apache.uima.UimaContext;

/**
 * @author afried (Annemarie Friedrich)
 * 
 * This annotator marks selected noun phrases with ClassificationAnnotation annotations.
 * Feature extraction is then done for those (instead of all NPs, for efficiency reasons).
 * 
 * NamedEntityMention: this is for example marked in ACE.
 * 
 */

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.type.NamedEntityMention;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.NP;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.PRP;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import sitent.types.ClassificationAnnotation;
import sitent.util.GrammarUtils;

public class NounPhraseSelectorAnnotator extends JCasAnnotator_ImplBase {

	public enum ClassificationTarget {
		NamedEntityMention, AllNounPhrases, Subjects, Objects
	};

	public static final String PARAM_TARGET = "target";
	@ConfigurationParameter(name = PARAM_TARGET, mandatory = true, defaultValue = "null", description = "Configure target of the classification, must be one of NamedEntityMention, AllNounPhrases")
	private String target;
	private ClassificationTarget classTarget;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		// classification target
		if (!target.matches("NamedEntityMention|AllNounPhrases|Subjects|Objects")) {
			System.err.println(
					"Target for NounPhraseSelectorAnnotator must be one of NamedEntityMention, AllNounPhrases, Subjects or Objects!");
			throw new ResourceInitializationException();
		}
		if (target.equals("NamedEntityMention")) {
			classTarget = ClassificationTarget.NamedEntityMention;
		} else if (target.equals("AllNounPhrases")) {
			classTarget = ClassificationTarget.AllNounPhrases;
		} else if (target.equals("Subjects")) {
			classTarget = ClassificationTarget.Subjects;
		} else if (target.equals("Objects")) {
			classTarget = ClassificationTarget.Objects;
		}

	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		switch (classTarget) {

		case NamedEntityMention:
			markNamedEntityMentions(jCas);
			break;
		case AllNounPhrases:
			markAllNounPhrases(jCas);
			break;
		case Subjects:
			markForGrammatRelation(jCas, "subj");
			break;
		case Objects:
			markForGrammatRelation(jCas, "obj");
			break;
		}

	}

	/**
	 * Mark all NamedEntityMention annotations for feature extraction.
	 * 
	 * @param jCas
	 */
	private void markNamedEntityMentions(JCas jCas) {
		Collection<NamedEntityMention> mentions = JCasUtil.select(jCas, NamedEntityMention.class);
		for (NamedEntityMention mention : mentions) {
			ClassificationAnnotation classAnnot = new ClassificationAnnotation(jCas, mention.getBegin(),
					mention.getEnd());
			classAnnot.setTask("NP");
			classAnnot.addToIndexes();
		}
	}

	/**
	 * Mark all noun phrases for feature extraction, including pronouns.
	 * 
	 * @param jCas
	 */
	private void markAllNounPhrases(JCas jCas) {
		Collection<NP> nounPhrases = JCasUtil.select(jCas, NP.class);
		for (NP np : nounPhrases) {
			ClassificationAnnotation classAnnot = new ClassificationAnnotation(jCas, np.getBegin(), np.getEnd());
			classAnnot.setTask("NP");
			classAnnot.addToIndexes();
		}
//		Collection<PRP> pronouns = JCasUtil.select(jCas, PRP.class);
//		for (PRP prp : pronouns) {
//			ClassificationAnnotation classAnnot = new ClassificationAnnotation(jCas, prp.getBegin(), prp.getEnd());
//			classAnnot.setTask("NP");
//			classAnnot.addToIndexes();
//		}
	}

	/**
	 * Mark all noun phrases for feature extraction whose head is the dependent
	 * of the given relationType.
	 * 
	 * @param jCas
	 * @param relationType
	 *            dependency relation type, e.g. subj or obj.
	 */
	private void markForGrammatRelation(JCas jCas, String relationType) {
		Collection<NP> nounPhrases = JCasUtil.select(jCas, NP.class);
		for (NP np : nounPhrases) {
			// is head in the grammatical relation?
			Token head = GrammarUtils.getHeadNoun(np, jCas);
			// this selects the dependencies where head is the dependent node.
			Collection<Dependency> deps = JCasUtil.selectCovered(Dependency.class, head);
			for (Dependency dep : deps) {
				if (dep.getDependencyType().contains(relationType)) {
					ClassificationAnnotation classAnnot = new ClassificationAnnotation(jCas, np.getBegin(),
							np.getEnd());
					classAnnot.setTask("NP");
					classAnnot.addToIndexes();
					break;
				}
			}
		}
	}
}
