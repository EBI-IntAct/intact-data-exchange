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

import org.junit.Assert;
import org.junit.Test;
import psidev.psi.mi.xml.model.Attribute;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.ExperimentDescription;
import uk.ac.ebi.intact.model.Annotation;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotationConverterTest extends AbstractConverterTest {

    @Test
    public void psiToIntact_default() throws Exception {
        EntrySet entrySet = getIntactEntrySet();

        Entry entry = entrySet.getEntries().iterator().next();

        ExperimentDescription expDesc = null;
        for (ExperimentDescription candidateExp : entry.getExperiments()) {
            if (candidateExp.getId() == 2) {
                expDesc = candidateExp;
            }
        }

        Assert.assertNotNull(expDesc);
        Assert.assertEquals("zhang-2006-1", expDesc.getNames().getShortLabel());

        AnnotationConverter annotationConverter = new AnnotationConverter(getMockInstitution());

        for (Attribute attribute : expDesc.getAttributes()) {
            Annotation annotation = annotationConverter.psiToIntact(attribute);

            Assert.assertNotNull(annotation);
            Assert.assertEquals(attribute.getValue().trim(), annotation.getAnnotationText());
            Assert.assertEquals(attribute.getName(), annotation.getCvTopic().getShortLabel());
        }


    }
}