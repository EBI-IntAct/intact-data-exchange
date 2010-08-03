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
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.ProteinUtils;

/**
 * InteractorEnricher Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */

public class InteractorEnricherTest extends EnricherBasicTestCase {

    @Autowired
    private InteractorEnricher enricher;

    @Autowired
    private EnricherConfig config;

    @Test
    public void enrich_uniprot_enabled() {
        BioSource human = getMockBuilder().createBioSource(9606, "human");
        Protein protein = getMockBuilder().createProtein("P18850", "unknownName", human);

        enricher.enrich(protein);

        Assert.assertEquals("atf6a_human", protein.getShortLabel());
        Assert.assertEquals("Cyclic AMP-dependent transcription factor ATF-6 alpha", protein.getFullName());
        Assert.assertNotNull(protein.getSequence());
    }

    @Test
    public void enrich_uniprot_disabled() {
        Assert.assertNotNull( config );
        config.setUpdateProteins( false );

        BioSource human = getMockBuilder().createBioSource(9606, "human");
        Protein protein = getMockBuilder().createProtein("P18850", "unknownName", human);
        final String seq = protein.getSequence();

        enricher.enrich(protein);

        Assert.assertEquals( "unknownName", protein.getShortLabel() );
        Assert.assertNull( protein.getFullName() );
        Assert.assertEquals( seq, protein.getSequence() );

        config.setUpdateProteins( true );
    }

    @Test
    public void enrich_chebi_enabled(){
        final SmallMolecule smallMolecule = getMockBuilder().createSmallMolecule( "CHEBI:15367", "unknownShortName" );
        Assert.assertEquals(0,smallMolecule.getAnnotations().size());

        enricher.enrich( smallMolecule );

        Assert.assertEquals("all-trans-retinoic acid",smallMolecule.getShortLabel());
        Assert.assertEquals("inchi id",smallMolecule.getAnnotations().iterator().next().getCvTopic().getShortLabel());
    }

    @Test
    public void enrich_chebi_disabled(){
        Assert.assertNotNull( config );
        config.setUpdateSmallMolecules( false );

        final SmallMolecule smallMolecule = getMockBuilder().createSmallMolecule( "CHEBI:15367", "unknownShortName" );
        Assert.assertEquals(0,smallMolecule.getAnnotations().size());

        enricher.enrich( smallMolecule );

        Assert.assertEquals("unknownShortName",smallMolecule.getShortLabel());
        Assert.assertEquals( 0, smallMolecule.getAnnotations().size() );

        config.setUpdateSmallMolecules( true );
    }

    @Test
    public void enrich_chebi_imatinib(){
        final SmallMolecule smallMolecule = getMockBuilder().createSmallMolecule( "CHEBI:45783", "unknownShortName" );
        Assert.assertEquals(0,smallMolecule.getAnnotations().size());

        enricher.enrich( smallMolecule );

        Assert.assertEquals("imatinib",smallMolecule.getShortLabel());
        Assert.assertEquals( 3, smallMolecule.getAnnotations().size() );
        assertHasAnnotation( smallMolecule, "function", "tyrosine kinase inhibitor" );
        assertHasAnnotation( smallMolecule, "function", "antineoplastic drug" );
        assertHasAnnotation( smallMolecule, "inchi id", "InChI=1/C29H31N7O/c1-21-5-10-25(18-27(21)34-29-31-13-11-26(33-" +
                                                        "29)24-4-3-12-30-19-24)32-28(37)23-8-6-22(7-9-23)20-36-16-14-35" +
                                                        "(2)15-17-36/h3-13,18-19H,14-17,20H2,1-2H3,(H,32,37)(H,31,33,34" +
                                                        ")/f/h32,34H" );
        assertHasAlias( smallMolecule, "iupac name", "4-[(4-methylpiperazin-1-yl)methyl]-N-{4-methyl-3-" +
                                                     "[(4-pyridin-3-ylpyrimidin-2-yl)amino]phenyl}benzamide" );

        // imatinib (from INN) is not stored as it is already the shortlabel
        // assertHasAlias( smallMolecule, null, "imatinib" );
    }

    @Test
    public void enrich_chebi_dopamin(){
        final SmallMolecule smallMolecule = getMockBuilder().createSmallMolecule( "CHEBI:18243", "unknownShortName" );
        Assert.assertEquals( 0, smallMolecule.getAnnotations().size() );
        Assert.assertEquals( 1, smallMolecule.getAliases().size() );

        enricher.enrich( smallMolecule );

        Assert.assertEquals( 4, smallMolecule.getAliases().size() );

        Assert.assertEquals("dopamine",smallMolecule.getShortLabel());
        assertHasAlias( smallMolecule, "iupac name", "4-(2-aminoethyl)benzene-1,2-diol" );
        // dopamine (from INN) is not stored as it is already the shortlabel
        assertHasAlias( smallMolecule, null, "dopamina" );
        assertHasAlias( smallMolecule, null, "dopaminum" );
    }

    @Test
    public void enrich_chebi_updateXrefs(){
        // created a small molecule of which the identifier is a secondary id in ChEBI, we expect the validator to
        // update this so that the currently primary id becomes identity and the others secondary-ac.

        Assert.assertNotNull( config );
        Assert.assertTrue( config.isUpdateSmallMolecules() );
        Assert.assertTrue( config.isUpdateSmallMoleculeChebiXrefs() );

        final SmallMolecule smallMolecule = getMockBuilder().createSmallMolecule( "CHEBI:11930", "imatinib" );
        Assert.assertEquals(1,smallMolecule.getXrefs().size());

        enricher.enrich( smallMolecule );

        Assert.assertEquals( 10, smallMolecule.getXrefs().size() );
        assertHasXref( smallMolecule, "chebi", "CHEBI:18243",  "identity" );
        assertHasXref( smallMolecule, "chebi", "CHEBI:11930",  "secondary-ac" );
        assertHasXref( smallMolecule, "chebi", "CHEBI:23886",  "secondary-ac" );
        assertHasXref( smallMolecule, "chebi", "CHEBI:43686",  "secondary-ac" );
        assertHasXref( smallMolecule, "chebi", "CHEBI:104584", "secondary-ac" );
        assertHasXref( smallMolecule, "chebi", "CHEBI:14203",  "secondary-ac" );
        assertHasXref( smallMolecule, "chebi", "CHEBI:11695",  "secondary-ac" );
        assertHasXref( smallMolecule, "chebi", "CHEBI:1764",   "secondary-ac" );
    }

    @Test
    public void enrich_chebi_xrefs_disabled(){
        Assert.assertNotNull( config );
        config.setUpdateSmallMoleculeChebiXrefs( false );
        
        final SmallMolecule smallMolecule = getMockBuilder().createSmallMolecule( "CHEBI:11930", "imatinib" );
        Assert.assertEquals(1,smallMolecule.getXrefs().size());

        enricher.enrich( smallMolecule );

        Assert.assertEquals( 1, smallMolecule.getXrefs().size() );

        config.setUpdateSmallMoleculeChebiXrefs( true );
    }

    private void assertHasAnnotation( SmallMolecule smallMolecule, String topic, String text ) {
        for ( Annotation annotation : smallMolecule.getAnnotations() ) {
            if( annotation.getCvTopic().getShortLabel().equals( topic ) && annotation.getAnnotationText().equals( text )){
                return;
            }
        }
        Assert.fail( "Could not find annotation( '"+topic+"', '"+ text +"' )" );
    }

    private void assertHasAlias( SmallMolecule smallMolecule, String type, String text ) {
        for ( Alias alias : smallMolecule.getAliases() ) {
            if( alias.getCvAliasType() != null ) {
                if( alias.getCvAliasType().getShortLabel().equals( type ) && alias.getName().equals( text ) ){
                    return;
                }
            } else {
                if( alias.getName().equals( text ) ){
                    return;
                }
            }
        }
        Assert.fail( "Could not find alias( '"+type+"', '"+ text +"' )" );
    }

    private void assertHasXref( SmallMolecule smallMolecule, String db, String id, String qualifier ) {
        for ( InteractorXref xref : smallMolecule.getXrefs() ) {
            if( xref.getCvDatabase().getShortLabel().equals( db ) &&
                    xref.getCvXrefQualifier().getShortLabel().equals( qualifier )
                    && xref.getPrimaryId().equals( id )) {
                return;
            }
        }

        Assert.fail( "Could not find Xref('"+db+"', '"+id+"', '"+qualifier+"')" );
    }

    @Test
    public void enrich_uniprot_noXrefs() {
        BioSource human = getMockBuilder().createBioSource(9606, "human");
        Protein protein = getMockBuilder().createProtein("P18850", "atf6a_human", human);
        protein.getXrefs().clear();

        enricher.enrich(protein);

        Assert.assertEquals("atf6a_human", protein.getShortLabel());
        Assert.assertEquals("Cyclic AMP-dependent transcription factor ATF-6 alpha", protein.getFullName());

        Assert.assertFalse(protein.getXrefs().isEmpty());
    }

    @Test
    public void enrich_alias() {
        BioSource ecoli = getMockBuilder().createBioSource(561, "ecoli");
        Protein protein = getMockBuilder().createProtein("P45530", "tusb_ecoli", ecoli);
        protein.getAliases().clear();

        Assert.assertNotSame("tusB", ProteinUtils.getGeneName(protein));

        enricher.enrich(protein);

        Assert.assertEquals("tusB", ProteinUtils.getGeneName(protein));
    }

    @Test
    public void enrich_invalidLabel() {
        BioSource human = getMockBuilder().createBioSource(9606, "human");
        Protein protein = getMockBuilder().createProtein("EBI-12345", "EBI-12345", human);
        protein.getXrefs().iterator().next().setCvDatabase(
                getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.INTACT_MI_REF, CvDatabase.INTACT));

        enricher.enrich(protein);

        Assert.assertEquals("EBI12345", protein.getShortLabel());
    }

    @Test
    public void enrich_uniprot_biosource() {
        BioSource lalaOrganism = getMockBuilder().createBioSource(50, "lala");
        Protein protein = getMockBuilder().createProtein("P18850", "unknownName", lalaOrganism);

        enricher.enrich(protein);

        Assert.assertEquals("atf6a_human", protein.getShortLabel());
        Assert.assertEquals("Cyclic AMP-dependent transcription factor ATF-6 alpha", protein.getFullName());
        Assert.assertNotNull(protein.getSequence());
        Assert.assertEquals("9606", protein.getBioSource().getTaxId());
        Assert.assertEquals("human", protein.getBioSource().getShortLabel());
    }
}
