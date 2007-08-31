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
import psidev.psi.mi.xml.model.ExperimentDescription;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Institution;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentConverterTest extends IntactBasicTestCase {

    @Test
    public void psiToIntact_default() {
        ExperimentDescription expDesc = PsiMockFactory.createMockExperiment();
        expDesc.setXref(null);

        ExperimentConverter converter = new ExperimentConverter(new Institution("testInst"));
        Experiment exp = converter.psiToIntact(expDesc);

        Assert.assertEquals(1, exp.getXrefs().size());
    }

    @Test
    public void psiToIntact_noLabel() {
        ExperimentDescription expDesc = PsiMockFactory.createMockExperiment();
        expDesc.setNames(null);
        expDesc.setXref(null);

        ExperimentConverter converter = new ExperimentConverter(new Institution("testInst"));
        Experiment exp = converter.psiToIntact(expDesc);
        

        Assert.assertEquals(1, exp.getXrefs().size());
    }

    @Test
    public void intactToPsi_default() {
        Experiment exp = getMockBuilder().createExperimentRandom(1);
        exp.getXrefs().clear();
        exp.addXref(getMockBuilder().createPrimaryReferenceXref(exp, "1234567"));

        ExperimentConverter converter = new ExperimentConverter(new Institution("testInst"));
        ExperimentDescription expDesc = converter.intactToPsi(exp);

        Assert.assertNotNull(expDesc.getBibref());
        Assert.assertNull(expDesc.getXref());
    }

    @Test
    public void intactToPsi_noLabel() {
        Experiment exp = getMockBuilder().createExperimentRandom(1);
        exp.setShortLabel(null);
        exp.getXrefs().clear();
        exp.addXref(getMockBuilder().createPrimaryReferenceXref(exp, "1234567"));

        ExperimentConverter converter = new ExperimentConverter(new Institution("testInst"));
        ExperimentDescription expDesc = converter.intactToPsi(exp);

        System.out.println(exp.getShortLabel());

        Assert.assertNotNull(expDesc.getBibref());
        Assert.assertNull(expDesc.getXref());
    }
}