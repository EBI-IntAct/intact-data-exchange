package uk.ac.ebi.intact.dataexchange.enricher.standard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.enricher.CvTermEnricher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.minimal.MinimalFeatureEvidenceEnricher;
import psidev.psi.mi.jami.enricher.listener.FeatureEnricherListener;
import psidev.psi.mi.jami.enricher.listener.FeatureEvidenceEnricherListener;
import psidev.psi.mi.jami.enricher.listener.impl.log.FeatureEvidenceEnricherLogger;
import psidev.psi.mi.jami.enricher.util.EnricherUtils;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.FeatureEvidence;
import psidev.psi.mi.jami.model.Parameter;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * Provides full enrichment of feature evidence.
 *
 * - enrich full properties of feature. See ModelledFeatureEnricher
 * - enrich detection methods if cv term enricher is not null
 *
 * @author Gabriel Aldam (galdam@ebi.ac.uk)
 * @since 13/08/13
 */
@Component(value = "intactFeatureEvidenceEnricher")
@Lazy
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class FeatureEvidenceEnricher extends MinimalFeatureEvidenceEnricher {

    @Autowired
    private EnricherContext enricherContext;

    @Resource(name = "intactFeatureEnricher")
    private psidev.psi.mi.jami.enricher.FeatureEnricher intactFeatureEnricher;

    public FeatureEvidenceEnricher(){
        super();
    }

    @Override
    public void enrich(FeatureEvidence featureToEnrich) throws EnricherException {
        // enrich full feature
        getIntactFeatureEnricher().enrich(featureToEnrich);
        // enrich other properties
        super.enrich(featureToEnrich);
    }

    @Override
    public void enrich(FeatureEvidence objectToEnrich, FeatureEvidence objectSource) throws EnricherException {
        // enrich full feature
        getIntactFeatureEnricher().enrich(objectToEnrich, objectSource);
        // enrich other properties
        super.enrich(objectToEnrich, objectSource);
    }

    @Override
    public void processMinimalUpdates(FeatureEvidence objectToEnrich, FeatureEvidence objectSource) throws EnricherException {
        // nothing to do
    }

    @Override
    public void processMinimalUpdates(FeatureEvidence featureToEnrich) throws EnricherException {
        // nothing to do
    }

    @Override
    protected void processOtherProperties(FeatureEvidence featureToEnrich) throws EnricherException {
        super.processOtherProperties(featureToEnrich);
        // process parameters
        processParameters(featureToEnrich, null);
    }

    @Override
    protected void processOtherProperties(FeatureEvidence featureToEnrich, FeatureEvidence objectSource) throws EnricherException {
        super.processOtherProperties(featureToEnrich, objectSource);
        // process parameters
        processParameters(featureToEnrich, objectSource);
    }

    protected void processParameters(FeatureEvidence featureToEnrich, FeatureEvidence objectSource) throws EnricherException{
        if (objectSource != null){
            EnricherUtils.mergeParameters(featureToEnrich, objectSource.getParameters(), objectSource.getParameters(), false,
                    getFeatureEnricherListener() instanceof FeatureEvidenceEnricherListener ?
                            (psidev.psi.mi.jami.listener.ParametersChangeListener<FeatureEvidence>) getFeatureEnricherListener() : null
            );
        }

        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvTermEnricher() != null){
            for (Parameter parameter : featureToEnrich.getParameters()) {
                getCvTermEnricher().enrich(parameter.getType());
                if (parameter.getUnit() != null){
                    getCvTermEnricher().enrich(parameter.getUnit());
                }
            }
        }
    }
    @Override
    protected void processDetectionMethods(FeatureEvidence featureToEnrich) throws EnricherException {
        if(enricherContext.getConfig().isUpdateCvTerms() && getCvTermEnricher() != null)
            getCvTermEnricher().enrich(featureToEnrich.getDetectionMethods());
    }

    @Override
    public CvTermEnricher getCvTermEnricher() {
        if (super.getCvTermEnricher() == null){
            super.setCvTermEnricher((CvTermEnricher<CvTerm>) ApplicationContextProvider.getBean("intactCvObjectEnricher"));
        }
        return super.getCvTermEnricher();
    }

    @Override
    public FeatureEnricherListener<FeatureEvidence> getFeatureEnricherListener() {
        if (super.getFeatureEnricherListener() == null){
            super.setFeatureEnricherListener(new FeatureEvidenceEnricherLogger());
        }
        return super.getFeatureEnricherListener();
    }

    @Override
    public void setFeaturesWithRangesToUpdate(Collection<FeatureEvidence> features) {
        super.setFeaturesWithRangesToUpdate(features);
        getIntactFeatureEnricher().setFeaturesWithRangesToUpdate(features);
    }

    @Override
    public void setFeatureEnricherListener(FeatureEnricherListener<FeatureEvidence> featureEnricherListener) {
        super.setFeatureEnricherListener(featureEnricherListener);
        if (getIntactFeatureEnricher() instanceof FeatureEnricher){
            ((FeatureEnricher)getIntactFeatureEnricher()).setFeatureEnricherListener(featureEnricherListener);
        }
    }

    @Override
    public void setCvTermEnricher(CvTermEnricher cvTermEnricher) {
        super.setCvTermEnricher(cvTermEnricher);
        if (getIntactFeatureEnricher() instanceof FeatureEnricher){
            ((FeatureEnricher)getIntactFeatureEnricher()).setCvTermEnricher(cvTermEnricher);
        }
    }

    public CvTermEnricher<CvTerm> getIntactCvObjectEnricher() {
        if (intactFeatureEnricher instanceof FeatureEnricher){
            return ((FeatureEnricher) intactFeatureEnricher).getIntactCvObjectEnricher();
        }
        return null;
    }

    public void setIntactCvObjectEnricher(CvTermEnricher<CvTerm> intactCvObjectEnricher) {
        if (intactFeatureEnricher instanceof FeatureEnricher){
            ((FeatureEnricher) intactFeatureEnricher).setIntactCvObjectEnricher(intactCvObjectEnricher);
        }
    }

    public psidev.psi.mi.jami.enricher.FeatureEnricher getIntactFeatureEnricher() {
        if (this.intactFeatureEnricher == null){
           this.intactFeatureEnricher = ApplicationContextProvider.getBean("intactFeatureEnricher");
        }
        return intactFeatureEnricher;
    }

    public void setIntactFeatureEnricher(psidev.psi.mi.jami.enricher.FeatureEnricher intactFeatureEnricher) {
        this.intactFeatureEnricher = intactFeatureEnricher;
    }
}
