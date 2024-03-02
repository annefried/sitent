"""
This script adds the situation entity gold annotations to the raw XMIs and
parses the files with spacy-udpipe to add UD dependency relations and morphological features.
The output format matches the input format XML1.1 used by IINCEpTION.
"""

import os, sys
from puima.collection_utils import DocumentCollection
from puima.pycas import PyCas, Annotation, AnnotationSpan
import spacy_udpipe
from lxml import etree
import io, regex


IN_PATH = "annotated_corpus/gold_xmi"
ANNOT_PATH = "annotated_corpus/annotations_xml"
OUT_PATH = "annotated_corpus/annotated_xmi"

# UIMA types
TOKEN_TYPE = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"
SENT_TYPE = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence"
SITUATION_TYPE = "webanno.custom.SituationEntities"
SE_LINK_TYPE = "webanno.custom.SE_Relation"
LEMMA_TYPE = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma"
POS_TYPE = "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"
FEAT_TYPE = "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures"
DEP_TYPE = "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency"


print("Loading Spacy UDPipe model")
spacy_udpipe.download("en") # download English model
nlp = spacy_udpipe.load("en")
print("Done.")


# Read the XML version
xmlParser = etree.XMLParser(remove_blank_text=True)
annotations = {}
for filename in os.listdir(ANNOT_PATH):
    if filename == "TypeSystem.xml":
        continue
    xmi_input_file = os.path.join(ANNOT_PATH, filename)
    with io.open(xmi_input_file, 'r', encoding="utf-8") as f:
        annotations[filename.replace(".xml", "")] = etree.parse(f, xmlParser) # DOM

    
# Read the XMI collection (which is based on the original raw texts and the Stanford tokenizer
# for sentence and token segmentation, such that the tokenization matches the annotation).
coll = DocumentCollection(IN_PATH)
for doc_name in sorted(coll.docs):
    doc = coll.docs[doc_name]
    print("\n", doc_name)
    dom = annotations[doc_name.replace(".txt", "")]
    # iterate over annotations
    segments = dom.xpath('//document/segment')
    for s in segments:
        instanceid = s.attrib["instanceid"]
        begin, end = int(s.attrib["begin"]), int(s.attrib["end"])
        text = None

        # add a Situation annotation (webanno.custom.Situation)
        se_annot = Annotation(doc, None, SITUATION_TYPE, begin, end)
        se_annot.set_feature_value("instanceid", instanceid)
        se_annot.add_to_indices()


        for child in s:
            if child.tag == "text":
                text = child.text

            if child.tag == "mainVerb":
                mv_begin, mv_end = int(child.attrib["begin"]), int(child.attrib["end"])
                mv_annot = Annotation(doc, None, SITUATION_TYPE, mv_begin, mv_end)
                mv_annot.add_to_indices()
                # create linke
                mv_link = Annotation(doc, None, SE_LINK_TYPE, se_annot.begin, se_annot.end) # dependent is the situation: easier for selection!
                mv_link.set_feature_value("label", "main_verb")
                mv_link.set_feature_value("Dependent", se_annot, valueRefersToAnnotation=True)
                mv_link.set_feature_value("Governor", mv_annot, valueRefersToAnnotation=True)
                mv_link.add_to_indices()

            if child.tag == "mainReferent":
                mr_begin, mr_end = int(child.attrib["begin"]), int(child.attrib["end"])
                mr_annot = Annotation(doc, None, SITUATION_TYPE, mr_begin, mr_end)
                mr_annot.add_to_indices()
                # create link
                mr_link = Annotation(doc, None, SE_LINK_TYPE, se_annot.begin, se_annot.end) # dependent is the situation: easier for selection!
                mr_link.set_feature_value("label", "main_referent")
                mr_link.set_feature_value("Dependent", se_annot, valueRefersToAnnotation=True)
                mr_link.set_feature_value("Governor", mr_annot, valueRefersToAnnotation=True)
                mr_link.add_to_indices()

            if child.tag == "annotation":
                annotator = child.attrib["annotator"]
                if annotator == "gold": # not adding the remaining ones, can be added later using the IDs
                    if "seType" in child.attrib:
                        se_annot.set_feature_value("SE_Labels", "SE_Type="+child.attrib["seType"])
                    if "mainReferentGenericity" in child.attrib:
                        mr_annot.set_feature_value("SE_Labels", "Genericity="+child.attrib["mainReferentGenericity"])
                    if "habituality" in child.attrib:
                        prefix = ""
                        if mv_annot.get_feature_value("SE_Labels"):
                            prefix = mv_annot.get_feature_value("SE_Labels") + "|"
                        mv_annot.set_feature_value("SE_Labels", prefix + "Habituality=" + child.attrib["habituality"])
                    if "mainVerbAspectualClass" in child.attrib:
                        prefix = ""
                        if mv_annot.get_feature_value("SE_Labels"):
                            prefix = mv_annot.get_feature_value("SE_Labels") + "|"
                        mv_annot.set_feature_value("SE_Labels", prefix + "AspectualClass=" + child.attrib["mainVerbAspectualClass"])
    #break # for development

# Adding UD features and dependency annotations using UDPipe
num_tokens_matched = 0
num_tokens_not_matched = 0
for doc_name in coll.docs:
    doc = coll.docs[doc_name]
    print(doc_name)
    for sent_annot in doc.select_annotations(SENT_TYPE):
        #print("SENTENCE:", doc.get_covered_text(sent_annot))

        # Spacy removes newlines within sentences! Hence, processing these parts of the sentences separately using Spacy (may introduce parsing errors)
        s = sent_annot.begin
        #sent_parts = doc.get_covered_text(sent_annot).split("\n")
        #for i, sent_part in enumerate(sent_parts):
        #    if sent_part == "":
        #        s + 1
        #        continue
        #    if i > 0:
        #        s = s+len(sent_parts[i-1]) + 2 # 2 =  newline characters so far
            
        spacydoc = nlp(doc.get_covered_text(sent_annot)) # TODO: could not get this to run with pre-tokenized text, but this should work according to the documentation?
        analysed_doc = nlp(spacydoc)
        spacyTok_to_xmiTok = {}
        spacyTok_to_xmiPos = {}

        for token in analysed_doc:
            x = 0
            while True:
                try:
                    #print("covered text by current spans:", doc.get_covered_text(AnnotationSpan(s+token.idx, s+token.idx+len(token.text))))
                    token_annot = next(doc.select_covered(TOKEN_TYPE, AnnotationSpan(s+x+token.idx, s+x+token.idx+len(token.text))))
                    
                    spacyTok_to_xmiTok[token] = token_annot
                    assert doc.get_covered_text(token_annot) == token.text
                    #print("found match:", token.text, doc.get_covered_text(token_annot), token.tag_)
                    num_tokens_matched += 1
                    break
                except:
                    token_annot_text = doc.get_covered_text(token_annot)
                    # happens for few special hyphenated cases:
                    # if one is contained within the other: does not matter that much
                    #print(token.text, "||", token_annot_text)
                    if token_annot_text in token.text:
                        break
                    try:
                        token_annot = next(doc.select_covering(TOKEN_TYPE, AnnotationSpan(s+x+token.idx, s+x+token.idx+len(token.text))))
                        token_annot_text = doc.get_covered_text(token_annot)
                        if token.text in token_annot_text:
                            break
                    except:
                        # did not find a covering annotation
                        pass
                    # otherwise: more severe mismatch, newline/spaces issue?
                    x += 1
                    if x>10:
                        break
            if x>10:
                num_tokens_not_matched += 1
                print("did not find token for:", token.text)
                #print(doc.get_covered_text(sent_annot))

        for token in analysed_doc:
            #print(token.__dir__()) # check available attributes
            if not token in spacyTok_to_xmiTok:
                continue

            try:
                token_annot = spacyTok_to_xmiTok[token]
                assert doc.get_covered_text(token_annot) == token.text

                lemma_annot = Annotation(doc, None, LEMMA_TYPE, token_annot.begin, token_annot.end)
                lemma_annot.set_feature_value("value", token.lemma_)
                lemma_annot.add_to_indices()
                feat_annot = Annotation(doc, None, FEAT_TYPE, token_annot.begin, token_annot.end)
                feat_annot.set_feature_value("value", token.morph)
                feat_annot.add_to_indices()
                pos_annot = Annotation(doc, None, POS_TYPE, token_annot.begin, token_annot.end)
                pos_annot.set_feature_value("coarseValue", token.pos_)
                pos_annot.set_feature_value("PosValue", token.tag_)
                pos_annot.add_to_indices()
                token_annot.set_feature_value("pos", pos_annot, valueRefersToAnnotation=True)
                spacyTok_to_xmiPos[token] = pos_annot
            except StopIteration:
                pass # a few are missing for whatever reason
                #print("did not find a XMI token for", token)
            except AssertionError:
                # did not find matching token
                pass

        for token in analysed_doc:
            if token not in spacyTok_to_xmiTok:
                continue

            token_annot = spacyTok_to_xmiTok[token]

            # add dependencies
            if token.head in spacyTok_to_xmiPos and token in spacyTok_to_xmiPos and \
                token.head in spacyTok_to_xmiTok and token in spacyTok_to_xmiTok: # if POS does not exist, rendering in Inception not possible
                dep_rel = Annotation(doc, None, DEP_TYPE, token_annot.begin, token_annot.end)
                dep_rel.set_feature_value("flavor", "basic")
                dep_rel.set_feature_value("Dependent", spacyTok_to_xmiTok[token], valueRefersToAnnotation=True)
                dep_rel.set_feature_value("Governor", spacyTok_to_xmiTok[token.head], valueRefersToAnnotation=True)
                dep_rel.set_feature_value("DependencyType", token.dep_)
                dep_rel.add_to_indices()

    #break   # for development

coll.serialize(OUT_PATH)
print(num_tokens_matched, num_tokens_not_matched)