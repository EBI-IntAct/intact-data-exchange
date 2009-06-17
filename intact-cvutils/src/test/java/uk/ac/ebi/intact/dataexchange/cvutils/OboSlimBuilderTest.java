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
package uk.ac.ebi.intact.dataexchange.cvutils;

import org.junit.Test;
import org.junit.Assert;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.impl.OBOClassImpl;

import java.net.URL;
import java.io.File;

/**
 * OboSlimBuilder Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.3
 */
public class OboSlimBuilderTest {

    @Test
    public void demo() throws Exception {
        OboSlimBuilder builder = new OboSlimBuilder();

        final URL url = OboSlimBuilderTest.class.getResource( "/ontologies/cars.obo" );
        builder.setOboLocation( url );

        builder.addTerm( "car:09007" ); // citroen
        builder.setIncludeParents( true );
        builder.setIncludeChildren( true );

        File output = new File( "target/car_citroen.obo" );

        builder.build( output );

        Assert.assertTrue( output.exists() );
        Assert.assertTrue( output.length() > 0 );

        final OBOSession exportedSession = OboUtils.createOBOSession( output.getAbsolutePath() );
        Assert.assertNotNull( exportedSession );
        Assert.assertTrue( exportedSession.getObjects().size() > 0 );

        Assert.assertNotNull( exportedSession.getObject( "car:09003" )); // Car
        Assert.assertNotNull( exportedSession.getObject( "car:09007" )); // Citroen
        Assert.assertNotNull( exportedSession.getObject( "car:09008" )); // C5
        Assert.assertNotNull( exportedSession.getObject( "car:09009" )); // AX

        // other terms should not be there
        Assert.assertNull( exportedSession.getObject( "car:09004" )); // Ford
        Assert.assertNull( exportedSession.getObject( "car:09006" )); // Rolls
        Assert.assertNull( exportedSession.getObject( "ID:090010" )); // vectra
        Assert.assertNull( exportedSession.getObject( "ID:090011" )); // astra
    }
}
