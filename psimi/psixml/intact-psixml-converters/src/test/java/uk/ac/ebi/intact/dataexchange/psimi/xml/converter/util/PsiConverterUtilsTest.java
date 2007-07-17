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

import static org.junit.Assert.*;
import org.junit.Test;
import psidev.psi.mi.xml.model.DbReference;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.PsiMockFactory;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;

import java.util.Arrays;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiConverterUtilsTest {

    @Test
    public void getIdentity_ordered() {
        DbReference dbRefUniprot = PsiMockFactory.createDbReference(CvXrefQualifier.IDENTITY, CvXrefQualifier.IDENTITY_MI_REF,
                                                                    CvDatabase.UNIPROT, CvDatabase.UNIPROT_MI_REF);
        DbReference dbRefRefSeq = PsiMockFactory.createDbReference(CvXrefQualifier.IDENTITY, CvXrefQualifier.IDENTITY_MI_REF,
                                                                   CvDatabase.REFSEQ, CvDatabase.REFSEQ_MI_REF);
        DbReference dbRefRandom = PsiMockFactory.createDbReference(CvXrefQualifier.IDENTITY, CvXrefQualifier.IDENTITY_MI_REF);

        List<DbReference> dbRefs = Arrays.asList(dbRefUniprot, dbRefRefSeq, dbRefRandom);

        DbReference identityRef = PsiConverterUtils.getIdentity(dbRefs);

        assertNotNull(identityRef);
        assertEquals(dbRefUniprot.getDb(), identityRef.getDb());
    }

    @Test
    public void getIdentity_unordered() {
        DbReference dbRefUniprot = PsiMockFactory.createDbReference(CvXrefQualifier.IDENTITY, CvXrefQualifier.IDENTITY_MI_REF,
                                                                    CvDatabase.UNIPROT, CvDatabase.UNIPROT_MI_REF);
        DbReference dbRefRefSeq = PsiMockFactory.createDbReference(CvXrefQualifier.IDENTITY, CvXrefQualifier.IDENTITY_MI_REF,
                                                                   CvDatabase.REFSEQ, CvDatabase.REFSEQ_MI_REF);
        DbReference dbRefRandom = PsiMockFactory.createDbReference(CvXrefQualifier.IDENTITY, CvXrefQualifier.IDENTITY_MI_REF);

        List<DbReference> dbRefs = Arrays.asList(dbRefRefSeq, dbRefUniprot, dbRefRandom);

        DbReference identityRef = PsiConverterUtils.getIdentity(dbRefs);

        assertNotNull(identityRef);
        assertEquals(dbRefUniprot.getDb(), identityRef.getDb());
    }
}