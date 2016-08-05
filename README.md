# Situation entity type labeling system

---
```
UNDER CONSTRUCTION!
```
---

This repository contains the code for the system described in:

Annemarie Friedrich, Alexis Palmer and Manfred Pinkal. **Situation entity types: automatic classification of clause-level aspect.** August 2016. In Proceedings of the 54th Annual Meeting of the Association for Computational Linguistics (ACL). Berlin, Germany.

Please cite this paper if you use the system.

For more details related to this project, please see our project web site (http://www.coli.uni-saarland.de/projects/sitent).

## Getting started

### Using the pre-trained system

* You can find this system in the folder pretrained_system, instructions below / scripts are for Linux-based systems.
* Input: All you need are your documents in plain text format with the file ending ".txt". Place them in pretrained_system/sample_data/raw_text. You can also start by processing the example files provided there.
* Make sure to have Java 8 installed, and have JAVA_HOME point to it.
* Install [CRF++](https://taku910.github.io/crfpp/).
* Start the system by executing the following commands in your bash / terminal:

  ```
  cd <your-path-to>/sitent/pretrained_system
  ./run_sitent_system.sh <your-path-to-CRF++> sample_data
  ```
* Be patient. The system is parsing your data, which takes a while.
* The output of the system will be in pretrained_system/sample_data/labeled_text in a readable XML format. Data from the intermediate steps can also be found in the sample_data folder: the temp folder has the corresponding ARFF and XMI files. The folders starting with "class" contain the input and output files from CRF++.
* Of course, you can replace the folder containing your data (here: sample_data) with a folder of your choice, it only needs to contain a subfolder called raw_text with your input data in .txt format.
* If the system is too slow, try processing your data in chunks of up to 100 documents / parallelize this process.
* IMPORTANT: the system uses the methods of the ACL 2016 and ACL 2015 papers for situation entity type and for main referent genericity. It also outputs values for lexical aspectual class and habituality using maximum entropy models (which differ from the methods in the ACL 2014 / EMNLP 2015 papers, which use Random Forest classifiers for this task).
 
### Using the code




## Celex-based features

The NP-level features for countability that we described in our paper have been extracted from the [Celex database](https://catalog.ldc.upenn.edu/LDC96L14). For licensing reasons, we cannot distribute this resource. In this system, however, we substituted this lexicon with a version extracted from the freely available [WebCelex database](http://celex.mpi.nl/).

For the version of our system using the pre-trained model, we used the WebCelex features, so it can be simply run on any raw text data. We also included a pre-trained model using the Celex features. In order to use this version, you need to provide your own version of the countability features extracted from Celex. The feature values in the case of WebCelex are Y and N, the feature values when using Celex should be COUNT, UNCOUNT and AMBIG.

Example (two columns, tab-separated):
```
American	COUNT
American Indian	COUNT
American football	UNCOUNT
Americanism	AMBIG
```

The results using Celex are about 0.8-2.4% in better in accuracy compared to the results obtained using WebCelex. (On the held-out test set: 74.9% using Celex, 72.5% using WebCelex. Using 10-fold cross validation on the dev set: 76.3% using Celex, 75.5% using WebCelex).



### Errata
* The POS features are actually binary per segment, stating whether or not a POS tag occurs in a segment (rather than numeric as the paper says).
* The linguistic indicator features are numeric; however, CRF++ treats them as strings, which means that they have little impact due to sparsity. Results without using the linguistic indicator features are 75.8% in the 10-fold cross validation on dev, and 73.3% on the test set (vs. 76.3% / 74.9 when using them).
