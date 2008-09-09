/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.uniprotExport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.config.CvPrimer;
import uk.ac.ebi.intact.config.impl.EssentialCvPrimer;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactUnit;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.util.SchemaUtils;
import uk.ac.ebi.intact.model.*;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24-Aug-2006</pre>
 */
public class CcLineExportDbTest extends UniprotExportTestCase {

    private static final Log log = LogFactory.getLog(CcLineExportDbTest.class);

    @Test
    public void generateCCLines() throws Exception {
        Collection<String> uniprotIds =
                CCLineExport.getEligibleProteinsFromFile(CcLineExportDbTest.class.getResource("uniprotlinks.dat").getFile());

        // build data
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

        StringWriter ccWriter = new StringWriter();
        Writer goaWriter = new StringWriter();

        LineExportConfig config = new LineExportConfig();
        config.setIgnoreUniprotDrExportAnnotation(true);

        CCLineExport ccLineExport = new CCLineExport(ccWriter, goaWriter, config, System.out);
        Assert.assertEquals(0, ccLineExport.getCcLineCount());
        Assert.assertEquals(0, ccLineExport.getGoaLineCount());

        //new CcLineExportProgressThread(ccLineExport, uniprotIds.size()).start();

        ccLineExport.generateCCLines(uniprotIds);

        Assert.assertEquals(3, ccLineExport.getCcLineCount());
        Assert.assertEquals(4, ccLineExport.getGoaLineCount());

        System.out.println(ccWriter.toString());
    }

    @Test
    public void generateCCLines_splicevariant() throws Exception {
        Collection<String> uniprotIds =
                CCLineExport.getEligibleProteinsFromFile(CcLineExportDbTest.class.getResource("uniprotlinks.dat").getFile());

        uniprotIds.add( "P14712-1" );

        // build data
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

        final Protein p14713 = getMockBuilder().createProtein( "P14713", "P14713_HUMAN", human );
        PersisterHelper.saveOrUpdate( p14713 );

        final Protein p14713_1 = getMockBuilder().createProtein( "P14712-1", "SV1", mouse );
        p14713_1.addXref( new InteractorXref( getMockBuilder().getInstitution(),
                                              getDaoFactory().getCvObjectDao(CvDatabase.class).getByPsiMiRef(CvDatabase.INTACT_MI_REF),
                                              p14713.getAc(),
                                              getDaoFactory().getCvObjectDao(CvXrefQualifier.class).getByPsiMiRef( CvXrefQualifier.ISOFORM_PARENT_MI_REF )) );

        final Protein p12345 = getMockBuilder().createProtein( "P12345", "P12345_HUMAN", human );

        final Interaction interaction1 = getMockBuilder().createInteraction( q9swi1, p14713_1 );
        final Experiment exp = getMockBuilder().createDeterministicExperiment();
        final CvTopic uniprotDrExport = getMockBuilder().createCvObject( CvTopic.class, null, CvTopic.UNIPROT_DR_EXPORT );
        final Annotation annotation = new Annotation( getMockBuilder().getInstitution(), uniprotDrExport, "yes" );
        exp.addAnnotation( annotation );
        interaction1.addExperiment( exp );

        PersisterHelper.saveOrUpdate( p14712, q9swi1, p14713_1, p12345 );

        StringWriter ccWriter = new StringWriter();
        Writer goaWriter = new StringWriter();

        LineExportConfig config = new LineExportConfig();
        config.setIgnoreUniprotDrExportAnnotation(true);

        CCLineExport ccLineExport = new CCLineExport(ccWriter, goaWriter, config, System.out);
        Assert.assertEquals(0, ccLineExport.getCcLineCount());
        Assert.assertEquals(0, ccLineExport.getGoaLineCount());

        ccLineExport.generateCCLines(uniprotIds);

        System.out.println( "CC Lines exported:" );
        System.out.println( "-----------------" );
        System.out.println(ccWriter.toString());
        System.out.println( "-----------------" );

        Assert.assertEquals(2, ccLineExport.getCcLineCount());
        Assert.assertEquals(2, ccLineExport.getGoaLineCount());
    }
}
