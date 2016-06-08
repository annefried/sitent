
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

/** A span of text representing a situation (may consist of merged segments).
 * Updated by JCasGen Wed Jun 08 16:31:35 CEST 2016
 * @generated */
public class Situation_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Situation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Situation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Situation(addr, Situation_Type.this);
  			   Situation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Situation(addr, Situation_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Situation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("sitent.types.Situation");
 
  /** @generated */
  final Feature casFeat_annotator;
  /** @generated */
  final int     casFeatCode_annotator;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getAnnotator(int addr) {
        if (featOkTst && casFeat_annotator == null)
      jcas.throwFeatMissing("annotator", "sitent.types.Situation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_annotator);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setAnnotator(int addr, String v) {
        if (featOkTst && casFeat_annotator == null)
      jcas.throwFeatMissing("annotator", "sitent.types.Situation");
    ll_cas.ll_setStringValue(addr, casFeatCode_annotator, v);}
    
  
 
  /** @generated */
  final Feature casFeat_mainReferent;
  /** @generated */
  final int     casFeatCode_mainReferent;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getMainReferent(int addr) {
        if (featOkTst && casFeat_mainReferent == null)
      jcas.throwFeatMissing("mainReferent", "sitent.types.Situation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_mainReferent);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMainReferent(int addr, int v) {
        if (featOkTst && casFeat_mainReferent == null)
      jcas.throwFeatMissing("mainReferent", "sitent.types.Situation");
    ll_cas.ll_setRefValue(addr, casFeatCode_mainReferent, v);}
    
  
 
  /** @generated */
  final Feature casFeat_aspectualClass;
  /** @generated */
  final int     casFeatCode_aspectualClass;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getAspectualClass(int addr) {
        if (featOkTst && casFeat_aspectualClass == null)
      jcas.throwFeatMissing("aspectualClass", "sitent.types.Situation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_aspectualClass);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setAspectualClass(int addr, int v) {
        if (featOkTst && casFeat_aspectualClass == null)
      jcas.throwFeatMissing("aspectualClass", "sitent.types.Situation");
    ll_cas.ll_setRefValue(addr, casFeatCode_aspectualClass, v);}
    
  
 
  /** @generated */
  final Feature casFeat_habituality;
  /** @generated */
  final int     casFeatCode_habituality;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getHabituality(int addr) {
        if (featOkTst && casFeat_habituality == null)
      jcas.throwFeatMissing("habituality", "sitent.types.Situation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_habituality);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setHabituality(int addr, int v) {
        if (featOkTst && casFeat_habituality == null)
      jcas.throwFeatMissing("habituality", "sitent.types.Situation");
    ll_cas.ll_setRefValue(addr, casFeatCode_habituality, v);}
    
  
 
  /** @generated */
  final Feature casFeat_seType;
  /** @generated */
  final int     casFeatCode_seType;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getSeType(int addr) {
        if (featOkTst && casFeat_seType == null)
      jcas.throwFeatMissing("seType", "sitent.types.Situation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_seType);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSeType(int addr, int v) {
        if (featOkTst && casFeat_seType == null)
      jcas.throwFeatMissing("seType", "sitent.types.Situation");
    ll_cas.ll_setRefValue(addr, casFeatCode_seType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_comment;
  /** @generated */
  final int     casFeatCode_comment;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getComment(int addr) {
        if (featOkTst && casFeat_comment == null)
      jcas.throwFeatMissing("comment", "sitent.types.Situation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_comment);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setComment(int addr, String v) {
        if (featOkTst && casFeat_comment == null)
      jcas.throwFeatMissing("comment", "sitent.types.Situation");
    ll_cas.ll_setStringValue(addr, casFeatCode_comment, v);}
    
  
 
  /** @generated */
  final Feature casFeat_segNums;
  /** @generated */
  final int     casFeatCode_segNums;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSegNums(int addr) {
        if (featOkTst && casFeat_segNums == null)
      jcas.throwFeatMissing("segNums", "sitent.types.Situation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_segNums);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSegNums(int addr, String v) {
        if (featOkTst && casFeat_segNums == null)
      jcas.throwFeatMissing("segNums", "sitent.types.Situation");
    ll_cas.ll_setStringValue(addr, casFeatCode_segNums, v);}
    
  
 
  /** @generated */
  final Feature casFeat_mainRefNotGrammSubj;
  /** @generated */
  final int     casFeatCode_mainRefNotGrammSubj;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getMainRefNotGrammSubj(int addr) {
        if (featOkTst && casFeat_mainRefNotGrammSubj == null)
      jcas.throwFeatMissing("mainRefNotGrammSubj", "sitent.types.Situation");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_mainRefNotGrammSubj);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMainRefNotGrammSubj(int addr, boolean v) {
        if (featOkTst && casFeat_mainRefNotGrammSubj == null)
      jcas.throwFeatMissing("mainRefNotGrammSubj", "sitent.types.Situation");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_mainRefNotGrammSubj, v);}
    
  
 
  /** @generated */
  final Feature casFeat_notSure;
  /** @generated */
  final int     casFeatCode_notSure;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getNotSure(int addr) {
        if (featOkTst && casFeat_notSure == null)
      jcas.throwFeatMissing("notSure", "sitent.types.Situation");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_notSure);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNotSure(int addr, boolean v) {
        if (featOkTst && casFeat_notSure == null)
      jcas.throwFeatMissing("notSure", "sitent.types.Situation");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_notSure, v);}
    
  
 
  /** @generated */
  final Feature casFeat_segmentationProblem;
  /** @generated */
  final int     casFeatCode_segmentationProblem;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getSegmentationProblem(int addr) {
        if (featOkTst && casFeat_segmentationProblem == null)
      jcas.throwFeatMissing("segmentationProblem", "sitent.types.Situation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_segmentationProblem);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSegmentationProblem(int addr, int v) {
        if (featOkTst && casFeat_segmentationProblem == null)
      jcas.throwFeatMissing("segmentationProblem", "sitent.types.Situation");
    ll_cas.ll_setRefValue(addr, casFeatCode_segmentationProblem, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Situation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_annotator = jcas.getRequiredFeatureDE(casType, "annotator", "uima.cas.String", featOkTst);
    casFeatCode_annotator  = (null == casFeat_annotator) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_annotator).getCode();

 
    casFeat_mainReferent = jcas.getRequiredFeatureDE(casType, "mainReferent", "uima.cas.StringList", featOkTst);
    casFeatCode_mainReferent  = (null == casFeat_mainReferent) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_mainReferent).getCode();

 
    casFeat_aspectualClass = jcas.getRequiredFeatureDE(casType, "aspectualClass", "uima.cas.StringList", featOkTst);
    casFeatCode_aspectualClass  = (null == casFeat_aspectualClass) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_aspectualClass).getCode();

 
    casFeat_habituality = jcas.getRequiredFeatureDE(casType, "habituality", "uima.cas.StringList", featOkTst);
    casFeatCode_habituality  = (null == casFeat_habituality) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_habituality).getCode();

 
    casFeat_seType = jcas.getRequiredFeatureDE(casType, "seType", "uima.cas.StringList", featOkTst);
    casFeatCode_seType  = (null == casFeat_seType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_seType).getCode();

 
    casFeat_comment = jcas.getRequiredFeatureDE(casType, "comment", "uima.cas.String", featOkTst);
    casFeatCode_comment  = (null == casFeat_comment) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_comment).getCode();

 
    casFeat_segNums = jcas.getRequiredFeatureDE(casType, "segNums", "uima.cas.String", featOkTst);
    casFeatCode_segNums  = (null == casFeat_segNums) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_segNums).getCode();

 
    casFeat_mainRefNotGrammSubj = jcas.getRequiredFeatureDE(casType, "mainRefNotGrammSubj", "uima.cas.Boolean", featOkTst);
    casFeatCode_mainRefNotGrammSubj  = (null == casFeat_mainRefNotGrammSubj) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_mainRefNotGrammSubj).getCode();

 
    casFeat_notSure = jcas.getRequiredFeatureDE(casType, "notSure", "uima.cas.Boolean", featOkTst);
    casFeatCode_notSure  = (null == casFeat_notSure) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_notSure).getCode();

 
    casFeat_segmentationProblem = jcas.getRequiredFeatureDE(casType, "segmentationProblem", "uima.cas.StringList", featOkTst);
    casFeatCode_segmentationProblem  = (null == casFeat_segmentationProblem) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_segmentationProblem).getCode();

  }
}



    