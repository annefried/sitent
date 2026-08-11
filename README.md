# Situation entity type labeling system

---
```
Feedback / questions appreciated!
```
---

This repository contains the code for the system described in:

Annemarie Friedrich, Alexis Palmer and Manfred Pinkal. **Situation entity types: automatic classification of clause-level aspect.** August 2016. In Proceedings of the 54th Annual Meeting of the Association for Computational Linguistics (ACL). Berlin, Germany.

Please cite this paper if you use the system.

For more details related to this project, please see our project web site (http://www.coli.uni-saarland.de/projects/sitent).

## Overview
Linguistic expressions form patterns in discourse. Passages of text can be analyzed in terms of the individuals, concepts, times, and situations that they introduce to the discourse. In this work, we focus on **situation entities**, which are expressed at the clause level.

In her work *Modes of Discourse* (2003, a shorter version can be found [here](http://uts.cc.utexas.edu/~carlota/papers/Iowa.pdf)), Carlota Smith distinguishes the following situation entity types:

1. **STATE**: introducing specific properties of specific individuals to the discourse.  
   *Carl is a tenacious fellow.*

2. **EVENT**: introducing a specific event to the discourse.  
   *The lobster won the quadrille.*

3. **GENERALIZING SENTENCE**: reporting regularities related to specific individuals.  
   *Mary often feeds my cats.*

4. **GENERIC SENTENCES**: making statements about kinds.  
   *The lion has a bushy tail.*

5. **ABSTRACT ENTITIES**: a type of embedded situation, the clausal complements of verbs of knowledge or belief.  
   *I know that Mary refused the offer.*

Although these categories are clearly distinct from one another on theoretical grounds, in practice it can be difficult to cleanly draw boundaries between them. Our main research questions are:

1. **Assessment of the applicability of classification of situation entity types** as described by Smith (2003): to what extent can situations be classified easily, which borderline cases occur, and how do humans perform on this task?

2. **Training, development, and evaluation of automatic systems** classifying situation entities, as well as sub-tasks which have (partially) been studied by the NLP community, but for which no large annotated corpora are available (for example, automatically predicting the fundamental aspectual class of verbs in context (Friedrich and Palmer, ACL 2014) or the genericity of clauses and noun phrases (Friedrich and Pinkal, ACL 2015)).

3. **Providing a foundation for analysis of the theory of Discourse Modes.** Smith (2003) proposes the modes of Narrative, Report, Description, Informative, and Argument at the level of passages. The modes differ by their kind of progression and distribution of situation entity types.


## Getting started

### Data formats

The annotated data is available under `annotated_corpus`. We include the labels of the three annotators a well as the majority labels used in the machine learning experiments.

_IMPORTANT:_ The annotators did not see the main referent or main verb annotations but inferred them on-the-fly during annotation. The span annotations provided in the files have been created automatically post-hoc using the Stanford parser, which is not always accurate. Please note that this does not have an effect on the accuracy of the annotations because the annotators did not see them. You can use your favorite parsing or extraction tool to infer the actual main referent/main verb instead.

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
* To reproduce the results in the paper or to work on your own experiments, take a look at the code base in de.uni-saarland.coli.sitent. The script run_sitent.sh in that folder has pointers to the entry points and necessary configurations.



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


### Contributors
* Annemarie Friedrich (researcher, Ph.D. candidate)
* Alexis Palmer (researcher, post-doc)
* Manfred Pinkal (advisor, professor)
* Melissa Peate Sørensen (annotator, intern)
* Kleio-Isidora Mavridou (annotator, M.Sc. thesis)
* Liesa Heuschkel (M.Sc. thesis)
* Christine Bocionek (annotator)
* Fernando Ardente (annotator)
* Damyana Gateva (annotator)
* Ruth Kühn (annotator)
* Ambika Kirkland (annotator)
* Steffen Witt (annotator)



## Additional files and resources
* The version of our detailed annotation manual that was used to annotated the data in this repository can be found here:
Annemarie Friedrich, Kleio-Isidora Mavridou and Alexis Palmer: [Situation entity types (annotation manual)(https://github.com/annefried/sitent/blob/master/additional_files_and_data/sitent_manual_v1.1.pdf). Version 1.1, April 2015.
* [Slides](https://github.com/annefried/sitent/blob/master/additional_files_and_datales_and_data/slides_potsdam.pdf) from Potsdam computational linguistics colloquium (November 2014)
* [Poster](https://github.com/annefried/sitent/blob/master/additional_files_and_data/poster_mmci-2014.pdf) from the MMCI Retreat 2014, giving an overview of our annotation schema and recent results.
* [Poster](https://github.com/annefried/sitent/blob/master/additional_files_and_data/poster-dfgs2015.pdf) from DGfS 2015, Annotation and automatic classification of situation entity types (abstract).

Please cite the respective papers if you are using these resources. Thank you.

* [AspMASC.csv](https://github.com/annefried/sitent/blob/master/additional_files_and_data/AspMASC.csv) and [Ashttps://github.com/annefried/sitent/blob/master/additional_files_and_datales_and_data/AspAmbig.csv): data annotated for aspectual class (please cite: Annemarie Friedrich and Alexis Palmer. 2014. [Automatic prediction of aspectual class of verbs in context.](https://aclanthology.org/P14-2085/) In Proceedings of the 52nd Annual Meeting of the Association for Computational Linguistics (Volume 2: Short Papers), pages 517–523, Baltimore, Maryland. Association for Computational Linguistics.)
* [linguistic-indicators-Gigaword-AFE-XIE.csv](https://github.com/annefried/sitent/blob/master/additional_files_and_data/linguistic-indicators-Gigaword-AFE-XIE.csv): database of linguistic indicator values (see ACL 2014 paper) computed from Gigaword (AFE and XIE sections), extracted using the method described in: Siegel, E. V., & McKeown, K. R. (2000). [Learning methods to combine linguistic indicators: Improving aspectual classification and revealing linguistic insights.](https://aclanthology.org/W97-0318/) Computational Linguistics, 26(4), 595-628.
* [WikiGenerics corpus v2.0](https://github.com/annefried/sitent/blob/master/additional_files_and_data/WikiGenerics v2.0.zip): annotated for genericity on NP- and clause-level (see LAW 2015 and ACL 2015 papers) and clausal aspect (see EMNLP 2015 paper). The MASC data annotated with genericity is the same as the SitEnt data (annotated for main referent genericity).