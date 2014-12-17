/*
 * Copyright 2001-2008 The European Bioinformatics Institute.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.bridges.fetcher.CvTermFetcher;
import psidev.psi.mi.jami.enricher.CvTermEnricher;
import psidev.psi.mi.jami.enricher.PublicationEnricher;
import psidev.psi.mi.jami.enricher.SourceEnricher;
import psidev.psi.mi.jami.enricher.listener.CvTermEnricherListener;
import psidev.psi.mi.jami.enricher.listener.impl.log.SourceEnricherLogger;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Source;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.AbstractCvObjectFetcher;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component(value = "intactInstitutionEnricher")
@Lazy
public class InstitutionEnricher extends AbstractCvObjectEnricher<Source> implements SourceEnricher{

    @Autowired
    public InstitutionEnricher(@Qualifier("miCvObjectFetcher") CvTermFetcher<CvTerm> intactCvObjectFetcher) {
        super((AbstractCvObjectFetcher)intactCvObjectFetcher);
    }

    @Override
    protected CvTermEnricher<CvTerm> getCvEnricher() {
        return ApplicationContextProvider.getBean("miCvObjectEnricher");
    }

    @Override
    public PublicationEnricher getPublicationEnricher() {
        return null;
    }

    @Override
    public CvTermEnricherListener<Source> getCvTermEnricherListener() {
        if (super.getCvTermEnricherListener() == null){
            super.setCvTermEnricherListener(new SourceEnricherLogger());
        }
        return super.getCvTermEnricherListener();
    }
}
