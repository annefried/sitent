

/* First created by JCasGen Wed Jun 08 16:31:35 CEST 2016 */
package sitent.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;


/** supertype for annotations that end up being spans to be classified for something.
 * Updated by JCasGen Wed Jun 08 16:31:35 CEST 2016
 * XML source: /local/gitRepos/sitent/de.uni-saarland.coli.sitent/src/main/java/sitent/types/SitEntTypeSystem.xml
 * @generated */
public class ClassificationAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ClassificationAnnotation.class);
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
  protected ClassificationAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public ClassificationAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public ClassificationAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public ClassificationAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: features

  /** getter for features - gets 
   * @generated
   * @return value of the feature 
   */
  public FSList getFeatures() {
    if (ClassificationAnnotation_Type.featOkTst && ((ClassificationAnnotation_Type)jcasType).casFeat_features == null)
      jcasType.jcas.throwFeatMissing("features", "sitent.types.ClassificationAnnotation");
    return (FSList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((ClassificationAnnotation_Type)jcasType).casFeatCode_features)));}
    
  /** setter for features - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFeatures(FSList v) {
    if (ClassificationAnnotation_Type.featOkTst && ((ClassificationAnnotation_Type)jcasType).casFeat_features == null)
      jcasType.jcas.throwFeatMissing("features", "sitent.types.ClassificationAnnotation");
    jcasType.ll_cas.ll_setRefValue(addr, ((ClassificationAnnotation_Type)jcasType).casFeatCode_features, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: task

  /** getter for task - gets identifier of this classification  task (in case items for more than one task are marked on the same JCas, this can be used to filter). Here, different features are extracted for "NP" and for "VERB".
   * @generated
   * @return value of the feature 
   */
  public String getTask() {
    if (ClassificationAnnotation_Type.featOkTst && ((ClassificationAnnotation_Type)jcasType).casFeat_task == null)
      jcasType.jcas.throwFeatMissing("task", "sitent.types.ClassificationAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ClassificationAnnotation_Type)jcasType).casFeatCode_task);}
    
  /** setter for task - sets identifier of this classification  task (in case items for more than one task are marked on the same JCas, this can be used to filter). Here, different features are extracted for "NP" and for "VERB". 
   * @generated
   * @param v value to set into the feature 
   */
  public void setTask(String v) {
    if (ClassificationAnnotation_Type.featOkTst && ((ClassificationAnnotation_Type)jcasType).casFeat_task == null)
      jcasType.jcas.throwFeatMissing("task", "sitent.types.ClassificationAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((ClassificationAnnotation_Type)jcasType).casFeatCode_task, v);}    
  }

    