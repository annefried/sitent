package sitent.syntSemFeatures.segment;

/**
 * First 9 digits in clustering: 511 clusters.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import sitent.types.Segment;
import sitent.util.FeaturesUtil;
import sitent.util.SitEntUtils;

public class BrownClusterFeaturesAnnotator extends JCasAnnotator_ImplBase {

	// segments to the left & right of the Segment
	private static final int WINDOW_SIZE = 4;

	static Logger log = Logger.getLogger(BrownClusterFeaturesAnnotator.class.getName());

	public static final String PARAM_BROWN_CLUSTER_DIR = "brownClusterDir";
	@ConfigurationParameter(name = PARAM_BROWN_CLUSTER_DIR, mandatory = true, defaultValue = "null", description = "Location for files with pretrained Conll Brown clusters..")
	private String brownClusterDir;

	/**
	 * First key --> number of Brown clusters (100, 320, 1000, 3200) maps: word
	 * --> Brown cluster ID
	 */
	Map<String, Map<String, String>> brownClusters;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		brownClusters = new HashMap<String, Map<String, String>>();

		// the names of the files (from
		// http://metaoptimize.com/projects/wordreprs/)
		Map<String, String> brownClusterFilenames = new HashMap<String, String>(4);
		brownClusterFilenames.put("100", "brown-rcv1.clean.tokenized-CoNLL03.txt-c100-freq1.txt");
		brownClusterFilenames.put("320", "brown-rcv1.clean.tokenized-CoNLL03.txt-c320-freq1.txt");
		brownClusterFilenames.put("1000", "brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt");
		brownClusterFilenames.put("3200", "brown-rcv1.clean.tokenized-CoNLL03.txt-c3200-freq1.txt");

		// read in Brown clusters
		try {
			log.info("Reading in Brown clusters...");
			for (String clusterSize : brownClusterFilenames.keySet()) {
				brownClusters.put(clusterSize, new HashMap<String, String>());
				if (clusterSize.equals("1000")) {
					brownClusters.put("511", new HashMap<String, String>());
				}
				String filename = brownClusterFilenames.get(clusterSize);
				log.info("..." + filename);
				BufferedReader r = new BufferedReader(new FileReader(brownClusterDir + "/" + filename));
				String line;
				while ((line = r.readLine()) != null) {
					String[] cols = line.split("\t");
					brownClusters.get(clusterSize).put(cols[1], cols[0]);
					if (clusterSize.equals("1000")) {
						// cut off path
						String clusterPath = cols[0];
						if (clusterPath.length() > 9) {
							clusterPath.substring(0, 9);
						}
						brownClusters.get("511").put(cols[1], clusterPath);
					}
				}
				r.close();
			}
			log.info("Done.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceInitializationException();
		}

	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		List<Segment> segments = new LinkedList<Segment>(JCasUtil.select(jCas, Segment.class));

		// features for each segment (counts)
		Map<Integer, Map<String, Double>> featureMap = new HashMap<Integer, Map<String, Double>>();
		// number of tokens for each segment, needed for normalization later on
		Map<Integer, Integer> numTokensPerSegment = new HashMap<Integer, Integer>();

		for (int i = 0; i < segments.size(); i++) {
			Segment segment = segments.get(i);
			numTokensPerSegment.put(i, JCasUtil.selectCovered(Token.class, segment).size());

			featureMap.put(i, new HashMap<String, Double>());

			// collect cluster assignments of tokens in segment
			Map<String, Map<String, Integer>> clusterAssignments = new HashMap<String, Map<String, Integer>>(4);
			for (String clusterSize : brownClusters.keySet()) {
				clusterAssignments.put(clusterSize, new HashMap<String, Integer>(50));
			}
			for (Token token : JCasUtil.selectCovered(Token.class, segment)) {
				for (String clusterSize : brownClusters.keySet()) {
					String clusterPath = brownClusters.get(clusterSize).get(token.getCoveredText());
					if (clusterPath != null) {
						SitEntUtils.incrementMapForKey(clusterAssignments.get(clusterSize), clusterPath);
						SitEntUtils.incrementMapForKeyDouble(featureMap.get(i), clusterSize + "_" + clusterPath);
					}
				}
			}
			// add features
			for (String clusterSize : brownClusters.keySet()) {
				for (String clusterPath : clusterAssignments.get(clusterSize).keySet()) {
					String featureName = "segment_brownCluster_" + clusterSize + "_" + clusterPath;
					FeaturesUtil.addFeature(featureName,
							clusterAssignments.get(clusterSize).get(clusterPath).toString(), jCas, segment);

				}
			}

			// add features for main verb / main referent
//			Annotation mainVerb = segment.getMainVerb();
//			if (mainVerb != null) {
//				String mvText = mainVerb.getCoveredText();
//
//				for (String clusterSize : clusterAssignments.keySet()) {
//					String bc = brownClusters.get(clusterSize).get(mvText);
//					if (bc == null) {
//						Token mvToken = (Token) mainVerb;
//						bc = brownClusters.get(clusterSize).get(mvToken.getLemma());
//					}
//					if (bc != null) {
//						FeaturesUtil.addFeature("main_verb_brownCluster_" + clusterSize,
//								bc, jCas, segment);
//					}
//					
//				}
//				Annotation mainReferent = segment.getMainReferent();
//				if (mainReferent != null) {
//					String mrText = mainReferent.getCoveredText();
//					for (String clusterSize : clusterAssignments.keySet()) {
//						String bc = brownClusters.get(clusterSize).get(mrText);
//						if (bc == null) {
//							Token mrToken = (Token) mainReferent;
//							bc = brownClusters.get(clusterSize).get(mrToken.getLemma());
//						}
//						if (bc != null) {
//							FeaturesUtil.addFeature("main_referent_brownCluster_" + clusterSize,
//									bc, jCas, segment);
//						}
//					}
//				}
//
//			}

		}

		// add features for wider context of each segment
		for (int i = 0; i < segments.size(); i++) {
			int sum = 0;
			Map<String, Double> aggregatedFeatures = new HashMap<String, Double>();
			for (int x = Math.max(0, i - WINDOW_SIZE); x < Math.min(i + WINDOW_SIZE, segments.size()); x++) {
				// System.out.println(x + " / " + segments.size());
				sum += numTokensPerSegment.get(x);
				for (String key : featureMap.get(x).keySet()) {
					for (int k = 0; k < featureMap.get(x).get(key); k++) {
						SitEntUtils.incrementMapForKeyDouble(aggregatedFeatures, key);
					}
				}
			}
			// normalize
			for (String key : aggregatedFeatures.keySet()) {
				String featVal = new Double(aggregatedFeatures.get(key) / (double) sum).toString();
				FeaturesUtil.addFeature("context_" + key, featVal, jCas, segments.get(i));
			}

		}

	}

}
