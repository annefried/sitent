#!/bin/bash

export LANG=en_US.utf8
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre

# the directory where the code is run
cd /proj/anne-phd/situation_entities/git_repo/sitent/de.uni-saarland.coli.sitent

###########################################
# Situation Entity Types labeling system  #
###########################################

# This script is for advanced users who want to reproduce our experiments
# or make modifications to the system. If you just want to run the system
# on your data, please follow the steps described in the README.txt.

# Should output at least version 8.
java -version

# In addition, you need to install CRF++: https://taku910.github.io/crfpp/


# -------------------------------- #
# Configuration of paths to data   #
# -------------------------------- #

# this is all the test+train data from ACL 2016 paper
INPUT=../annotated_corpus/raw_text
# writes the XMI/ARFF files here (for inspection or potential future processing)
OUTPUT=../annotated_corpus/processed_xmi
ARFF=../annotated_corpus/processed_arff
# gold standard annotations for the above input files
ANNOTATIONS=../annotated_corpus/annotations_xml
# info which file is in dev / test fold for MASC+Wiki as used in ACL 2016 paper
TRAIN_TEST_SPLIT=../annotated_corpus/train_test_split.csv
# could also be class_aspectual_class, class_main_referent or class_habituality
TASK=class_sitent_type

# --------------------------------------------- #
# Configuration for experiment / labeling mode  #
# --------------------------------------------- #
# Take a look at this example config file (it is the full system as used in the
# ACL 2016 paper
EXPERIMENT_CONFIG=../annotated_corpus/experiments_data/config-a-b.xml

# ----------------------------------- #
# Configuration of paths for system   #
# ----------------------------------- #
COUNTABILITY_PATH=resources/countability/webcelex_countabilityNouns.txt
# Here, you need to put the directory where you installed CRF++ on your 
# system, see above.
CRFPP_INSTALL_DIR=/proj/anne-phd/software/CRF++-0.58-fixed

# --------------- #
# Run the system  #
# --------------- #

# Step 1: preprocessing and feature extraction
#	- for labeled training/dev corpus
#	- for labeled held-out test set
#	--> this generates XMIs
java -jar target/de.uni-saarland.coli.sitent-0.0.1-SNAPSHOT-featureExtraction.jar -input $INPUT -output $OUTPUT -annotations $ANNOTATIONS -countability $COUNTABILITY_PATH -arff $ARFF -task $TASK
	
# Step 2: make the ARFF files compatible so Weka can process them

java -jar target/de.uni-saarland.coli.sitent-0.0.1-SNAPSHOT-arffCompatible.jar -input $ARFF -output $ARFF-compatible -sparse -classAttribute $TASK

# Step 3: create test/train sets

rm -rf $ARFF"_split"
mkdir $ARFF"_split"
python3 python-scripts/split.py $ARFF-compatible $TRAIN_TEST_SPLIT $ARFF"_split"/dev $ARFF"_split"/test "arff"

# Step 4: run experiment: filter data according to configured features, train classifier, develop using 10-fold-CV, or apply on test set.

java -jar target/de.uni-saarland.coli.sitent-0.0.1-SNAPSHOT-experimenter.jar $EXPERIMENT_CONFIG $CRFPP_INSTALL_DIR

# Results will be in the $EXPERIMENT_CONFIG folder: look in the folders created for each experiment there!
