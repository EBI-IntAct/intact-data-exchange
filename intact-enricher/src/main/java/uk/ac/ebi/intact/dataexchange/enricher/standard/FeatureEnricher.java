package uk.ac.ebi.intact.dataexchange.enricher.standard;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.enricher.CvTermEnricher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.full.FullFeatureEnricher;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;

/**
 * Provides full enrichment of feature.
 * <p>
 * - enrich minimal properties of feature (see MinimalFeatureEnricher)
 * - enrich interaction dependency
 * - enrich interaction effect
 * - enrich xrefs
 * - enrich aliases
 * - enrich annotations
 * - enrich linked features
 *
 * @since 13/08/13
 */
@Component(value = "intactFeatureEnricher")
@Lazy
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FeatureEnricher<F extends Feature> extends FullFeatureEnricher<F> {

    @Autowired
    private EnricherContext enricherContext;

    @Resource(name = "intactCvObjectEnricher")
    private CvTermEnricher<CvTerm> intactCvObjectEnricher;

    public FeatureEnricher() {
        super();
        setFeaturesWithRangesToUpdate(Collections.EMPTY_LIST);
    }

    @Override
    protected void processRole(F featureToEnrich) throws EnricherException {
        if (enricherContext.getConfig().isUpdateCvTerms()
                && getCvTermEnricher() != null
                && featureToEnrich.getRole() != null) {
            getCvTermEnricher().enrich(featureToEnrich.getRole());
        }
    }

    @Override
    protected void processFeatureType(F featureToEnrich) throws EnricherException {
        if (enricherContext.getConfig().isUpdateCvTerms()
                && getIntactCvObjectEnricher() != null
                && featureToEnrich.getType() != null) {
            getIntactCvObjectEnricher().enrich(featureToEnrich.getType());
        }
    }

    @Override
    protected void processRangeStatus(CvTerm status) throws EnricherException {
        if (enricherContext.getConfig().isUpdateCvTerms()
                && getCvTermEnricher() != null
                && status != null) {
            getCvTermEnricher().enrich(status);
        }
    }

    @Override
    protected void processRanges(F featureToEnrich, F fetched) throws EnricherException {
        if (fetched != null) {
            super.processRanges(featureToEnrich, fetched);
        }
    }

    @Override
    protected void processXrefs(F objectToEnrich, F fetched) throws EnricherException {
        if (fetched != null) {
            super.processXrefs(objectToEnrich, fetched);
        }

        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvTermEnricher() != null) {
            for (Object obj : objectToEnrich.getXrefs()) {
                Xref xref = (Xref) obj;
                if (xref.getQualifier() != null) {
                    getCvTermEnricher().enrich(xref.getQualifier());
                }
                getCvTermEnricher().enrich(xref.getDatabase());
            }
        }
    }

    @Override
    protected void processIdentifiers(F objectToEnrich, F fetched) throws EnricherException {
        if (fetched != null) {
            super.processIdentifiers(objectToEnrich, fetched);
        }

        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvTermEnricher() != null) {
            for (Object obj : objectToEnrich.getIdentifiers()) {
                Xref xref = (Xref) obj;
                if (xref.getQualifier() != null) {
                    getCvTermEnricher().enrich(xref.getQualifier());
                }
                getCvTermEnricher().enrich(xref.getDatabase());
            }
        }
    }

    @Override
    protected void processAnnotations(F objectToEnrich, F fetchedObject) throws EnricherException {
        if (fetchedObject != null) {
            super.processAnnotations(objectToEnrich, fetchedObject);
        }

        if (enricherContext.getConfig().isUpdateCvTerms() && getCvTermEnricher() != null) {
            for (Object obj : objectToEnrich.getAnnotations()) {
                Annotation annotation = (Annotation) obj;
                getCvTermEnricher().enrich(annotation.getTopic());
            }
        }
    }

    @Override
    protected void processAliases(F objectToEnrich, F fetched) throws EnricherException {
        if (fetched != null) {
            super.processAliases(objectToEnrich, fetched);
        }

        if (enricherContext.getConfig().isUpdateCvInXrefsAliasesAnnotations() && getCvTermEnricher() != null) {
            for (Object obj : objectToEnrich.getAliases()) {
                Alias alias = (Alias) obj;
                if (alias.getType() != null) {
                    getCvTermEnricher().enrich(alias.getType());
                }
            }
        }
    }

    @Override
    protected void processShortLabel(F objectToEnrich, F fetched) throws EnricherException {
        if (fetched != null) {
            super.processShortLabel(objectToEnrich, fetched);
        }

        // DIP Hack ----------------
        // If the short label is empty or null we copy the full name, if it is available, to avoid missing the mutation data
        if(objectToEnrich.getShortName()== null || objectToEnrich.getShortName().isEmpty() ) {
            if (objectToEnrich.getFullName() != null && !objectToEnrich.getFullName().isEmpty()) {
                objectToEnrich.setShortName(objectToEnrich.getFullName());

                // FeatureListener needs more time because doesn't look the right place to call it'
                if (getFeatureEnricherListener() != null)
                    getFeatureEnricherListener().onShortNameUpdate(objectToEnrich, null);
            }
        }
        //Detect mutation

        //Translate from one letter code to three letter code

        //DIP Hack ----------------

    }

    @Override
    protected void onInvalidRange(F feature, Range range, Collection<String> errorMessages) {
        super.onInvalidRange(feature, range, errorMessages);
        throw new IllegalStateException("Cannot enrich a feature having invalid ranges: " + StringUtils.join(errorMessages, ", "));
    }

    @Override
    protected void onOutOfDateRange(F feature, Range range, Collection<String> errorMessages, String oldSequence) {
        super.onOutOfDateRange(feature, range, errorMessages, oldSequence);
        throw new IllegalStateException("Cannot enrich a feature having out of date ranges: " + StringUtils.join(errorMessages, ", "));
    }

    @Override
    protected void processOtherProperties(F featureToEnrich) throws EnricherException {
        super.processOtherProperties(featureToEnrich);

        // process identifiers
        processIdentifiers(featureToEnrich, null);

        // process xrefs
        processXrefs(featureToEnrich, null);

        // process synonyms
        processAliases(featureToEnrich, null);

        // process annotations
        processAnnotations(featureToEnrich, null);

        //process short label
        processShortLabel(featureToEnrich, null);
    }

    @Override
    public CvTermEnricher getCvTermEnricher() {
        if (super.getCvTermEnricher() == null) {
            super.setCvTermEnricher((CvTermEnricher<CvTerm>) ApplicationContextProvider.getBean("miCvObjectEnricher"));
        }
        return super.getCvTermEnricher();
    }

    public CvTermEnricher<CvTerm> getIntactCvObjectEnricher() {
        return intactCvObjectEnricher;
    }

    public void setIntactCvObjectEnricher(CvTermEnricher<CvTerm> intactCvObjectEnricher) {
        this.intactCvObjectEnricher = intactCvObjectEnricher;
    }

    protected EnricherContext getEnricherContext() {
        return enricherContext;
    }

    protected enum AminoAcids {
        A("alanine", "ala", "A"),
        R("arginine", "arg", "R"),
        N("asparagine", "asn", "N"),
        D("aspartic acid", "asp", "D"),
        B("asparagine or aspartic acid", "asx", "B"),
        C("cysteine", "cys", "C"),
        E("glutamic acid", "glu", "E"),
        Q("glutamine", "gln", "Q"),
        Z("glutamine or glutamic acid", "glx", "Z"),
        G("glycine", "gly", "G"),
        H("histidine", "his", "H"),
        I("isoleucine", "ile", "I"),
        L("leucine", "leu", "L"),
        K("lysine", "lys", "K"),
        M("methionine", "met", "M"),
        F("phenylalanine", "phe", "F"),
        P("proline", "pro", "P"),
        S("serine", "ser", "S"),
        T("threonine", "thr", "T"),
        W("tryptophan", "trp", "W"),
        Y("tyrosine", "tyr", "Y"),
        V("valine", "val", "V");

        private final String name;
        private final String threeLetterCode;
        private final String oneLetterCode;

        AminoAcids(String name, String threeLetterCode, String oneLetterCode) {
            this.name = name;
            this.threeLetterCode = threeLetterCode;
            this.oneLetterCode = oneLetterCode;
        }

        public String getName() {
            return name;
        }

        public String getThreeLetterCode() {
            return threeLetterCode;
        }

        public String getOneLetterCode() {
            return oneLetterCode;
        }
    }
}
