#!/bin/bash

###########################################
# Situation Entity Types labeling system  #
###########################################

# This script runs a pre-trained version of our system on the data of your choice.
# The system has been trained with the dev portion as described in the ACL 2016 paper.

# Annemarie Friedrich, Alexis Palmer and Manfred Pinkal. Situation entity types:
# automatic classification of clause-level aspect. August 2016. In Proceedings of the
# 54th Annual Meeting of the Association for Computational Linguistics (ACL). Berlin,
# Germany.

# You need at least Java version 8.
java -version

# In addition, you need to install CRF++: https://taku910.github.io/crfpp/
# !! Adapt the path to your installation below.
CRFPP_INSTALL_DIR=$1

# By default, WebCelex countability features are used.
# Substitute this path with the path to your Celex countability file
# if you have the license and want to use Celex instead.
COUNTABILITY_PATH=resources/countability/webcelex_countabilityNouns.txt

# If you are using Celex, adapt the path to it above and simply change "webCelex"
# in the variable here to "celex". (See also the filenames in the models directory.)
CELEX="_webCelex"

export LANG=en_US.utf8

# -------------------------------- #
# Configuration of paths to data   #
# -------------------------------- #

EXPERIMENT_FOLDER=$2 #for example: sample_data. This folder needs to contain a subfolder called raw_text with the input (raw) text documents ending in .txt

# Your text data (in raw text format)
# (The sample data are some texts from our held-out test set.)
INPUT=$EXPERIMENT_FOLDER/raw_text

# copy config files
cp models/configs/* $EXPERIMENT_FOLDER

# XMI/ARFF with intermediate steps and results (for inspection or potential future processing)
XMI_OUTPUT=$EXPERIMENT_FOLDER/temp/processed_xmi
ARFF=$EXPERIMENT_FOLDER/temp/processed_arff
OUTPUT_XMI_FINAL=$EXPERIMENT_FOLDER/temp/processed_xmi_final

# The labeled output in XML format.
XML_OUTPUT=$EXPERIMENT_FOLDER/labeled_text

declare -a TASKS=("class_sitent_type" "class_main_referent" "class_habituality" "class_aspectual_class")
TASK="class_sitent_type"

# --------------- #
# Run the system  #
# --------------- #

# Step 1: preprocessing and feature extraction (only for test part of data)
#	--> this generates XMIs with the feature information
#   --> also segments the texts, approximating situation entity segmentation.
#       This is done automatically if no gold annotations are given.

java -jar jars/de.uni-saarland.coli.sitent-0.0.1-SNAPSHOT-featureExtraction.jar -input $INPUT -output $XMI_OUTPUT -countability $COUNTABILITY_PATH -arff $ARFF -task $TASK

# copy XMI
mkdir $OUTPUT_XMI_FINAL
cp -rf $XMI_OUTPUT/* $OUTPUT_XMI_FINAL/

for TASK in "${TASKS[@]}"
do
  	echo $TASK

	# Take a look at this example config file (it is the full system as used in the
	# ACL 2016 paper) if you want to make changes to the set of features used or to the
	# classification method.
	EXPERIMENT_CONFIG=config_$TASK.xml
	cp $EXPERIMENT_CONFIG $EXPERIMENT_FOLDER/

	PREDICTED_FEATURE_NAME=predicted_$TASK
	echo $MODEL
	echo $PREDICTED_FEATURE_NAME

	# Step 2: make the ARFF files compatible so Weka can process them
	# (copy the ARFF file containing the header for the training data to the ARFF directory)

	cp models/trainHeaderFiltered_$TASK$CELEX.arff $ARFF/
	java -jar jars/de.uni-saarland.coli.sitent-0.0.1-SNAPSHOT-arffCompatible.jar -input $ARFF -output $ARFF"_compatible" -sparse -classAttribute $TASK
	rm $ARFF/trainHeaderFiltered_$TASK$CELEX.arff
	rm $ARFF"_compatible"/trainHeaderFiltered_$TASK$CELEX.arff

	# Step 3: run system: filter text data according to configured features, classify instances.
	java -jar jars/de.uni-saarland.coli.sitent-0.0.1-SNAPSHOT-experimenter.jar $EXPERIMENT_FOLDER/$EXPERIMENT_CONFIG $CRFPP_INSTALL_DIR $TASK $MODEL models/trainHeaderFiltered_$TASK$CELEX.arff	

	# Step 4: add predictions to XMI
	java -jar jars/de.uni-saarland.coli.sitent-0.0.1-SNAPSHOT-collectPredictions.jar -input $OUTPUT_XMI_FINAL -outputXmi $OUTPUT_XMI_FINAL -featureName $PREDICTED_FEATURE_NAME -predictions $EXPERIMENT_FOLDER/$TASK/crfpp/predictions.csv

done


# Step 4: output in XML format (XMI with predictions can be found at  ..)
echo "Writing XML files ..."
mkdir $XML_OUTPUT
java -jar jars/de.uni-saarland.coli.sitent-0.0.1-SNAPSHOT-xmlWriter.jar -input $OUTPUT_XMI_FINAL -output $XML_OUTPUT

