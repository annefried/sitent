
/* First created by JCasGen Wed Jun 08 16:31:35 CEST 2016 */
package sitent.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** Information about source document, e.g. filename.
 * Updated by JCasGen Fri Jun 10 18:03:49 CEST 2016
 * @generated */
public class SourceDocumentInformation_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SourceDocumentInformation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SourceDocumentInformation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SourceDocumentInformation(addr, SourceDocumentInformation_Type.this);
  			   SourceDocumentInformation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SourceDocumentInformation(addr, SourceDocumentInformation_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = SourceDocumentInformation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("sitent.types.SourceDocumentInformation");
 
  /** @generated */
  final Feature casFeat_AbsolutePath;
  /** @generated */
  final int     casFeatCode_AbsolutePath;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getAbsolutePath(int addr) {
        if (featOkTst && casFeat_AbsolutePath == null)
      jcas.throwFeatMissing("AbsolutePath", "sitent.types.SourceDocumentInformation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_AbsolutePath);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setAbsolutePath(int addr, String v) {
        if (featOkTst && casFeat_AbsolutePath == null)
      jcas.throwFeatMissing("AbsolutePath", "sitent.types.SourceDocumentInformation");
    ll_cas.ll_setStringValue(addr, casFeatCode_AbsolutePath, v);}
    
  
 
  /** @generated */
  final Feature casFeat_DocId;
  /** @generated */
  final int     casFeatCode_DocId;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getDocId(int addr) {
        if (featOkTst && casFeat_DocId == null)
      jcas.throwFeatMissing("DocId", "sitent.types.SourceDocumentInformation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_DocId);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDocId(int addr, String v) {
        if (featOkTst && casFeat_DocId == null)
      jcas.throwFeatMissing("DocId", "sitent.types.SourceDocumentInformation");
    ll_cas.ll_setStringValue(addr, casFeatCode_DocId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_hasPdtbAnnotations;
  /** @generated */
  final int     casFeatCode_hasPdtbAnnotations;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getHasPdtbAnnotations(int addr) {
        if (featOkTst && casFeat_hasPdtbAnnotations == null)
      jcas.throwFeatMissing("hasPdtbAnnotations", "sitent.types.SourceDocumentInformation");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_hasPdtbAnnotations);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setHasPdtbAnnotations(int addr, boolean v) {
        if (featOkTst && casFeat_hasPdtbAnnotations == null)
      jcas.throwFeatMissing("hasPdtbAnnotations", "sitent.types.SourceDocumentInformation");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_hasPdtbAnnotations, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public SourceDocumentInformation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_AbsolutePath = jcas.getRequiredFeatureDE(casType, "AbsolutePath", "uima.cas.String", featOkTst);
    casFeatCode_AbsolutePath  = (null == casFeat_AbsolutePath) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_AbsolutePath).getCode();

 
    casFeat_DocId = jcas.getRequiredFeatureDE(casType, "DocId", "uima.cas.String", featOkTst);
    casFeatCode_DocId  = (null == casFeat_DocId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_DocId).getCode();

 
    casFeat_hasPdtbAnnotations = jcas.getRequiredFeatureDE(casType, "hasPdtbAnnotations", "uima.cas.Boolean", featOkTst);
    casFeatCode_hasPdtbAnnotations  = (null == casFeat_hasPdtbAnnotations) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_hasPdtbAnnotations).getCode();

  }
}



    