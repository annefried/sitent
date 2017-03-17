package sitent.classifiers;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Set;

import org.apache.log4j.Logger;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class LIBSVMUtils {

	static Logger log = Logger.getLogger(CrfppUtils.class.getName());

	static DecimalFormat df = new DecimalFormat();
	static {
		df.setMaximumFractionDigits(10);
		df.setMinimumFractionDigits(1);
	}

	/**
	 * Writes a Weka Instances data set to a file in LibSVM format.
	 * 
	 * 
	 * @param instances
	 *            A Weka Instances object.
	 * @param path
	 *            Path where to write the LIBSVM input file.
	 * @throws FileNotFoundException
	 */
	public static StringBuffer getLibSVMRepresentation(Instances instances, Set<String> trainDocs)
			throws FileNotFoundException {
		log.info("Getting LIBSVM representation: " + instances.numInstances());
		log.info("Train docs null? " + trainDocs == null);
		int x = 0;
		StringBuffer content = new StringBuffer();

		// Map<String, Set<String>> singleValuedAttributes = new HashMap<String,
		// Set<String>>();

		instancesLoop: for (int j = 0; j < instances.numInstances(); j++) {
			if (j % 100 == 0) {
				log.info("writing input for LIBSVM... " + j);
			}
			String line = "";
			Instance inst = instances.instance(j);

			// get document Id
			Attribute instIdAttr = instances.attribute("instanceid");
			String instId = inst.stringValue(instIdAttr);
			String documentId = instId.substring(0, instId.lastIndexOf("_"));

			if (trainDocs != null) {
				// skip instance if from a document that should not be used in
				// training
				// System.out.println("Document id: " + documentId);
				if (!trainDocs.contains(documentId)) {
					// System.out.println("skipping: " + documentId);
					continue instancesLoop;
				}
				// System.out.println("using: " + documentId);
			}
			
			// class label: value index!
			line += new Double(inst.value(inst.classIndex())).intValue();
			String endOfLine = "";
			
			for (int k = 0; k < instances.numAttributes(); k++) {
				if (k != instances.classIndex()) {
					String val;
					if (instances.attribute(k).isNumeric()) {
						// for "binary" features (1/0):
						if (inst.value(k) == 0) {
							// feature off
							endOfLine += " " + (k +  instances.numAttributes()) + ":1.0";
						}
						else {
							// value should be 1.0
							// feature on
							line += " " + k + ":1.0";
						}
						
						/*
						try {
							val = df.format(inst.value(k));// new
															// BigDecimal(inst.value(k)).toPlainString();
						} catch (NumberFormatException e) {
							val = "0.0";
						}
						line += " " + k + ":" + val;
						*/
					} else {
						if (instances.attribute(k).name().equals("instanceid")) {
							continue;
						}
						
						log.error("LIBSVM can only handle numeric features!");
						log.error("Trying to use feature: " + instances.attribute(k).name());
						throw new IllegalStateException();

					}
				}

			}
			content.append(line + " " + endOfLine + "\n");
			x++;
		}
		log.info("wrote " + x + " lines");
		return content;
	}

}
