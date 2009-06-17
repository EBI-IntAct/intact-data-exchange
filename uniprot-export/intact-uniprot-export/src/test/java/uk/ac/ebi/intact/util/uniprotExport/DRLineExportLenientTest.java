/*
 * Copyright (c) 2008 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
 
package uk.ac.ebi.intact.util.uniprotExport;

import org.junit.Test;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.model.*;

import java.util.List;
import java.util.Set;
import java.sql.SQLException;

import junit.framework.Assert;

/**
 * DRLineExportLenient Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class DRLineExportLenientTest extends UniprotExportTestCase {

    @Test
    public void getEligibleProteins_only_uniprot() throws Exception {

        // build data:
        //             4 uniprot proteins
        //             2 interactions: involving only 3 distinct uniprot proteins

        final BioSource human = getMockBuilder().createBioSource( 9606, "human" );
        final BioSource mouse = getMockBuilder().createBioSource( 10032, "mouse" );
        mouse.setFullName( "Mus musculus" );
        final Protein q9swi1 = getMockBuilder().createProtein( "Q9SWI1", "Q9SWI1_HUMAN", human );
        q9swi1.getAliases().clear();
        q9swi1.addAlias( new InteractorAlias( getMockBuilder().getInstitution(), q9swi1,
                                              getMockBuilder().createCvObject( CvAliasType.class,
                                                                               CvAliasType.GENE_NAME_MI_REF,
                                                                               CvAliasType.GENE_NAME ),
                                              "foo"));
        final Protein p14712 = getMockBuilder().createProtein( "P14712", "P14712_HUMAN", mouse );
        final Protein p14713 = getMockBuilder().createProtein( "P14713", "P14712_HUMAN", human );
        p14713.getAliases().clear();
        p14713.addAlias( new InteractorAlias( getMockBuilder().getInstitution(), p14713,
                                              getMockBuilder().createCvObject( CvAliasType.class,
                                                                               CvAliasType.ORF_NAME_MI_REF,
                                                                               CvAliasType.ORF_NAME ),
                                              "bar"));
        final Protein p12345 = getMockBuilder().createProtein( "P12345", "P12345_HUMAN", human );


        final Interaction interaction1 = getMockBuilder().createInteraction( q9swi1, p14713 );
        final Interaction interaction2 = getMockBuilder().createInteraction( q9swi1, p14712 );
        final Experiment exp = getMockBuilder().createDeterministicExperiment();
        final CvTopic uniprotDrExport = getMockBuilder().createCvObject( CvTopic.class, null, CvTopic.UNIPROT_DR_EXPORT );
        final Annotation annotation = new Annotation( getMockBuilder().getInstitution(), uniprotDrExport, "yes" );
        exp.addAnnotation( annotation );
        interaction1.addExperiment( exp );
        interaction2.addExperiment( exp );

        PersisterHelper.saveOrUpdate( q9swi1, p14712, p14713, p12345 );
        DRLineExportLenient exporter = new DRLineExportLenient( );

        final List<ProteinImpl> allProteins = getDaoFactory().getProteinDao().getAll();
        final Set<String> identifiers = exporter.getEligibleProteins( allProteins );

        Assert.assertNotNull( identifiers );
        Assert.assertEquals( 3, identifiers.size() );
        Assert.assertTrue( identifiers.contains( "Q9SWI1" ));
        Assert.assertTrue( identifiers.contains( "P14712" ));
        Assert.assertTrue( identifiers.contains( "P14713" ));
    }

    @Test
    public void getEligibleProteins_uniprot_and_non_uniprot() throws Exception {

        // build data:
        //             2 proteins (1 uniprot, 1 non uniprot),
        //             1 interactions involving a uniprot and a non uniprot protein
        
        final BioSource human = getMockBuilder().createBioSource( 9606, "human" );
        final Protein q9swi1 = getMockBuilder().createProtein( "Q9SWI1", "Q9SWI1_HUMAN", human );

        final CvTopic noUniprot = getDaoFactory().getCvObjectDao(CvTopic.class).getByShortLabel( CvTopic.NON_UNIPROT );
        Assert.assertNotNull( noUniprot );
        final Protein q98765 = getMockBuilder().createProtein( "XYZ", "unknown", human );
        q98765.addAnnotation( new Annotation( getMockBuilder().getInstitution(), noUniprot, "blablabla" ) );

        final Interaction interaction = getMockBuilder().createInteraction( q9swi1, q98765 );
        final Experiment exp = getMockBuilder().createDeterministicExperiment();
        final CvTopic uniprotDrExport = getMockBuilder().createCvObject( CvTopic.class, null, CvTopic.UNIPROT_DR_EXPORT );
        final Annotation annotation = new Annotation( getMockBuilder().getInstitution(), uniprotDrExport, "yes" );
        exp.addAnnotation( annotation );
        interaction.addExperiment( exp );

        PersisterHelper.saveOrUpdate( q9swi1, q98765 );
        DRLineExportLenient exporter = new DRLineExportLenient( );

        final List<ProteinImpl> allProteins = getDaoFactory().getProteinDao().getAll();
        final Set<String> identifiers = exporter.getEligibleProteins( allProteins );

        Assert.assertNotNull( identifiers );
        Assert.assertEquals( 0, identifiers.size() );
    }

    @Test
    public void getEligibleProteins_protein_and_splice_variant() throws Exception {
        // build data:
        //             1 proteins, and other protein with its splice variant
        //             1 interactions involving a uniprot protein and a splice variant
        final BioSource human = getMockBuilder().createBioSource( 9606, "human" );
        final Protein q9swi1 = getMockBuilder().createProtein( "Q9SWI1", "Q9SWI1_HUMAN", human );

        final Protein p12345 = getMockBuilder().createProtein( "P12345", "P12345_HUMAN", human );
        PersisterHelper.saveOrUpdate( p12345 );

        final Protein p12345_1 = getMockBuilder().createProteinSpliceVariant( p12345, "P12345-2", "P12345-2" );

        final Interaction interaction = getMockBuilder().createInteraction( q9swi1, p12345_1 );
        final Experiment exp = getMockBuilder().createDeterministicExperiment();
        final CvTopic uniprotDrExport = getMockBuilder().createCvObject( CvTopic.class, null, CvTopic.UNIPROT_DR_EXPORT );
        final Annotation annotation = new Annotation( getMockBuilder().getInstitution(), uniprotDrExport, "yes" );
        exp.addAnnotation( annotation );
        interaction.addExperiment( exp );

        PersisterHelper.saveOrUpdate( q9swi1, p12345, p12345_1 );
        DRLineExportLenient exporter = new DRLineExportLenient( );

        final List<ProteinImpl> allProteins = getDaoFactory().getProteinDao().getAll();
        final Set<String> identifiers = exporter.getEligibleProteins( allProteins );

        Assert.assertNotNull( identifiers );
        Assert.assertEquals( 2, identifiers.size() );
        Assert.assertTrue( identifiers.contains( "Q9SWI1" ));
        Assert.assertFalse( identifiers.contains( "P12345-2" )); // we should have remapped to the parent entry
        Assert.assertTrue( identifiers.contains( "P12345" )); 
    }

    @Test
    public void getEligibleProteins_uniprot_and_nucleicacid() throws Exception {

        // build data:
        //             1 proteins and 1 peptide
        //             1 interactions involving a uniprot protein and a peptide

        final BioSource human = getMockBuilder().createBioSource( 9606, "human" );
        final Protein q9swi1 = getMockBuilder().createProtein( "Q9SWI1", "Q9SWI1_HUMAN", human );

        final CvTopic noUniprot = getMockBuilder().createCvObject( CvTopic.class, null, CvTopic.NON_UNIPROT );
        final NucleicAcid nucleicacid = getMockBuilder().createNucleicAcidRandom();

        final Interaction interaction = getMockBuilder().createInteraction( q9swi1, nucleicacid );
        final Experiment exp = getMockBuilder().createDeterministicExperiment();
        final CvTopic uniprotDrExport = getMockBuilder().createCvObject( CvTopic.class, null, CvTopic.UNIPROT_DR_EXPORT );
        final Annotation annotation = new Annotation( getMockBuilder().getInstitution(), uniprotDrExport, "yes" );
        exp.addAnnotation( annotation );
        interaction.addExperiment( exp );

        PersisterHelper.saveOrUpdate( q9swi1, nucleicacid );
        DRLineExportLenient exporter = new DRLineExportLenient( );

        final List<ProteinImpl> allProteins = getDaoFactory().getProteinDao().getAll();
        final Set<String> identifiers = exporter.getEligibleProteins( allProteins );

        Assert.assertNotNull( identifiers );
        Assert.assertEquals( 0, identifiers.size() );
    }
}
