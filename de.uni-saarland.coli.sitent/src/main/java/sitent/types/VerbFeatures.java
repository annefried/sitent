

/* First created by JCasGen Wed Jun 08 16:31:35 CEST 2016 */
package sitent.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** adds tense and voice information
 * Updated by JCasGen Wed Jun 08 16:31:35 CEST 2016
 * XML source: /local/gitRepos/sitent/de.uni-saarland.coli.sitent/src/main/java/sitent/types/SitEntTypeSystem.xml
 * @generated */
public class VerbFeatures extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(VerbFeatures.class);
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
  protected VerbFeatures() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public VerbFeatures(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public VerbFeatures(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public VerbFeatures(JCas jcas, int begin, int end) {
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
  //* Feature: tense

  /** getter for tense - gets 
   * @generated
   * @return value of the feature 
   */
  public String getTense() {
    if (VerbFeatures_Type.featOkTst && ((VerbFeatures_Type)jcasType).casFeat_tense == null)
      jcasType.jcas.throwFeatMissing("tense", "sitent.types.VerbFeatures");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VerbFeatures_Type)jcasType).casFeatCode_tense);}
    
  /** setter for tense - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTense(String v) {
    if (VerbFeatures_Type.featOkTst && ((VerbFeatures_Type)jcasType).casFeat_tense == null)
      jcasType.jcas.throwFeatMissing("tense", "sitent.types.VerbFeatures");
    jcasType.ll_cas.ll_setStringValue(addr, ((VerbFeatures_Type)jcasType).casFeatCode_tense, v);}    
   
    
  //*--------------*
  //* Feature: voice

  /** getter for voice - gets 
   * @generated
   * @return value of the feature 
   */
  public String getVoice() {
    if (VerbFeatures_Type.featOkTst && ((VerbFeatures_Type)jcasType).casFeat_voice == null)
      jcasType.jcas.throwFeatMissing("voice", "sitent.types.VerbFeatures");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VerbFeatures_Type)jcasType).casFeatCode_voice);}
    
  /** setter for voice - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setVoice(String v) {
    if (VerbFeatures_Type.featOkTst && ((VerbFeatures_Type)jcasType).casFeat_voice == null)
      jcasType.jcas.throwFeatMissing("voice", "sitent.types.VerbFeatures");
    jcasType.ll_cas.ll_setStringValue(addr, ((VerbFeatures_Type)jcasType).casFeatCode_voice, v);}    
   
    
  //*--------------*
  //* Feature: headOfVerbChain

  /** getter for headOfVerbChain - gets Token: head of verb chain
   * @generated
   * @return value of the feature 
   */
  public Annotation getHeadOfVerbChain() {
    if (VerbFeatures_Type.featOkTst && ((VerbFeatures_Type)jcasType).casFeat_headOfVerbChain == null)
      jcasType.jcas.throwFeatMissing("headOfVerbChain", "sitent.types.VerbFeatures");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((VerbFeatures_Type)jcasType).casFeatCode_headOfVerbChain)));}
    
  /** setter for headOfVerbChain - sets Token: head of verb chain 
   * @generated
   * @param v value to set into the feature 
   */
  public void setHeadOfVerbChain(Annotation v) {
    if (VerbFeatures_Type.featOkTst && ((VerbFeatures_Type)jcasType).casFeat_headOfVerbChain == null)
      jcasType.jcas.throwFeatMissing("headOfVerbChain", "sitent.types.VerbFeatures");
    jcasType.ll_cas.ll_setRefValue(addr, ((VerbFeatures_Type)jcasType).casFeatCode_headOfVerbChain, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    