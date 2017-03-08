package sitent.syntSemFeatures.segment;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

/**
 * Adds the word2vec features generated by Eva.
 * 
 * - composed_data.csv :: composed vectors for the wiki annotated data. each vector has the instanceid
 * as the identifier and the composition is a simple additive of the main referent plus all words in the
 * text (all those found in the vocabulary of the constituent vectors). I did not include instances where
 * there was no referent or we do not have a vector for the referent. Also note that OOV words from the
 * text were excluded from the composition.

- vectors.tokens.50-min-count.15-iters.8-win.300 :: contains all constituent vectors. these are
 300-dimensional tokenized w2v vectors built from wikipedia with a word fq threshold of 50, and a
 window of 8
 * 
 */

import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import au.com.bytecode.opencsv.CSVReader;
import sitent.types.Segment;
import sitent.util.FeaturesUtil;

public class VectorFeaturesAnnotator extends JCasAnnotator_ImplBase {
	
	static Logger log = Logger.getLogger(VectorFeaturesAnnotator.class.getName());

	public static final String PARAM_COMPOSED_VECTORS_DIR = "composedVectorsFile";
	@ConfigurationParameter(name = PARAM_COMPOSED_VECTORS_DIR, mandatory = true, defaultValue = "null", description = "Location for files with composed w2v vectors")
	private String composedVectorsFile;
	
	// instance id ---> vector
	// don't parse to double here, happens later
	Map<String, List<String>> composedVectors;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		log.info("Reading in word2vec features ...");
		
		composedVectors = new HashMap<String, List<String>>();
		
		// read in composed vectors
		CSVReader reader;
		try {
			reader = new CSVReader(new FileReader(composedVectorsFile));
			 String [] line;
		     while ((line = reader.readNext()) != null) {
		        String instanceId = line[0];
		        // rest of line is the vector
		        List<String> vector = new LinkedList<String>();
		        for (int i=1; i<line.length; i++) {
		        	vector.add(line[i]);
		        }
		        composedVectors.put(instanceId, vector);
		     }
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceInitializationException();
		}
		
	    try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    log.info("done.");
	    
	}
	
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		
		List<Segment> segments = new LinkedList<Segment>(JCasUtil.select(jCas, Segment.class));
		for (Segment segment : segments) {
			String instanceId = FeaturesUtil.getFeatureValue("instanceid", segment);
			log.info("instanceid: " + instanceId + " " + segment.getCoveredText());
			if (!composedVectors.containsKey(instanceId)) {
				continue;
			}
			log.info("adding vector for: " + instanceId);
			List<String> vector = composedVectors.get(instanceId);
			for (int i=1; i<vector.size(); i++) {
				// one feature per dimension of the vector
				FeaturesUtil.addFeature("w2vComp_" + i, vector.get(i), jCas, segment);
			}
		}
		
	}

}
