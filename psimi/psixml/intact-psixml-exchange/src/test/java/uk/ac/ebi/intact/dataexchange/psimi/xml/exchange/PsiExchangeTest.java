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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.unit.IntactUnit;
import uk.ac.ebi.intact.model.CvBiologicalRole;
import uk.ac.ebi.intact.model.CvObjectXref;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

import java.io.InputStream;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiExchangeTest  {

    private static final String INTACT_FILE = "/xml/intact_2006-07-19.xml";

    protected InputStream getIntactStream() {
         return PsiExchangeTest.class.getResourceAsStream(INTACT_FILE);
    }

    protected EntrySet getIntactEntrySet() throws Exception{
        PsimiXmlReader reader = new PsimiXmlReader();
        return reader.read(getIntactStream());
    }

    protected void clearSchema() throws Exception {
        IntactUnit iu = new IntactUnit();
        iu.createSchema(false);
    }

    protected DaoFactory getDaoFactory() {
         return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }

    protected void beginTransaction() {
         IntactContext.getCurrentInstance().getDataContext().beginTransaction();
    }

    protected void commitTransaction() throws Exception {
         IntactContext.getCurrentInstance().getDataContext().commitTransaction();
    }

    @Test
    public void importXml() throws Exception {
        clearSchema();

        PsiExchange.importIntoIntact(getIntactEntrySet(), false);

        beginTransaction();
        int count = getDaoFactory().getInteractionDao().countAll();
        commitTransaction();

        Assert.assertEquals(6, count);
    }

    @Test
    @Ignore
    public void checkPsiMiIdentities() throws Exception {
        commitTransaction();
        
        clearSchema();

        PsiExchange.importIntoIntact(getIntactEntrySet(), false);

        beginTransaction();
        CvBiologicalRole bioRole = getDaoFactory().getCvObjectDao(CvBiologicalRole.class).getAll(0,1).iterator().next();

        Assert.assertNotNull(bioRole);

        CvObjectXref identityXref = CvObjectUtils.getPsiMiIdentityXref(bioRole);

        System.out.println(identityXref);

        commitTransaction();


    }

}