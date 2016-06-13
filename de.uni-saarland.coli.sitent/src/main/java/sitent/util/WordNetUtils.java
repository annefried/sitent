/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 * 
 * Parts of this code are from https://svn.apache.org/repos/asf/ctakes/trunk/ctakes-relation-extractor/src/main/java/org/apache/ctakes/relationextractor/ae/features/WordNetUtils.java
 * 
 * Modifications were done by the author of this software.
 * @author afried (Annemarie Friedrich)
 * 
 */

package sitent.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.WordnetStemmer;
import sitent.types.ClassificationAnnotation;

/**
 * This is a wrapper for the MIT WordNet inteface that simplifies basic
 * operations such as retrieving synonyms and hypernyms for a word.
 * 
 * @author dmitriy dligach
 * 
 */
public class WordNetUtils {

	/**
	 * A simple way to get the head word of a phrase.
	 */
	public static String getHeadWord(String s) {

		String[] elements = s.split(" ");
		return elements[elements.length - 1];
	}

	/**
	 * Initialize WordNet dictionary.
	 */
	public static IDictionary getDictionary(String wordNetPath) throws IOException {

		URL url = new URL("file", null, wordNetPath);
		IDictionary iDictionary = new Dictionary(url);
		iDictionary.open();

		return iDictionary;
	}

	/**
	 * Get a list of possible stems. Assume we are looking up a noun.
	 */
	public static List<String> getStems(String word, String posTag, IDictionary iDictionary) {

		POS pos = POS.getPartOfSpeech(posTag.charAt(0));
		if (pos == null) {
			return new ArrayList<String>();
		}

		WordnetStemmer wordnetStemmer = new WordnetStemmer(iDictionary);
		List<String> stems = wordnetStemmer.findStems(word, pos);

		return stems;
	}

	/**
	 * Retrieve a set of synonyms for a word. Use only the first sense if
	 * useFirstSense flag is true.
	 */
	public static List<ISynset> getSynsets(IDictionary iDictionary, String word, String posTag,
			boolean firstSenseOnly) {

		// need a set to avoid repeating words
		List<ISynset> synonyms = new LinkedList<ISynset>();

		POS pos = POS.getPartOfSpeech(posTag.charAt(0));
		if (pos == null) {
			return synonyms;
		}

		IIndexWord iIndexWord = iDictionary.getIndexWord(word, pos);
		if (iIndexWord == null) {
			return synonyms; // no senses found
		}

		// iterate over senses
		for (IWordID iWordId : iIndexWord.getWordIDs()) {
			IWord iWord = iDictionary.getWord(iWordId);

			ISynset iSynset = iWord.getSynset();
			synonyms.add(iSynset);

			if (firstSenseOnly) {
				break;
			}
		}

		return synonyms;
	}

	/**
	 * Retrieve a set of synonyms for a word. Use only the first sense if
	 * useFirstSense flag is true.
	 */
	public static HashSet<String> getSynonyms(IDictionary iDictionary, String word, String posTag,
			boolean firstSenseOnly) {

		// need a set to avoid repeating words
		HashSet<String> synonyms = new HashSet<String>();

		POS pos = POS.getPartOfSpeech(posTag.charAt(0));
		if (pos == null) {
			return synonyms;
		}

		IIndexWord iIndexWord = iDictionary.getIndexWord(word, pos);
		if (iIndexWord == null) {
			return synonyms; // no senses found
		}

		// iterate over senses
		for (IWordID iWordId : iIndexWord.getWordIDs()) {
			IWord iWord = iDictionary.getWord(iWordId);

			ISynset iSynset = iWord.getSynset();
			for (IWord synsetMember : iSynset.getWords()) {
				synonyms.add(synsetMember.getLemma());
			}

			if (firstSenseOnly) {
				break;
			}
		}

		return synonyms;
	}

	/**
	 * Retrieve a set of hypernyms for a word. Use only the first sense if
	 * useFirstSense flag is true.
	 */
	public static HashSet<String> getHypernyms(IDictionary dict, String word, String posTag, boolean firstSenseOnly) {

		HashSet<String> hypernyms = new HashSet<String>();

		POS pos = POS.getPartOfSpeech(posTag.charAt(0));
		if (pos == null) {
			return hypernyms;
		}

		IIndexWord iIndexWord = dict.getIndexWord(word, pos);
		if (iIndexWord == null) {
			return hypernyms; // no senses found
		}

		// iterate over senses
		for (IWordID iWordId : iIndexWord.getWordIDs()) {
			IWord iWord1 = dict.getWord(iWordId);
			ISynset iSynset = iWord1.getSynset();

			// multiple hypernym chains are possible for a synset
			for (ISynsetID iSynsetId : iSynset.getRelatedSynsets(Pointer.HYPERNYM)) {
				List<IWord> iWords = dict.getSynset(iSynsetId).getWords();
				for (IWord iWord2 : iWords) {
					String lemma = iWord2.getLemma();
					hypernyms.add(lemma.replace(' ', '_')); // also get rid of
															// spaces
				}
			}

			if (firstSenseOnly) {
				break;
			}
		}

		return hypernyms;
	}

	public static HashSet<String> getHyperHypernyms(IDictionary dict, String word, String posTag,
			boolean firstSenseOnly) {

		HashSet<String> hypernyms = new HashSet<String>();

		POS pos = POS.getPartOfSpeech(posTag.charAt(0));
		if (pos == null) {
			return hypernyms;
		}

		IIndexWord iIndexWord = dict.getIndexWord(word, pos);
		if (iIndexWord == null) {
			return hypernyms; // no senses found
		}

		// iterate over senses
		for (IWordID iWordId : iIndexWord.getWordIDs()) {
			IWord iWord1 = dict.getWord(iWordId);
			ISynset iSynset = iWord1.getSynset();

			for (ISynsetID iSynsetId1 : iSynset.getRelatedSynsets(Pointer.HYPERNYM)) {
				for (ISynsetID iSynsetId2 : dict.getSynset(iSynsetId1).getRelatedSynsets(Pointer.HYPERNYM)) {
					List<IWord> iWords = dict.getSynset(iSynsetId2).getWords();
					for (IWord iWord2 : iWords) {
						String lemma = iWord2.getLemma();
						hypernyms.add(lemma.replace(' ', '_')); // also get rid
																// of spaces
					}
				}
			}

			if (firstSenseOnly) {
				break;
			}
		}

		return hypernyms;
	}

	public static Boolean isHypernym(IDictionary dict, String word, String posTag, String hypoWord,
			boolean firstSenseOnly) {
		List<ISynset> synsets = getSynsets(dict, hypoWord, posTag, firstSenseOnly);
		for (ISynset synset : synsets) {
			if (isHypernym(dict, hypoWord, posTag, synset, firstSenseOnly)) {
				// System.out.println("hypernym found: " + synset.getGloss() +
				// word );
				return true;
			}
		}
		//System.out.println("not a hypo: " + word);
		return false;
	}

	/**
	 * written by anne
	 * 
	 * @param dict
	 * @param word
	 * @param posTag
	 * @param firstSenseOnly
	 * @return
	 */
	public static Boolean isHypernym(IDictionary dict, String word, String posTag, ISynset hypernym,
			boolean firstSenseOnly) {

		POS pos = POS.getPartOfSpeech(posTag.charAt(0));
		if (pos == null) {
			return false;
		}

		IIndexWord iIndexWord = dict.getIndexWord(word, pos);
		if (iIndexWord == null) {
			return false; // no senses found
		}

		// iterate over senses
		for (IWordID iWordId : iIndexWord.getWordIDs()) {
			IWord iWord1 = dict.getWord(iWordId);
			ISynset iSynset = iWord1.getSynset();

			if (iSynset.equals(hypernym)) {
				return true;
			}

			for (ISynsetID iSynsetId : iSynset.getRelatedSynsets(Pointer.HYPERNYM)) {
				ISynset hyperSynset = dict.getSynset(iSynsetId);
				if (isHypernym(dict, hyperSynset, hypernym, firstSenseOnly)) {
					return true;
				}
			}

			if (firstSenseOnly) {
				break;
			}
		}

		return false;
	}

	/**
	 * written by anne
	 * 
	 * @param dict
	 * @param word
	 * @param posTag
	 * @param firstSenseOnly
	 * @return
	 */
	public static Boolean isHypernym(IDictionary dict, ISynset synset, ISynset hypernym, boolean firstSenseOnly) {

		if (synset.equals(hypernym)) {
			return true;
		}

		for (ISynsetID iSynsetId : synset.getRelatedSynsets(Pointer.HYPERNYM)) {
			ISynset hyperSynset = dict.getSynset(iSynsetId);
			if (isHypernym(dict, hyperSynset, hypernym, firstSenseOnly)) {
				return true;
			}
		}

		return false;
	}

	public static List<ISynset> getRoots(IDictionary dict, String posTag) {

		POS pos = POS.getPartOfSpeech(posTag.charAt(0));
		if (pos == null) {
			return null;
		}

		List<ISynset> roots = new LinkedList<ISynset>();

		if (pos == POS.NOUN) {
			roots.add(WordNetUtils.getSynsets(dict, "person", "n", true).get(0));
			roots.add(WordNetUtils.getSynsets(dict, "thing", "n", true).get(0));
			ISynset entity = WordNetUtils.getSynsets(dict, "entity", "n", true).get(0);
			for (ISynsetID id : entity.getRelatedSynsets(Pointer.HYPONYM)) {
				roots.add(dict.getSynset(id));
			}
			return roots;
		}

		// get all synsets of the given POS, choose the ones that don't have a
		// hypernym
		Iterator<ISynset> it = dict.getSynsetIterator(pos);
		while (it.hasNext()) {
			ISynset s = it.next();
			if (s.getRelatedSynsets(Pointer.HYPERNYM).size() == 0
					&& s.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE).isEmpty()) {
				// if (pos == POS.NOUN) {
				// // use some hyponyms as well (only entity fulfills this
				// condition)
				// for (ISynsetID hypo1 : s.getRelatedSynsets(Pointer.HYPONYM))
				// {
				// ISynset hypoSynset = dict.getSynset(hypo1);
				// for (ISynsetID hypo2 :
				// hypoSynset.getRelatedSynsets(Pointer.HYPONYM)) {
				// ISynset hypoSynset2 = dict.getSynset(hypo2);
				// for (ISynsetID hypo3 :
				// hypoSynset2.getRelatedSynsets(Pointer.HYPONYM)) {
				// ISynset hypoSynset3 = dict.getSynset(hypo3);
				// roots.add(hypoSynset3);
				// System.out.println(hypoSynset3.getID().toString() + " " +
				// hypoSynset3.getWords() + " "
				// + hypoSynset3.getGloss());
				// }
				// }
				// }
				//
				// }
				// else {
				roots.add(s);
				// }

			}
		}

		return roots;

	}

	/**
	 * test
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		IDictionary dict = WordNetUtils.getDictionary("resources/wordnet3.0");

		ISynset hypernym = getSynsets(dict, "act", "VERB", false).get(0);
		System.out.println(hypernym.getGloss());

		boolean b = isHypernym(dict, "exist", "VERB", hypernym, false);
		System.out.println(b);

		System.out.println(isHypernym(dict, "exist", "VERB", "buy", false));

		// getRoots(dict);
		// System.out.println(getRoots(dict).size());

		ISynset mother = getSynsets(dict, "mother", "N", false).get(0);
		System.out.println(mother);
		ISynset person = getSynsets(dict, "person", "N", false).get(0);
		System.out.println(person);
		System.out.println(isHypernym(dict, mother, person, false));

		System.out.println(getRoots(dict, "N").size());
		for (ISynset s : getRoots(dict, "N")) {
			System.out.println("Noun root: " + s);
		}

	}

	/**
	 * JWI POS tag from Penn tag. (by Nils Reiter)
	 * 
	 * @param postag
	 * @return
	 */
	public static POS getPOSFromPenn(String postag) {
		POS pos = null;
		switch (postag.charAt(0)) {
		case 'J':
		case 'j':
			pos = POS.ADJECTIVE;
			break;
		case 'N':
		case 'n':
			pos = POS.NOUN;
			break;
		case 'V':
		case 'v':
			pos = POS.VERB;
			break;
		case 'R':
		case 'r':
			if (postag.charAt(1) == 'B' || postag.charAt(1) == 'b')
				pos = POS.ADVERB;
			break;
		}
		return pos;
	}

	/**
	 * @author afried, Annemarie Friedrich
	 * 
	 *         WordNet based features, use Most Frequent Sense heuristic.
	 *         Adapted from code by Nils Reiter.
	 * 
	 * @param token
	 * @param classAnnot
	 */
	public static void setWordNetFeatures(Token token, ClassificationAnnotation classAnnot, JCas jCas,
			String featurePrefix, IDictionary wordnet) {
		if (token.getPos() != null && token.getLemma() != null) {

			POS pos = WordNetUtils.getPOSFromPenn(token.getPos().getPosValue());
			if (pos != null) {
				IIndexWord iw = wordnet.getIndexWord(token.getLemma().getValue(), pos);

				if (iw != null) {
					ISynsetID mfs = iw.getWordIDs().get(0).getSynsetID();
					ISynset synset = wordnet.getSynset(mfs);

					// lexcial filename:
					FeaturesUtil.addFeature(featurePrefix + "wnLexicalFilename", synset.getLexicalFile().getName(),
							jCas, classAnnot);
					int gran = 0;
					ISynset curr = synset;
					Set<ISynset> seen = new HashSet<ISynset>();
					while (!seen.contains(curr) && !curr.getRelatedSynsets(Pointer.HYPERNYM).isEmpty()) {
						seen.add(curr);
						// The substring operation removes the leading 'SID-'
						String senseId = curr.getID().toString().substring(4);
						if (gran == 0) {
							FeaturesUtil.addFeature(featurePrefix + "sense0", senseId, jCas, classAnnot);
						} else if (gran == 1) {
							FeaturesUtil.addFeature(featurePrefix + "sense1", senseId, jCas, classAnnot);
						} else if (gran == 2) {
							FeaturesUtil.addFeature(featurePrefix + "sense2", senseId, jCas, classAnnot);
						} else if (gran == 3) {
							FeaturesUtil.addFeature(featurePrefix + "sense3", senseId, jCas, classAnnot);
						}
						curr = wordnet.getSynset(curr.getRelatedSynsets(Pointer.HYPERNYM).get(0));
						gran++;
					}
					FeaturesUtil.addFeature(featurePrefix + "wnGranularity", new Integer(gran).toString(), jCas,
							classAnnot);
					// curr must now refer to the top sense

					FeaturesUtil.addFeature(featurePrefix + "senseTop", curr.getID().toString().substring(4), jCas,
							classAnnot);
				}
			}
		}
	}
	
	/**
	 * Returns WordNet lexical filename for the lemma of the token, or POS if
	 * none is available. No word sense disambiguation applied here, simply uses
	 * most frequent sense.
	 * 
	 * TODO: used to return lemma in more cases?
	 * 
	 * @param token
	 * @return
	 */
	public static String getLexNameOrPOS(Token token, IDictionary dict) {
		if (token.getPos().getPosValue().equals("PRP")) {
			if (token.getLemma().getValue().matches("you|I|we|he|she|they")) {
				return "noun.person";
			}
			return token.getPos().getPosValue();
		}
		if (token.getPos().getPosValue().equals("DT")) {
			return token.getLemma().getValue();
		}

		// uses first sense only
		List<ISynset> synsets = WordNetUtils.getSynsets(dict, token.getLemma()
				.getValue(), token.getPos().getPosValue(), true);
		if (synsets.isEmpty()) {
			if (token.getLemma().getValue().startsWith("NN")) {
				// assume person for all names (approximation)
				return "noun.person";
			}
			return token.getPos().getPosValue();
		}
		ISynset synset = synsets.get(0);

		String lexName = synset.getLexicalFile().getName();

		return lexName;
	}

}
