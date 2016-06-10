

/* First created by JCasGen Wed Jun 08 16:31:35 CEST 2016 */
package sitent.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Information about source document, e.g. filename.
 * Updated by JCasGen Fri Jun 10 18:03:49 CEST 2016
 * XML source: /local/gitRepos/sitent/de.uni-saarland.coli.sitent/src/main/java/sitent/types/SitEntTypeSystem.xml
 * @generated */
public class SourceDocumentInformation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(SourceDocumentInformation.class);
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
  protected SourceDocumentInformation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public SourceDocumentInformation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public SourceDocumentInformation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public SourceDocumentInformation(JCas jcas, int begin, int end) {
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
  //* Feature: AbsolutePath

  /** getter for AbsolutePath - gets absolute path to file
   * @generated
   * @return value of the feature 
   */
  public String getAbsolutePath() {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_AbsolutePath == null)
      jcasType.jcas.throwFeatMissing("AbsolutePath", "sitent.types.SourceDocumentInformation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_AbsolutePath);}
    
  /** setter for AbsolutePath - sets absolute path to file 
   * @generated
   * @param v value to set into the feature 
   */
  public void setAbsolutePath(String v) {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_AbsolutePath == null)
      jcasType.jcas.throwFeatMissing("AbsolutePath", "sitent.types.SourceDocumentInformation");
    jcasType.ll_cas.ll_setStringValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_AbsolutePath, v);}    
   
    
  //*--------------*
  //* Feature: DocId

  /** getter for DocId - gets filename of the document
   * @generated
   * @return value of the feature 
   */
  public String getDocId() {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_DocId == null)
      jcasType.jcas.throwFeatMissing("DocId", "sitent.types.SourceDocumentInformation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_DocId);}
    
  /** setter for DocId - sets filename of the document 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocId(String v) {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_DocId == null)
      jcasType.jcas.throwFeatMissing("DocId", "sitent.types.SourceDocumentInformation");
    jcasType.ll_cas.ll_setStringValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_DocId, v);}    
   
    
  //*--------------*
  //* Feature: hasPdtbAnnotations

  /** getter for hasPdtbAnnotations - gets Set to true if the file has PDTB annotations.
   * @generated
   * @return value of the feature 
   */
  public boolean getHasPdtbAnnotations() {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_hasPdtbAnnotations == null)
      jcasType.jcas.throwFeatMissing("hasPdtbAnnotations", "sitent.types.SourceDocumentInformation");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_hasPdtbAnnotations);}
    
  /** setter for hasPdtbAnnotations - sets Set to true if the file has PDTB annotations. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setHasPdtbAnnotations(boolean v) {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_hasPdtbAnnotations == null)
      jcasType.jcas.throwFeatMissing("hasPdtbAnnotations", "sitent.types.SourceDocumentInformation");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_hasPdtbAnnotations, v);}    
  }

    