package sitent.syntSemFeatures.segment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.UimaContext;
/**
 * Implements features that are the same for all segments of a document.
 * A selection of features which have been shown to capture genre/style in other work.
 * - statistics over POS tags
 * - function word frequencies
 * - topics? for paragraphs?
 * - character 4-grams (presence/absence) -- maybe too sparse here.
 */
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import sitent.types.Passage;
import sitent.util.FeaturesUtil;
import sitent.util.SitEntUtils;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class GlobalFeaturesAnnotator extends JCasAnnotator_ImplBase {

	private static final int NUM_SENTENCES_PER_PASSAGE = 5;
	private static final int MAX_NUM_SENTENCES_PER_PARAGRAPH = 10;

	public static final String PARAM_STOP_WORDS = "stopWordsFile";
	@ConfigurationParameter(name = PARAM_STOP_WORDS, mandatory = true, defaultValue = "null", description = "File with stopwords (Snowball).")
	private String stopWordsFile;

	private Set<String> stopWords;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);

		stopWords = new HashSet<String>();

		try {
			BufferedReader r = new BufferedReader(new FileReader(stopWordsFile));
			String line;
			while ((line = r.readLine()) != null) {
				if (line.startsWith(" ") || line.startsWith("\t")) {
					continue;
				} else {
					String word = line.split(" ")[0];
					if (!word.equals("")) {
						stopWords.add(word);
					}
				}
			}
			r.close();
			// System.out.println("Read the following stop words:");
			// for (String word : stopWords) {
			// System.out.println(word);
			// }
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceInitializationException();
		}

	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		DocumentMetaData dm = JCasUtil.selectSingle(jCas,
				DocumentMetaData.class);
		String genre = dm.getDocumentId().split("_")[0];
		String docId = dm.getDocumentId();

		// split text into passages
		Integer passageIdx = 0;

		// take line breaks into account (Paragraphs)
		Collection<Paragraph> paragraphs = JCasUtil.select(jCas,
				Paragraph.class);
		for (Paragraph paragraph : paragraphs) {

			//System.out.println("P: " + paragraph.getCoveredText());

			Collection<Sentence> sentences = JCasUtil.selectCovered(
					Sentence.class, paragraph);

			if (sentences.size() > MAX_NUM_SENTENCES_PER_PARAGRAPH) {
				// if more than 10 sentences, split the paragraph into smaller
				// passages

				int s = 0;
				Integer begin = null;
				Integer end = null;

				for (Sentence sentence : sentences) {
					if (s > 0 && s % NUM_SENTENCES_PER_PASSAGE == 0) {
						// add a new passage annotation
						Passage passage = new Passage(jCas);
						passage.setBegin(begin);
						passage.setEnd(sentence.getEnd());
						passage.setPassageId(docId + "_"
								+ (passageIdx++).toString());
						passage.addToIndexes();
						begin = null;
					} else {
						if (begin == null) {
							begin = sentence.getBegin();
						}
						end = sentence.getEnd();
					}
					s++;
				}
				if (begin != null) {
					// add a new passage annotation
					Passage passage = new Passage(jCas);
					passage.setBegin(begin);
					passage.setEnd(end);
					passage.setPassageId(docId + "_"
							+ (passageIdx++).toString());
					passage.addToIndexes();
				}
			} else {
				// if 10 or less sentences, use the entire paragraph as a
				// passage.
				Passage passage = new Passage(jCas);
				passage.setBegin(paragraph.getBegin());
				passage.setEnd(paragraph.getEnd());
				passage.setPassageId(docId + "_" + (passageIdx++).toString());
				passage.addToIndexes();
			}
		}

		// collect statistics per Passage: add as features.
		Collection<Passage> passages = JCasUtil.select(jCas, Passage.class);

		for (Passage passage : passages) {

			// add genre as substitute class for now
			// can use discourse mode later
			FeaturesUtil.addFeature("class_genre", genre, jCas, passage);

			// identifier of passage as feature
			FeaturesUtil.addFeature("passageId", passage.getPassageId(), jCas,
					passage);

			// Statistics over POS tags: how often used in the document?
			HashMap<String, Double> posCounts = new HashMap<String, Double>();
			double total = 0;
			// Statistics over function words: how often used in the document?
			HashMap<String, Double> stopWordcounts = new HashMap<String, Double>();
			double totalTokens = 0;
			// Lemmas
			HashSet<String> lemmas = new HashSet<String>();

			Collection<Token> tokens = JCasUtil.selectCovered(Token.class,
					passage);
			totalTokens += tokens.size();
			for (Token token : tokens) {
				if (token.getPos() != null) {
					String pos = token.getPos().getPosValue();
					SitEntUtils.incrementMapForKeyDouble(posCounts, pos);
					total++;
				}
				if (stopWords.contains(token.getCoveredText())) {
					SitEntUtils.incrementMapForKeyDouble(stopWordcounts,
							token.getCoveredText());
				}
				if (token.getLemma() != null) {
					lemmas.add(token.getLemma().getValue());
				}
			}

			// Normalize per passage.
			for (String pos : posCounts.keySet()) {
				posCounts.put(pos, posCounts.get(pos) / total);
			}
			for (String stopWord : stopWordcounts.keySet()) {
				stopWordcounts.put(stopWord, stopWordcounts.get(stopWord)
						/ totalTokens);
			}
			// add as feature to passage
			for (String pos : posCounts.keySet()) {
				FeaturesUtil.addFeature("passage_pos_" + pos, posCounts
						.get(pos).toString(), jCas, passage);
			}
			/*
			for (String stopWord : stopWordcounts.keySet()) {
				FeaturesUtil.addFeature("passage_stopWord_" + stopWord,
						stopWordcounts.get(stopWord).toString(), jCas, passage);
			}
			*/
			for (String lemma : lemmas) {
				// use only alphanumeric lemmas
				if (lemma.matches("[A-Za-z]+")) {

					FeaturesUtil.addFeature(
							"passage_lemma_" + lemma.toLowerCase(), "1", jCas,
							passage);
				}
			}

		}

	}

}
