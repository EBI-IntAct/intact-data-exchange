/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.enricher.standard;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.model.ModelledFeature;
import psidev.psi.mi.jami.model.ModelledParticipant;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;

/**
 * Modelled participant enricher
 *
 * @version $Id$
 */
@Component(value = "intactModelledParticipantEnricher")
@Lazy
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class ModelledComponentEnricher extends ParticipantEnricher<ModelledParticipant, ModelledFeature>{

    public ModelledComponentEnricher() {
    }

    @Override
    public psidev.psi.mi.jami.enricher.FeatureEnricher<ModelledFeature> getFeatureEnricher() {
        if (super.getFeatureEnricher() == null){
            super.setFeatureEnricher((ModelledFeatureEnricher) ApplicationContextProvider.getBean("intactModelledFeatureEnricher"));
        }
        return super.getFeatureEnricher();
    }
}
