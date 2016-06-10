
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

/** supertype for annotations that end up being spans to be classified for something.
 * Updated by JCasGen Fri Jun 10 18:03:49 CEST 2016
 * @generated */
public class ClassificationAnnotation_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ClassificationAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ClassificationAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ClassificationAnnotation(addr, ClassificationAnnotation_Type.this);
  			   ClassificationAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ClassificationAnnotation(addr, ClassificationAnnotation_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ClassificationAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("sitent.types.ClassificationAnnotation");
 
  /** @generated */
  final Feature casFeat_features;
  /** @generated */
  final int     casFeatCode_features;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getFeatures(int addr) {
        if (featOkTst && casFeat_features == null)
      jcas.throwFeatMissing("features", "sitent.types.ClassificationAnnotation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_features);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setFeatures(int addr, int v) {
        if (featOkTst && casFeat_features == null)
      jcas.throwFeatMissing("features", "sitent.types.ClassificationAnnotation");
    ll_cas.ll_setRefValue(addr, casFeatCode_features, v);}
    
  
 
  /** @generated */
  final Feature casFeat_task;
  /** @generated */
  final int     casFeatCode_task;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getTask(int addr) {
        if (featOkTst && casFeat_task == null)
      jcas.throwFeatMissing("task", "sitent.types.ClassificationAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_task);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTask(int addr, String v) {
        if (featOkTst && casFeat_task == null)
      jcas.throwFeatMissing("task", "sitent.types.ClassificationAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_task, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ClassificationAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_features = jcas.getRequiredFeatureDE(casType, "features", "uima.cas.FSList", featOkTst);
    casFeatCode_features  = (null == casFeat_features) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_features).getCode();

 
    casFeat_task = jcas.getRequiredFeatureDE(casType, "task", "uima.cas.String", featOkTst);
    casFeatCode_task  = (null == casFeat_task) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_task).getCode();

  }
}



    