package sitent.syntSemFeatures.verbs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import sitent.types.ClassificationAnnotation;
import sitent.util.FeaturesUtil;

public class LinguisticIndicatorsAnnotator extends JCasAnnotator_ImplBase {

	public static final String PARAM_LING_IND_FILE = "lingIndPath";
	@ConfigurationParameter(name = PARAM_LING_IND_FILE, mandatory = true, defaultValue = "default path", description = "File with precomputed linguistic indicator values (Siegel & McKeown (2000)).")
	private String lingIndPath;

	// indicator values by Siegel & McKeown (200) (key: verb)
	HashMap<String, Map<String, Double>> inds;
	List<String> indNames;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {

		// initalize the configuration parameters
		super.initialize(context);

		try {
			// read in pre-computed features: from Siegel & McKeown (2000)
			inds = new HashMap<String, Map<String, Double>>();
			indNames = new LinkedList<String>();

			BufferedReader r = new BufferedReader(new FileReader(lingIndPath));
			String line = r.readLine();
			String[] parts = line.split("\t");
			for (int i = 1; i < parts.length; i++) {
				indNames.add("lingInd_" + parts[i]);
			}
			while ((line = r.readLine()) != null) {
				parts = line.split("\t");
				String verb = parts[0];
				if (!inds.containsKey(verb)) {
					inds.put(verb, new HashMap<String, Double>());
				}
				for (int i = 1; i < parts.length; i++) {
					inds.get(verb).put(indNames.get(i - 1), Double.parseDouble(parts[i]));
				}
			}
			r.close();

		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceInitializationException();
		}

	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		
		DocumentMetaData dm = JCasUtil.selectSingle(jcas, DocumentMetaData.class);
		System.out.println(dm.getDocumentId());

		// add LINGUISTIC INDICATOR features for verbs
		Iterator<ClassificationAnnotation> annots = JCasUtil.select(jcas, ClassificationAnnotation.class).iterator();

		while (annots.hasNext()) {

			ClassificationAnnotation annot = annots.next();

			if (annot.getTask() == null) {
				continue;
			}

			if (!annot.getTask().equals("VERB")) {
				continue;
			}

			// ClassificationAnnotation for verbs covers exactly one token (the
			// head)
			List<Token> tokens = JCasUtil.selectCovered(Token.class, annot);
			if (tokens.size() != 1) {
				// this should not happen
				System.err.println(tokens.size() + annot.getCoveredText());
				for (Token token: tokens) {
					System.out.println(token);
				}
				return;
				//throw new IllegalStateException();
			}
			Token verb = tokens.get(0);

			for (String indName : indNames) {
				Double val = 0.0;
				if (inds.containsKey(verb.getLemma().getValue())) {
					val = inds.get(verb.getLemma().getValue()).get(indName);
				}
				FeaturesUtil.addFeature(indName, val.toString(), jcas, annot);
			}

		}
	}

}
