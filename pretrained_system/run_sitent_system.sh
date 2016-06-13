#!/bin/bash

export LANG=en_US.utf8
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre

# the directory where the code is run
cd /proj/anne-phd/situation_entities/git_repo/sitent/de.uni-saarland.coli.sitent

###########################################
# Situation Entity Types labeling system  #
###########################################

# This script runs a pre-trained version of our system on the data of your choice.
# The system has been trained with the dev portion as described in the ACL 2016 paper.

# Should output at least version 8.
java -version

# In addition, you need to install CRF++: https://taku910.github.io/crfpp/
# !! Adapt the path to your installation below.


# -------------------------------- #
# Configuration of paths to data   #
# -------------------------------- #

# your data that should be processed, should be in raw text format.
# (The sample data are some texts from our held-out test set.)
INPUT=../sample_data/raw_text
# writes the XMI/ARFF of your test files here (for inspection or potential future processing)
OUTPUT=../sample_data/processed_xmi
ARFF=../sample_data/processed_arff

# TODO: create pre-trained models for all tasks, make this configurable here.
# -- run test setting once per task, use those models.
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

# Step 1: preprocessing and feature extraction (only for test part of data)
#	--> this generates XMIs
#   --> also segments the texts, approximating situation entity segmentation. This is done automatically  if no gold annotations are given. TODO: test this
java -jar target/de.uni-saarland.coli.sitent-0.0.1-SNAPSHOT-featureExtraction.jar -input $INPUT -output $OUTPUT -countability $COUNTABILITY_PATH -arff $ARFF -task $TASK
	
# Step 2: make the ARFF files compatible so Weka can process them
# TODO: add a different mode of making ARFF compatible to the training file!!

java -jar target/de.uni-saarland.coli.sitent-0.0.1-SNAPSHOT-arffCompatible.jar -input $ARFF -output $ARFF-compatible -sparse -classAttribute $TASK


# Step 3: run system: filter data according to configured features, apply system on the "test" data.

java -jar target/de.uni-saarland.coli.sitent-0.0.1-SNAPSHOT-experimenter.jar $EXPERIMENT_CONFIG $CRFPP_INSTALL_DIR

# Results will be in the $EXPERIMENT_CONFIG folder: look in the folders created for each experiment there!

# Step 4: TODO: how to output this in a nice format?
