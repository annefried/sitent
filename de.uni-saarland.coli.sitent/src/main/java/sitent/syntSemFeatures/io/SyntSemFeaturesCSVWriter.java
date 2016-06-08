package sitent.syntSemFeatures.io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

/**
 * Writes the feature extracted using UIMA to a CSV file in a standoff format.
 * 
 */

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import au.com.bytecode.opencsv.CSVWriter;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import sitent.types.ClassificationAnnotation;
import sitent.types.SEFeature;
import sitent.util.SitEntUimaUtils;

public class SyntSemFeaturesCSVWriter extends JCasAnnotator_ImplBase {

	public static final String PARAM_OUTPUT_FOLDER = "outputFolder";
	@ConfigurationParameter(name = PARAM_OUTPUT_FOLDER, mandatory = true, defaultValue = "null", description = "Output folder for CSV with extracted features (in standoff-format).")
	private String outputFolder;

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		// open file for this document
		DocumentMetaData meta = JCasUtil.selectSingle(jcas, DocumentMetaData.class);
		// DocumentMetaData meta = DocumentMetaData.get(jcas);
		String baseUri = meta.getDocumentBaseUri();
		String docUri = meta.getDocumentUri();

		String relativeDocumentPath = docUri.substring(baseUri.length());
		relativeDocumentPath = FilenameUtils.removeExtension(relativeDocumentPath);

		// collect the headers (done on the fly in order to allow for changes
		// any time)
		Set<String> npFeatures = new HashSet<String>();
		Set<String> verbFeatures = new HashSet<String>();

		for (ClassificationAnnotation classAnnot : JCasUtil.select(jcas, ClassificationAnnotation.class)) {
			Set<String> featureSet = npFeatures;
			if (classAnnot.getTask().equals("VERB")) {
				featureSet = verbFeatures;
			}
			for (Annotation annot : SitEntUimaUtils.getList(classAnnot.getFeatures())) {
				SEFeature seFeat = (SEFeature) annot;
				featureSet.add(seFeat.getName());
			}
		}

		// create ordered lists
		List<String> npFeatureList = new LinkedList<String>(npFeatures);
		List<String> verbFeatureList = new LinkedList<String>(verbFeatures);
		Collections.sort(npFeatureList);
		Collections.sort(verbFeatureList);

		try {
			// write features to files
			CSVWriter npFile = new CSVWriter(new FileWriter(outputFolder + "/" + relativeDocumentPath + "_np.csv"),
					'\t');
			CSVWriter verbFile = new CSVWriter(new FileWriter(outputFolder + "/" + relativeDocumentPath + "_verb.csv"),
					'\t');

			// writer headers
			npFeatureList.add(0, "end");
			npFeatureList.add(0, "begin");
			npFeatureList.add(0, "token");
			verbFeatureList.add(0, "end");
			verbFeatureList.add(0, "begin");
			verbFeatureList.add(0, "token");

			String[] npHeader = new String[npFeatureList.size()];
			npFeatureList.toArray(npHeader);

			String[] verbHeader = new String[verbFeatureList.size()];
			verbFeatureList.toArray(verbHeader);

			npFile.writeNext(npHeader);
			verbFile.writeNext(verbHeader);

			for (ClassificationAnnotation classAnnot : JCasUtil.select(jcas, ClassificationAnnotation.class)) {
				// map feature names to their values for this instances
				Map<String, String> featMap = new HashMap<String, String>();
				for (Annotation annot : SitEntUimaUtils.getList(classAnnot.getFeatures())) {
					SEFeature seFeat = (SEFeature) annot;
					featMap.put(seFeat.getName(), seFeat.getValue());
				}
				featMap.put("token", classAnnot.getCoveredText());
				featMap.put("begin", Integer.toString(classAnnot.getBegin()));
				featMap.put("end", Integer.toString(classAnnot.getEnd()));

				// write line into csv file
				if (classAnnot.getTask().equals("NP")) {
					String[] line = new String[npHeader.length];
					for (int i = 0; i < npHeader.length; i++) {
						line[i] = featMap.get(npHeader[i]);
					}
					npFile.writeNext(line);
				}
				if (classAnnot.getTask().equals("VERB")) {
					String[] line = new String[verbHeader.length];
					for (int i = 0; i < verbHeader.length; i++) {
						line[i] = featMap.get(verbHeader[i]);
					}
					verbFile.writeNext(line);
				}

			}

			// close files
			npFile.close();
			verbFile.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
