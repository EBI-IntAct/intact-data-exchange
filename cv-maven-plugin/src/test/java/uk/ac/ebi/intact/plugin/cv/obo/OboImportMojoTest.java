/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.plugin.cv.obo;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.io.File;

import uk.ac.ebi.intact.context.IntactContext;

public class OboImportMojoTest extends AbstractMojoTestCase
{

    public void testSimpleGeneration() throws Exception {
        File pluginXmlFile = new File( getBasedir(), "src/test/plugin-configs/oboimp-config.xml" );

        OboImportMojo mojo = (OboImportMojo) lookupMojo( "obo-imp", pluginXmlFile );
        mojo.setLog( new SystemStreamLog() );

        mojo.execute();

        IntactContext.getCurrentInstance().getConfig().getDefaultDataConfig().closeSessionFactory();
    }
}
