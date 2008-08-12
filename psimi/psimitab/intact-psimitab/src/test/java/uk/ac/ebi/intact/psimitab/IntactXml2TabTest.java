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
import psidev.psi.mi.tab.converter.xml2tab.Xml2Tab;
import psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.CrossReferenceFactory;
import uk.ac.ebi.intact.psimitab.processor.IntactClusterInteractorPairProcessor;

import java.io.File;
import java.util.Collection;

/**
 * TODO comment that class header
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 */
public class IntactXml2TabTest extends AbstractPsimitabTestCase {

    @Test
    public void xmlConvert() throws Exception {
        File xmlFile = getFileByResources( "/psi25-testset/9971739.xml", IntactTabTest.class );
        assertTrue( xmlFile.canRead() );

        // convert into Tab object model
        Xml2Tab xml2tab = new IntactXml2Tab( false, false );

        xml2tab.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        xml2tab.addOverrideSourceDatabase( CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" ) );
        xml2tab.setPostProcessor( new IntactClusterInteractorPairProcessor() );

        Collection<BinaryInteraction> interactions = xml2tab.convert( xmlFile, false );
        assertEquals( interactions.size(), 3 );

        for ( BinaryInteraction bi : interactions ) {
            IntactBinaryInteraction dbi = ( IntactBinaryInteraction ) bi;
            assertTrue( dbi.getAuthors().get( 0 ).getName().contains( "Leung" ) );
            assertTrue( dbi.hasExperimentalRolesInteractorA() );
            assertTrue( dbi.hasExperimentalRolesInteractorB() );
            assertTrue( dbi.hasPropertiesA() );
            assertTrue( dbi.hasPropertiesB() );
            assertTrue( BinaryInteractionImpl.class.isAssignableFrom( dbi.getClass() ) );
        }
    }
}