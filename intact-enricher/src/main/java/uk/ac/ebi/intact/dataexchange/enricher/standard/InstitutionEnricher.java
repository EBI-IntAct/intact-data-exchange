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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.model.Alias;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.InstitutionXref;
import uk.ac.ebi.intact.model.util.XrefUtils;

import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class InstitutionEnricher extends AnnotatedObjectEnricher<Institution>{

    @Autowired
    private ApplicationContext applicationContext;

    public InstitutionEnricher() {
    }

    public void enrich(Institution objectToEnrich) {
        if (objectToEnrich == null) return;

        Collection<Institution> availableInstitutions = applicationContext.getBeansOfType(Institution.class).values();

        InstitutionXref psiMiIdentity = XrefUtils.getPsiMiIdentityXref(objectToEnrich);

        for (Institution referenceInstitution : availableInstitutions) {
            if (psiMiIdentity != null) {
               InstitutionXref id = XrefUtils.getPsiMiIdentityXref(referenceInstitution);
                if (psiMiIdentity.equals(id)) {
                    enrichInstitutionFromReference(objectToEnrich, referenceInstitution);
                    break;
                }
            } else {
                if (objectToEnrich.getShortLabel().equalsIgnoreCase(referenceInstitution.getShortLabel())) {
                    enrichInstitutionFromReference(objectToEnrich, referenceInstitution);
                    break;
                } else {
                    for (Alias alias : referenceInstitution.getAliases()) {
                        if (objectToEnrich.getShortLabel().equals(alias.getName())) {
                            enrichInstitutionFromReference(objectToEnrich, referenceInstitution);
                            break;
                        }
                    }
                }
            }
        }

    }

    protected void enrichInstitutionFromReference(Institution institutionToEnrich, Institution reference) {
        institutionToEnrich.setShortLabel(reference.getShortLabel());
        institutionToEnrich.setFullName(reference.getFullName());
        institutionToEnrich.setPostalAddress(reference.getPostalAddress());
        institutionToEnrich.setUrl(reference.getUrl());
        institutionToEnrich.setXrefs(reference.getXrefs());
        institutionToEnrich.setAliases(reference.getAliases());
        institutionToEnrich.setAnnotations(reference.getAnnotations());
    }
}
