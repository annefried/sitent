import sys
import re
import os
import numpy

def readCategoryInfo(filename):
    cats = {}
    with open(filename) as f:
        for line in f:
            if line.strip() != "":
                cols = line.strip().split("\t")
                cats[cols[1] + "_" + cols[0]] = cols[1]
    return cats 

def getInstanceIDIndex(filename):
    with open(filename) as f:
        line = f.readline()
        line = line.split("\t")
        i = 0
        for col in line:
            if re.match(".*txt_\d.*", col):
                return i
            i+=1

def readData(filename, data):
    instIdCol = getInstanceIDIndex(filename)
    with open(filename) as f:
        for line in f:
            if line.strip() != "":
                cols = line.strip().split("\t")
                # instance id, predicted, gold
                # print(cols[instIdCol], cols[-1], cols[-2])
                data[cols[instIdCol]] = (cols[-1], cols[-2])
            
def getDataSubset(data, cats, cat):
    newData = {}
    for key in data:
        filename = key.rsplit("_", 1)[0]
        if cats[filename] == cat:
            newData[key] = data[key]
    return newData

def computeStatistics(data):
    # determine the set of values
    valueList = ['EVENT', 'GENERALIZING_SENTENCE', 'GENERIC_SENTENCE', 'IMPERATIVE', 'QUESTION', 'REPORT', 'STATE']
    #valueSet = set()
    #for entry in data.values():
    #    valueSet.add(entry[0])
    #    valueSet.add(entry[1])
    #valueList = sorted(list(valueSet))
    numValues = len(valueList)
    
    # confusion matrix
    m = [[0 for i in range(numValues)] for j in range(numValues)]
 
    # add data into matrix
    accuracy = 0
    for key in data:
        pred, gold = data[key]
        m[valueList.index(gold)][valueList.index(pred)] += 1
        if pred == gold:
            accuracy += 1
    
    accuracy /= len(data)
    print("\taccuracy: %.1f" % (accuracy*100))


    #for row in m:
    #    print(row)

    # precision, recall
    precisions = []
    recalls = []
    fscores = []
    for value in valueList:
        idx = valueList.index(value)
        correct = m[idx][idx]
        totalGold = 0
        totalPred = 0
        
        for i in range(numValues):
            # rowsum is total gold
            totalGold += m[idx][i]
            # colsum is total predicted
            totalPred += m[i][idx]

        if totalPred == 0:
            p = 100
        else :
            p = float(correct)/float(totalPred)*100
        if totalGold == 0:
            r = 100
        else:
            r = correct/totalGold*100.0
        if p!=0 or r!= 0:
            f = 2*p*r/(p+r)
            #print(value, "%.1f" % p, "%.1f" % r, "%.1f" % f)  
            precisions.append(p)
            recalls.append(r)
            fscores.append(f)

        # skip other cases

    avg_p = sum(precisions)/len(valueList)
    avg_r = sum(recalls)/len(valueList)
    f_of_macro = 2*avg_p*avg_r/(avg_p+avg_r)
    print("\tPrecision: %.1f" % avg_p)
    print("\tRecall: %.1f" % avg_r)
    print("\tF-of-macro: %.1f" % f_of_macro)
    



if __name__ == "__main__":

    categoryFile = sys.argv[2]
    cats = readCategoryInfo(categoryFile)
    print(cats)

    data = {}

    predictionsDir = sys.argv[1]
    for filename in os.listdir(predictionsDir):
        if filename.startswith("predictions"):
            print(filename)
            readData(os.path.join(predictionsDir, filename), data)
    print(len(data))

    for cat in set(cats.values()):
        d = getDataSubset(data, cats, cat)
        print(cat, len(d))
        computeStatistics(d)
