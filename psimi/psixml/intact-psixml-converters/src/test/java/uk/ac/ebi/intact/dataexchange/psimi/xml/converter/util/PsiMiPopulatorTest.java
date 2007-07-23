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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.easymock.classextension.EasyMock.*;
import org.junit.Test;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.InteractorXref;
import uk.ac.ebi.intact.model.ProteinImpl;
import uk.ac.ebi.intact.model.Xref;

import java.util.ArrayList;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiMiPopulatorTest {

    @Test
    public void populateWithPsiMi_AnnotatedObject() throws Exception {

        ProteinImpl mockProtein = createNiceMock(ProteinImpl.class);
        String mi = "MI:007";

        expect(mockProtein.getOwner()).andReturn(new Institution()).once();
        expect(mockProtein.getXrefs()).andReturn(new ArrayList<InteractorXref>()).atLeastOnce();

        replay(mockProtein);

        PsiMiPopulator populator = new PsiMiPopulator(new Institution("institution"));
        populator.populateWithPsiMi(mockProtein, mi);

        verify(mockProtein);

        assertEquals(1, mockProtein.getXrefs().size());

        Xref xref = mockProtein.getXrefs().iterator().next();
        assertEquals(mi, xref.getPrimaryId());
        assertNotNull(xref.getCvDatabase());
        assertNotNull(xref.getCvXrefQualifier());

    }
}