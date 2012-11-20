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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import psidev.psi.mi.xml.model.Attribute;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiMiPopulator;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Institution;

import java.util.regex.Matcher;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotationConverter extends AbstractIntactPsiConverter<Annotation, Attribute> {
    private PsiMiPopulator psiMiPopulator;

    public AnnotationConverter(Institution institution) {
        super(institution);
        psiMiPopulator = new PsiMiPopulator(institution);
    }

    public Annotation psiToIntact(Attribute psiObject) {
        psiStartConversion(psiObject);

        CvTopic cvTopic = new CvTopic(getInstitution(), psiObject.getName());

        // all name Acs should be from psi mi controlled vocabularies
        if (psiObject.getNameAc() != null) {
            cvTopic.setIdentifier(psiObject.getNameAc());
            psiMiPopulator.populateWithPsiMi(cvTopic, psiObject.getNameAc());
        }

        Annotation annotation = new Annotation(getInstitution(), cvTopic, psiObject.getValue());

        psiEndConversion(psiObject);

        return annotation;
    }

    public Attribute intactToPsi(Annotation intactObject) {
        intactStartConversation(intactObject);

        String name=null;
        String nameAc= null;
        if (intactObject.getCvTopic() == null){
            name=CvTopic.COMMENT;
            nameAc=CvTopic.COMMENT_MI_REF;
        }
        else {
            name = intactObject.getCvTopic().getShortLabel();

            if (intactObject.getCvTopic().getIdentifier() != null) {

                String upperId = intactObject.getCvTopic().getIdentifier().toUpperCase();
                Matcher topicMatcher = CvObjectConverter.MI_REGEXP.matcher(upperId);

                if (topicMatcher.find() && topicMatcher.group().equalsIgnoreCase(upperId)){
                    nameAc = intactObject.getCvTopic().getIdentifier();
                }
            }

            if (name == null && nameAc == null){
                name=CvTopic.COMMENT;
                nameAc=CvTopic.COMMENT_MI_REF;
            }
        }

        Attribute attribute = new Attribute(nameAc, name, intactObject.getAnnotationText());

        intactEndConversion(intactObject);

        return attribute;
    }
}