package sitent.classifiers;

import java.io.FileNotFoundException;
import java.util.Set;

import org.apache.log4j.Logger;

import weka.core.Instances;

public class CrfSuiteUtils {
	
static Logger log = Logger.getLogger(CrfppUtils.class.getName());
	
	/**
	 * Writes a Weka Instances data set to a file in CrfSuite format.
	 * 
	 * 
	 * @param instances
	 *            A Weka Instances object.
	 * @param path
	 *            Path where to write the CrfSuite input file.
	 *            
	 * @throws FileNotFoundException
	 */
	public static StringBuffer getCrfSuiteRepresentation(Instances instances, Set<String> trainDocs) throws FileNotFoundException {
		log.info("Getting CRFPP representation: " + instances.numInstances());
		log.info("Train docs null? " + trainDocs == null);
		
		
		return null;
	}
	
	

}
