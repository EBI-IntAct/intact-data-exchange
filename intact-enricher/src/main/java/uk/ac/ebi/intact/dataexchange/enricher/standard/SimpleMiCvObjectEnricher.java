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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.bridges.fetcher.CvTermFetcher;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.enricher.impl.full.FullCvTermEnricher;
import psidev.psi.mi.jami.model.CvTerm;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.AbstractCvObjectFetcher;

/**
 * CvObject enricher.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component(value = "simpleMiCvObjectEnricher")
@Lazy
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class SimpleMiCvObjectEnricher extends FullCvTermEnricher<CvTerm> {
    @Autowired
    private EnricherContext enricherContext;

    @Autowired
    public SimpleMiCvObjectEnricher(@Qualifier("miCvObjectFetcher") CvTermFetcher<CvTerm> intactCvObjectFetcher) {
        super((AbstractCvObjectFetcher)intactCvObjectFetcher);
    }

    public SimpleMiCvObjectEnricher(AbstractCvObjectFetcher intactCvObjectFetcher) {
        super(intactCvObjectFetcher);
    }

    @Override
    protected void processFullName(CvTerm cvTermToEnrich, CvTerm cvTermFetched) throws EnricherException{
        if((cvTermFetched.getFullName() != null && !cvTermFetched.getFullName().equals(cvTermToEnrich.getFullName()))
                || (cvTermFetched.getFullName() == null
                && cvTermToEnrich.getFullName() != null)){

            String oldValue = cvTermToEnrich.getFullName();
            cvTermToEnrich.setFullName(cvTermFetched.getFullName());
            if (getCvTermEnricherListener() != null)
                getCvTermEnricherListener().onFullNameUpdate(cvTermToEnrich, oldValue);
        }
    }

    @Override
    protected void processMinimalUpdates(CvTerm cvTermToEnrich, CvTerm termFetched) throws EnricherException {
        super.processMinimalUpdates(cvTermToEnrich, termFetched);

        // process shortlabel
        processShortName(cvTermToEnrich, termFetched);
    }

    protected void processShortName(CvTerm cvTermToEnrich, CvTerm cvTermFetched) {
        if(cvTermFetched.getShortName() != null
                && ! cvTermFetched.getShortName().equalsIgnoreCase(cvTermToEnrich.getShortName())){

            String oldValue = cvTermToEnrich.getShortName();
            cvTermToEnrich.setShortName(cvTermFetched.getShortName());
            if (getCvTermEnricherListener() != null)
                getCvTermEnricherListener().onShortNameUpdate(cvTermToEnrich, oldValue);
        }
    }
}
