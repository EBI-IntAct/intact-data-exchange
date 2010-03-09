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
import psidev.psi.mi.xml.model.CellType;
import psidev.psi.mi.xml.model.Organism;
import psidev.psi.mi.xml.model.Tissue;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.CvCellType;
import uk.ac.ebi.intact.model.CvTissue;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OrganismConverterTest extends IntactBasicTestCase {

    @Test
    public void psiToIntact_default() throws Exception {
        Organism organism = PsiMockFactory.createMockOrganism();

        OrganismConverter organismConverter = new OrganismConverter(getMockBuilder().getInstitution());
        BioSource bioSource = organismConverter.psiToIntact(organism);

        Assert.assertNotNull(bioSource);
        Assert.assertNotNull(bioSource.getOwner().getShortLabel());
    }

    @Test
    public void psiToIntact_withCellType() throws Exception {
        Organism organism = PsiMockFactory.createMockOrganism();
        organism.setNcbiTaxId(9606);

        final CellType type = PsiMockFactory.createCvType(CellType.class, null, "293");
        organism.setCellType(type);

        final Tissue tissue = PsiMockFactory.createCvType(Tissue.class, "IA:0191", "lalaTissue");
        organism.setTissue(tissue);

        OrganismConverter organismConverter = new OrganismConverter(getMockBuilder().getInstitution());
        BioSource bioSource = organismConverter.psiToIntact(organism);

        Assert.assertNotNull(bioSource);
        Assert.assertEquals("9606", bioSource.getTaxId());
        Assert.assertNotNull(bioSource.getOwner().getShortLabel());
        Assert.assertNotNull(bioSource.getCvCellType());
        Assert.assertNotNull(bioSource.getCvTissue());
        Assert.assertEquals("293", bioSource.getCvCellType().getShortLabel());
        Assert.assertEquals("lalatissue", bioSource.getCvTissue().getShortLabel());
        Assert.assertEquals("IA:0191", bioSource.getCvTissue().getIdentifier());
    }

    @Test
    public void intactToPsi_withCvs() throws Exception {
        BioSource bioSource = getMockBuilder().createBioSource(9606, "human");
        CvCellType cellType = getMockBuilder().createCvObject(CvCellType.class, null, "293");
        CvTissue tissue = getMockBuilder().createCvObject(CvTissue.class, "IA:0191", "lalatissue");

        bioSource.setCvCellType(cellType);
        bioSource.setCvTissue(tissue);

        OrganismConverter organismConverter = new OrganismConverter(getMockBuilder().getInstitution());
        Organism organism = organismConverter.intactToPsi(bioSource);

        Assert.assertNotNull(organism);
        Assert.assertEquals(9606, organism.getNcbiTaxId());
        Assert.assertEquals("human", organism.getNames().getShortLabel());
        Assert.assertNotNull(organism.getCellType());
        Assert.assertNotNull(organism.getTissue());
        Assert.assertEquals("293", organism.getCellType().getNames().getShortLabel());
        Assert.assertEquals("lalatissue", organism.getTissue().getNames().getShortLabel());
        Assert.assertEquals("IA:0191", organism.getTissue().getXref().getPrimaryRef().getId());
    }

}