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
package uk.ac.ebi.intact.dataexchange.enricher.standard;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.model.BioactiveEntity;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.model.impl.DefaultBioactiveEntity;
import psidev.psi.mi.jami.utils.AliasUtils;
import psidev.psi.mi.jami.utils.XrefUtils;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;

import javax.annotation.Resource;

/**
 * InteractorEnricher Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */

public class BioactiveEntityEnricherTest extends EnricherBasicTestCase {

    @Resource(name = "intactBioactiveEntityEnricher")
    private BioActiveEntityEnricher enricher;

    @Autowired
    private EnricherConfig config;

    @Test
    public void enrich_chebi_enabled() throws EnricherException {
        BioactiveEntity smallMolecule = new DefaultBioactiveEntity( "unknownShortName", XrefUtils.createChebiIdentity("CHEBI:15367"));
        Assert.assertEquals(0,smallMolecule.getAnnotations().size());
        Assert.assertEquals(0,smallMolecule.getChecksums().size());

        enricher.enrich( smallMolecule );

        Assert.assertEquals("alltransretinoic acid",smallMolecule.getShortName());
        Assert.assertNotNull(smallMolecule.getStandardInchi());
    }

    @Test
    public void enrich_chebi_imatinib() throws EnricherException {
        BioactiveEntity smallMolecule = new DefaultBioactiveEntity( "unknownShortName", XrefUtils.createChebiIdentity("CHEBI:45783"));
        Assert.assertEquals(0,smallMolecule.getAnnotations().size());
        Assert.assertEquals(0,smallMolecule.getChecksums().size());

        enricher.enrich( smallMolecule );

        Assert.assertEquals("imatinib",smallMolecule.getShortName());
        Assert.assertEquals( 3, smallMolecule.getChecksums().size() );
        Assert.assertEquals("CN1CCN(Cc2ccc(cc2)C(=O)Nc2ccc(C)c(Nc3nccc(n3)-c3cccnc3)c2)CC1", smallMolecule.getSmile());
        Assert.assertEquals("KTUFNOKKBVMGRW-UHFFFAOYSA-N", smallMolecule.getStandardInchiKey());
        Assert.assertEquals("InChI=1S/C29H31N7O/c1-21-5-10-25(18-27(21)34-29-31-13-11-26(33-29)24-4-3-12-30-19-24)32-28(37)23-8-6-22(7-9-23)20-36-16-14-35(2)15-17-36/h3-13,18-19H,14-17,20H2,1-2H3,(H,32,37)(H,31,33,34)", smallMolecule.getStandardInchi());
        Assert.assertFalse(AliasUtils.collectAllAliasesHavingTypeAndName(smallMolecule.getAliases(),
                null, "iupac name", "4-[(4-methylpiperazin-1-yl)methyl]-N-{4-methyl-3-" +
                        "[(4-pyridin-3-ylpyrimidin-2-yl)amino]phenyl}benzamide"
        ).isEmpty());

        // imatinib (from INN) is not stored as it is already the shortlabel
        // assertHasAlias( smallMolecule, null, "imatinib" );
    }

    @Test
    public void enrich_chebi_dopamin() throws EnricherException {
        BioactiveEntity smallMolecule = new DefaultBioactiveEntity( "unknownShortName", XrefUtils.createChebiIdentity("CHEBI:18243"));
        Assert.assertEquals( 0, smallMolecule.getAnnotations().size() );
        Assert.assertEquals( 0, smallMolecule.getAliases().size() );

        enricher.enrich( smallMolecule );

        Assert.assertEquals( 16, smallMolecule.getAliases().size() );

        Assert.assertEquals("dopamine", smallMolecule.getShortName());
        Assert.assertFalse(AliasUtils.collectAllAliasesHavingTypeAndName(smallMolecule.getAliases(),
                null, "iupac name", "4-(2-aminoethyl)benzene-1,2-diol"
        ).isEmpty());
        Assert.assertFalse(AliasUtils.collectAllAliasesHavingTypeAndName(smallMolecule.getAliases(),
                null, "synonym", "dopamina"
        ).isEmpty());
        Assert.assertFalse(AliasUtils.collectAllAliasesHavingTypeAndName(smallMolecule.getAliases(),
                null, "synonym", "dopaminum"
        ).isEmpty());
    }

    @Test
    public void enrich_chebi_updateXrefs() throws EnricherException {
        // created a small molecule of which the identifier is a secondary id in ChEBI, we expect the validator to
        // update this so that the currently primary id becomes identity and the others secondary-ac.

        Assert.assertNotNull(config);
        Assert.assertTrue( config.isUpdateSmallMolecules() );

        BioactiveEntity smallMolecule = new DefaultBioactiveEntity( "imatinib", XrefUtils.createChebiIdentity("CHEBI:45781"));
        Assert.assertEquals(1,smallMolecule.getIdentifiers().size());

        enricher.enrich( smallMolecule );

        Assert.assertEquals(4, smallMolecule.getIdentifiers().size());
        Assert.assertFalse(XrefUtils.collectAllXrefsHavingDatabaseQualifierAndId(smallMolecule.getIdentifiers(),
                Xref.CHEBI_MI, Xref.CHEBI, "CHEBI:45783", Xref.IDENTITY_MI, Xref.IDENTITY
        ).isEmpty());
        Assert.assertFalse(XrefUtils.collectAllXrefsHavingDatabaseQualifierAndId(smallMolecule.getIdentifiers(),
                Xref.CHEBI_MI, Xref.CHEBI, "CHEBI:45781", Xref.SECONDARY_MI, Xref.SECONDARY
        ).isEmpty());
        Assert.assertFalse(XrefUtils.collectAllXrefsHavingDatabaseQualifierAndId(smallMolecule.getIdentifiers(),
                Xref.CHEBI_MI, Xref.CHEBI, "CHEBI:305376", Xref.SECONDARY_MI, Xref.SECONDARY
        ).isEmpty());
        Assert.assertFalse(XrefUtils.collectAllXrefsHavingDatabaseQualifierAndId(smallMolecule.getIdentifiers(),
                Xref.CHEBI_MI, Xref.CHEBI, "CHEBI:38918", Xref.SECONDARY_MI, Xref.SECONDARY
        ).isEmpty());
    }


}
