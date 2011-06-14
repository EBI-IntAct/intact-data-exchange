/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.psimitab.converters.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.*;

import java.io.StringWriter;
import java.util.List;

/**
 * DatabaseSimpleMitabExporter Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.2
 */
public class DatabaseSimpleMitabExporterTest extends IntactBasicTestCase {

    private DatabaseSimpleMitabExporter exporter;

    @Before
    public void before() throws Exception {
        exporter = new DatabaseSimpleMitabExporter();
    }

    @After
    public void after() throws Exception {
        exporter = null;
    }

    @Test
    @Transactional(propagation = Propagation.NEVER)
    @DirtiesContext
    public void exportInteractions() throws Exception {
        TransactionStatus status1 = getDataContext().beginTransaction();

        // this interaction is going to generate 2 binary i1 via the spoke expansion
        Interaction i1 = getMockBuilder().createInteraction("a1", "a2", "a3");

        // this interaction is going to be exported even though a1-a2 is already in the i1 above (no clustering)
        Interaction i2 = getMockBuilder().createInteraction("a1", "a2");

        // this interaction is going to be exported even though a1-a2 is already in the i1 above (no clustering)
        Interaction i3 = getMockBuilder().createInteraction("a4", "a2");

        final Interactor interactor = i1.getComponents().iterator().next().getInteractor();
        CvDatabase goDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO);
        interactor.addXref(getMockBuilder().createXref(interactor, "GO:0007028", null, goDb));

        getCorePersister().saveOrUpdate( i1, i2, i3);

        Assert.assertEquals(3, getDaoFactory().getInteractionDao().countAll());
        Assert.assertEquals(4, getDaoFactory().getInteractorDao( ProteinImpl.class ).countAll());

        getDataContext().commitTransaction(status1);

        StringWriter mitabWriter = new StringWriter();

        TransactionStatus status = getDataContext().beginTransaction();

        exporter.exportAllInteractions(mitabWriter);
        mitabWriter.close();

        getDataContext().commitTransaction(status);

        final String mitab = mitabWriter.toString();
        final String[] lines = mitab.split( "\n" );
        Assert.assertEquals(4, lines.length);
        for ( String line : lines ) {

            Assert.assertTrue( line.contains( "(rigid)" ));
            Assert.assertTrue( line.contains( "(rogid)" ));

        }
    }

    @Test
    @DirtiesContext
    public void exportInteractionsWithSameRoles() throws Exception {

        Assert.assertEquals( 0, getDaoFactory().getInteractorDao( InteractorImpl.class ).countAll() );

        Protein prot1 = getMockBuilder().createDeterministicProtein( "P1234", "p1" );
        Interaction interaction1 = getMockBuilder().createInteraction( prot1 );
        interaction1.setShortLabel( "p1-p1" );
        final Component baitComponent = getMockBuilder().createComponentBait(interaction1, prot1 );
        final Component preyComponent = getMockBuilder().createComponentPrey(interaction1, prot1 );

        getCorePersister().saveOrUpdate( baitComponent );
        getCorePersister().saveOrUpdate( preyComponent );

        Assert.assertEquals( 1, getDaoFactory().getProteinDao().countAll() );
        Assert.assertEquals( 3, getDaoFactory().getComponentDao().countAll() );

        Protein prot2 = getMockBuilder().createDeterministicProtein( "Q3334", "q2" );
        Protein prot3 = getMockBuilder().createDeterministicProtein( "Q3335", "q3" );
        getCorePersister().saveOrUpdate( prot2,prot3 );

        Interaction interaction2 = getMockBuilder().createInteraction( prot2, prot3 );
        interaction2.setShortLabel( "q2-q3" );

        getCorePersister().saveOrUpdate( interaction1,interaction2 );
        Assert.assertEquals( 3, getDaoFactory().getProteinDao().countAll() );

        //p1,q2,q3, and interaction p1-p1 and interaction q2-q2
        Assert.assertEquals( 5, getDaoFactory().getInteractorDao( InteractorImpl.class ).countAll() );

        final List<InteractorImpl> proteinList = getDaoFactory().getInteractorDao( InteractorImpl.class ).getAll();
        StringWriter mitabWriter = new StringWriter();

        exporter.exportAllInteractions( mitabWriter );

        mitabWriter.close();

        final String mitab = mitabWriter.toString();
        final String[] lines = mitab.split( "\n" );
        Assert.assertEquals( 3, lines.length );
        for ( String line : lines ) {
          Assert.assertFalse( line.contains("psi-mi:\"MI:0498\"(prey)|psi-mi:\"MI:0496\"(bait)"));
        }

    }

}
