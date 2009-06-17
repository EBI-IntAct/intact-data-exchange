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
package uk.ac.ebi.intact.dataexchange.pdbe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.TransactionStatus;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.model.*;
import psidev.psi.mi.xml.model.Xref;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.persister.stats.PersisterStatistics;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.enricher.PsiEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.enricher.PsiEnricherException;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.core.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Imports wwPDB data into intact respecting the data already present. That is, we only add data that is not already
 * there.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
public class ImportPdbXmlFiles {

    private static final Log log = LogFactory.getLog( ImportPdbXmlFiles.class );

    private static final String MSD_DATASET_NAME = "MSD - Data obtained from The Macromolecular Structure Database";
    private static final boolean REUSE_ENRICHED_FILE = false;

    public static void main( String[] args ) throws Exception {

        // Setup database access
        File dbConfigFile = new File( ImportPdbXmlFiles.class.getResource( "/zdev-hibernate.cfg.xml" ).getFile() );
        IntactContext.initStandaloneContext( dbConfigFile );

        // Initialise institution
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        TransactionStatus transactionStatus = dataContext.beginTransaction();
        DaoFactory daoFactory = dataContext.getDaoFactory();
        final int institutionCount = daoFactory.getInstitutionDao().countAll();
        log.info( "Found " + institutionCount + " institutions in this IntAct node:" );
        for ( Institution institution : daoFactory.getInstitutionDao().getAll() ) {
            log.info( " - " + institution.getShortLabel() + " (" + institution.getAc() + ")" );
        }
        dataContext.commitTransaction(transactionStatus);


        // Prepare files to be imported
//        final String importDirectory = "C:\\pdb-export\\msddata_06-april-2009\\small";
        final String importDirectory = "C:\\pdb-export\\msddata5.2\\msddata5";
//        final String importDirectory = "C:\\pdb-export\\msddata_06-april-2009\\msddata4";
        final int maxFilesToImport = -1;

        File dir = new File( importDirectory );
        final String[] xmlFilenames = dir.list( new FilenameFilter() {
            public boolean accept( File dir, String name ) {
                return name.endsWith( ".xml" );
            }
        } );

        log.info( "Found " + xmlFilenames.length + " files in " + importDirectory );

        int totalExperimentCount = 0;
        int totalExperimentDeleted = 0;

        int totalInteractionCount = 0;
        int totalInteractionDeleted = 0;

        int totalProteinCount = 0;

        int totalSmallMoleculeCount = 0;

        for ( int i = 0; i < xmlFilenames.length; i++ ) {

            if ( maxFilesToImport != -1 && i >= maxFilesToImport ) {
                log.warn( "Abort import after " + ( i + 1 ) + " file based on user imput." );
                break;
            }

            String xmlFilename = xmlFilenames[i];
            File xmlFile = new File( dir, xmlFilename );
            log.info( "Importing PSI-MI XML file: " + xmlFile.getAbsolutePath() );

            // TODO: interactions don't have a interaction type.
            boolean autofixed = false;
            PsimiXmlReader xmlReader = new PsimiXmlReader();
            final EntrySet entrySet = xmlReader.read( xmlFile );
            for ( Entry entry : entrySet.getEntries() ) {
                for ( psidev.psi.mi.xml.model.Interaction interaction : entry.getInteractions() ) {
                    if ( interaction.getInteractionTypes().isEmpty() ) {
                        final InteractionType type = buildDirectInteraction();
                        interaction.getInteractionTypes().add( type );
                        log.warn( "AUTOFIX - Added interaction type 'direct interaction'" );
                        autofixed = true;
                    }
                }
            }

            if ( autofixed ) {
                PsimiXmlWriter writer = new PsimiXmlWriter();
                final String truncatedName = xmlFile.getName().substring( 0, xmlFile.getName().length() - 4 );
                final String fixedFileName = truncatedName + ".fixed.xml";
                File tempFile = new File( xmlFile.getParentFile(), fixedFileName );
                writer.write( entrySet, tempFile );

                // make sure we use this one.
                xmlFile = tempFile;
            }

            xmlFile = enrich( xmlFile );

            TransactionStatus transactionStatus2 = null;

            try {
                transactionStatus2 = dataContext.beginTransaction();

                // Check if that PMID was already annotated in IntAct and skip/log if so.
                // check that we have a single PMID for that Entry

                boolean pmidAlreadyInIntact = false;
                boolean allExperimentWerePartOfMsdDataset = false;

                for ( Entry entry : entrySet.getEntries() ) {
                    for ( ExperimentDescription experiment : entry.getExperiments() ) {

                        if ( experiment.getBibref().getXref() != null ) {
                            final Xref xref = experiment.getBibref().getXref();
                            final DbReference ref = findFirstDbReference( xref,
                                                                          CvDatabase.PUBMED_MI_REF,
                                                                          CvXrefQualifier.PRIMARY_REFERENCE_MI_REF );
                            if ( ref != null ) {
                                // found it, now check in IntAct
                                final String pmid = ref.getId();
                                daoFactory = dataContext.getDaoFactory();
                                final List<Experiment> experiments = daoFactory.getExperimentDao().getByPubId( pmid );
                                if ( !experiments.isEmpty() ) {
                                    log.info( "IntAct has already " + experiments.size() + " experiment(s) for PMID: " + pmid );
                                    pmidAlreadyInIntact = true;

                                    // now if that's the case, let's check if it was part of the old MSD import
                                    // and in case it is, let's delete the whole publication and reimport it.
                                    log.info( "Now checking if all of them are from the MSD dataset ..." );
                                    for ( Iterator<Experiment> iterator = experiments.iterator(); iterator.hasNext(); ) {
                                        Experiment e = iterator.next();

                                        if ( hasMsdDatasetFlag( e ) ) {
                                            log.info( e.getAc() + " is from dataset(MSD)" );
                                            // at the end of that loop we only want to have left experiments that are
                                            // not part of the MSD dataset
                                            iterator.remove();

                                            // then it should be deleted.
                                            log.warn( "Experiment " + e.getShortLabel() + " (pmid:" + pmid + ") is going to be deleted prior to re-import as part of the wwPDB dataset" );
                                            for ( uk.ac.ebi.intact.model.Interaction interaction : e.getInteractions() ) {
                                                log.warn( "Deteting Interaction '" + interaction.getShortLabel() + "' - " + interaction.getAc() );
                                                daoFactory.getInteractionDao().delete( ( InteractionImpl ) interaction );
                                                totalInteractionDeleted++;
                                            }

                                            log.warn( "Deteting experiment '" + e.getShortLabel() + "' - " + e.getAc() );
                                            daoFactory.getExperimentDao().delete( e );
                                            totalExperimentDeleted++;

                                            // commit and re-open transaction...
                                            dataContext.commitTransaction(transactionStatus2);
                                            transactionStatus2 = dataContext.beginTransaction();

                                        } else {
                                            log.info( e.getAc() + " is NOT from dataset(MSD)" );
                                        }
                                    } // for

                                    if ( experiments.isEmpty() ) {
                                        log.info( "All experiment found were from the MSD dataset, we are going to import" );
                                        allExperimentWerePartOfMsdDataset = true;
                                    }
                                }
                            } else {
                                log.error( "Unexpected experiment (" + experiment.getId() + ") without pubmed primary-reference." );
                            }
                        } else {
                            log.error( "Unexpected experiment (" + experiment.getId() + ") without bibRef" );
                        }

                    } // xml experiment
                } // entry set


                if ( !pmidAlreadyInIntact || ( pmidAlreadyInIntact && allExperimentWerePartOfMsdDataset ) ) {

                    log.info( "After careful checking, about to import data file..." );

                    // new FileInputStream( xmlFile )
                    PsiExchange psiExchange = (PsiExchange) IntactContext.getCurrentInstance().getSpringContext()
                            .getBean("psiExchange");
                    final PersisterStatistics stats = psiExchange.importIntoIntact( entrySet );

                    final int experimentCount = stats.getPersistedCount( Experiment.class, false );
                    final int interactionCount = stats.getPersistedCount( InteractionImpl.class, false );
                    final int proteinCount = stats.getPersistedCount( ProteinImpl.class, false );
                    final int smallMoleculeCount = stats.getPersistedCount( SmallMoleculeImpl.class, false );

                    totalExperimentCount += experimentCount;
                    totalInteractionCount += interactionCount;
                    totalProteinCount += proteinCount;
                    totalSmallMoleculeCount += smallMoleculeCount;

                    if ( log.isDebugEnabled() ) {

                        final int fileCount = i + 1;
                        final float percentComplete = ( ( float ) fileCount / xmlFilenames.length ) * 100;
                        final DecimalFormat formatter = new DecimalFormat( "#.##" );

                        log.info( "Completed import (" + fileCount + "/" + xmlFilenames.length + " - " +
                                  formatter.format( percentComplete ) + "%) of " + xmlFile.getAbsolutePath() );

                        log.info( "E:" + experimentCount +
                                  " I:" + interactionCount +
                                  " P:" + proteinCount +
                                  " SM:" + smallMoleculeCount );
                    }

                } // if PMID not yet in IntAct
                else {
                    log.info( "Cancelling import of " + xmlFile.getAbsolutePath() + " as it is already in IntAct and not part of the dataset MSD" );
                }

            } catch ( Exception e ) {
                log.error( "An error occur while importing: " + xmlFilename, e );
                if ( !transactionStatus2.isCompleted() ) {
                    dataContext.rollbackTransaction(transactionStatus2);
                }
            } finally {
                if ( !transactionStatus2.isCompleted() ) {
                    dataContext.commitTransaction(transactionStatus2);
                }
            }

            log.info( "------------------------------------------------------------------------------------" );

        } // for

        // Final stats
        log.info( "Completed import of directory " + importDirectory );

        log.info( "Statistics" );
        log.info( "----------" );
        log.info( "Imported experiments:   " + totalExperimentCount );
        log.info( "Deleted experiments:    " + totalExperimentDeleted );
        log.info( "Imported interactions:  " + totalInteractionCount );
        log.info( "Deleted interactions:   " + totalInteractionDeleted );
        log.info( "Imported protein:       " + totalProteinCount );
        log.info( "Imported small molecule:" + totalSmallMoleculeCount );
    }

    public static File enrich( File fileToImport ) throws IOException {

        final String truncatedName = fileToImport.getName().substring( 0, fileToImport.getName().length() - 4 );
        final String enrichedFileName = truncatedName + ".enriched.xml";
        File tempFile = new File( fileToImport.getParentFile(), enrichedFileName );

        if ( REUSE_ENRICHED_FILE && tempFile.exists() ) {
            log.info( "Enriched file already exist, reuse it." );
            return tempFile;
        }

        // enrich that file
        EnricherConfig enricherConfig = new EnricherConfig();
        enricherConfig.setUpdateInteractionShortLabels( true );
        enricherConfig.setUpdateExperiments( true );
        enricherConfig.setUpdateProteins( true );

        InputStream is = new FileInputStream( fileToImport );
        Writer writer = new FileWriter( tempFile );

        log.info( fileToImport.getAbsolutePath() + " was enriched into " + tempFile.getAbsolutePath() );

        try {
            PsiEnricher psiEnricher = (PsiEnricher) IntactContext.getCurrentInstance().getSpringContext()
                            .getBean("psiEnricher");
            psiEnricher.enrichPsiXml( is, writer, enricherConfig );
        } catch ( PsiEnricherException e ) {
            throw new RuntimeException( "Error while enriching: " + fileToImport.getAbsolutePath(), e );
        }
        
        return tempFile;
    }

    private static boolean hasMsdDatasetFlag( Experiment experiment ) {

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        final CvObjectDao<CvTopic> topicDao = daoFactory.getCvObjectDao( CvTopic.class );
        final CvTopic dataset = topicDao.getByPsiMiRef( CvTopic.DATASET_MI_REF );
        if ( dataset == null ) {
            throw new RuntimeException( "CvTopic( dataset ) was missing from the database" );
        }
        final Collection<Annotation> topics =
                AnnotatedObjectUtils.findAnnotationsByCvTopic( experiment, Arrays.asList( dataset ) );

        for ( Annotation annotation : topics ) {
            if ( MSD_DATASET_NAME.equals( annotation.getAnnotationText() ) ) {
                return true;
            }
        }

        return false;
    }

    private static DbReference findFirstDbReference( Xref xref, String dbAc, String qualifierAc ) {

        for ( DbReference ref : xref.getAllDbReferences() ) {

            if ( dbAc != null && !dbAc.equals( ref.getDbAc() ) ) {
                continue;
            }

            if ( qualifierAc != null && !qualifierAc.equals( ref.getRefTypeAc() ) ) {
                continue;
            }

            return ref;
        }

        return null;
    }

    private static InteractionType buildDirectInteraction() {
        InteractionType type = new InteractionType();
        type.setNames( new Names() );
        type.getNames().setShortLabel( "direct interaction" );
        type.setXref( new Xref() );
        type.getXref().setPrimaryRef( new DbReference( "psi-mi", "MI:0488", "MI:0407", "identity", "MI:0356" ) );
        return type;
    }
}
