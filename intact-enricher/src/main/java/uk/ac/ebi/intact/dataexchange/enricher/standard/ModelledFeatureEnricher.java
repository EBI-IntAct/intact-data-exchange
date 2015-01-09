package uk.ac.ebi.intact.dataexchange.enricher.standard;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.model.ModelledFeature;

/**
 * Provides full enrichment of feature.
 *
 * - enrich minimal properties of feature (see MinimalFeatureEnricher)
 * - enrich interaction dependency
 * - enrich interaction effect
 * - enrich xrefs
 * - enrich aliases
 * - enrich annotations
 * - enrich linked features
 *
 *
 * @since 13/08/13
 */
@Component(value = "intactModelledFeatureEnricher")
@Lazy
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class ModelledFeatureEnricher extends FeatureEnricher<ModelledFeature> {

    public ModelledFeatureEnricher(){
        super();
    }
}
