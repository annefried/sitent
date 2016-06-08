package sitent.util;

import static org.apache.uima.fit.util.JCasUtil.selectSingle;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.EmptyFSList;
import org.apache.uima.jcas.cas.EmptyStringList;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.cas.NonEmptyStringList;
import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import sitent.syntSemFeatures.nounPhrase.NounPhraseFeatures;
import sitent.types.SEFeature;
import sitent.types.SourceDocumentInformation;

public class SitEntUimaUtils {

	/**
	 * Get document ID as in sitent database for a filename.
	 * 
	 * @param jcas
	 * @return
	 */
	public static String getDocid(JCas jcas) {
		try {
			SourceDocumentInformation sdi = selectSingle(jcas, SourceDocumentInformation.class);
			return sdi.getDocId();
		} catch (IllegalArgumentException e) {
			System.err.println("CAS does not contain any [sitent.types.SourceDocumentInformation]");
			// try to find MetaData info
			DocumentMetaData dm = selectSingle(jcas, DocumentMetaData.class);
			return dm.getDocumentId();
		}
	}

	/**
	 * Adds an item to a FSList.
	 * 
	 * @param fsList
	 * @param newItem
	 * @param jcas
	 * @return
	 */
	public static NonEmptyFSList addToFSList(FSList fsList, TOP newItem, JCas jcas) {
		NonEmptyFSList retVal = new NonEmptyFSList(jcas);
		retVal.setHead(newItem);
		retVal.setTail(fsList);
		return retVal;
	}

	/**
	 * Adds an item to a FSList.
	 * 
	 * @param fsList
	 * @param newItem
	 * @param jcas
	 * @return
	 */
	public static NonEmptyStringList addToStringList(StringList sList, String newString, JCas jcas) {
		NonEmptyStringList retVal = new NonEmptyStringList(jcas);
		retVal.setHead(newString);
		retVal.setTail(sList);
		return retVal;
	}

	/**
	 * Checks whether an FSList is empty or not.
	 * 
	 * @param sList
	 * @return
	 */
	public static boolean isEmpty(FSList fsList) {
		if (fsList instanceof EmptyFSList) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks whether a StringList is empty or not.
	 * 
	 * @param sList
	 * @return
	 */
	public static boolean isEmpty(StringList sList) {
		if (sList instanceof EmptyStringList) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the StringList as a normal list.
	 * 
	 * @param sList
	 * @return
	 */
	public static List<String> getList(StringList sList) {
		List<String> list = new LinkedList<String>();
		while (sList instanceof NonEmptyStringList) {
			String head = ((NonEmptyStringList) sList).getHead();
			list.add(head);
			sList = ((NonEmptyStringList) sList).getTail();
		}
		return list;
	}

	/**
	 * Returns the FSList as a normal list.
	 * 
	 * @param sList
	 * @return
	 */
	public static List<Annotation> getList(FSList fsList) {
		List<Annotation> list = new LinkedList<Annotation>();
		while (fsList instanceof NonEmptyFSList) {
			Annotation head = (Annotation) ((NonEmptyFSList) fsList).getHead();
			list.add(head);
			fsList = ((NonEmptyFSList) fsList).getTail();
		}
		return list;
	}

	/**
	 * Returns the FSList as a normal list.
	 * 
	 * @param sList
	 * @return
	 */
	public static List<TOP> getTopList(FSList fsList) {
		List<TOP> list = new LinkedList<TOP>();
		while (fsList instanceof NonEmptyFSList) {
			TOP head = (TOP) ((NonEmptyFSList) fsList).getHead();
			list.add(head);
			fsList = ((NonEmptyFSList) fsList).getTail();
		}
		return list;
	}

	/**
	 * checks whether covering covers covered.
	 * 
	 * @param covering
	 * @param covered
	 * @return
	 */
	public static boolean covers(Annotation covering, Annotation covered) {
		if (covering.getBegin() <= covered.getBegin() && covering.getEnd() >= covered.getEnd()) {
			return true;
		}
		return false;
	}

	/**
	 * checks whether covering covers covered.
	 * 
	 * @param covering
	 * @param covered
	 * @return
	 */
	public static boolean coversLarger(Annotation covering, Annotation covered) {
		if (covering.getBegin() <= covered.getBegin() && covering.getEnd() >= covered.getEnd()) {
			if (covering.getBegin() != covered.getBegin() || covering.getEnd() != covered.getEnd()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes the SEFeature with the given featName from the FSList.
	 * 
	 * @param fsList
	 * @param featName
	 * @param jcas
	 * @return
	 */
	public static FSList removeSEFeatureFromList(FSList fsList, String featName, JCas jcas) {
		FSList retVal = new EmptyFSList(jcas);
		for (Annotation annot : getList(fsList)) {
			SEFeature feat = (SEFeature) annot;
			if (!feat.getName().equals(featName)) {
				retVal = addToFSList(retVal, feat, jcas);
			}
		}
		return retVal;
	}

	public static Token getHead(Annotation annotation, JCas jcas) {
		// find covered tokens and dependencies
		List<Token> tokens = JCasUtil.selectCovered(Token.class, annotation);
		if (tokens.size() == 0) {
			// in some rare cases, the named entity mention does not span an
			// entire Token,
			// e.g. annotation of "California" in "California-based"
			tokens = JCasUtil.selectCovering(Token.class, annotation);
		}

		List<Dependency> deps = JCasUtil.selectCovered(Dependency.class, annotation);
		Set<Token> tokenSet = new HashSet<Token>(tokens);
		// find the head of the tokens
		// remove tokens not in the dependencies: have been collapsed.
		for (Token token : tokens) {
			boolean foundDep = false;
			for (Dependency dep : deps) {

				if (dep.getDependent() == dep.getGovernor()) {
					// occurred for conj_and
					continue;
				}

				// relative clause modifier creates cycles
				if (dep.getDependencyType().equals("rcmod")) {
					continue;
				}

				if (dep.getGovernor() == token) {
					foundDep = true;
				}
				if (dep.getDependent() == token) {
					foundDep = true;
					if (tokens.contains(dep.getGovernor())) {
						tokenSet.remove(token);
					}
				}
			}
			if (!foundDep && deps.size() > 0) {
				tokenSet.remove(token);
			}
		}
		if (tokenSet.size() > 1) {
			System.err.println("could not identify head: " + annotation.getCoveredText());
			System.err.println(tokenSet.size());
			for (Token t : tokenSet) {
				System.err.println("token: " + t.getCoveredText());
			}
			for (Dependency dep : deps) {
				System.err.println(dep.getGovernor().getCoveredText() + " --> " + dep.getDependent().getCoveredText());
			}
			// throw new RuntimeException();
		}
		if (tokenSet.isEmpty()) {
			System.err.println("SitEntUimaUtils: No head found.");
			for (Dependency dep : deps) {
				System.err.println(dep.getGovernor().getCoveredText() + "(" + dep.getGovernor().getBegin() + ") --> "
						+ dep.getDependencyType() + "-->" + dep.getDependent().getCoveredText() + " ("
						+ dep.getDependent().getBegin() + ")");
			}
			return null;
		}
		Iterator<Token> it = tokenSet.iterator();
		Token head = it.next();
		while (!NounPhraseFeatures.isNounOrPronoun(head) && it.hasNext()) {
			head = it.next();
		}
		// if in doubt, returns last token.
		return head;
	}

	/**
	 * Returns true if the first annotation covers the second, and false
	 * otherwise.
	 * 
	 * @param covering
	 * @param covered
	 * @param jcas
	 * @return
	 */
	public static boolean covers(Annotation covering, Annotation covered, JCas jcas) {
		List<? extends Annotation> allCovered = JCasUtil.selectCovering(jcas, covered.getClass(), covering);
		return allCovered.contains(covered);
	}

	/**
	 * This method returns true if a 'true overlap' exists: annot1 starts equal
	 * to or before annot2, and annot1 ends equals to or before annot2.
	 * 
	 * @param annot1
	 * @param annot2
	 * @return
	 */
	public static boolean overlapsDirected(Annotation annot1, Annotation annot2) {
		if (annot1.getBegin() <= annot2.getBegin() && annot1.getEnd() <= annot2.getEnd()) {
			return true;
		}
		return false;
	}

	/**
	 * This method computes whether annot1 and annot2 overlap in any way.
	 * 
	 * @param annot1
	 * @param annot2
	 * @param jCas
	 * @return
	 */
	public static boolean overlaps(Annotation annot1, Annotation annot2, JCas jCas) {
		if (overlapsDirected(annot1, annot2) || overlapsDirected(annot2, annot1) || covers(annot1, annot2, jCas)
				|| covers(annot2, annot1, jCas)) {
			return true;
		}
		return false;
	}

	/**
	 * return a list with all the 'leaf' directories (which do not contain any
	 * directories).
	 * 
	 * @param dir
	 * @return
	 */
	public static List<File> getLeafDirectories(File dir) {
		List<File> retVal = new LinkedList<File>();
		boolean subDirFound = false;
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				subDirFound = true;
				retVal.addAll(getLeafDirectories(f));
			}
		}
		if (!subDirFound) {
			retVal.add(dir);
		}
		return retVal;
	}

}
