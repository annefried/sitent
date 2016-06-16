# Situation entity type labeling system

This repository contains the code for the system described in:

Annemarie Friedrich, Alexis Palmer and Manfred Pinkal. **Situation entity types: automatic classification of clause-level aspect.** August 2016. In Proceedings of the 54th Annual Meeting of the Association for Computational Linguistics (ACL). Berlin, Germany.

Please cite this paper if you use the system.

For more details related to this project, please see our project web site (http://www.coli.uni-saarland.de/projects/sitent).

## Celex-based features

The NP-level features for countability that we described in our paper have been extracted from the Celex database (https://catalog.ldc.upenn.edu/LDC96L14). For licensing reasons, we cannot distribute this resource. In this system, however, we substituted this lexicon with a version extracted from the freely available WebCelex database (http://celex.mpi.nl/).

For the version of our system using the pre-trained model, we used the WebCelex features, so it can be simply run on any raw text data. We also included a pre-trained model using the Celex features. In order to use this version, you need to provide your own version of the countability features extracted from Celex. The feature values in the case of WebCelex are Y and N, the feature values when using Celex should be COUNT, UNCOUNT and AMBIG.

Example (two columns, tab-separated):
```
American	COUNT
American Indian	COUNT
American football	UNCOUNT
Americanism	AMBIG
```

The results using Celex are about 0.8-2.4% in better in accuracy compared to the results obtained using WebCelex. (On the held-out test set: 74.9% using Celex, 72.5% using WebCelex. Using 10-fold cross validation on the dev set: 76.3% using Celex, 75.5% using WebCelex).
