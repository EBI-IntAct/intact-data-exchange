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
import psidev.psi.mi.xml.model.Interactor;
import psidev.psi.mi.xml.model.InteractorType;
import psidev.psi.mi.xml.model.Organism;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

/**
 * InteractorConverter Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorConverterTest {

    @Test
    public void psiToIntact_default() throws Exception {
        Interactor psiInteractor = PsiMockFactory.createMockInteractor();
        psiInteractor.getInteractorType().getXref().getPrimaryRef().setId("MI:0326"); // The interactor type is protein but the ids was generated automatically.
        InteractorConverter interactorConverter = new InteractorConverter(new Institution("testInstitution"));

        uk.ac.ebi.intact.model.Interactor interactor = interactorConverter.psiToIntact(psiInteractor);

        CvObjectXref identityXref = CvObjectUtils.getPsiMiIdentityXref(interactor.getCvInteractorType());
        Assert.assertEquals(CvXrefQualifier.IDENTITY, identityXref.getCvXrefQualifier().getShortLabel());

        Assert.assertEquals(psiInteractor.getNames().getAliases().size(), interactor.getAliases().size());
        Assert.assertNotNull(interactor.getOwner());
    }

    @Test
    public void create_peptide() throws Exception {

        InteractorConverter interactorConverter = new InteractorConverter(new Institution("testInstitution"));

        Organism organism = PsiMockFactory.createMockOrganism();
        InteractorType interactorType = PsiMockFactory.createCvType(InteractorType.class, "MI:0327", "peptide");

        uk.ac.ebi.intact.model.Interactor interactor = interactorConverter.newInteractorAccordingToType(organism, "interactorTest", interactorType);

        Assert.assertEquals(true, interactor instanceof ProteinImpl);
        Assert.assertEquals("peptide", interactor.getCvInteractorType().getShortLabel());
    }

    @Test
    public void create_peptide_withShortLabel() throws Exception {

        InteractorConverter interactorConverter = new InteractorConverter(new Institution("testInstitution"));

        Organism organism = PsiMockFactory.createMockOrganism();
        InteractorType interactorType = PsiMockFactory.createCvType(InteractorType.class, null, "peptide");

        interactorType.setXref(null);

        uk.ac.ebi.intact.model.Interactor interactor = interactorConverter.newInteractorAccordingToType(organism, "interactorTest", interactorType);

        Assert.assertEquals(true, interactor instanceof ProteinImpl);
        Assert.assertEquals("peptide", interactor.getCvInteractorType().getShortLabel());
    }

    @Test
    public void create_protein_withShortLabel() throws Exception {

        InteractorConverter interactorConverter = new InteractorConverter(new Institution("testInstitution"));

        Organism organism = PsiMockFactory.createMockOrganism();
        InteractorType interactorType = PsiMockFactory.createCvType(InteractorType.class, null, "protein");

        interactorType.setXref(null);

        uk.ac.ebi.intact.model.Interactor interactor = interactorConverter.newInteractorAccordingToType(organism, "interactorTest", interactorType);

        Assert.assertEquals(true, interactor instanceof ProteinImpl);
        Assert.assertEquals("protein", interactor.getCvInteractorType().getShortLabel());
    }

    @Test
    public void psiToIntact_trna() throws Exception {
        Interactor psiInteractor = PsiMockFactory.createMockInteractor();
        psiInteractor.getInteractorType().getXref().getPrimaryRef().setId( "MI:0325" ); // tRNA
        psiInteractor.getInteractorType().getNames().setShortLabel( "tRNA" ); // tRNA

        InteractorConverter interactorConverter = new InteractorConverter(new Institution("testInstitution"));

        uk.ac.ebi.intact.model.Interactor interactor = interactorConverter.psiToIntact(psiInteractor);

        Assert.assertTrue( interactor.getClass().getName(), interactor instanceof NucleicAcidImpl );
    }
}