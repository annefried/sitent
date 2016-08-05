import sys
import csv
import numpy as np

# read in linguistic indicators
original = {}
with open(sys.argv[1]) as csvfile:
    r = csv.reader(csvfile, delimiter="\t")
    colHeaders= next(r)
    for i in range(0,len(colHeaders)):
        original[colHeaders[i]] = []
    for row in r:
        original[colHeaders[0]].append(row[0])
        for i in range(1, len(row)):
            original[colHeaders[i]].append(float(row[i]))

# prepare bins for frequency
freq_bins = list(np.linspace(0,max(original["freq"]), num=10))
print(freq_bins)

# prepare bins for linguistic indicators
bins = list(np.linspace(0.0,1.0,num=10))
print(bins)
print(len(bins))

binned = {}
for l in original:
    print(l, len(original[l]))
    if l != "verb" and l!="freq":
        binned[l] = np.digitize(original[l], bins)
    if l == "freq":
        binned[l] = np.digitize(original[l], freq_bins)

with open(sys.argv[2], 'w') as csvfile:
    w = csv.writer(csvfile, delimiter='\t')
    w.writerow(colHeaders)
    for verb in original["verb"]:
        i = original["verb"].index(verb)
        row = [binned[l][i] for l in colHeaders if l != "verb"]
        row = [verb] + row
        #print(row)
        w.writerow(row)
        
