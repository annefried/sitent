package sitent.syntSemFeatures.segment;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import au.com.bytecode.opencsv.CSVReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import sitent.types.Passage;
import sitent.types.SEFeature;
import sitent.types.Segment;
import sitent.util.FeaturesUtil;
import sitent.util.SitEntUimaUtils;

public class AddParagraphClusterFeatures extends JCasAnnotator_ImplBase {

	public static final String PARAM_CLUSTER_FILE = "clusterFile";
	@ConfigurationParameter(name = PARAM_CLUSTER_FILE, mandatory = true, defaultValue = "null", description = "Location for file with cluster assignments.")
	private String clusterFile;

	private Map<String, String[]> passageToAssignments;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		// read in clusters file
		try {
			passageToAssignments = new HashMap<String, String[]>();

			CSVReader csvR = new CSVReader(new FileReader(clusterFile), '\t');
			String[] row = null;
			while ((row = csvR.readNext()) != null) {
				String passageID = row[0];
				for (int i = 1; i < row.length; i++) {
					passageToAssignments.put(passageID, row);
				}
			}
			csvR.close();

		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceInitializationException();
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		// add passage cluster features to each Segment instance
		Collection<Passage> passages = JCasUtil.select(jCas, Passage.class);
		for (Passage passage : passages) {
			Collection<Segment> segments = JCasUtil.selectCovered(Segment.class, passage);
			String[] passageAssignments = passageToAssignments.get(passage.getPassageId());
			for (Segment segment : segments) {
				// passage cluster features
				for (int i = 1; i < passageAssignments.length; i++) {
					FeaturesUtil.addFeature("passage_cluster_" + i, passageAssignments[i], jCas, segment);
				}
				// add passage topic features directly to the Segments
				for (Annotation annot : SitEntUimaUtils.getList(passage.getFeatures())) {
					SEFeature feat = (SEFeature) annot;
					FeaturesUtil.addFeature(feat.getName(), feat.getValue(), jCas, segment);
				}
			}
		}

	}

	public static void main(String[] args) {

		// Command line options
		Options options = new Options();
		options.addOption("input", true, "Path to directory with input XMIs/texts");
		options.addOption("output", true, "Path to directory with output XMIs/texts");
		options.addOption("clusters", true, "path to file with cluster IDs");

		// Parse command line and configure
		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cmd = parser.parse(options, args);
			String input = cmd.getOptionValue("input");
			String output = cmd.getOptionValue("output");
			String clusterFile = cmd.getOptionValue("clusters");

			// read XMI
			System.out.println("Reading xmi...");
			CollectionReader reader = createReader(XmiReader.class, XmiReader.PARAM_SOURCE_LOCATION, input,
					XmiReader.PARAM_PATTERNS, new String[] { "[+]*.xmi" });

			AnalysisEngineDescription addClusterFeatures = AnalysisEngineFactory.createEngineDescription(
					AddParagraphClusterFeatures.class, AddParagraphClusterFeatures.PARAM_CLUSTER_FILE, clusterFile);

			AnalysisEngineDescription xmiWriter = AnalysisEngineFactory.createEngineDescription(XmiWriter.class,
					XmiWriter.PARAM_TARGET_LOCATION, output);

			runPipeline(reader, addClusterFeatures, xmiWriter);

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		} catch (UIMAException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
