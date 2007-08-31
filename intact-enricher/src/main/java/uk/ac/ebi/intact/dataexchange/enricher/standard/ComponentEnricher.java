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

import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvExperimentalPreparation;
import uk.ac.ebi.intact.model.CvIdentification;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ComponentEnricher implements Enricher<Component>{

     private static ThreadLocal<ComponentEnricher> instance = new ThreadLocal<ComponentEnricher>() {
        @Override
        protected ComponentEnricher initialValue() {
            return new ComponentEnricher();
        }
    };

    public static ComponentEnricher getInstance() {
        return instance.get();
    }

    protected ComponentEnricher() {
    }

    public void enrich(Component objectToEnrich) {
        if (objectToEnrich.getExpressedIn() != null) {
            BioSourceEnricher bioSourceEnricher = BioSourceEnricher.getInstance();
            bioSourceEnricher.enrich(objectToEnrich.getExpressedIn());
        }

        InteractorEnricher interactorEnricher = InteractorEnricher.getInstance();
        interactorEnricher.enrich(objectToEnrich.getInteractor());

        CvObjectEnricher cvObjectEnricher = CvObjectEnricher.getInstance();
        if (objectToEnrich.getCvBiologicalRole() != null) {
            cvObjectEnricher.enrich(objectToEnrich.getCvBiologicalRole());
        }
        if (objectToEnrich.getCvExperimentalRole() != null) {
            cvObjectEnricher.enrich(objectToEnrich.getCvExperimentalRole());
        }
        for (CvExperimentalPreparation experimentalPreparation : objectToEnrich.getExperimentalPreparations()) {
            cvObjectEnricher.enrich(experimentalPreparation);
        }
        for (CvIdentification participantDetectionMethods : objectToEnrich.getParticipantDetectionMethods()) {
            cvObjectEnricher.enrich(participantDetectionMethods);
        }
    }

    public void close() {
    }
}