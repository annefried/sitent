package sitent.tests;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import org.apache.uima.fit.component.JCasAnnotator_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class ParseWriterAnnotator extends JCasAnnotator_ImplBase {

	public static final String PARAM_OUTPUT_FILE = "outputFile";
	@ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true, defaultValue = "null", description = "File with tense patterns by Thomas Meyer and Sharid Loaiciga.")
	private String outputFile;

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		// open file for this document
		DocumentMetaData meta = JCasUtil.selectSingle(jCas,
				DocumentMetaData.class);
		String baseUri = meta.getDocumentBaseUri();
		String docUri = meta.getDocumentUri();

		String relativeDocumentPath = docUri.substring(baseUri.length());
		relativeDocumentPath = FilenameUtils
				.removeExtension(relativeDocumentPath);

		try {
			PrintWriter w = new PrintWriter(new FileWriter(outputFile + "/"
					+ relativeDocumentPath + ".deps"));

			// iterate over sentences and write out dependency parses
			Iterator<Sentence> sentences = JCasUtil
					.select(jCas, Sentence.class).iterator();

			while (sentences.hasNext()) {

				Sentence sent = sentences.next();
				w.println(sent.getCoveredText());

				List<Dependency> sentDeps = JCasUtil.selectCovered(
						Dependency.class, sent);
				for (Dependency dep : sentDeps) {
					w.println(dep.getGovernor().getCoveredText() + " --"
							+ dep.getDependencyType() + "--> "
							+ dep.getDependent().getCoveredText());
				}

				w.println();
			}

			w.close();

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

	}
}
