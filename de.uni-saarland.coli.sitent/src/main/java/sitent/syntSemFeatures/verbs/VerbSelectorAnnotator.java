package sitent.syntSemFeatures.verbs;

/**
 * @author afried (Annemarie Friedrich)
 * 
 * This annotator selects all finite verbs for annotation.
 * 
 */

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
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.VP;
import sitent.types.ClassificationAnnotation;
import sitent.util.GrammarUtils;

public class VerbSelectorAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		
		Collection<VP> verbPhrases = JCasUtil.select(jCas, VP.class);
		for (VP vp : verbPhrases) {
			Token head = GrammarUtils.getHeadVerb(vp, jCas, true);
			if (head == null) {
				// skip cases where head could not be identified
				continue;
			}
			if (JCasUtil.selectCovered(ClassificationAnnotation.class, head).isEmpty()) {
				ClassificationAnnotation classAnnot = new ClassificationAnnotation(jCas, head.getBegin(),
						head.getEnd());
				classAnnot.setTask("VERB");
				classAnnot.addToIndexes();
			}
		}
	}
}
