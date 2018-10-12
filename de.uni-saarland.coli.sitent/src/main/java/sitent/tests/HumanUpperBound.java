package sitent.tests;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import sitent.classifiers.EvaluationUtils;
import sitent.io.TextReaderWithFilename;
import sitent.io.XmlAnnotationsReader;
import sitent.types.Segment;
import sitent.types.Situation;
import sitent.util.SitEntUimaUtils;
import sitent.util.SitEntUtils;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;

public class HumanUpperBound extends JCasAnnotator_ImplBase {
	
	static Logger log = Logger.getLogger(HumanUpperBound.class.getName());

	/***
	 * We compute an upper bound for system performance by iterating over all
	 * clauses: for each pair of human annotators, two entries are added to a
	 * co-occurrence matrix (similar to a confusion matrix), with each label
	 * serving once as “gold standard” and once as the “prediction.” From this
	 * matrix, we can compute scores in the same manner as for system
	 * predictions. Precision and recall scores are symmetric in this case, and
	 * accuracy corresponds to observed agreement.
	 */

	private String resultsOutDir = "/media/annemarie/692EF2020A456E14/Work/workspace-data/sitent/temp-results.txt";

	private static Map<String, Map<String, Integer>> confMatrix = new HashMap<>();
	private static Map<String, Integer> goldStandardCounts = new HashMap<String, Integer>();
	
	private static Set<String> usedSeTypes = new HashSet<String>();
	static {
		usedSeTypes.add("EVENT");
		usedSeTypes.add("STATE");
		usedSeTypes.add("GENERIC_SENTENCE");
		usedSeTypes.add("GENERALIZING_SENTENCE");
		usedSeTypes.add("REPORT");
		usedSeTypes.add("IMPERATIVE");
		usedSeTypes.add("QUESTION");
	}
	
	
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		

		Collection<Segment> segments = JCasUtil.select(jCas, Segment.class);
		Iterator<Segment> segIt = segments.iterator();
		while (segIt.hasNext()) {
			Segment segment = segIt.next();

			// all the Situation annotations for the segment
			// TODO: This didn't work, need to change in XMLAnnotationsReader??
//			List<Annotation> situations = SitEntUimaUtils.getList(segment
//					.getSituationAnnotations());
			List<Situation> situations = JCasUtil.selectCovered(Situation.class, segment);
			

			for (Situation sit : situations) {
				Set<String> sitLabels1 = new HashSet<String>(
						SitEntUimaUtils.getList(sit.getSeType()));
				sitLabels1.retainAll(usedSeTypes);

				if (sitLabels1.isEmpty()) {
					continue;
				}
				
				if (sit.getAnnotator().equals("gold")) {
					SitEntUtils.incrementMapForKey(goldStandardCounts, sitLabels1.iterator().next());
					continue;
				}
				
				String seType1 = sitLabels1.iterator().next();

				for (Annotation annot2 : situations) {
					Situation sit2 = (Situation) annot2;
					if (sit2.getAnnotator().equals("gold")
							|| sit.getAnnotator().equals(sit2.getAnnotator())) {
						continue;
					}
					// add labels for this pair of annotators to co-occurrence
					// matrix
					// if one annotator has given multiple values, check if
					// there is an overlap!
					Set<String> sitLabels2 = new HashSet<String>(
							SitEntUimaUtils.getList(sit2.getSeType()));
					sitLabels2.retainAll(usedSeTypes);
					if (sitLabels2.isEmpty()) {
						// TODO what if seType1 != None??
						continue;
					}

					// this is the intersection:
					sitLabels1.retainAll(sitLabels2);
					if (!sitLabels1.isEmpty()) {
						// agreement; get "first" element
						String seType = sitLabels1.iterator().next();
						if (confMatrix.containsKey(seType)) {
							if (confMatrix.get(seType).containsKey(seType)) {
								confMatrix.get(seType).put(seType,
										confMatrix.get(seType).get(seType) + 2);
							} else {
								confMatrix.get(seType).put(seType, 2);
							}
						} else {
							confMatrix.put(seType,
									new HashMap<String, Integer>());
							confMatrix.get(seType).put(seType, 2);
						}
					} else {
						// disagreement; simply use "first" labels of each
						// annotator
						String seType2 = sitLabels2.iterator().next();
						if (!confMatrix.containsKey(seType1)) {
							confMatrix.put(seType1,
									new HashMap<String, Integer>());
						}
						if (!confMatrix.containsKey(seType2)) {
							confMatrix.put(seType2,
									new HashMap<String, Integer>());
						}

						SitEntUtils.incrementMapForKey(confMatrix.get(seType1),
								seType2);

						SitEntUtils.incrementMapForKey(confMatrix.get(seType2),
								seType1);
						//System.out.println("HumanUpperBound:: " + seType1 + " " + seType2);

					}
				}
			}

		}

	}

	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {

		Set<String> classValuesUsed = confMatrix.keySet();
		// assume that we never predicted classes not existing in gold standard
		String[] values = new String[classValuesUsed.size()];
		int i = 0;
		for (String s : classValuesUsed) {
			values[i++] = s;
		}
				
		PrintWriter w;
		try {
			w = new PrintWriter(new FileWriter(resultsOutDir));
			EvaluationUtils.printResults(confMatrix, w, values);
			
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (String label : goldStandardCounts.keySet()) {
			log.info(label + "\t" + goldStandardCounts.get(label));
		}

		
		
		
		super.collectionProcessComplete();
	}

	public static void main(String[] args) throws UIMAException, IOException {
		// read test or dev set
		String inputDir = "/media/annemarie/692EF2020A456E14/Work/workspace-data/sitent/test_data_txt";
		String annotationsDir = "/media/annemarie/692EF2020A456E14/Work/workspace-data/sitent/test_data_xml";

		// read text
		CollectionReader reader = createReader(TextReaderWithFilename.class,
				TextReaderWithFilename.PARAM_SOURCE_LOCATION, inputDir,
				TextReaderWithFilename.PARAM_PATTERNS,
				new String[] { "[+]*.txt" },
				TextReaderWithFilename.PARAM_LANGUAGE, "en");

		// tokenize
		AnalysisEngineDescription stTokenizer = AnalysisEngineFactory
				.createEngineDescription(StanfordSegmenter.class,
						StanfordSegmenter.PARAM_LANGUAGE, "en");

		// add manual annotations
		AnalysisEngineDescription xmlAnnotations = AnalysisEngineFactory
				.createEngineDescription(XmlAnnotationsReader.class,
						XmlAnnotationsReader.PARAM_INPUT_DIR, annotationsDir);

		// compute human upper bound as described in thesis p. 130
		AnalysisEngineDescription upperBound = AnalysisEngineFactory
				.createEngineDescription(HumanUpperBound.class);

		runPipeline(reader, stTokenizer, xmlAnnotations, upperBound);

	}

}
