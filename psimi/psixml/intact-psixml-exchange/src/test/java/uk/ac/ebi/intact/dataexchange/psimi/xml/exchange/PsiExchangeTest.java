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
package uk.ac.ebi.intact.dataexchange.psimi.xml.exchange;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.core.unit.IntactUnit;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Alias;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.util.DebugUtil;
import uk.ac.ebi.intact.context.IntactContext;

import java.io.StringWriter;
import java.util.Arrays;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiExchangeTest extends AbstractPsiExchangeTest  {

    @Before
    public void prepare() throws Exception {
        IntactUnit iu = new IntactUnit();
        iu.createSchema();
    }

    @After
    public void endTest() throws Exception {
        commitTransaction();
    }

    @Test
    public void importXml_intact() throws Exception {
        EntrySet set = getIntactEntrySet();
        /*
        for (Entry entry : set.getEntries()) {
            for (psidev.psi.mi.xml.model.Interaction interaction : entry.getInteractions()) {
                for (Participant p : interaction.getParticipants()) {
                    p.getFeatures().clear();
                }
            }
        }  */

        PsiExchange.importIntoIntact(set, false);


        beginTransaction();
        int count = getDaoFactory().getInteractionDao().countAll();
        System.out.println(DebugUtil.labelList(getDaoFactory().getInteractionDao().getAll()));
        commitTransaction();

        Assert.assertEquals(6, count);
    }

    @Test
    public void importXml_mint() throws Exception {

        PsiExchange.importIntoIntact(getMintEntrySet(), false);

        beginTransaction();
        int count = getDaoFactory().getInteractionDao().countAll();
        System.out.println(DebugUtil.labelList(getDaoFactory().getInteractionDao().getAll()));
        commitTransaction();

        
        Assert.assertEquals(11, count);
    }

    @Test
    public void importXml_dip() throws Exception {
        PsiExchange.importIntoIntact(getDipEntrySet(), false);

        beginTransaction();
        int count = getDaoFactory().getInteractionDao().countAll();
        System.out.println(DebugUtil.labelList(getDaoFactory().getInteractionDao().getAll()));
        commitTransaction();

        Assert.assertEquals(32, count);
    }

    @Test
    public void importXml_all() throws Exception {
        PsiExchange.importIntoIntact(getIntactEntrySet(), false);

        beginTransaction();
        Assert.assertEquals(6, getDaoFactory().getInteractionDao().countAll());
        commitTransaction();

        PsiExchange.importIntoIntact(getMintEntrySet(), false);

        beginTransaction();
        Assert.assertEquals(17, getDaoFactory().getInteractionDao().countAll());
        commitTransaction();

        PsiExchange.importIntoIntact(getDipEntrySet(), false);

        beginTransaction();
        Assert.assertEquals(49, getDaoFactory().getInteractionDao().countAll());
        commitTransaction();

    }

    @Test
    public void checkPsiMiIdentities() throws Exception {
        PsiExchange.importIntoIntact(getIntactEntrySet(), false);

        beginTransaction();
        CvExperimentalRole expRole = getDaoFactory().getCvObjectDao(CvExperimentalRole.class).getAll(0,1).iterator().next();

        Assert.assertNotNull(expRole);

        CvObjectXref identityXref = CvObjectUtils.getPsiMiIdentityXref(expRole);

        Assert.assertNotNull(identityXref);

        commitTransaction();
    }

    @Test
    public void checkAliases() throws Exception {
        PsiExchange.importIntoIntact(getIntactEntrySet(), false);

        beginTransaction();
        Interactor interactor = getDaoFactory().getInteractorDao().getByShortLabel("fadd_mouse");

        Alias alias = interactor.getAliases().iterator().next();
      
        Assert.assertEquals("Fadd", alias.getName());

        CvObjectXref aliasTypeIdentXref = CvObjectUtils.getPsiMiIdentityXref(alias.getCvAliasType());
        Assert.assertNotNull(aliasTypeIdentXref);
        Assert.assertEquals(CvAliasType.GENE_NAME_MI_REF, aliasTypeIdentXref.getPrimaryId());

        commitTransaction();
    }

    @Test
    public void export() throws Exception {

        Interaction mockInteraction = getMockBuilder().createInteractionRandomBinary();
        Experiment exp = mockInteraction.getExperiments().iterator().next();
        exp.addXref(getMockBuilder().createPrimaryReferenceXref(exp, "1234567"));

        IntactEntry entry = new IntactEntry(Arrays.asList(mockInteraction));

        StringWriter writer = new StringWriter();
        PsiExchange.exportToPsiXml(writer, entry);

        System.out.println(writer);
    }

}