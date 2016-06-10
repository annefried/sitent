

/* First created by JCasGen Wed Jun 08 16:31:35 CEST 2016 */
package sitent.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;


/** A segment as created automatically by SPADE + our post-processing script.
 * Updated by JCasGen Fri Jun 10 18:03:49 CEST 2016
 * XML source: /local/gitRepos/sitent/de.uni-saarland.coli.sitent/src/main/java/sitent/types/SitEntTypeSystem.xml
 * @generated */
public class Segment extends ClassificationAnnotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Segment.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Segment() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Segment(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Segment(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Segment(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: docid

  /** getter for docid - gets docid
   * @generated
   * @return value of the feature 
   */
  public String getDocid() {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_docid == null)
      jcasType.jcas.throwFeatMissing("docid", "sitent.types.Segment");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Segment_Type)jcasType).casFeatCode_docid);}
    
  /** setter for docid - sets docid 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocid(String v) {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_docid == null)
      jcasType.jcas.throwFeatMissing("docid", "sitent.types.Segment");
    jcasType.ll_cas.ll_setStringValue(addr, ((Segment_Type)jcasType).casFeatCode_docid, v);}    
   
    
  //*--------------*
  //* Feature: segid

  /** getter for segid - gets segmentid in the database
   * @generated
   * @return value of the feature 
   */
  public int getSegid() {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_segid == null)
      jcasType.jcas.throwFeatMissing("segid", "sitent.types.Segment");
    return jcasType.ll_cas.ll_getIntValue(addr, ((Segment_Type)jcasType).casFeatCode_segid);}
    
  /** setter for segid - sets segmentid in the database 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSegid(int v) {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_segid == null)
      jcasType.jcas.throwFeatMissing("segid", "sitent.types.Segment");
    jcasType.ll_cas.ll_setIntValue(addr, ((Segment_Type)jcasType).casFeatCode_segid, v);}    
   
    
  //*--------------*
  //* Feature: situationAnnotations

  /** getter for situationAnnotations - gets List of situation annotations linked to this segment.
   * @generated
   * @return value of the feature 
   */
  public FSList getSituationAnnotations() {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_situationAnnotations == null)
      jcasType.jcas.throwFeatMissing("situationAnnotations", "sitent.types.Segment");
    return (FSList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Segment_Type)jcasType).casFeatCode_situationAnnotations)));}
    
  /** setter for situationAnnotations - sets List of situation annotations linked to this segment. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSituationAnnotations(FSList v) {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_situationAnnotations == null)
      jcasType.jcas.throwFeatMissing("situationAnnotations", "sitent.types.Segment");
    jcasType.ll_cas.ll_setRefValue(addr, ((Segment_Type)jcasType).casFeatCode_situationAnnotations, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: predictionNoSituation

  /** getter for predictionNoSituation - gets set to true if classifier predicts that segment does not contain a situation.
   * @generated
   * @return value of the feature 
   */
  public boolean getPredictionNoSituation() {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_predictionNoSituation == null)
      jcasType.jcas.throwFeatMissing("predictionNoSituation", "sitent.types.Segment");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((Segment_Type)jcasType).casFeatCode_predictionNoSituation);}
    
  /** setter for predictionNoSituation - sets set to true if classifier predicts that segment does not contain a situation. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setPredictionNoSituation(boolean v) {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_predictionNoSituation == null)
      jcasType.jcas.throwFeatMissing("predictionNoSituation", "sitent.types.Segment");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((Segment_Type)jcasType).casFeatCode_predictionNoSituation, v);}    
   
    
  //*--------------*
  //* Feature: mainVerb

  /** getter for mainVerb - gets The main verb Token.
   * @generated
   * @return value of the feature 
   */
  public Annotation getMainVerb() {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_mainVerb == null)
      jcasType.jcas.throwFeatMissing("mainVerb", "sitent.types.Segment");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Segment_Type)jcasType).casFeatCode_mainVerb)));}
    
  /** setter for mainVerb - sets The main verb Token. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setMainVerb(Annotation v) {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_mainVerb == null)
      jcasType.jcas.throwFeatMissing("mainVerb", "sitent.types.Segment");
    jcasType.ll_cas.ll_setRefValue(addr, ((Segment_Type)jcasType).casFeatCode_mainVerb, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: mainReferent

  /** getter for mainReferent - gets Note that the main referent is not necessarily a token within the segment.
   * @generated
   * @return value of the feature 
   */
  public Annotation getMainReferent() {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_mainReferent == null)
      jcasType.jcas.throwFeatMissing("mainReferent", "sitent.types.Segment");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Segment_Type)jcasType).casFeatCode_mainReferent)));}
    
  /** setter for mainReferent - sets Note that the main referent is not necessarily a token within the segment. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setMainReferent(Annotation v) {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_mainReferent == null)
      jcasType.jcas.throwFeatMissing("mainReferent", "sitent.types.Segment");
    jcasType.ll_cas.ll_setRefValue(addr, ((Segment_Type)jcasType).casFeatCode_mainReferent, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: tokens

  /** getter for tokens - gets The list of tokens for this "segment", for the situation entity segmentation method based on dependency parses.
   * @generated
   * @return value of the feature 
   */
  public FSList getTokens() {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_tokens == null)
      jcasType.jcas.throwFeatMissing("tokens", "sitent.types.Segment");
    return (FSList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Segment_Type)jcasType).casFeatCode_tokens)));}
    
  /** setter for tokens - sets The list of tokens for this "segment", for the situation entity segmentation method based on dependency parses. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setTokens(FSList v) {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_tokens == null)
      jcasType.jcas.throwFeatMissing("tokens", "sitent.types.Segment");
    jcasType.ll_cas.ll_setRefValue(addr, ((Segment_Type)jcasType).casFeatCode_tokens, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    