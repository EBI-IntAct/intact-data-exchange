/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.psimitab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Assert;
import psidev.psi.mi.tab.converter.xml2tab.Xml2Tab;
import psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.CrossReferenceFactory;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.Interaction;
import psidev.psi.mi.xml.model.Participant;
import uk.ac.ebi.intact.psimitab.processor.IntactClusterInteractorPairProcessor;

import java.io.File;
import java.io.StringWriter;
import java.util.Collection;

/**
 * IntactXml2Tab Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.2
 */
public class IntactXml2TabTest extends AbstractPsimitabTestCase {

    @Test
    public void xmlConvert() throws Exception {
        File xmlFile = getFileByResources( "/psi25-testset/9971739.xml", IntactTabTest.class );
        assertTrue( xmlFile.canRead() );

        // convert into Tab object model
        Xml2Tab xml2tab = new IntactXml2Tab();

        xml2tab.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        xml2tab.addOverrideSourceDatabase( CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" ) );
        xml2tab.setPostProcessor( new IntactClusterInteractorPairProcessor() );

        Collection<BinaryInteraction> interactions = xml2tab.convert( xmlFile, false );
        assertEquals( interactions.size(), 3 );

        for ( BinaryInteraction bi : interactions ) {
            IntactBinaryInteraction dbi = ( IntactBinaryInteraction ) bi;
            assertTrue( dbi.getAuthors().get( 0 ).getName().contains( "Leung" ) );
            assertTrue( dbi.getInteractorA().hasExperimentalRoles() );
            assertTrue( dbi.getInteractorB().hasExperimentalRoles() );
            assertTrue( dbi.getInteractorA().hasProperties() );
            assertTrue( dbi.getInteractorB().hasProperties() );
        }
    }

    @Test
    public void xmlConvertWithIdentifierInPropertiesRoundTrip() throws Exception {
        File xmlFile = getFileByResources("/psi25-testset/propertiesTest.xml", IntactTabTest.class);
        assertTrue(xmlFile.canRead());

        // convert into Tab object model
        Xml2Tab xml2tab = new IntactXml2Tab();

        xml2tab.setExpansionStrategy(new SpokeWithoutBaitExpansion());
        xml2tab.addOverrideSourceDatabase(CrossReferenceFactory.getInstance().build("MI", "0469", "intact"));
        xml2tab.setPostProcessor(new IntactClusterInteractorPairProcessor());

        Collection<BinaryInteraction> interactions = xml2tab.convert(xmlFile, false);
        assertEquals(interactions.size(), 1);

        IntactBinaryInteraction dbi = (IntactBinaryInteraction) interactions.iterator().next();

        assertTrue(dbi.getInteractorA().hasProperties());
        assertTrue(dbi.getInteractorB().hasProperties());

        assertEquals(4, dbi.getInteractorA().getProperties().size());
        assertEquals(2, dbi.getInteractorB().getProperties().size());

        Assert.assertTrue("No identifier in Property", checkIfPropertiesHasIdentity(dbi));

        IntactPsimiTabWriter writer = new IntactPsimiTabWriter(false, false);
        writer.setHeaderEnabled(false);
        StringWriter sw = new StringWriter();
        writer.write(dbi, sw);
        String mitabline = sw.getBuffer().toString();

        //Mitab line
        mitabline = mitabline.trim();

        String propertiesA = mitabline.split("\t")[19];
        Assert.assertNotNull(propertiesA);
        int numOfPropsA = propertiesA.split("\\|").length;
        Assert.assertEquals(4, numOfPropsA); //four properties

        String propertiesB = mitabline.split("\t")[20];
        Assert.assertNotNull(propertiesB);
        int numOfPropsB = propertiesB.split("\\|").length;
        Assert.assertEquals(2, numOfPropsB); //two properties

        PsimiTabReader reader = new IntactPsimiTabReader(false);
        Collection<BinaryInteraction> bis = reader.read(mitabline);
        Assert.assertEquals(1, bis.size());
        IntactTab2Xml tab2xml = new IntactTab2Xml();
        final EntrySet entrySet = tab2xml.convert(bis);

        Collection<Interaction> xmlinteractions = entrySet.getEntries().iterator().next().getInteractions();
        Assert.assertEquals(1, xmlinteractions.size());

        Interaction interaction = xmlinteractions.iterator().next();

        int xrefACount = 0;
        int xrefBCount = 0;
        for (Participant participant : interaction.getParticipants()) {
            if (participant.getInteractor().getNames().getShortLabel().equals("primId_interactorA")) {
                xrefACount = participant.getInteractor().getXref().getAllDbReferences().size();
            }
            if (participant.getInteractor().getNames().getShortLabel().equals("primId_interactorB")) {
                xrefBCount = participant.getInteractor().getXref().getAllDbReferences().size();
            }
        }
        Assert.assertEquals(4, xrefACount);
        Assert.assertEquals(2, xrefBCount);

    }


    private boolean checkIfPropertiesHasIdentity(IntactBinaryInteraction dbi) {
        boolean hasIdentifierInProperty = false;
        for (CrossReference crossReference : dbi.getInteractorA().getProperties()) {
            if (crossReference.getText().equals("identity")) {
                hasIdentifierInProperty = true;
            }
        }
        return hasIdentifierInProperty;
    }

}