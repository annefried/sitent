

/* First created by JCasGen Wed Jun 08 16:31:35 CEST 2016 */
package sitent.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.jcas.tcas.Annotation;


/** A span of text representing a situation (may consist of merged segments).
 * Updated by JCasGen Fri Jun 10 18:03:49 CEST 2016
 * XML source: /local/gitRepos/sitent/de.uni-saarland.coli.sitent/src/main/java/sitent/types/SitEntTypeSystem.xml
 * @generated */
public class Situation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Situation.class);
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
  protected Situation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Situation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Situation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Situation(JCas jcas, int begin, int end) {
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
  //* Feature: annotator

  /** getter for annotator - gets 
   * @generated
   * @return value of the feature 
   */
  public String getAnnotator() {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_annotator == null)
      jcasType.jcas.throwFeatMissing("annotator", "sitent.types.Situation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Situation_Type)jcasType).casFeatCode_annotator);}
    
  /** setter for annotator - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setAnnotator(String v) {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_annotator == null)
      jcasType.jcas.throwFeatMissing("annotator", "sitent.types.Situation");
    jcasType.ll_cas.ll_setStringValue(addr, ((Situation_Type)jcasType).casFeatCode_annotator, v);}    
   
    
  //*--------------*
  //* Feature: mainReferent

  /** getter for mainReferent - gets genericity of main referent.
   * @generated
   * @return value of the feature 
   */
  public StringList getMainReferent() {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_mainReferent == null)
      jcasType.jcas.throwFeatMissing("mainReferent", "sitent.types.Situation");
    return (StringList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Situation_Type)jcasType).casFeatCode_mainReferent)));}
    
  /** setter for mainReferent - sets genericity of main referent. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setMainReferent(StringList v) {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_mainReferent == null)
      jcasType.jcas.throwFeatMissing("mainReferent", "sitent.types.Situation");
    jcasType.ll_cas.ll_setRefValue(addr, ((Situation_Type)jcasType).casFeatCode_mainReferent, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: aspectualClass

  /** getter for aspectualClass - gets lexical aspectual class of verb constellation.
   * @generated
   * @return value of the feature 
   */
  public StringList getAspectualClass() {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_aspectualClass == null)
      jcasType.jcas.throwFeatMissing("aspectualClass", "sitent.types.Situation");
    return (StringList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Situation_Type)jcasType).casFeatCode_aspectualClass)));}
    
  /** setter for aspectualClass - sets lexical aspectual class of verb constellation. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setAspectualClass(StringList v) {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_aspectualClass == null)
      jcasType.jcas.throwFeatMissing("aspectualClass", "sitent.types.Situation");
    jcasType.ll_cas.ll_setRefValue(addr, ((Situation_Type)jcasType).casFeatCode_aspectualClass, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: habituality

  /** getter for habituality - gets habituality of clause
   * @generated
   * @return value of the feature 
   */
  public StringList getHabituality() {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_habituality == null)
      jcasType.jcas.throwFeatMissing("habituality", "sitent.types.Situation");
    return (StringList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Situation_Type)jcasType).casFeatCode_habituality)));}
    
  /** setter for habituality - sets habituality of clause 
   * @generated
   * @param v value to set into the feature 
   */
  public void setHabituality(StringList v) {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_habituality == null)
      jcasType.jcas.throwFeatMissing("habituality", "sitent.types.Situation");
    jcasType.ll_cas.ll_setRefValue(addr, ((Situation_Type)jcasType).casFeatCode_habituality, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: seType

  /** getter for seType - gets Situation entity type(s) for the situation.
   * @generated
   * @return value of the feature 
   */
  public StringList getSeType() {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_seType == null)
      jcasType.jcas.throwFeatMissing("seType", "sitent.types.Situation");
    return (StringList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Situation_Type)jcasType).casFeatCode_seType)));}
    
  /** setter for seType - sets Situation entity type(s) for the situation. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSeType(StringList v) {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_seType == null)
      jcasType.jcas.throwFeatMissing("seType", "sitent.types.Situation");
    jcasType.ll_cas.ll_setRefValue(addr, ((Situation_Type)jcasType).casFeatCode_seType, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: comment

  /** getter for comment - gets comment given by the annotator (if any)
   * @generated
   * @return value of the feature 
   */
  public String getComment() {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_comment == null)
      jcasType.jcas.throwFeatMissing("comment", "sitent.types.Situation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Situation_Type)jcasType).casFeatCode_comment);}
    
  /** setter for comment - sets comment given by the annotator (if any) 
   * @generated
   * @param v value to set into the feature 
   */
  public void setComment(String v) {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_comment == null)
      jcasType.jcas.throwFeatMissing("comment", "sitent.types.Situation");
    jcasType.ll_cas.ll_setStringValue(addr, ((Situation_Type)jcasType).casFeatCode_comment, v);}    
   
    
  //*--------------*
  //* Feature: segNums

  /** getter for segNums - gets belongs to this situation (ever only one??)
   * @generated
   * @return value of the feature 
   */
  public String getSegNums() {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_segNums == null)
      jcasType.jcas.throwFeatMissing("segNums", "sitent.types.Situation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Situation_Type)jcasType).casFeatCode_segNums);}
    
  /** setter for segNums - sets belongs to this situation (ever only one??) 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSegNums(String v) {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_segNums == null)
      jcasType.jcas.throwFeatMissing("segNums", "sitent.types.Situation");
    jcasType.ll_cas.ll_setStringValue(addr, ((Situation_Type)jcasType).casFeatCode_segNums, v);}    
   
    
  //*--------------*
  //* Feature: mainRefNotGrammSubj

  /** getter for mainRefNotGrammSubj - gets Main referent is not the grammatical subject of the sentence.
   * @generated
   * @return value of the feature 
   */
  public boolean getMainRefNotGrammSubj() {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_mainRefNotGrammSubj == null)
      jcasType.jcas.throwFeatMissing("mainRefNotGrammSubj", "sitent.types.Situation");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((Situation_Type)jcasType).casFeatCode_mainRefNotGrammSubj);}
    
  /** setter for mainRefNotGrammSubj - sets Main referent is not the grammatical subject of the sentence. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setMainRefNotGrammSubj(boolean v) {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_mainRefNotGrammSubj == null)
      jcasType.jcas.throwFeatMissing("mainRefNotGrammSubj", "sitent.types.Situation");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((Situation_Type)jcasType).casFeatCode_mainRefNotGrammSubj, v);}    
   
    
  //*--------------*
  //* Feature: notSure

  /** getter for notSure - gets annotator is not sure here.
   * @generated
   * @return value of the feature 
   */
  public boolean getNotSure() {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_notSure == null)
      jcasType.jcas.throwFeatMissing("notSure", "sitent.types.Situation");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((Situation_Type)jcasType).casFeatCode_notSure);}
    
  /** setter for notSure - sets annotator is not sure here. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setNotSure(boolean v) {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_notSure == null)
      jcasType.jcas.throwFeatMissing("notSure", "sitent.types.Situation");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((Situation_Type)jcasType).casFeatCode_notSure, v);}    
   
    
  //*--------------*
  //* Feature: segmentationProblem

  /** getter for segmentationProblem - gets annotations given for segmentation problem (if any)
   * @generated
   * @return value of the feature 
   */
  public StringList getSegmentationProblem() {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_segmentationProblem == null)
      jcasType.jcas.throwFeatMissing("segmentationProblem", "sitent.types.Situation");
    return (StringList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Situation_Type)jcasType).casFeatCode_segmentationProblem)));}
    
  /** setter for segmentationProblem - sets annotations given for segmentation problem (if any) 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSegmentationProblem(StringList v) {
    if (Situation_Type.featOkTst && ((Situation_Type)jcasType).casFeat_segmentationProblem == null)
      jcasType.jcas.throwFeatMissing("segmentationProblem", "sitent.types.Situation");
    jcasType.ll_cas.ll_setRefValue(addr, ((Situation_Type)jcasType).casFeatCode_segmentationProblem, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    