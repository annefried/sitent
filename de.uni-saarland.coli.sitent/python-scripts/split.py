'''
This script splits the files into train/dev and held-out test set, 
depending on the CSV file that has the 'fold' information for each
document.
'''


import shutil
import sys
import csv
import os

inDir = sys.argv[1]
csvFile = sys.argv[2]
outDirTrain = sys.argv[3]
outDirTest = sys.argv[4]
fileEnding = sys.argv[5]

splitInfo = {}
with open(csvFile) as f:
    csvFile = csv.reader(f, delimiter="\t")
    for row in csvFile:
        splitInfo[row[0][:-4]] = row[3] # removing ".txt" ending

if os.path.exists(outDirTrain):
    shutil.rmtree(outDirTrain)

if os.path.exists(outDirTest):
    shutil.rmtree(outDirTest)

os.makedirs(outDirTrain)
os.makedirs(outDirTest)

# traverse input directory
for filename in os.listdir(inDir):
    if not filename.endswith(fileEnding):
        continue
    filename = filename[:-1-len(fileEnding)]
    fileId = filename.split("_", 1)[1]
    if splitInfo[filename] == "train":
        shutil.copyfile(os.path.join(inDir, filename + "." + fileEnding), os.path.join(outDirTrain, filename + "." + fileEnding))
    if splitInfo[filename] == "test":
        shutil.copyfile(os.path.join(inDir, filename + "." + fileEnding), os.path.join(outDirTest, filename + "." + fileEnding))

# copy type system file (if using this for xmi)
if fileEnding == "xmi":
    shutil.copyfile(os.path.join(inDir, "typesystem.xml"), os.path.join(outDirTrain, "typesystem.xml")) 
    shutil.copyfile(os.path.join(inDir, "typesystem.xml"), os.path.join(outDirTest, "typesystem.xml"))
