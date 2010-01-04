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
import uk.ac.ebi.chebi.webapps.chebiWS.client.ChebiWebServiceClient;
import uk.ac.ebi.chebi.webapps.chebiWS.model.OntologyDataItem;
import uk.ac.ebi.chebi.webapps.chebiWS.model.OntologyDataItemList;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherBasicTestCase;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.ProteinUtils;

import java.util.List;

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
    }

    private void assertHasAnnotation( SmallMolecule smallMolecule, String topic, String text ) {
        for ( Annotation annotation : smallMolecule.getAnnotations() ) {
            if( annotation.getCvTopic().getShortLabel().equals( topic ) && annotation.getAnnotationText().equals( text )){
                return;
            }
        }
        Assert.fail( "Could not find annotation( '"+topic+"', '"+ text +"' )" );
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