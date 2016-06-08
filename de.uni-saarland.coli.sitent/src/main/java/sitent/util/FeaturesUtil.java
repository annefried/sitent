package sitent.util;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.EmptyFSList;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;

import sitent.types.ClassificationAnnotation;
import sitent.types.SEFeature;

public class FeaturesUtil {

	public static void addFeature(String name, String value, JCas jcas, ClassificationAnnotation segment) {

		SEFeature feat = new SEFeature(jcas);
		feat.setName(name);
		feat.setValue(value);
		feat.addToIndexes();
		segment.setFeatures(SitEntUimaUtils.addToFSList(segment.getFeatures(), feat, jcas));
	}

	/**
	 * A convenience method for printing the features and values of a
	 * ClassificationAnnotation to the console. (Just for debugging.)
	 * 
	 * @param classAnnot
	 *            ClassificationAnnotation instance, features and values are
	 *            printed for inspection.
	 */
	public static void printFeatures(ClassificationAnnotation classAnnot) {

		for (Annotation annot : SitEntUimaUtils.getList(classAnnot.getFeatures())) {
			SEFeature feat = (SEFeature) annot;
			System.out.println("\t" + feat.getName() + "\t" + feat.getValue());
		}
	}

	/**
	 * Returns the value of a particular feature
	 * 
	 * @param name
	 * @param classAnnot
	 * @return
	 */
	public static String getFeatureValue(String name, ClassificationAnnotation classAnnot) {
		List<Annotation> features = SitEntUimaUtils.getList(classAnnot.getFeatures());
		for (Annotation annot : features) {
			SEFeature feat = (SEFeature) annot;
			if (feat.getName().equals(name)) {
				return feat.getValue();
			}
		}
		return null;
		//throw new IllegalStateException("Feature not found! " + name);
	}
	
	/**
	 * Removes all features with the given prefix from the
	 * ClassificationAnnotation object.
	 * 
	 * @param prefix
	 * @param classAnnot
	 * @param jCas
	 */
	public static void removeFeature(String prefix,
			ClassificationAnnotation classAnnot, JCas jCas) {
		FSList fsList = classAnnot.getFeatures();
		FSList retVal = new EmptyFSList(jCas);
		for (Annotation annot : SitEntUimaUtils.getList(fsList)) {
			SEFeature feat = (SEFeature) annot;
			if (!feat.getName().matches(prefix)) {
				retVal = SitEntUimaUtils.addToFSList(retVal, feat, jCas);
			}
		}
		classAnnot.setFeatures(retVal);
	}

}
