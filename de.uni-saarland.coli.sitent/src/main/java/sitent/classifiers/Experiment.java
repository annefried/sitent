package sitent.classifiers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import sitent.util.WekaUtils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.functions.Logistic;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Copy;
import weka.filters.unsupervised.attribute.MergeManyValues;
import weka.filters.unsupervised.attribute.Remove;

public class Experiment implements Runnable {

	static Logger log = Logger.getLogger(Experiment.class.getName());

	private Random randomGenerator = new Random();

	// whether or not to use only the situations that also have FEATURE labels
	// (main ref, aspectual class, habituality) -- set true for 'pipeline'
	// experimentsF
	private static final boolean PARAM_USE_FULL_SITUATIONS = false;

	// genre, xFold, test
	private String setting;
	// folder in which experiment is being run
	private String experimentFolder;
	// short description of experiment (for directory name)
	private String description;
	// timestamp for marking the directory name with output
	private String timestampText;
	// experiment in which the thread's results are written
	private String experimentSubDir;

	// directory with input files
	private String arffDir;
	// directory with extra training data (if any)
	private String extraTrainingDataDir;
	// path to config file so it can be copied
	private String configFile;
	// path to pre-trained model (optional)
	private String pretrainedModel;
	// for processing unlabeled data: need to map to features used when training
	// the model
	private String trainDataArff = null;
	private String pretrainedModelType = null;

	// info on class attribute
	private String classAttribute;
	private String[] classValues;

	// info on features to be used / excluded
	private Set<String> featuresUsed;
	private Set<String> featuresExcluded;
	private Map<String, Integer> featuresMinimumOccurrences;

	// additional setting parameters
	private int numFolds;
	private String wekaClassifierType;
	private String useBigramFeatures;
	private Double trainSampleFactor;

	private String updatedClassAttribute;
	private String[] updatedClassValues;

	private boolean useCrfpp;
	private boolean useCrfsuite;
	private boolean useLibLinear;
	// path to CRFPP installation
	private String crfppPath;
	// path to CRFSuite installation
	private String crfsuitePath;
	// path to LibLinear installation
	private String libLinearPath;
	// path to file for CRFPP files
	private String crfppFilesDir;
	// path to file for CRFsuite files
	private String crfsuiteDir;
	// path to file with LIBSVM files
	private String libSVMDir;

	// private double trainDocsPercentage;

	/*
	 * using setter methods instead of constructor to keep construction of
	 * objects readable
	 */
	public void setSetting(String setting) {
		this.setting = setting;
	}

	public void setExperimentFolder(String experimentFolder) {
		this.experimentFolder = experimentFolder;
	}

	public void setTrainDataArff(String trainDataArff) {
		this.trainDataArff = trainDataArff;
	}

	public void setExtraTrainingDataDir(String extraTrainingDataDir) {
		this.extraTrainingDataDir = extraTrainingDataDir;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setPretrainedModel(String pretrainedModel) {
		this.pretrainedModel = pretrainedModel;
	}

	public void setPretrainedModelType(String pretrainedModelType) {
		this.pretrainedModelType = pretrainedModelType;
	}

	public void setTimestampText(String timestampText) {
		this.timestampText = timestampText;
	}

	public void setArffDir(String arffDir) {
		this.arffDir = arffDir;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public void setClassAttribute(String classAttribute) {
		this.classAttribute = classAttribute;
	}

	public void setClassValues(String[] classValues) {
		this.classValues = classValues;
	}

	public void setFeaturesUsed(Set<String> featuresUsed) {
		this.featuresUsed = featuresUsed;
	}

	public void setFeaturesExcluded(Set<String> featuresExcluded) {
		this.featuresExcluded = featuresExcluded;
	}

	public void setFeaturesMinimumOccurrences(Map<String, Integer> featuresMinimumOccurrences) {
		this.featuresMinimumOccurrences = featuresMinimumOccurrences;
	}

	public void setClassifierType(String classifierType) {
		this.wekaClassifierType = classifierType;
	}

	public void setNumFolds(int numFolds) {
		this.numFolds = numFolds;
	}

	public void setUseBigramFeatures(String useBigramFeatures) {
		this.useBigramFeatures = useBigramFeatures;
	}

	public void setTrainSampleFactor(Double trainSampleFactor) {
		this.trainSampleFactor = trainSampleFactor;
	}

	public void setCrfppPath(String crfppPath) {
		this.crfppPath = crfppPath;
	}

	public void setCrfsuitePath(String crfsuitePath) {
		this.crfsuitePath = crfsuitePath;
	}

	public void setLibLinearPath(String libLinearPath) {
		this.libLinearPath = libLinearPath;

	}

	public void setUseCrfpp(boolean useCrfpp) {
		this.useCrfpp = useCrfpp;
	}

	public void setUseCrfsuite(boolean useCrfsuite) {
		this.useCrfsuite = useCrfsuite;
	}

	public void setUseLibLinear(boolean useLibLinear) {
		this.useLibLinear = useLibLinear;
	}

	public void setUpdatedClassAttribute(String updatedClassAttribute) {
		this.updatedClassAttribute = updatedClassAttribute;
	}

	public void setUpdatedClassValues(String[] updatedClassValues) {
		this.updatedClassValues = updatedClassValues;
	}

	/**
	 * Separates the documents in the given directory into folds. Returns a list
	 * of instances: one Instances object per fold. The instances of each
	 * document go into the same fold. Distribution of documents over folds is
	 * 'alphabetical', i.e., the different genres are distributed across the
	 * folds.
	 * 
	 * @param arffDir
	 *            a folder with one ARFF per document.
	 * @param numFolds
	 *            number of cross validation folds
	 * @return list of Instances representing the folds
	 * 
	 *         TODO: implement a sister function that splits data by verb. TODO:
	 *         implement a sister function that splits data by category / genre.
	 * @throws IOException
	 */
	private Instances[] getFoldsCrossValidationEqual(String dir, String genre) throws IOException {
		log.info(setting + "\t" + "getting x folds: " + numFolds + " from " + dir);

		String[] inputFiles = new File(dir).list();
		List<String> inputFilesList = new LinkedList<String>();
		for (int i = 0; i < inputFiles.length; i++) {
			if (genre != null) {
				// skip documents from other genres.
				if (!inputFiles[i].startsWith(genre)) {
					continue;
				}
			}

			inputFilesList.add(inputFiles[i]);
		}
		Collections.sort(inputFilesList, Collections.reverseOrder());
		inputFilesList.toArray(inputFiles);

		// some genres have fewer documents, do document-wise CV in this case
		numFolds = Math.min(numFolds, inputFilesList.size());

		Instances[] folds = new Instances[numFolds];
		for (int i = 0; i < numFolds; i++) {
			folds[i] = null;
		}

		for (int i = 0; i < inputFilesList.size(); i++) {

			log.info("reading: " + dir + "/" + inputFilesList.get(i));
			BufferedReader reader = new BufferedReader(new FileReader(dir + "/" + inputFilesList.get(i)));
			Instances data = new Instances(reader);
			reader.close();

			// choose fold and add
			if (folds[i % numFolds] == null) {
				folds[i % numFolds] = data;
			} else {
				// add to instances in this fold
				for (int j = 0; j < data.numInstances(); j++) {
					folds[i % numFolds].add(data.instance(j));
				}
			}
		}

		return folds;
	}

	/**
	 * Separates the documents in the given directory into folds. Returns a list
	 * of instances: one Instances object per fold. All instances of one genre
	 * (MASC or Wiki) go into one fold.
	 * 
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	private Instances[] getFoldsCrossValidationByGenre(String dir) throws IOException {
		log.info(setting + "\t" + "Getting CV folds by genre...");

		Map<String, List<String>> filesByGenre = new HashMap<String, List<String>>();
		String[] inputFiles = new File(dir).list();
		for (String inputFile : inputFiles) {
			String genre = inputFile.split("_")[0];
			if (!filesByGenre.containsKey(genre)) {
				filesByGenre.put(genre, new LinkedList<String>());
			}
			filesByGenre.get(genre).add(inputFile);
		}

		// one fold per genre
		int numFolds = filesByGenre.keySet().size();
		Instances[] folds = new Instances[numFolds];
		for (int i = 0; i < numFolds; i++) {
			folds[i] = null;
		}

		int i = 0;
		for (String genre : filesByGenre.keySet()) {
			log.info(setting + "\t" + "GENRE: " + genre);
			for (String inputFile : filesByGenre.get(genre)) {
				BufferedReader reader = new BufferedReader(new FileReader(dir + "/" + inputFile));
				Instances data = new Instances(reader);
				reader.close();
				if (folds[i] == null) {
					folds[i] = data;
				} else {
					// add to instances in this fold
					for (int j = 0; j < data.numInstances(); j++) {
						folds[i].add(data.instance(j));
					}
				}
			}
			i++;
		}

		log.info(setting + "\t" + "Done.");
		return folds;

	}

	/**
	 * Read in data into one Instances object (for the setting of just labeling
	 * data using a pre-trained model). (returning array for compatibility with
	 * other functions, array will have only one element)
	 * 
	 * @param arffDir
	 * @return
	 * @throws IOException
	 */
	private Instances[] getData(String arffDir) throws IOException {
		Instances data = null;
		log.info("Reading arffs from directory: " + arffDir);
		for (String inputFile : new File(arffDir).list()) {
			BufferedReader reader = new BufferedReader(new FileReader(arffDir + "/" + inputFile));
			Instances fileData = new Instances(reader);
			if (data == null) {
				data = fileData;
			} else {
				for (int j = 0; j < fileData.numInstances(); j++) {
					data.add(fileData.instance(j));
				}
			}
		}
		Instances[] retVal = new Instances[1];
		retVal[0] = data;
		return retVal;
	}

	/**
	 * Gets training (dev) and test fold.
	 * 
	 * @param arffDir
	 * @return
	 * @throws IOException
	 */
	private Instances[] getTrainAndTestFold(String arffDir) throws IOException {
		// first fold is test, second fold is train!
		Instances[] folds = new Instances[2];

		for (String inputFile : new File(arffDir + "/test").list()) {
			BufferedReader reader = new BufferedReader(new FileReader(arffDir + "/test/" + inputFile));
			Instances data = new Instances(reader);
			reader.close();
			if (folds[0] == null) {
				folds[0] = data;
			} else {
				for (int j = 0; j < data.numInstances(); j++) {
					folds[0].add(data.instance(j));
				}
			}
		}

		for (String inputFile : new File(arffDir + "/dev").list()) {
			BufferedReader reader = new BufferedReader(new FileReader(arffDir + "/dev/" + inputFile));
			Instances data = new Instances(reader);
			reader.close();
			if (folds[1] == null) {
				folds[1] = data;
			} else {
				for (int j = 0; j < data.numInstances(); j++) {
					folds[1].add(data.instance(j));
				}
			}
		}

		log.info(setting + "\t" + "Size of training data: " + folds[1].numInstances());
		log.info(setting + "\t" + "Size of test data:     " + folds[0].numInstances());

		return folds;
	}

	/**
	 * Sets the index of the class attribute, and removes other attribute
	 * starting with 'class'. TODO: filter instances based on valid values for
	 * class.
	 * 
	 * @param data
	 * @param classAttribute
	 * @throws Exception
	 */
	private Instances[] prepareData(Instances[] data) throws Exception {

		// Need to use batch filtering to make sure all folds get the same
		// filtering: Filter.useFilter(...)

		log.info(setting + "\t" + "Setting class attribute and filtering instances...");
		log.info(setting + "\t" + "number of attributes at beginning: " + data[0].numAttributes());

		log.info(setting + "\t" + "size of class values " + classValues.length);
		log.info(setting + "\t" + "Number of folds: " + data.length);

		// for applyModel case: read in arff used for training model
		Set<String> keepFeatures = new HashSet<String>();
		if (setting.equals("applyModel")) {
			System.out.println("Working Directory = " + System.getProperty("user.dir"));
			BufferedReader reader = new BufferedReader(new FileReader(trainDataArff));
			Instances keepFeaturesInst = new Instances(reader);
			log.info(setting + "\tattributes in training: " + keepFeaturesInst.numAttributes());
			for (int a = 0; a < keepFeaturesInst.numAttributes(); a++) {
				keepFeatures.add(keepFeaturesInst.attribute(a).name());
			}
		}

		List<Integer> indicesToRemove;
		int[] indices;

		// Filter out cases without a situation entity label or where one of the
		// other layers did not get a meaningful label.
		if (PARAM_USE_FULL_SITUATIONS) {
			data = WekaUtils.removeWithValues(data, "class_sitent_type", new String[] { "THE-DUMMY-VALUE" });
			data = WekaUtils.removeWithValues(data, "class_sitent_type", new String[] { "CANNOT_DECIDE" });
			data = WekaUtils.removeWithValues(data, "class_main_referent",
					new String[] { "THE-DUMMY-VALUE", "CANNOT_DECIDE", "EXPLETIVE" });
			data = WekaUtils.removeWithValues(data, "class_habituality",
					new String[] { "THE-DUMMY-VALUE", "CANNOT_DECIDE" });
			// data = WekaUtils.removeWithValues(data, "class_aspectual_class",
			// new String[] { "THE-DUMMY-VALUE", "CANNOT_DECIDE" });
		}

		// for (int i = 0; i < data.length; i++) {
		// Instances d = data[i];
		// for (int j =0; j<d.numInstances(); j++) {
		// String v =
		// d.instance(j).stringValue(d.attribute("class_sitent_type").index());
		// System.out.println(v);
		// }
		// }

		// filter out all features that are not in the configured list; also
		// keep class attribute. It is also possible to exclude some features
		// via additional patterns or minimum number of occurrences constraints.
		indicesToRemove = new LinkedList<Integer>();
		for (int i = 0; i < data[0].numAttributes(); i++) {
			String attrName = data[0].attribute(i).name();
			// in applyModel case, keep exactly the features that the training
			// data had
			if (setting.equals("applyModel")) {
				if (!keepFeatures.contains(attrName)) {
					log.debug("Removing (keepFeatures version): " + attrName);
					indicesToRemove.add(i);
				}
			}

			else {
				if (attrName.equals(classAttribute)) {
					continue;
				} else if (!featuresUsed.contains(attrName)) {
					boolean matchFound = false;
					for (String pattern : featuresUsed) {
						if (attrName.matches(pattern)) {
							matchFound = true;
							break;

						}
					}
					for (String pattern : featuresExcluded) {
						if (attrName.matches(pattern)) {
							matchFound = false;
							break;
						}
					}
					if (!matchFound) {
						indicesToRemove.add(i);
					}

					// additionally remove infrequently occuring features and
					// features that occur only with the same value within any
					// fold
					// (that do not occur in a fold)
					else {
						// is there a minimum occurrence constraint?
						Integer configuredMinOccurrence = null;
						for (String key : featuresMinimumOccurrences.keySet()) {
							if (attrName.matches(key)) {
								configuredMinOccurrence = featuresMinimumOccurrences.get(key);
							}
						}
						if (configuredMinOccurrence != null) {
							if (!ClassificationUtils.frequencyHigherEqualThan(data, data[0].attribute(i),
									configuredMinOccurrence)) {
								indicesToRemove.add(i);
								log.info("Removing: " + attrName);
							}
						}
					}
				}
			}
		}
		indices = new int[indicesToRemove.size()];
		for (int i = 0; i < indicesToRemove.size(); i++) {
			indices[i] = indicesToRemove.get(i);
		}

		Remove removeFilter = new Remove();
		removeFilter.setAttributeIndicesArray(indices);
		removeFilter.setInputFormat(data[0]);
		log.info(setting + "\t" + "... filtering.");
		for (int i = 0; i < data.length; i++) {
			data[i] = Filter.useFilter(data[i], removeFilter);
		}

		int index = data[0].attribute(classAttribute).index();

		// set class attribute to index
		for (int i = 0; i < data.length; i++) {
			data[i].setClassIndex(index);
			data[i].setClass(data[i].attribute(index));
		}

		log.info(setting + "\t" + "Number of attributes after filtering: " + data[0].numAttributes());

		// filter cases where class value is not one of the configured cases.
		List<String> valuesToBeFiltered = new LinkedList<String>();
		Attribute classAttr = data[0].classAttribute();
		Set<String> classValSet = new HashSet<String>();
		for (String cv : classValues) {
			classValSet.add(cv);
		}
		for (int i = 0; i < classAttr.numValues(); i++) {
			String value = classAttr.value(i);
			if (!classValSet.contains(value)) {
				valuesToBeFiltered.add(value);
			}
		}
		String[] valuesToBeFilteredArray = new String[valuesToBeFiltered.size()];
		valuesToBeFiltered.toArray(valuesToBeFilteredArray);
		if (!setting.equals("applyModel")) {
			data = WekaUtils.removeWithValues(data, classAttribute, valuesToBeFilteredArray);
		} else {
			// remove instances only from train fold.
			// new version: there is no train fold
			// Instances[] dataTemp = new Instances[1];
			// dataTemp[0] = data[1];
			// dataTemp = WekaUtils.removeWithValues(dataTemp, classAttribute,
			// valuesToBeFilteredArray);
			// data[1] = dataTemp[0];
		}

		// change class attribute if given
		log.debug("updated class attribute: " + updatedClassAttribute);
		if (updatedClassAttribute != null) {

			// index of original class attribute
			index = data[0].attribute(classAttribute).index();

			/*
			 * Adds a new class attribute (copying the previous one). Replaces
			 * all values except for the ones given with 'OTHER'.
			 */
			// copy existing class attribute
			Copy copyFilter = new Copy();
			copyFilter.setAttributeIndicesArray(new int[] { index });
			copyFilter.setInputFormat(data[0]);
			for (int i = 0; i < data.length; i++) {
				data[i] = Filter.useFilter(data[i], copyFilter);
			}
			// rename new attribute
			for (int i = 0; i < data.length; i++) {
				Attribute newClassAttribute = data[i].attribute("Copy of " + classAttribute);
				data[i].renameAttribute(newClassAttribute, updatedClassAttribute);
			}

			// change values
			Attribute newClassAttribute = data[0].attribute(updatedClassAttribute);
			int attIndex = newClassAttribute.index();
			System.out.println(attIndex + " " + newClassAttribute);

			String range = "";
			Enumeration<Object> valuesEnum = newClassAttribute.enumerateValues();
			Integer r = 0;
			Set<String> classValsToKeepSet = new HashSet<String>();
			for (String classVal : updatedClassValues) {
				classValsToKeepSet.add(classVal);
				System.out.println(">>" + classVal);
			}
			while (valuesEnum.hasMoreElements()) {
				String val = (String) valuesEnum.nextElement();
				r++;
				// TODO: is r correct?
				System.out.println(val + " " + r);
				if (!classValsToKeepSet.contains(val)) {
					range += r + ",";
				}
			}
			range = range.substring(0, range.length() - 1);
			// System.out.println("Merging: " + range);

			System.out.println("attribute being merged: " + data[0].attribute(attIndex) + " " + attIndex);

			MergeManyValues mergeValues = new MergeManyValues();
			// Weka's filter needs attribute index + 1 AARRRRGGGH
			mergeValues.setAttributeIndex(Integer.toString(attIndex + 1));
			mergeValues.setLabel("OTHER");
			mergeValues.setMergeValueRange(range);
			mergeValues.setInputFormat(data[0]);
			for (String option : mergeValues.getOptions()) {
				System.out.println(option);
			}
			for (int i = 0; i < data.length; i++) {
				data[i] = Filter.useFilter(data[i], mergeValues);
			}

			// set class index
			for (int i = 0; i < data.length; i++) {
				data[i].setClassIndex(attIndex);
			}

			// remove old class attribute
			removeFilter = new Remove();
			removeFilter.setAttributeIndicesArray(new int[] { index });
			removeFilter.setInputFormat(data[0]);
			for (int i = 0; i < data.length; i++) {
				data[i] = Filter.useFilter(data[i], removeFilter);
			}

		}

		// this is just for debugging
		// System.out.println("XXX");
		// for (int i = 0; i < data.length; i++) {
		// Instances d = data[i];
		// for (int j = 0; j < d.numInstances(); j++) {
		// String v = d.instance(j).stringValue(d.classIndex());
		// System.out.println(v);
		// }
		// }

		// print out the attributes actually used
		// log.info("Using the following features:");
		// for (int i = 0; i < data[0].numAttributes(); i++) {
		// log.info("> " + data[0].attribute(i).name());
		// if (i == data[0].classIndex()) {
		// log.info("<< this is the class attribute");
		// }
		// }

		// write out (filtered) ARFF data for debugging
		if (!setting.equals("applyModel")) {
			for (int i = 0; i < data.length; i++) {
				File folder = new File(experimentFolder + "/data_filtered");
				if (folder.exists()) {
					folder.delete();
				}
				folder.mkdirs();
				PrintWriter w = new PrintWriter(new FileWriter(experimentFolder + "/data_filtered/" + i + ".arff"));
				w.println("@relation sitent");
				for (int a = 0; a < data[i].numAttributes(); a++) {
					String featName = "\"" + data[i].attribute(a).name().replaceAll("\"|``", "QUOTE")
							.replaceAll(",", "COMMA").replaceAll(" ", "SPACE") + "\"";
					if (data[i].attribute(a).isNumeric()) {
						w.println("@attribute " + featName + " numeric");
					}
					if (data[i].attribute(a).isString()) {
						w.println("@attribute " + featName + " string");
					}
					if (data[i].attribute(a).isNominal()) {
						// feature values already escaped?
						StringBuffer line = new StringBuffer("@attribute " + featName + " {");
						for (int v = 0; v < data[i].attribute(a).numValues(); v++) {
							line.append("\"" + data[i].attribute(a).value(v) + "\"");
							if (v < data[i].attribute(a).numValues() - 1) {
								line.append(",");
							}
						}
						line.append("}");
						w.println(line);
					}
				}
				w.println("");
				w.println("@data");
				w.close();
			}
		}

		return data;
	}

	/**
	 * 
	 * @param folds
	 * @param predictionsPath
	 *            - write predictions for instances.
	 * @param crfppDir
	 * @param resultsFile
	 * @param testSetting
	 * @param trainDocsByGenre
	 * @throws Exception
	 */
	private void performCrossValidation(Instances[] folds, Instances extraTrainingData, String predictionsPath,
			String crfppDir, String crfsuiteDir, String libSVMDir, String resultsFile) throws Exception {

		// this was just used for pipeline experiments
		// if (this.wekaClassifierType.equals("J48")) {
		// crfppDir = null;
		// }

		log.info(setting + "\t" + "Performing cross validation... " + setting);

		Instances allData = new Instances(folds[0]);
		for (int i = 1; i < folds.length; i++) {
			for (int j = 0; j < folds[i].numInstances(); j++) {
				allData.add(folds[i].instance(j));
			}
		}

		PrintWriter predWriter = null;
		if (predictionsPath != null) {
			predWriter = new PrintWriter(new FileWriter(predictionsPath));
		}

		Evaluation eval = new Evaluation(allData);

		// StringBuffer[] crfppFileContents = new StringBuffer[folds.length];
		if (useCrfpp) {
			log.info(setting + "\t" + "writing CRFPP template...");
			// create the template file
			// num attributes -1 because we don't need the class attribute
			PrintWriter w = new PrintWriter(new FileWriter(crfppDir + "/template.txt"));
			for (int i = 0; i < folds[0].numAttributes() - 1; i++) {
				// unigram features for now
				w.println("U" + i + ":%x[0," + i + "]");
			}
			if (useBigramFeatures.equals("true")) {
				// using the bigram feature (class of previous item)
				w.println("B");
			} else if (useBigramFeatures.equals("gold")) {
				// simulating using bigram features if previous item would have
				// been predicted correctly
				w.println("U" + (allData.numAttributes() - 1) + ":%x[-1," + (allData.numAttributes() - 1) + "]");
			}
			w.close();

			// get the CRF++ representations for each fold
			// for (int i = 0; i < folds.length; i++) {
			// crfppFileContents[i] =
			// CrfppUtils.getCrfppRepresentation(folds[i]);
			// }

		}

		// perform cross validation
		for (int i = 0; i < folds.length; i++) {
			log.info(setting + "\t" + "...fold: " + (i + 1));

			String crfppTrain = "";
			String crfppTest = null;

			String crfsuiteTrain = "";
			String crfsuiteTest = null;

			String libSVMTrain = "";
			String libSVMTest = null;

			Instances train = new Instances(folds[i]);
			train.delete(); // remove all instances
			Instances test = null;
			for (int j = 0; j < folds.length; j++) {
				if (i == j) {
					test = folds[j];
					if (useCrfpp) {
						crfppTest = CrfppUtils.getCrfppRepresentation(test, null).toString();// crfppFileContents[j].toString();
					}
					if (useCrfsuite) {
						crfsuiteTest = CrfSuiteUtils.getCrfsuiteRepresentation(test, null).toString();
					}
					if (useLibLinear) {
						libSVMTest = LIBSVMUtils.getLibSVMRepresentation(test, null).toString();
					}
				} else {
					for (int k = 0; k < folds[j].numInstances(); k++) {
						train.add(folds[j].instance(k));
					}
				}
			}
			// add extra training data if any
			if (extraTrainingData != null) {
				for (int k = 0; k < extraTrainingData.numInstances(); k++) {
					train.add(extraTrainingData.instance(k));
				}
			}

			// downsample training
			if (trainSampleFactor != null) {
				if (trainSampleFactor != 100) {
					// downsample training data
					log.info("Training size: " + train.numInstances());
					// Weka sampling -- ignores order & documents
					// Resample resample = new Resample();
					// resample.setSampleSizePercent(trainSampleFactor);
					// resample.setNoReplacement(true);
					// resample.setInputFormat(train);
					// train = Filter.useFilter(train, resample);
					long numInstancesAfterFiltering = Math.round(train.numInstances() * trainSampleFactor / 100);
					while (train.numInstances() > numInstancesAfterFiltering) {
						train = removeDocument(train);
					}

					log.info(setting + "\t" + trainSampleFactor + "\n" + "Downsampled training: "
							+ train.numInstances());
				}
			}

			// create crfppTrain // crfsuiteTrain files only now (after
			// potential downsampling)
			if (useCrfpp) {
				crfppTrain += CrfppUtils.getCrfppRepresentation(train, null) + "\n";
			}
			if (useCrfsuite) {
				crfsuiteTrain += CrfSuiteUtils.getCrfsuiteRepresentation(train, null) + "\n";
			}
			if (useLibLinear) {
				libSVMTrain += LIBSVMUtils.getLibSVMRepresentation(train, null);
			}

			Classifier classifier = null;
			switch (wekaClassifierType) {
			case "ZeroR": {
				classifier = new ZeroR();
				break;
			}
			case "RandomForest": {
				classifier = new RandomForest();
				break;
			}
			case "Logistic": {
				classifier = new Logistic();
				break;
			}
			case "BayesNet": {
				classifier = new BayesNet();
				break;
			}
			case "J48": {
				classifier = new J48();
				break;
			}
			}

			// using Weka
			classifier.buildClassifier(train);
			// write to file
			weka.core.SerializationHelper.write(experimentFolder + "/weka.model", classifier);
			eval.evaluateModel(classifier, test);

			if (predictionsPath != null) {
				// find the instanceid Attribute
				int instanceIdIndex = -1;
				for (int a = 0; a < test.instance(0).numAttributes(); a++) {
					Attribute attr = test.instance(0).attribute(a);
					if (attr.name().equals("instanceid")) {
						instanceIdIndex = a;
						break;
					}
				}
				if (instanceIdIndex == -1) {
					System.err.println("No instanceid attribute.");
					log.error("No instanceid attribute.");
					throw new RuntimeException();
				}
				// get array of class values
				List<String> classVals = new LinkedList<String>();
				for (int a = 0; a < test.classAttribute().numValues(); a++) {
					classVals.add(test.classAttribute().value(a));
				}

				// Get predicted labels for the instances
				for (int k = 0; k < test.numInstances(); k++) {
					Instance inst = test.instance(k);
					String instId = inst.stringValue(instanceIdIndex);
					double classInd = classifier.classifyInstance(inst);
					predWriter.println(instId + "\t" + classVals.get((int) inst.classValue()) + "\t"
							+ classVals.get((int) classInd));

				}

			}

			if (useLibLinear && libSVMDir != null) {
				log.info("Writing LIBSVM (LIBLINEAR) files ... ");
				PrintWriter w = new PrintWriter(new FileWriter(libSVMDir + "/train" + i + ".csv"));
				w.print(libSVMTrain.trim());
				w.close();
				w = new PrintWriter(new FileWriter(libSVMDir + "/test" + i + ".csv"));
				w.print(libSVMTest);
				w.close();

				log.info("done.");

				// run LibLINEAR: train model
				Process p = Runtime.getRuntime().exec(libLinearPath + "/train -s 1 -e 0.0000001 -B 1 " + libSVMDir
						+ "/train" + i + ".csv " + libSVMDir + "/sitent" + i + ".model");
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader stError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String s = null;
				while ((s = stdInput.readLine()) != null || (s = stError.readLine()) != null) {
					log.info(s);
				}
				p.waitFor();
				log.info("Training done, will now predict.");
				// apply on test data
				p = Runtime.getRuntime().exec(libLinearPath + "/predict " + libSVMDir + "/test" + i + ".csv "
						+ libSVMDir + "/sitent" + i + ".model " + libSVMDir + "/predictions" + i + ".csv");
				p.waitFor();
				log.info("Predictions done.");
			}

			if (useCrfpp && crfppDir != null) {
				log.info(setting + "\t" + "Running CRF++ / CRFSuite...");
				// CRFPP input files
				PrintWriter w = new PrintWriter(new FileWriter(crfppDir + "/train" + i + ".csv"));
				w.print(crfppTrain.trim());
				w.close();
				w = new PrintWriter(new FileWriter(crfppDir + "/test" + i + ".csv"));
				w.print(crfppTest);
				w.close();

				log.info(setting + "\t" + "done writing the crf files.");

				// run CRF++: train model
				Process p = Runtime.getRuntime().exec(crfppPath + "/crf_learn -p 4 " + crfppDir + "/template.txt "
						+ crfppDir + "/train" + i + ".csv " + crfppDir + "/sitent" + i + ".model");
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader stError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String s = null;
				while ((s = stdInput.readLine()) != null || (s = stError.readLine()) != null) {
					log.info(s);
				}
				p.waitFor();
				log.info("Training done, will now predict.");
				// apply on test data
				p = Runtime.getRuntime().exec(crfppPath + "/crf_test -m " + crfppDir + "/sitent" + i + ".model "
						+ crfppDir + "/test" + i + ".csv");
				stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				w = new PrintWriter(new FileWriter(crfppDir + "/predictions" + i + ".csv"));
				while ((s = stdInput.readLine()) != null) {
					w.println(s);
				}
				p.waitFor();
				w.close();
				log.info("Predictions done.");
			}

			if (useCrfsuite) {

				System.out.println(crfsuiteTrain.trim().length());

				// CRFSuite input files
				PrintWriter w = new PrintWriter(new FileWriter(crfsuiteDir + "/train" + i + ".csv"));
				w.print(crfsuiteTrain.trim());
				w.close();
				w = new PrintWriter(new FileWriter(crfsuiteDir + "/test" + i + ".csv"));
				w.print(crfsuiteTest);
				w.close();
				log.info("done writing CRFSuite input files.");

				// run CRFsuite: train model
				Process p = Runtime.getRuntime().exec(crfsuitePath + "/crfsuite learn -m " + crfsuiteDir + "/sitent" + i
						+ ".model " + crfsuiteDir + "/train" + i + ".csv ");
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader stError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String s = null;
				while ((s = stdInput.readLine()) != null || (s = stError.readLine()) != null) {
					log.info(s);
				}
				p.waitFor();
				log.info("Training done, will now predict.");
				// apply on test data
				p = Runtime.getRuntime().exec(crfsuitePath + "/crfsuite tag -m " + crfsuiteDir + "/sitent" + i
						+ ".model " + crfsuiteDir + "/test" + i + ".csv");
				stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				w = new PrintWriter(new FileWriter(crfsuiteDir + "/predictions" + i + ".csv"));
				while ((s = stdInput.readLine()) != null) {
					w.println(s);
				}
				p.waitFor();
				w.close();
				log.info("Predictions done.");

			}

			// if train and test setting, do this only once (first fold is test,
			// 2nd fold is train)
			if (setting.equals("test") || setting.equals("applyModel")) {
				break;
			}

		}

		if (predictionsPath != null)

		{
			predWriter.close();
		}

		// Output results
		PrintWriter w = new PrintWriter(new FileWriter(resultsFile));
		log.info(setting + "\t" + "Results by Weka: " + wekaClassifierType);
		w.println("Results by Weka: " + wekaClassifierType);

		log.info("\n" + setting + "\t" + "Class statistics");
		w.println("\nClass statistics");
		// Print P, R, F for the classes of interest
		double sum_p = 0;
		double sum_r = 0;
		double sum_f = 0;
		for (String classValName : classValues) {
			Integer classInd = folds[0].classAttribute().indexOfValue(classValName);
			if (classInd == -1) {
				// class value not in data
				continue;
			}

			double p = eval.precision(classInd);
			double r = eval.recall(classInd);
			double f = eval.fMeasure(classInd);
			sum_p += p;
			sum_r += r;
			sum_f += f;
			log.info(setting + "\t" + String.format("%.2f\t%.2f\t%.2f", p * 100, r * 100, f * 100) + "\t"
					+ classValName);
			w.println(String.format("%.2f\t%.2f\t%.2f", p * 100, r * 100, f * 100) + "\t" + classValName);
		}
		// aggregate statistics
		double macro_p = sum_p / classValues.length * 100;
		double macro_r = sum_r / classValues.length * 100;
		double macro_f = sum_f / classValues.length * 100;

		double f_of_macro = 2 * macro_p * macro_r / (macro_p + macro_r);

		log.info(setting + "\t" + String.format("%.2f\t%.2f\t%.2f", macro_p, macro_r, macro_f) + "\tmacro-avg");
		w.println(String.format("%.2f\t%.2f\t%.2f", macro_p, macro_r, macro_f) + "\tmacro-avg");

		log.info(setting + "\t" + String.format("F-of-Macro: %.2f", f_of_macro));
		log.info(setting + "\t" + String.format("Accuracy: %.2f", eval.pctCorrect()));
		log.info(setting + "\t" + "Number of instances: " + eval.numInstances());

		w.println("Number of instances: " + eval.numInstances());
		w.println(String.format("Macro-average P: %.2f", macro_p * 100));
		w.println(String.format("Macro-average R: %.2f", macro_r * 100));
		w.println(String.format("Macro-average F: %.2f", macro_f * 100));
		w.println(String.format("F-of-Macro: %.2f", f_of_macro));
		w.println(String.format("Accuracy: %.2f", eval.pctCorrect()));

		if (useCrfpp) {
			evaluateCrfpp(crfppDir, classValues, w);
		}

		if (useLibLinear) {
			evaluateLibLinear(libSVMDir, folds[0], w);
		}

		w.close();

		// evaluate CRFPP results by genre (if genre-based cross validation)
		if (setting.matches("genre||genreSelected")) {
			File byGenre = new File(crfppDir);
			evaluateCrfppByGenre(crfppDir, folds, classValues);
			evaluateCrfppByGenre(byGenre.getParent(), folds, classValues);
		}

	}

	/**
	 * Evaluates entire CRFPP results.
	 * 
	 * @param crfppDir
	 * @param classValues
	 * @param w
	 * @throws IOException
	 */
	private static void evaluateCrfpp(String crfppDir, String[] classValues, PrintWriter w) throws IOException {
		// log.info(setting + "\t" + "Evaluating CRF++ results...");
		w.println("\n\nCRF++ results");

		Map<String, Map<String, Integer>> confMatrix = new HashMap<String, Map<String, Integer>>();
		double total = 0;
		double totalCorrect = 0;

		// evaluate CRF++
		for (String predFile : new File(crfppDir).list()) {
			if (!predFile.startsWith("prediction")) {
				continue;
			}
			BufferedReader r = new BufferedReader(new FileReader(crfppDir + "/" + predFile));
			String line;
			while ((line = r.readLine()) != null) {
				if (line.trim().equals("")) {
					continue;
				}
				String[] parts = line.split("\t");
				// second last column: gold label
				String gold = parts[parts.length - 2];
				// last column: predicted label
				String pred = parts[parts.length - 1];
				if (!confMatrix.containsKey(gold)) {
					confMatrix.put(gold, new HashMap<String, Integer>());
				}
				if (!confMatrix.get(gold).containsKey(pred)) {
					confMatrix.get(gold).put(pred, 0);
				}
				confMatrix.get(gold).put(pred, confMatrix.get(gold).get(pred) + 1);
				total++;
				if (gold.equals(pred)) {
					totalCorrect++;
				}
			}
			r.close();
		}

		// log.info(setting + "\t" + "Accuracy CRF++: " + totalCorrect / total);
		// log.info(setting + "\t" + "Number of instances: " + total);
		if (w != null) {
			w.println("Accuracy CRF++: " + totalCorrect / total);
			w.println("Number of instances: " + total);
		}

		EvaluationUtils.printResults(confMatrix, w, classValues);
	}

	private void evaluateCrfppByGenre(String crfppDir, Instances[] folds, String[] classValues) throws IOException {
		PrintWriter w = new PrintWriter(crfppDir + "/results-by-genre.txt");

		// identify genre attribute
		int genreIdx = CrfppUtils.getCrfppGenreIndex(folds[0]);
		log.info(setting + "\t" + "genre index: " + genreIdx);
		for (String predFile : new File(crfppDir).list()) {
			if (!predFile.startsWith("prediction")) {
				continue;
			}

			Map<String, Map<String, Integer>> confMatrix = new HashMap<String, Map<String, Integer>>();
			double total = 0;
			double totalCorrect = 0;

			BufferedReader r = new BufferedReader(new FileReader(crfppDir + "/" + predFile));
			String line;
			String genre = null;
			while ((line = r.readLine()) != null) {
				if (line.trim().equals("")) {
					continue;
				}
				String[] parts = line.split("\t");
				// second last column: gold label
				String gold = parts[parts.length - 2];
				// last column: predicted label
				String pred = parts[parts.length - 1];
				if (!confMatrix.containsKey(gold)) {
					confMatrix.put(gold, new HashMap<String, Integer>());
				}
				if (!confMatrix.get(gold).containsKey(pred)) {
					confMatrix.get(gold).put(pred, 0);
				}
				confMatrix.get(gold).put(pred, confMatrix.get(gold).get(pred) + 1);
				total++;
				if (gold.equals(pred)) {
					totalCorrect++;
				}
				if (genre == null) {
					genre = parts[genreIdx];
					System.out.println("Genre is: " + genre);
					w.println("Genre is: " + genre);
				}
			}
			r.close();

			w.println("------------------------------------------");
			w.println("GENRE: " + genre);
			log.info(setting + "\t" + "Accuracy CRF++: " + totalCorrect / total);
			log.info(setting + "\t" + "Number of instances: " + total);
			if (w != null) {
				w.println("Accuracy CRF++: " + totalCorrect / total);
				w.println("Number of instances: " + total);
			}

			EvaluationUtils.printResults(confMatrix, w, classValues);

			w.println("\n\n");
		}
		w.close();
	}

	private void evaluateLibLinear(String libSVMDir, Instances data, PrintWriter w) throws IOException {
		// map from value index to class value names
		Map<Integer, String> classValNames = new HashMap<Integer, String>();
		Attribute classAttr = data.attribute(data.classIndex());
		for (int i = 0; i < classAttr.numValues(); i++) {
			classValNames.put(i, classAttr.value(i));
		}

		List<String> predictions = new LinkedList<String>();
		List<String> goldList = new LinkedList<String>();

		// read gold / predictions
		for (String predFile : new File(libSVMDir).list()) {
			if (!predFile.startsWith("prediction")) {
				continue;
			}
			// read predictions
			BufferedReader r = new BufferedReader(new FileReader(libSVMDir + "/" + predFile));
			String line;
			while ((line = r.readLine()) != null) {
				if (line.trim().equals("")) {
					continue;
				}
				predictions.add(classValNames.get(Integer.parseInt(line.trim())));
			}
			r.close();
			// read gold standard
			r = new BufferedReader(new FileReader(libSVMDir + "/" + predFile.replace("predictions", "test")));
			while ((line = r.readLine()) != null) {
				if (line.trim().equals("")) {
					continue;
				}
				String[] columns = line.split(" ");
				goldList.add(classValNames.get(Integer.parseInt(columns[0])));
			}
			r.close();
		}

		w.println("\n\nLIBLINEAR results");

		Map<String, Map<String, Integer>> confMatrix = new HashMap<String, Map<String, Integer>>();
		double total = 0;
		double totalCorrect = 0;

		// fill confusion matrix
		for (int i = 0; i < predictions.size(); i++) {
			// second last column: gold label
			String gold = goldList.get(i);
			// last column: predicted label
			String pred = predictions.get(i);
			if (!confMatrix.containsKey(gold)) {
				confMatrix.put(gold, new HashMap<String, Integer>());
			}
			if (!confMatrix.get(gold).containsKey(pred)) {
				confMatrix.get(gold).put(pred, 0);
			}
			confMatrix.get(gold).put(pred, confMatrix.get(gold).get(pred) + 1);
			total++;
			if (gold.equals(pred)) {
				totalCorrect++;
			}
		}

		if (w != null) {
			w.println("Accuracy LIBLINEAR++: " + totalCorrect / total);
			w.println("Number of instances: " + total);
		}

		EvaluationUtils.printResults(confMatrix, w, classValues);

	}

	private void applyModel(String model, Instances data, String crfppDir) throws IOException, InterruptedException {
		log.info("Applying model on unlabeled data ..." + data.numAttributes());
		if (pretrainedModelType.equals("crfpp")) {
			log.info("CRF++ model ...");
			// get CRFPP input files
			String crfppFileContent = CrfppUtils.getCrfppRepresentation(data, null).toString();
			PrintWriter w = new PrintWriter(new FileWriter(crfppDir + "/data.csv"));
			w.print(crfppFileContent.trim());
			w.close();
			// run CRF++
			// apply on test data
			log.info("Now running CRFPP using pretrained model: " + pretrainedModel);
			log.info(crfppPath + "/crf_test -m " + pretrainedModel + " " + crfppDir + "/data.csv");
			Process p = Runtime.getRuntime()
					.exec(crfppPath + "/crf_test -m " + pretrainedModel + " " + crfppDir + "/data.csv");
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String s = null;
			w = new PrintWriter(new FileWriter(crfppDir + "/predictions.csv"));
			while ((s = stdInput.readLine()) != null) {
				w.println(s);
			}
			p.waitFor();
			w.close();
			log.info("Done running CRFPP.");
		} else if (pretrainedModelType.equals("weka")) {
			// TODO
		}

	}

	@SuppressWarnings("rawtypes")
	/**
	 * Two command line arguments: arg[0] should point to the configuration
	 * file, arg[1] to the installation directory of CRF++, arg[2] to the
	 * installation directory of CRFsuite.
	 * 
	 * CRFSuite requires numeric features to be normalized, i.e., [0,1]. The
	 * code in this class does not ensure normalization as the normalizatio
	 * method may differ per feature group -- make sure that configuration
	 * allows only normalized feautures if using CRFsuite.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// config file
		String configFile = args[0];

		// path to installation of CRFPP
		String CRFPP_INSTALLATION_DIR = args[1];
		String LIBLINEAR_INSTALLATION_DIR = args[2];
		String CRFSUITE_INSTALLATION_DIR = args[3];

		// String crfppDir =
		// "/proj/anne-phd/situation_entities/git_repo/sitent/annotated_corpus/experiments_data/2016-06-16_16:48_dev-celex-binnedLingInd_xFold/crfpp";
		// String[] classValues = new String[] { "EVENT", "STATE",
		// "GENERIC_SENTENCE", "REPORT", "GENERALIZING_SENTENCE",
		// "IMPERATIVE", "QUESTION" };
		// PrintWriter w = new PrintWriter(new FileWriter(
		// "/proj/anne-phd/situation_entities/git_repo/sitent/annotated_corpus/experiments_data/2016-06-16_16:48_dev-celex-binnedLingInd_xFold/results.txt"));
		// evaluateCrfpp(crfppDir, classValues, w);
		// w.close();

		log.info("Reading experiment(s) configuration from: " + configFile);

		// Read configuration for experiments.
		File fXmlFile = new File(configFile);
		SAXReader reader = new SAXReader();
		Document doc = reader.read(fXmlFile);
		Element root = doc.getRootElement();

		// experiment / labeling setting
		Node settingNode = root.selectSingleNode("settings/setting");
		String SETTING = settingNode.getText();
		log.info("Will run setting: " + SETTING);

		// parent directory for experiments (when the config file is in some
		// subdirectory)
		String EXPERIMENT_FOLDER = new File(args[0]).getParent();
		log.info("Experiment folder: " + EXPERIMENT_FOLDER);

		String EXPERIMENT_DESCRIPTION = root.selectSingleNode("desc").getText();
		String TIMESTAMP_TEXT = null;
		String ARFF_DIR = null;
		String EXTRA_TRAINING_DATA_DIR = null;
		int NUM_FOLDS = 0;
		String UPDATED_CLASS_ATTRIBUTE = null;
		String[] UPDATED_CLASS_VALUES = null;
		Set<String> FEATURES_USED = null;
		Map<String, Integer> FEATURES_MIN_OCCURRENCES = null;
		Set<String> FEATURES_EXCLUDED = null;
		Double TRAIN_SAMPLE_FACTOR = null;
		String USE_BIGRAM_FEATURES = null;
		String PRETRAINED_MODEL = null;
		String TRAIN_FEATURE_CONFIG = null;
		String PRETRAINED_MODEL_TYPE = null;
		String WEKA_CLASSIFIER = null;
		boolean USE_CRFPP = false;
		boolean USE_CRFSUITE = false;
		boolean USE_LIBLINEAR = false;

		// class attribute: classification / sequence labeling task executed for
		// this feature
		String CLASS_ATTRIBUTE = root.selectSingleNode("classAttribute/@featureName").getText();
		log.info("Class attribute: " + CLASS_ATTRIBUTE);

		// filtering by class values
		List<String> classVals = new LinkedList<String>();
		Element valNodes = (Element) root.selectSingleNode("classAttribute");
		for (Iterator i = valNodes.elementIterator("value"); i.hasNext();) {
			Element foo = (Element) i.next();
			classVals.add(foo.getText());
		}
		String[] CLASS_VALUES = new String[classVals.size()];
		for (int i = 0; i < classVals.size(); i++) {
			CLASS_VALUES[i] = classVals.get(i);
			log.info("- class value: " + classVals.get(i));
		}

		if (!SETTING.equals("applyModel")) {
			// an actual experiment
			// timestamp to be used for folders of this experiment
			Date date = new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			String timeStampString = timestamp.toString();
			TIMESTAMP_TEXT = timeStampString.split(" ")[0];
			String timeStr = timeStampString.split(" ")[1];
			TIMESTAMP_TEXT += "_" + timeStr.split(":")[0] + ":" + timeStr.split(":")[1];

			// input directory with one ARFF per document.
			ARFF_DIR = EXPERIMENT_FOLDER + "/" + root.selectSingleNode("inputArffDir").getText();
			log.info("ARFF data in: " + ARFF_DIR);

			// input directory with one ARFF per document, extra training data
			if (root.selectSingleNode("extraTrainingData") != null) {
				EXTRA_TRAINING_DATA_DIR = EXPERIMENT_FOLDER + "/"
						+ root.selectSingleNode("extraTrainingData").getText();
				log.info("Extra training data: " + EXTRA_TRAINING_DATA_DIR);
			}

			// number of folds, applies for xFold or withinGenre setting
			if (root.selectSingleNode("numFolds") != null) {
				NUM_FOLDS = Integer.parseInt(root.selectSingleNode("numFolds").getText());
				log.info("Number of folds: " + NUM_FOLDS);
			}

			// possibly updating class attribute, e.g. mapping to something else
			List<String> updatedClassVals = new LinkedList<String>();
			Element updatedValNodes = (Element) root.selectSingleNode("updatedClassAttribute");
			if (updatedValNodes != null) {
				for (Iterator i = updatedValNodes.elementIterator("value"); i.hasNext();) {
					Element foo = (Element) i.next();
					updatedClassVals.add(foo.getText());
				}
				UPDATED_CLASS_VALUES = new String[updatedClassVals.size()];
				for (int i = 0; i < updatedClassVals.size(); i++) {
					UPDATED_CLASS_VALUES[i] = updatedClassVals.get(i);
					System.out.println("- updated class value: " + updatedClassVals.get(i));
				}
				UPDATED_CLASS_ATTRIBUTE = root.selectSingleNode("updatedClassAttribute/@featureName").getText();
			}

			// Weka classifier for comparison
			WEKA_CLASSIFIER = root.selectSingleNode("wekaClassifier").getText();
			log.info("Weka classifier to be run: " + WEKA_CLASSIFIER);

			// Use all the features configured in FEATURES_USED, but exclude the
			// ones that additionally match FEATURES_EXCLUDED. NodeList
			// featNodes

			FEATURES_USED = new HashSet<String>();
			FEATURES_MIN_OCCURRENCES = new HashMap<String, Integer>();
			FEATURES_EXCLUDED = new HashSet<String>();
			Element featPatternNode = (Element) root.selectSingleNode("featurePatterns");
			for (Iterator i = featPatternNode.elementIterator("feature"); i.hasNext();) {
				Node pattern = (Node) i.next();
				if (pattern.selectSingleNode("@used") != null
						&& pattern.selectSingleNode("@used").getStringValue().equals("false")) {
					String patternText = pattern.getText();
					if (!patternText.endsWith(".*")) {
						patternText = "\\Q" + patternText + "\\E";
					}
					FEATURES_EXCLUDED.add(patternText);
				} else {
					// assume being used as the default
					String patternText = pattern.getText();
					if (!patternText.endsWith(".*")) {
						patternText = "\\Q" + patternText + "\\E";
					}
					FEATURES_USED.add(patternText);
				}
				if (pattern.selectSingleNode("@min") != null) {
					try {
						Integer minimumOccurrences = Integer.parseInt(pattern.selectSingleNode("@min").getText());
						FEATURES_MIN_OCCURRENCES.put(pattern.getText(), minimumOccurrences);
					} catch (NumberFormatException e) {
						log.error("Minimum number of occurrences of feature must be an integer!");
						log.error("Error trying to get minimum for feature: " + pattern.getText());
						System.err.println("Minimum number of occurrences of feature must be an integer!");
						System.err.println("Error trying to get minimum for feature: " + pattern.getText());
						throw new RuntimeException();
					}
				}
			}

			// which CRF toolkit to use
			// <crfToolkit crpp="true" crfsuite="true" liblinear="true"/>
			USE_CRFPP = Boolean.parseBoolean(root.selectSingleNode("crfToolkit/@crfpp").getStringValue());
			USE_CRFSUITE = Boolean.parseBoolean(root.selectSingleNode("crfToolkit/@crfsuite").getStringValue());
			USE_LIBLINEAR = Boolean.parseBoolean(root.selectSingleNode("crfToolkit/@liblinear").getStringValue());

			// whether or not to use bigram features in CRF, values are true,
			// false
			// or gold.
			USE_BIGRAM_FEATURES = root.selectSingleNode("bigramFeature/@used").getStringValue();
			if (Boolean.parseBoolean(root.selectSingleNode("bigramFeature/@gold").getStringValue())) {
				USE_BIGRAM_FEATURES = "gold";
			}
			log.info("Using bigram features: " + USE_BIGRAM_FEATURES);

			// downsampling training data?
			Node trainSampleNode = root.selectSingleNode("trainSampleFactor");

			if (trainSampleNode != null) {
				TRAIN_SAMPLE_FACTOR = Double.parseDouble(trainSampleNode.getText());
			}
			log.info("Train sample factor: " + TRAIN_SAMPLE_FACTOR);

			// updating the class attribute?
			if (UPDATED_CLASS_ATTRIBUTE != null) {
				String[] newClassVals = new String[UPDATED_CLASS_VALUES.length + 1];
				for (int i = 0; i < UPDATED_CLASS_VALUES.length; i++) {
					newClassVals[i] = UPDATED_CLASS_VALUES[i];
				}
				newClassVals[UPDATED_CLASS_VALUES.length] = "OTHER";
				UPDATED_CLASS_VALUES = newClassVals;
			}

		} else {
			// applying a pre-trained model on some unlabeled data
			Node pretrainedModelNode = root.selectSingleNode("model");
			PRETRAINED_MODEL = pretrainedModelNode.getText();
			Node featuresNode = root.selectSingleNode("features");
			TRAIN_FEATURE_CONFIG = featuresNode.getText();
			PRETRAINED_MODEL_TYPE = root.selectSingleNode("model/@type").getStringValue();
			ARFF_DIR = EXPERIMENT_FOLDER + "/temp/processed_arff_compatible";
		}

		log.info("Experiments running in folder: " + EXPERIMENT_FOLDER);
		// start the experiment / labeling
		Experiment experiment = new Experiment();
		experiment.setSetting(SETTING);
		experiment.setExperimentFolder(EXPERIMENT_FOLDER);
		experiment.setDescription(EXPERIMENT_DESCRIPTION);
		experiment.setTimestampText(TIMESTAMP_TEXT);
		experiment.setArffDir(ARFF_DIR);
		experiment.setExtraTrainingDataDir(EXTRA_TRAINING_DATA_DIR);
		experiment.setConfigFile(configFile);
		experiment.setPretrainedModel(PRETRAINED_MODEL);
		experiment.setPretrainedModelType(PRETRAINED_MODEL_TYPE);
		experiment.setTrainDataArff(TRAIN_FEATURE_CONFIG);
		experiment.setClassAttribute(CLASS_ATTRIBUTE);
		experiment.setClassValues(CLASS_VALUES);
		experiment.setFeaturesUsed(FEATURES_USED);
		experiment.setFeaturesExcluded(FEATURES_EXCLUDED);
		experiment.setFeaturesMinimumOccurrences(FEATURES_MIN_OCCURRENCES);
		experiment.setClassifierType(WEKA_CLASSIFIER);
		experiment.setNumFolds(NUM_FOLDS);
		experiment.setUseBigramFeatures(USE_BIGRAM_FEATURES);
		experiment.setTrainSampleFactor(TRAIN_SAMPLE_FACTOR);
		experiment.setCrfppPath(CRFPP_INSTALLATION_DIR);
		experiment.setCrfsuitePath(CRFSUITE_INSTALLATION_DIR);
		experiment.setLibLinearPath(LIBLINEAR_INSTALLATION_DIR);
		experiment.setUseCrfpp(USE_CRFPP);
		experiment.setUseCrfsuite(USE_CRFSUITE);
		experiment.setUseLibLinear(USE_LIBLINEAR);
		experiment.setUpdatedClassAttribute(UPDATED_CLASS_ATTRIBUTE);
		experiment.setUpdatedClassValues(UPDATED_CLASS_VALUES);
		// start this experiment thread
		(new Thread(experiment)).start();
	}

	// randomly remove instances of one document
	private Instances removeDocument(Instances data) {
		int pickInstance = randomGenerator.nextInt(data.numInstances());
		Attribute instIdAttribute = data.attribute("instanceid");
		String instanceId = data.instance(pickInstance).stringValue(instIdAttribute);
		String documentId = instanceId.substring(0, instanceId.lastIndexOf("_"));
		for (int i = data.numInstances() - 1; i >= 0; i--) {
			String instanceId2 = data.instance(i).stringValue(instIdAttribute);
			if (instanceId2.startsWith(documentId)) {
				data.delete(i);
			}
		}
		return data;
	}

	@Override
	public void run() {

		log.info("STARTING AN EXPERIMENT THREAD with setting: " + setting);

		experimentSubDir = experimentFolder + "/" + timestampText + "_" + description + "_" + setting;
		if (this.setting.equals("applyModel")) {
			// don't use timestamp, just use the configured name.
			// (To be able to find the predictions later on)
			experimentSubDir = experimentFolder + "/" + classAttribute;
		}

		if (this.trainSampleFactor != null && this.trainSampleFactor != 100.0) {
			experimentSubDir += "_sample:" + this.trainSampleFactor;
		}

		new File(experimentSubDir).mkdirs();
		log.info("Files will be written to : " + experimentSubDir);

		// copy configuration file
		String configFilename = FilenameUtils.getBaseName(configFile) + ".xml";
		try {
			FileUtils.copyFile(new File(configFile), new File(experimentSubDir + "/" + configFilename));
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error copying config file!");
		}

		// will hold data for experiments
		Instances[] folds = null;
		Instances extraTrainingData = null;

		try {
			switch (setting) {
			case "test":
				folds = getTrainAndTestFold(arffDir);
				break;
			case "xFold":
				folds = getFoldsCrossValidationEqual(arffDir, null);
				break;
			case "applyModel":
				folds = getData(arffDir);
				break;
			case "genre":
				folds = getFoldsCrossValidationByGenre(arffDir);
				// needed for evaluation
				featuresUsed.add("document_genre");
				featuresExcluded.remove("document_genre");
				break;
			}
			// read extra training data if any given
			if (extraTrainingDataDir != null) {
				log.info("Reading extra training data...");
				extraTrainingData = getData(extraTrainingDataDir)[0];
				log.info(extraTrainingData.numInstances() + " additional training instances.");
			}

		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error reading arff data from " + arffDir);
			throw new RuntimeException();
		}

		if (this.setting.equals("applyModel")) {
			try {
				// filter data according to feature configuration
				log.info("Number of attributes in ARFF: " + folds[0].numAttributes());
				folds = prepareData(folds);
				log.info("after filtering: " + folds[0].numAttributes());
				// create CRFPP representation
				// TODO: does this match the one used in training?
				crfppFilesDir = experimentSubDir + "/crfpp";
				crfsuiteDir = experimentSubDir + "/crfsuite";
				libSVMDir = experimentSubDir = "/libsvm";
				if (useCrfpp) {
					new File(crfppFilesDir).mkdirs();
				}
				if (useCrfsuite) {
					new File(crfsuiteDir).mkdirs();
				}
				if (useLibLinear) {
					new File(libSVMDir).mkdirs();
				}
				applyModel(pretrainedModel, folds[0], crfppFilesDir);

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (this.setting.equals("withinGenre")) {
			// perform one within-genre experiment per genre
			for (String genre : new String[] { "blog", "email", "letters", "govt-docs", "fiction", "jokes", "journal",
					"travel", "ficlets", "technical", "news", "essays", "wiki" }) {
				String genreSubDir = experimentSubDir + "/" + genre;
				File f = new File(genreSubDir);
				f.mkdirs();

				String genreResultsFile = genreSubDir + "/" + "results_" + description + "_" + timestampText + "_"
						+ genre + ".txt";

				try {
					// reset to 10
					numFolds = 10;
					folds = getFoldsCrossValidationEqual(arffDir, genre);
					folds = prepareData(folds);
					crfppFilesDir = genreSubDir + "/crfpp";
					crfsuiteDir = genreSubDir + "/crfsuite";
					libSVMDir = genreSubDir + "/libsvm";
					if (useCrfpp) {
						new File(crfppFilesDir).mkdirs();
					}
					if (useCrfsuite) {
						new File(crfsuiteDir).mkdirs();
					}
					if (useLibLinear) {
						new File(libSVMDir).mkdirs();
					}
					performCrossValidation(folds, null, null, crfppFilesDir, crfsuiteDir, libSVMDir, genreResultsFile);
				} catch (Exception e) {
					e.printStackTrace();
					log.error("Error doing within-genre cross validation");
				}
			}

			// TODO: write results for the genres into one file
			// e.g., execute the python script from here
		}

		else {
			// test, genre, xFold settings
			crfppFilesDir = experimentSubDir + "/crfpp";
			crfsuiteDir = experimentSubDir + "/crfsuite";
			libSVMDir = experimentSubDir + "/libsvm";
			if (useCrfpp) {
				new File(crfppFilesDir).mkdirs();
			}
			if (useCrfsuite) {
				new File(crfsuiteDir).mkdirs();
			}
			if (useLibLinear) {
				new File(libSVMDir).mkdirs();
			}

			// file with results (for most settings)
			String resultsFile = experimentSubDir + "/" + "results_" + description + "_" + timestampText + ".txt";

			try {
				// if there is extra training data, treat as additional fold for
				// filtering
				// folds = prepareData(folds);
				Instances[] tempFolds = folds;
				if (extraTrainingData != null) {
					tempFolds = new Instances[folds.length + 1];
					for (int i = 0; i < folds.length; i++) {
						tempFolds[i] = folds[i];
					}
					tempFolds[folds.length] = extraTrainingData;
				}
				// filter
				tempFolds = prepareData(tempFolds);
				// copy back
				if (extraTrainingData != null) {
					for (int i = 0; i < folds.length; i++) {
						folds[i] = tempFolds[i];
					}
					extraTrainingData = tempFolds[tempFolds.length - 1];
				}
				performCrossValidation(folds, extraTrainingData, null, crfppFilesDir, crfsuiteDir, libSVMDir,
						resultsFile);
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Error doing cross validation");
			}

		}

	}

}
