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
package uk.ac.ebi.intact.application.dataConversion;

import junit.framework.TestCase;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.plugins.targetspecies.UpdateTargetSpecies;

import java.io.File;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class DataConversionAbstractTest extends TestCase
{

    private static boolean prepared = false;

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        if (!prepared)
        {
            // update target species
            IntactContext.getCurrentInstance().getDataContext().beginTransaction();
            UpdateTargetSpecies.update(System.out, false);
            IntactContext.getCurrentInstance().getDataContext().commitTransaction();

            File file = new File("reverseMapping.txt.ser");
            if (file.exists()) file.delete();
        }

        prepared = true;

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
    }
}
