package sitent.classifiers;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.Set;

import org.apache.log4j.Logger;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class CrfppUtils {


	static Logger log = Logger.getLogger(CrfppUtils.class.getName());
	
	/**
	 * Writes a Weka Instances data set to a file in CRF++ format.
	 * 
	 * 
	 * @param instances
	 *            A Weka Instances object.
	 * @param path
	 *            Path where to write the CRF++ input file.
	 * @throws FileNotFoundException
	 */
	public static StringBuffer getCrfppRepresentation(Instances instances, Set<String> trainDocs) throws FileNotFoundException {
		log.info("Getting CRFPP representation: " + instances.numInstances());
		log.info("Train docs null? " + trainDocs == null);
		int x = 0;
		StringBuffer content = new StringBuffer();
		
		//Map<String, Set<String>> singleValuedAttributes = new HashMap<String, Set<String>>();
		
		String previousDocId = "";
		
		instancesLoop : for (int j = 0; j < instances.numInstances(); j++) {
			if (j % 100 == 0) {
				log.info("writing input for CRF++... " + j);
			}
			String line = "";
			Instance inst = instances.instance(j);
			
			
			// get document Id
			Attribute instIdAttr = instances.attribute("instanceid");
			String instId = inst.stringValue(instIdAttr);
			String documentId = instId.substring(0, instId.lastIndexOf("_"));
			if (!documentId.equals(previousDocId)) {
				// extra new line to separate documents
				content.append("\n");
				log.info("adding extra newline");
				previousDocId = documentId;
			}
			
			if (trainDocs != null) {
				// skip instance if from a document that should not be used in training
				//System.out.println("Document id: " + documentId);
				if (!trainDocs.contains(documentId)) {
					//System.out.println("skipping: " + documentId);
					continue instancesLoop;
				}
				//System.out.println("using: " + documentId);
			}
			for (int k = 0; k < instances.numAttributes(); k++) {
				if (k != instances.classIndex()) {
					String val;
					if (instances.attribute(k).isNominal()) {
						val = inst.stringValue(k);
					} else {
						try {
							val = new BigDecimal(inst.value(k)).toPlainString();
						} catch (NumberFormatException e) {
							val = "0.0";
						}
					}
					if (val.equals("THE-DUMMY-VALUE")) {
						line += "none\t";
					} else {
						if (instances.attribute(k).isNumeric()) {
							if (val.length() > 11) {
								// Nachkommastellen abschneiden.
								// Stattdessen Logarithmus nehmen? Oder ist das
								// ok so, avoiding overfitting?
								val = val.substring(0, 10);
							}
						}
						line += val + "\t";
					}
				}

			}
			line += inst.stringValue(inst.classIndex()) + "\t" + inst.stringValue(inst.classIndex());
			content.append(line + "\n");
			x++;
		}
		log.info("wrote " + x + " lines");
		return content;
	}

	public static int getCrfppGenreIndex(Instances instances) {
		// Weka indices
		int idx = instances.attribute("document_genre").index();
		int classIdx = instances.classIndex();
		// adapt index to CRFPP (where class is at end)
		if (classIdx < idx) {
			return idx - 1;
		} else {
			return idx;
		}
	}

}
