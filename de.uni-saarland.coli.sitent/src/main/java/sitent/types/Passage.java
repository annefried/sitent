

/* First created by JCasGen Wed Jun 08 16:31:35 CEST 2016 */
package sitent.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** passge of text, features can be extracted for it
 * Updated by JCasGen Fri Jun 10 18:03:49 CEST 2016
 * XML source: /local/gitRepos/sitent/de.uni-saarland.coli.sitent/src/main/java/sitent/types/SitEntTypeSystem.xml
 * @generated */
public class Passage extends ClassificationAnnotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Passage.class);
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
  protected Passage() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Passage(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Passage(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Passage(JCas jcas, int begin, int end) {
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
  //* Feature: passageId

  /** getter for passageId - gets identifier for the passage
   * @generated
   * @return value of the feature 
   */
  public String getPassageId() {
    if (Passage_Type.featOkTst && ((Passage_Type)jcasType).casFeat_passageId == null)
      jcasType.jcas.throwFeatMissing("passageId", "sitent.types.Passage");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Passage_Type)jcasType).casFeatCode_passageId);}
    
  /** setter for passageId - sets identifier for the passage 
   * @generated
   * @param v value to set into the feature 
   */
  public void setPassageId(String v) {
    if (Passage_Type.featOkTst && ((Passage_Type)jcasType).casFeat_passageId == null)
      jcasType.jcas.throwFeatMissing("passageId", "sitent.types.Passage");
    jcasType.ll_cas.ll_setStringValue(addr, ((Passage_Type)jcasType).casFeatCode_passageId, v);}    
  }

    