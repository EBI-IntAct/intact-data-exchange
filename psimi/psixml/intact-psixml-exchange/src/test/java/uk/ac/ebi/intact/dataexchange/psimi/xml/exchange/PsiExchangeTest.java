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

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.PsimiXmlReader;
import uk.ac.ebi.intact.core.unit.IntactUnit;
import uk.ac.ebi.intact.core.persister.stats.PersisterStatistics;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.util.DebugUtil;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.context.DataContext;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiExchangeTest extends AbstractPsiExchangeTest  {

    @Test
    public void importXml_intact2() throws Exception {
        EntrySet set = getIntactEntrySet();

        PersisterStatistics stats = PsiExchange.importIntoIntact(set);

        Assert.assertEquals(6, stats.getPersistedCount(InteractionImpl.class, false));
        Assert.assertEquals(0, stats.getDuplicatesCount(InteractionImpl.class, false));
        Assert.assertEquals(6, getDaoFactory().getInteractionDao().countAll());

        Assert.assertEquals(6, getDaoFactory().getInteractionDao().countAll());

        IntactContext intactContext = IntactContext.getCurrentInstance();
        DataContext dataContext = intactContext.getDataContext();
        dataContext.beginTransaction();

        DaoFactory daoFactory = dataContext.getDaoFactory();
        final Collection<InteractionImpl> interactions = daoFactory.getInteractionDao().getAll();
        IntactEntry intactEntry = new IntactEntry(new ArrayList<Interaction>( interactions ) );

        Writer writer = new StringWriter();
        PsiExchange.exportToPsiXml(writer, intactEntry);




        dataContext.commitTransaction();
    }

    @Test
    public void importXml_intact() throws Exception {
        EntrySet set = getIntactEntrySet();

        PersisterStatistics stats = PsiExchange.importIntoIntact(set);

        Assert.assertEquals(6, stats.getPersistedCount(InteractionImpl.class, false));
        Assert.assertEquals(0, stats.getDuplicatesCount(InteractionImpl.class, false));
        Assert.assertEquals(6, getDaoFactory().getInteractionDao().countAll());

        Assert.assertEquals(6, getDaoFactory().getInteractionDao().countAll());
    }

    @Test
    public void importXml_mint() throws Exception {
        PersisterStatistics stats = PsiExchange.importIntoIntact(getMintEntrySet());

        Assert.assertEquals(16, stats.getPersistedCount(InteractionImpl.class, false));
        Assert.assertEquals(2, stats.getDuplicatesCount(InteractionImpl.class, false));
        Assert.assertEquals(16, getDaoFactory().getInteractionDao().countAll());
    }

    @Test
    public void importXml_mint_simplified() throws Exception {
        PsimiXmlReader reader = new PsimiXmlReader();
        EntrySet entrySet = reader.read(PsiExchangeTest.class.getResourceAsStream("/xml/mint_2006-07-18_simplified.xml"));
        PsiExchange.importIntoIntact(entrySet);

        int count = getDaoFactory().getInteractionDao().countAll();
        final List<String> labels = DebugUtil.labelList(getDaoFactory().getInteractionDao().getAll());


        Assert.assertEquals(2, count);
    }

    @Test
    public void importXml_dupes() throws Exception {
        PersisterStatistics stats = PsiExchange.importIntoIntact(PsiExchangeTest.class.getResourceAsStream("/xml/dupes.xml"));

        // there are 8 interactions in the file, but 1 is a duplicate
        Assert.assertEquals(7, getDaoFactory().getInteractionDao().countAll());

        Assert.assertEquals(7, stats.getPersistedCount(InteractionImpl.class, false));
        Assert.assertEquals(1, stats.getDuplicatesCount(InteractionImpl.class, false));
    }

    @Test
    public void importXml_dip() throws Exception {
        PsiExchange.importIntoIntact(getDipEntrySet());

        int count = getDaoFactory().getInteractionDao().countAll();
        Assert.assertEquals(74, count);
    }

    @Test
    public void importXml_all() throws Exception {
        PersisterStatistics intactStatistics = PsiExchange.importIntoIntact(getIntactStream());

        Assert.assertEquals(6, intactStatistics.getPersistedCount(InteractionImpl.class, false));
        Assert.assertEquals(6, getDaoFactory().getInteractionDao().countAll());

        PersisterStatistics mintStats = PsiExchange.importIntoIntact(getMintStream());

        Assert.assertEquals(16, mintStats.getPersistedCount(InteractionImpl.class, false));
        Assert.assertEquals(2, mintStats.getDuplicatesCount(InteractionImpl.class, false));
        Assert.assertEquals(22, getDaoFactory().getInteractionDao().countAll());

        PersisterStatistics dipStats = PsiExchange.importIntoIntact(getDipStream());

        Assert.assertEquals(74, dipStats.getPersistedCount(InteractionImpl.class, false));
        Assert.assertEquals(96, getDaoFactory().getInteractionDao().countAll());

    }

    @Test
    public void checkPsiMiIdentities() throws Exception {
        PsiExchange.importIntoIntact(getIntactEntrySet());

        CvExperimentalRole expRole = getDaoFactory().getCvObjectDao(CvExperimentalRole.class).getAll(0,1).iterator().next();

        Assert.assertNotNull(expRole);

        CvObjectXref identityXref = CvObjectUtils.getPsiMiIdentityXref(expRole);

        Assert.assertNotNull(identityXref);

    }

    @Test
    public void checkAliases() throws Exception {
        PsiExchange.importIntoIntact(getIntactEntrySet());

        Interactor interactor = getDaoFactory().getInteractorDao().getByShortLabel("fadd_mouse");

        Alias alias = interactor.getAliases().iterator().next();
      
        Assert.assertEquals("Fadd", alias.getName());

        CvObjectXref aliasTypeIdentXref = CvObjectUtils.getPsiMiIdentityXref(alias.getCvAliasType());
        Assert.assertNotNull(aliasTypeIdentXref);
        Assert.assertEquals(CvAliasType.GENE_NAME_MI_REF, aliasTypeIdentXref.getPrimaryId());

    }

    @Test
    public void export() throws Exception {

        Interaction mockInteraction = getMockBuilder().createInteractionRandomBinary();
        Experiment exp = mockInteraction.getExperiments().iterator().next();
        exp.addXref(getMockBuilder().createPrimaryReferenceXref(exp, "1234567"));

        IntactEntry entry = new IntactEntry(Arrays.asList(mockInteraction));

        StringWriter writer = new StringWriter();
        PsiExchange.exportToPsiXml(writer, entry);

        
    }

    @Test
    public void getReleaseDates() throws Exception {
        final List<DateTime> releaseDates = PsiExchange.getReleaseDates(PsiExchangeTest.class.getResourceAsStream("/xml/dip_2006-11-01.xml"));

        Assert.assertEquals(26, releaseDates.size());

        for (DateTime releaseDate : releaseDates) {
            Assert.assertEquals(releaseDate, new DateTime("2006-11-01"));
        }
    }

    @Test
    public void toDateTime() throws Exception {
        String date1 = "Wed Sep 20 11:54:49 PDT 2006";
        String date2 = "2006-09-20";

        Assert.assertEquals(new DateTime("2006-09-20T11:54:49.000"), PsiExchange.toDateTime(date1));
        Assert.assertEquals(new DateTime("2006-09-20"), PsiExchange.toDateTime(date2));
    }

}