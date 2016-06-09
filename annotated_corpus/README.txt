#############################
# Situation Entities corpus #
#############################

This data set contains text corpora annotated for situation entities, lexical aspectual class,
habituality and genericity. For the related publications and appropriate citations when using
this data, please see our project web site: http://www.coli.uni-saarland.de/projects/sitent

Text have been taken from Wikipedia and the written part of MASC (http://www.anc.org/data/masc).


raw_texts:
    The raw text files. Prefixes indicate the MASC genre of each document (or wiki).

categories.csv
    This file contains for each file (a) its MASC genre or whether it is from Wikipedia,
    and (b) if it is a Wikipedia document, further a categorization according to several
    categories: animals, biographies, games etc.

train_test_split.csv
    This file contains the split of the documents into a development (training) set and a
    test set, as used in the ACL 2016 paper.

annotations_xml
    This folder contains the annotated corpus in XML format including the gold standard
    constructed via majority voting for the ACL 2016 paper and the annotations as given
    by each annotator (for computing agreement). If an annotator gave multiple values for
    SE type, lexical aspectual class, habituality or main referent genericity, the values
    are separated by colons. Note that the main verb and main referent given for each segment
    were identified automatically (this may contain some noise).
