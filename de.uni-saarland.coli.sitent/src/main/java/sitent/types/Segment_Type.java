
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

/** A segment as created automatically by SPADE + our post-processing script.
 * Updated by JCasGen Fri Jun 10 18:03:49 CEST 2016
 * @generated */
public class Segment_Type extends ClassificationAnnotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Segment_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Segment_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Segment(addr, Segment_Type.this);
  			   Segment_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Segment(addr, Segment_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Segment.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("sitent.types.Segment");
 
  /** @generated */
  final Feature casFeat_docid;
  /** @generated */
  final int     casFeatCode_docid;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getDocid(int addr) {
        if (featOkTst && casFeat_docid == null)
      jcas.throwFeatMissing("docid", "sitent.types.Segment");
    return ll_cas.ll_getStringValue(addr, casFeatCode_docid);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDocid(int addr, String v) {
        if (featOkTst && casFeat_docid == null)
      jcas.throwFeatMissing("docid", "sitent.types.Segment");
    ll_cas.ll_setStringValue(addr, casFeatCode_docid, v);}
    
  
 
  /** @generated */
  final Feature casFeat_segid;
  /** @generated */
  final int     casFeatCode_segid;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getSegid(int addr) {
        if (featOkTst && casFeat_segid == null)
      jcas.throwFeatMissing("segid", "sitent.types.Segment");
    return ll_cas.ll_getIntValue(addr, casFeatCode_segid);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSegid(int addr, int v) {
        if (featOkTst && casFeat_segid == null)
      jcas.throwFeatMissing("segid", "sitent.types.Segment");
    ll_cas.ll_setIntValue(addr, casFeatCode_segid, v);}
    
  
 
  /** @generated */
  final Feature casFeat_situationAnnotations;
  /** @generated */
  final int     casFeatCode_situationAnnotations;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getSituationAnnotations(int addr) {
        if (featOkTst && casFeat_situationAnnotations == null)
      jcas.throwFeatMissing("situationAnnotations", "sitent.types.Segment");
    return ll_cas.ll_getRefValue(addr, casFeatCode_situationAnnotations);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSituationAnnotations(int addr, int v) {
        if (featOkTst && casFeat_situationAnnotations == null)
      jcas.throwFeatMissing("situationAnnotations", "sitent.types.Segment");
    ll_cas.ll_setRefValue(addr, casFeatCode_situationAnnotations, v);}
    
  
 
  /** @generated */
  final Feature casFeat_predictionNoSituation;
  /** @generated */
  final int     casFeatCode_predictionNoSituation;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getPredictionNoSituation(int addr) {
        if (featOkTst && casFeat_predictionNoSituation == null)
      jcas.throwFeatMissing("predictionNoSituation", "sitent.types.Segment");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_predictionNoSituation);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPredictionNoSituation(int addr, boolean v) {
        if (featOkTst && casFeat_predictionNoSituation == null)
      jcas.throwFeatMissing("predictionNoSituation", "sitent.types.Segment");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_predictionNoSituation, v);}
    
  
 
  /** @generated */
  final Feature casFeat_mainVerb;
  /** @generated */
  final int     casFeatCode_mainVerb;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getMainVerb(int addr) {
        if (featOkTst && casFeat_mainVerb == null)
      jcas.throwFeatMissing("mainVerb", "sitent.types.Segment");
    return ll_cas.ll_getRefValue(addr, casFeatCode_mainVerb);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMainVerb(int addr, int v) {
        if (featOkTst && casFeat_mainVerb == null)
      jcas.throwFeatMissing("mainVerb", "sitent.types.Segment");
    ll_cas.ll_setRefValue(addr, casFeatCode_mainVerb, v);}
    
  
 
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
      jcas.throwFeatMissing("mainReferent", "sitent.types.Segment");
    return ll_cas.ll_getRefValue(addr, casFeatCode_mainReferent);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMainReferent(int addr, int v) {
        if (featOkTst && casFeat_mainReferent == null)
      jcas.throwFeatMissing("mainReferent", "sitent.types.Segment");
    ll_cas.ll_setRefValue(addr, casFeatCode_mainReferent, v);}
    
  
 
  /** @generated */
  final Feature casFeat_tokens;
  /** @generated */
  final int     casFeatCode_tokens;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTokens(int addr) {
        if (featOkTst && casFeat_tokens == null)
      jcas.throwFeatMissing("tokens", "sitent.types.Segment");
    return ll_cas.ll_getRefValue(addr, casFeatCode_tokens);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTokens(int addr, int v) {
        if (featOkTst && casFeat_tokens == null)
      jcas.throwFeatMissing("tokens", "sitent.types.Segment");
    ll_cas.ll_setRefValue(addr, casFeatCode_tokens, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Segment_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_docid = jcas.getRequiredFeatureDE(casType, "docid", "uima.cas.String", featOkTst);
    casFeatCode_docid  = (null == casFeat_docid) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_docid).getCode();

 
    casFeat_segid = jcas.getRequiredFeatureDE(casType, "segid", "uima.cas.Integer", featOkTst);
    casFeatCode_segid  = (null == casFeat_segid) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_segid).getCode();

 
    casFeat_situationAnnotations = jcas.getRequiredFeatureDE(casType, "situationAnnotations", "uima.cas.FSList", featOkTst);
    casFeatCode_situationAnnotations  = (null == casFeat_situationAnnotations) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_situationAnnotations).getCode();

 
    casFeat_predictionNoSituation = jcas.getRequiredFeatureDE(casType, "predictionNoSituation", "uima.cas.Boolean", featOkTst);
    casFeatCode_predictionNoSituation  = (null == casFeat_predictionNoSituation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_predictionNoSituation).getCode();

 
    casFeat_mainVerb = jcas.getRequiredFeatureDE(casType, "mainVerb", "uima.tcas.Annotation", featOkTst);
    casFeatCode_mainVerb  = (null == casFeat_mainVerb) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_mainVerb).getCode();

 
    casFeat_mainReferent = jcas.getRequiredFeatureDE(casType, "mainReferent", "uima.tcas.Annotation", featOkTst);
    casFeatCode_mainReferent  = (null == casFeat_mainReferent) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_mainReferent).getCode();

 
    casFeat_tokens = jcas.getRequiredFeatureDE(casType, "tokens", "uima.cas.FSList", featOkTst);
    casFeatCode_tokens  = (null == casFeat_tokens) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_tokens).getCode();

  }
}



    