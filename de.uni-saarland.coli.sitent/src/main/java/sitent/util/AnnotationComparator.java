package sitent.util;

/**
 * @author anne
 * Orders annotation by their begin.
 */

import java.util.Comparator;

import org.apache.uima.jcas.tcas.Annotation;

public class AnnotationComparator implements Comparator<Annotation> {

	public int compare(Annotation a1, Annotation a2) {
		return a1.getBegin() - a2.getBegin();
	}

}
