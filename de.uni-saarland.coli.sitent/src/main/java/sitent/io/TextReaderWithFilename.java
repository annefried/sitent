package sitent.io;

/**
 * @author anne
 * 
 * Based on: de.tudarmstadt.ukp.dkpro.core.io.text.TextReader
 *
 * Adds the functionality to get information about the source document (filename).
 */

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import com.ibm.icu.text.CharsetDetector;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import sitent.types.SourceDocumentInformation;

/**
 * UIMA collection reader for plain text files.
 * 
 * @author Richard Eckart de Castilho
 */
public class TextReaderWithFilename extends ResourceCollectionReaderBase {
	/**
	 * Automatically detect encoding.
	 * 
	 * @see CharsetDetector
	 */
	public static final String ENCODING_AUTO = "auto";

	/**
	 * Name of configuration parameter that contains the character encoding used
	 * by the input files.
	 */
	public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String encoding;

	public void getNext(CAS aCAS) throws IOException, CollectionException {
		Resource res = nextFile();
		initCas(aCAS, res);

		InputStream is = null;

		try {

			URI uri = res.getResolvedUri();
			String path[] = uri.getPath().split("/");
			System.out.println(uri.getPath());
			String filename = path[path.length - 1];
			
			is = new BufferedInputStream(res.getInputStream());
			if (ENCODING_AUTO.equals(encoding)) {
				CharsetDetector detector = new CharsetDetector();
				String docText = IOUtils.toString(detector.getReader(is, null));
				docText = stripXML(docText, filename);
				aCAS.setDocumentText(docText);
			} else {
				String docText = IOUtils.toString(is, encoding);
				docText = stripXML(docText, filename);
				aCAS.setDocumentText(docText);
			}

			JCas jcas = aCAS.getJCas();
			SourceDocumentInformation sdi = new SourceDocumentInformation(jcas);
			sdi.setAbsolutePath(uri.getPath());
			sdi.setDocId(filename);
			sdi.addToIndexes();

		} catch (CASException e) {
			e.printStackTrace();
			System.err.println("Error retrieving JCas from CAS.");
			throw new RuntimeException();
		} finally {
			closeQuietly(is);
		}
	}

	/**
	 * Strips of the XML in case of jokes texts.
	 * 
	 * @param docText
	 * @param filename
	 * @return
	 */
	private String stripXML(String docText, String filename) {
		if (filename.matches("masc_jokes_jokes\\d+\\.txt")) {
			docText = docText.replaceAll("<[^>]+>", "");
		}
		return docText;
	}

}
