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
import uk.ac.ebi.intact.model.*;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotationConverter extends AbstractIntactPsiConverter<Annotation, Attribute> {

    public AnnotationConverter(Institution institution) {
        super(institution);
    }

    public Annotation psiToIntact(Attribute psiObject) {
        psiEndConversion(psiObject);

        CvTopic cvTopic = new CvTopic(getInstitution(), psiObject.getName());
        Annotation annotation = new Annotation(getInstitution(), cvTopic, psiObject.getValue());

        psiEndConversion(psiObject);

        return annotation;
    }

    public Attribute intactToPsi(Annotation intactObject) {
        intactStartConversation(intactObject);

        Attribute attribute = new Attribute(intactObject.getCvTopic().getShortLabel(), intactObject.getAnnotationText());

        psiEndConversion(intactObject);

        return attribute;
    }
}