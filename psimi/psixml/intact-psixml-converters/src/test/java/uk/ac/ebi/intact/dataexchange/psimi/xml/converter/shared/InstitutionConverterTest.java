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
import psidev.psi.mi.xml.model.Source;
import uk.ac.ebi.intact.model.Institution;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InstitutionConverterTest extends AbstractConverterTest {

    @Test
    public void psiToIntact_intact() throws Exception {
        Source source = PsiMockFactory.createMockSource();
        source.getAttributes().add(PsiMockFactory.createAttribute());

        Assert.assertEquals(Institution.INTACT, source.getNames().getShortLabel());

        InstitutionConverter institutionConverter = new InstitutionConverter();
        Institution institution = institutionConverter.psiToIntact(source);

        Assert.assertNotNull(institution);
        Assert.assertEquals(Institution.INTACT, institution.getShortLabel());
        Assert.assertNotNull(institution.getXrefs().iterator().next().getOwner().getShortLabel());
        Assert.assertNotNull(institution.getAliases().iterator().next().getOwner().getShortLabel());
        Assert.assertNotNull(institution.getAnnotations().iterator().next().getOwner().getShortLabel());
    }

    @Test
    public void intactToPsi_intact() throws Exception {
        Institution institution = getMockBuilder().createInstitution(Institution.MINT_REF, Institution.MINT);

        Assert.assertEquals(Institution.MINT, institution.getShortLabel());

        InstitutionConverter institutionConverter = new InstitutionConverter();
        Source source = institutionConverter.intactToPsi(institution);

        Assert.assertNotNull(source);
        Assert.assertEquals(Institution.MINT, source.getNames().getShortLabel());
        Assert.assertEquals(Institution.MINT_REF, source.getXref().getPrimaryRef().getId());
    }

}