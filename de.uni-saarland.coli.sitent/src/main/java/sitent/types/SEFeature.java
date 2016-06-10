

/* First created by JCasGen Wed Jun 08 16:31:35 CEST 2016 */
package sitent.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Key-value pair for a feature (attribute in classification) and its feature value.
 * Updated by JCasGen Fri Jun 10 18:03:49 CEST 2016
 * XML source: /local/gitRepos/sitent/de.uni-saarland.coli.sitent/src/main/java/sitent/types/SitEntTypeSystem.xml
 * @generated */
public class SEFeature extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(SEFeature.class);
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
  protected SEFeature() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public SEFeature(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public SEFeature(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public SEFeature(JCas jcas, int begin, int end) {
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
  //* Feature: name

  /** getter for name - gets name of the feature
   * @generated
   * @return value of the feature 
   */
  public String getName() {
    if (SEFeature_Type.featOkTst && ((SEFeature_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "sitent.types.SEFeature");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SEFeature_Type)jcasType).casFeatCode_name);}
    
  /** setter for name - sets name of the feature 
   * @generated
   * @param v value to set into the feature 
   */
  public void setName(String v) {
    if (SEFeature_Type.featOkTst && ((SEFeature_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "sitent.types.SEFeature");
    jcasType.ll_cas.ll_setStringValue(addr, ((SEFeature_Type)jcasType).casFeatCode_name, v);}    
   
    
  //*--------------*
  //* Feature: value

  /** getter for value - gets the feature value (as String such that it can be directly written to ARFF)
   * @generated
   * @return value of the feature 
   */
  public String getValue() {
    if (SEFeature_Type.featOkTst && ((SEFeature_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "sitent.types.SEFeature");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SEFeature_Type)jcasType).casFeatCode_value);}
    
  /** setter for value - sets the feature value (as String such that it can be directly written to ARFF) 
   * @generated
   * @param v value to set into the feature 
   */
  public void setValue(String v) {
    if (SEFeature_Type.featOkTst && ((SEFeature_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "sitent.types.SEFeature");
    jcasType.ll_cas.ll_setStringValue(addr, ((SEFeature_Type)jcasType).casFeatCode_value, v);}    
  }

    