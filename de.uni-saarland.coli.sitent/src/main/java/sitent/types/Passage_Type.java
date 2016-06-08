
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

/** passge of text, features can be extracted for it
 * Updated by JCasGen Wed Jun 08 16:31:35 CEST 2016
 * @generated */
public class Passage_Type extends ClassificationAnnotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Passage_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Passage_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Passage(addr, Passage_Type.this);
  			   Passage_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Passage(addr, Passage_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Passage.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("sitent.types.Passage");
 
  /** @generated */
  final Feature casFeat_passageId;
  /** @generated */
  final int     casFeatCode_passageId;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPassageId(int addr) {
        if (featOkTst && casFeat_passageId == null)
      jcas.throwFeatMissing("passageId", "sitent.types.Passage");
    return ll_cas.ll_getStringValue(addr, casFeatCode_passageId);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPassageId(int addr, String v) {
        if (featOkTst && casFeat_passageId == null)
      jcas.throwFeatMissing("passageId", "sitent.types.Passage");
    ll_cas.ll_setStringValue(addr, casFeatCode_passageId, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Passage_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_passageId = jcas.getRequiredFeatureDE(casType, "passageId", "uima.cas.String", featOkTst);
    casFeatCode_passageId  = (null == casFeat_passageId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_passageId).getCode();

  }
}



    