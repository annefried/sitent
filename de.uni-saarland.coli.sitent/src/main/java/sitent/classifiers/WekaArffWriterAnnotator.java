package sitent.classifiers;

/**
 * Creates ARFF file from XMIs.
 * 
 * TODO: is there a way of making the instanceid attribute a string attribute
 * instead of a nominal attribute without messing up the CRF input files later
 * (they have to match the values for the pre-trained models...)
 */

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import sitent.types.ClassificationAnnotation;
import sitent.types.Passage;
import sitent.types.SEFeature;
import sitent.types.Segment;
import sitent.util.FeaturesUtil;
import sitent.util.SitEntUimaUtils;

public class WekaArffWriterAnnotator extends JCasAnnotator_ImplBase {

	public static final String PARAM_ARFF_LOCATION = "arffLocation";
	@ConfigurationParameter(name = PARAM_ARFF_LOCATION, mandatory = true, defaultValue = "null", description = "Location for ARFF file.")
	private String arffLocation;

	public static final String PARAM_SPARSE_FORMAT = "sparseFormat";
	@ConfigurationParameter(name = PARAM_SPARSE_FORMAT, mandatory = true, defaultValue = "false", description = "Write sparse ARFF file.")
	private Boolean sparseFormat;

	public static final String PARAM_OMIT_FEATURES = "omitFeaturesPatterns";
	@ConfigurationParameter(name = PARAM_OMIT_FEATURES, mandatory = true, defaultValue = "false", description = "Feature patterns: omit these features when writing ARFF.")
	private String[] omitFeaturesPatterns;

	public static final String PARAM_TARGET_TYPE = "targetType";
	@ConfigurationParameter(name = PARAM_TARGET_TYPE, mandatory = true, defaultValue = "false", description = "Segment or Passage.")
	private String targetType;

	public static final String PARAM_CLASS_ATTRIBUTE = "classAttribute";
	@ConfigurationParameter(name = PARAM_CLASS_ATTRIBUTE, mandatory = true, defaultValue = "null", description = "name of class attribute (last attribute in ARFF).")
	private String classAttribute;

	public static final String PARAM_RESET_SEGIDS = "resetSegIds";
	@ConfigurationParameter(name = PARAM_RESET_SEGIDS, mandatory = true, defaultValue = "false", description = "whether to make sure segids are correct (for instanceid)")
	private Boolean resetSegIds;

	public static final String PARAM_ESCAPE_VALUES = "escapeValues";
	@ConfigurationParameter(name = PARAM_ESCAPE_VALUES, mandatory = true, defaultValue = "true", description = "escape the values so a valid ARFF is created. set to false if values are already quoted in SEFeature.")
	private Boolean escapeValues;

	// public static final String PARAM_UNLABELED_DATA = "unlabeledData";
	// @ConfigurationParameter(name = PARAM_UNLABELED_DATA, mandatory = true,
	// defaultValue = "false", description = "set to true if data does not
	// contain gold standard labels")
	// private boolean unlabeledData;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {

		super.initialize(context);

		// create ARFF dir in case it doesn't exist yet.
		File arffDir = new File(arffLocation);
		if (!arffDir.exists()) {
			arffDir.mkdirs();
		} else {
			arffDir.delete();
			arffDir.mkdirs();
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		DocumentMetaData dm = JCasUtil.selectSingle(jCas,
				DocumentMetaData.class);
		System.out.println("Writing arff for: " + dm.getDocumentId());

		if (resetSegIds) {
			int i = 1;
			for (Segment segment : JCasUtil.select(jCas, Segment.class)) {
				FeaturesUtil.removeFeature("instanceid", segment, jCas);
				String instanceId = dm.getDocumentId() + "_" + i++;
				// System.out.println(instanceId);
				FeaturesUtil
						.addFeature("instanceid", instanceId, jCas, segment);
			}
		}

		List<ClassificationAnnotation> classAnnots = new LinkedList<ClassificationAnnotation>();
		if (targetType.equals("Segment")) {
			Collection<Segment> segments = JCasUtil.select(jCas, Segment.class);
			classAnnots.addAll(segments);

		} else if (targetType.equals("Passage")) {
			Collection<Passage> passages = JCasUtil.select(jCas, Passage.class);
			classAnnots.addAll(passages);
		} else if (targetType.equals("ClassificationAnnotation")) {
			Collection<ClassificationAnnotation> classAnnot = JCasUtil.select(
					jCas, ClassificationAnnotation.class);
			classAnnots.addAll(classAnnot);
		}

		try {
			String filename = null;
			if (dm.getDocumentUri() != null) {
				String[] path = dm.getDocumentUri().split("/");
				filename = path[path.length - 1];
			} else {
				filename = dm.getDocumentId();
			}

			PrintWriter arffWriter = new PrintWriter(new FileWriter(
					arffLocation + "/" + filename + ".arff"));

			// collect information about features
			Map<String, Boolean> isNumericFeature = new HashMap<String, Boolean>();
			// collect values for nominal features
			Map<String, Set<String>> nominalFeatures = new HashMap<String, Set<String>>();

			// iterate over segments
			for (ClassificationAnnotation segment : classAnnots) {
				for (Annotation annot : SitEntUimaUtils.getList(segment
						.getFeatures())) {
					SEFeature feat = (SEFeature) annot;

					if (feat.getName()
							.matches(
									"main_verb_brownCluster_.*|main_referent_brownCluster.*")) {
						isNumericFeature.put(feat.getName(), false);
						continue;
					}

					try {
						Double.parseDouble(feat.getValue());
						if (!isNumericFeature.containsKey(feat.getName())) {
							isNumericFeature.put(feat.getName(), true);
						}
					} catch (NumberFormatException e) {
						isNumericFeature.put(feat.getName(), false);
					}
					// collect values even if it seems to be a numeric feature,
					// as there might be non-numeric values for this feature
					// later, if it's actually nominal and only the first value
					// seen is numeric.
					if (!nominalFeatures.containsKey(feat.getName())) {
						nominalFeatures.put(feat.getName(),
								new HashSet<String>());
					}
					nominalFeatures.get(feat.getName()).add(feat.getValue());
				}
			}
			// create header
			arffWriter.println("@relation sitent");
			arffWriter.println("");
			List<String> featList = new LinkedList<String>(
					isNumericFeature.keySet());
			Collections.sort(featList);

			// move class to end
			if (featList.contains(classAttribute)) {
				featList.remove(classAttribute);
				featList.add(classAttribute);
			}

			// omit features that match one of the 'omit' patterns
			for (int i = featList.size() - 1; i >= 0; i--) {
				String featName = featList.get(i);
				for (String omit : omitFeaturesPatterns) {
					if (featName.matches(omit)) {
						// if (featList.get(i).equals("instanceid"))
						// System.out.println("removing: " + featList.get(i));
						featList.remove(i);
						break;
					}
				}
			}

			for (String featName : featList) {

				// if (featName.equals("instanceid"))
				// System.out.println(featName);

				// add other features to header
				if (isNumericFeature.get(featName)) {
					arffWriter.println("@attribute \""
							+ featName.replaceAll("\"|``", "QUOTE")
									.replaceAll(",", "COMMA")
									.replaceAll(" ", "SPACE") + "\" numeric");
				} else {
					// if (nominalFeatures.get(featName).size() > 12) {
					// arffWriter.println("@attribute " + featName + " string");
					// } else {

					StringBuffer values = new StringBuffer("");
					if (sparseFormat) {
						// add a dummy value, see
						// https://weka.wikispaces.com/ARFF+%28stable+version%29
						values.append("\"THE-DUMMY-VALUE\",");
					}
					List<String> valueList = new LinkedList<String>(
							nominalFeatures.get(featName));
					Collections.sort(valueList);
					for (String value : valueList) {
						if (escapeValues) {
							values.append("\""
									+ value.replaceAll("\"|``", "QUOTE")
											.replaceAll(",", "COMMA")
											.replaceAll(" ", "SPACE") + "\",");
						} else {
							values.append(value + ",");
						}

					}
					values = new StringBuffer(values.toString().substring(0,
							values.length() - 1));
					// add quotes to unquoted feature names
					if (!featName.startsWith("\"")) {
						featName = "\"" + featName + "\"";
					}
					arffWriter.println("@attribute " + featName + " {" + values
							+ "}");
					// }
				}
			}
			arffWriter.println("");
			arffWriter.println("@data");
			// speed up: map feature names to indices
			Map<String, Integer> indexMap = new HashMap<String, Integer>();
			for (int i = 0; i < featList.size(); i++) {
				String featName = featList.get(i);
				indexMap.put(featName, i);
			}

			// create lines for instances
			for (ClassificationAnnotation segment : classAnnots) {
				Map<String, String> featMap = new HashMap<String, String>();
				for (Annotation annot : SitEntUimaUtils.getList(segment
						.getFeatures())) {
					SEFeature feat = (SEFeature) annot;
					if (!isNumericFeature.get(feat.getName())) {
						if (!(feat.getValue().startsWith("\"") && feat
								.getValue().endsWith("\""))) {
							feat.setValue("\""
									+ feat.getValue()
											.replaceAll("\"|``", "QUOTE")
											.replaceAll(",", "COMMA")
											.replaceAll(" ", "SPACE") + "\"");
						} else if (feat.getValue().matches("\"|``")) {
							feat.setValue("QUOTE");
						}
					}
					featMap.put(feat.getName(), feat.getValue());
				}
				if (!sparseFormat) {
					StringBuffer line = new StringBuffer("");
					for (String featName : featList) {
						if (!featMap.containsKey(featName)) {
							line.append("?,");
						} else {
							line.append(featMap.get(featName) + ",");
						}
					}
					arffWriter.println(line.toString().substring(0,
							line.length() - 1));
				} else {
					// sparse format
					StringBuffer values = new StringBuffer("");
					for (String featName : featList) {
						if (featMap.keySet().contains(featName)) {
							if (featMap.get(featName) != null) {
								values.append(indexMap.get(featName) + " "
										+ featMap.get(featName) + ", ");
							}
						}
					}
					if (values.toString().length() >= 2) {
						arffWriter.println("{"
								+ values.toString().substring(0,
										values.length() - 2) + "}");
					} else {
						arffWriter.println("{}");
					}
				}
			}
			arffWriter.close();

		} catch (

		IOException e)

		{
			e.printStackTrace();
			throw new AnalysisEngineProcessException();
		}

	}

	public static void main(String[] args) {

		String inputDir = args[0]; // with XMis
		String arffPath = args[1]; // ARFF files are written here
		String task = args[2]; // e.g., class_sitent_type

		// write ARFF files (for classification toolkit Weka, also used to
		// generate CRFPP input files)
		try {

			// read XMI
			CollectionReader reader = createReader(XmiReader.class,
					XmiReader.PARAM_SOURCE_LOCATION, inputDir,
					XmiReader.PARAM_PATTERNS, new String[] { "[+]*.xmi" },
					XmiReader.PARAM_LANGUAGE, "en");

			AnalysisEngineDescription arffWriter = AnalysisEngineFactory
					.createEngineDescription(WekaArffWriterAnnotator.class,
							WekaArffWriterAnnotator.PARAM_RESET_SEGIDS, false,
							WekaArffWriterAnnotator.PARAM_ARFF_LOCATION,
							arffPath,
							WekaArffWriterAnnotator.PARAM_CLASS_ATTRIBUTE,
							task, WekaArffWriterAnnotator.PARAM_SPARSE_FORMAT,
							true, WekaArffWriterAnnotator.PARAM_OMIT_FEATURES,
							"segment_acl2007_G_verbLemma_.*",
							WekaArffWriterAnnotator.PARAM_TARGET_TYPE,
							"Segment",
							WekaArffWriterAnnotator.PARAM_ESCAPE_VALUES, false);

			runPipeline(reader, arffWriter);

		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		} catch (UIMAException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
