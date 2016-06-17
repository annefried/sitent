Brown clusters

They are induced as described in the paper:
	Joseph Turian, Lev-Arie Ratinov and Yoshua Bengio (2010) "WORD
	REPRESENTATIONS: A SIMPLE AND GENERAL METHOD FOR SEMI-SUPERVISED
	LEARNING",
on the RCV1 corpus, cleaned as described in the paper (roughly 37M words of News text).

	brown-rcv1.clean.tokenized-CoNLL03.txt-c*-freq1.txt
		Brown clusters for a particular number of induced classes.
		The first column is the name of the cluster.
		The second column is the word.
		The third column is the frequency of the word in the Corpus.
		You should use prefixes of length 4, 6, 10, and 20 of
		the cluster name as the word's "features", to replicate
		our conditions.
