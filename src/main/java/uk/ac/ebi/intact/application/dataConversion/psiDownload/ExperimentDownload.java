// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.*;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Interaction2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Interaction2xmlI;
import uk.ac.ebi.intact.application.dataConversion.util.DisplayXML;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.persistence.dao.BaseDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.util.Chrono;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * That class is not meant to be used in production but rather as a playground for PSI file generation.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentDownload {

    public static final String OUTPUT_DIRECTORY = "";

    /**
     * @param root
     * @param file
     */
    public static void write( Element root, File file ) throws IOException {

        System.out.print( "\nWriting DOM to " + file.getAbsolutePath() + " ... " );
        System.out.flush();

        // prepare a file writer.
        Writer writer = new BufferedWriter( new FileWriter( file ) );

        // Write the content in the file (indented !!)
        DisplayXML.write( root, writer, "   " );

        writer.flush();

        // Close the file
        writer.close();

        System.out.println( "done." );
    }

    private static CvMapping cvMapping = null;

    public static void exportExperiment( Experiment experiment, PsiVersion version ) throws IOException, TransformerException {


        System.out.println( "Processing: " + experiment.getShortLabel() + ", has " +
                            experiment.getInteractions().size() + " interaction(s)." );
        System.out.println( version );

        UserSessionDownload session = new UserSessionDownload( version );
        session.filterObsoleteAnnotationTopic();



        boolean loadMapping = false;

        if ( loadMapping && version == PsiVersion.VERSION_1 ) {

            if ( cvMapping == null ) {

                try {
                    cvMapping = new CvMapping();

//                    cvMapping.loadFile( new File( "C:/reverseMapping.txt" ), helper );

                } catch ( IntactException e ) {
                    e.printStackTrace();
                }
            }
        }

        Interaction2xmlI interaction2xml = Interaction2xmlFactory.getInstance( session );

        // in order to have them in that order, experimentList, then interactorList, at last interactionList.
        session.getExperimentListElement();
        session.getInteractorListElement();

        Collection interactions = experiment.getInteractions();
        int count = 0;
        for ( Iterator iterator1 = interactions.iterator(); iterator1.hasNext(); ) {
            Interaction interaction = (Interaction) iterator1.next();

            interaction2xml.create( session, session.getInteractionListElement(), interaction );
            if ( ( count % 50 ) == 0 ) {
                System.out.println( "" );
            }
            System.out.print( "." );
            System.out.flush();
            count++;
        }


        File file = new File( OUTPUT_DIRECTORY + "/" + experiment.getShortLabel() + ".PSI" + version.getVersion() + ".xml" );
        write( session.getPsiDocument().getDocumentElement(), file );

        DataBuilder builder = new PsiDataBuilder();
        try {
            builder.writeData( file.getAbsolutePath(), session.getPsiDocument() );
        } catch ( DataConversionException e ) {
            e.printStackTrace();
        }

        PsiValidator.validate( file );

//                DisplayXML.print( session.getPsiDocument().getDocumentElement() );
//                System.out.println( "\n\n\n" );

    }


    public static void main( String[] args ) throws IntactException, SQLException,
                                                    TransformerException, IOException {

        Chrono chrono = new Chrono();
        chrono.start();

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();

        try {
            BaseDao dao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBaseDao();
            System.out.println( "Database: " + dao.getDbName() );
            System.out.println( "Username: " + IntactContext.getCurrentInstance().getUserContext().getUserId() );

            System.out.print( "Searching for all experiment: " );

            String experimentShortlabels = "rual-2005-1";

            Collection<Experiment> experiments = new ArrayList<Experiment>();
            Collection<String> experimentsLabel = new ArrayList<String>();

            StringTokenizer st = new StringTokenizer( experimentShortlabels, "," );
            while ( st.hasMoreTokens() ) {
                experimentsLabel.add( st.nextToken() );
            }

            for ( Iterator iterator = experimentsLabel.iterator(); iterator.hasNext(); ) {
                String label = (String) iterator.next();
                System.out.print( "Loading " + label + "..." );
                System.out.flush();
                experiments.addAll( IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getExperimentDao().getByShortLabelLike(label));
                System.out.println( "done." );
            }

            System.out.println( experiments.size() + " experiment" + ( experiments.size() > 1 ? "s" : "" ) + " found." );

            long timePsi1 = 0;
//            long timePsi2 = 0;
            long timePsi25 = 0;

            for ( Iterator iterator = experiments.iterator(); iterator.hasNext(); ) {

                Experiment experiment = (Experiment) iterator.next();

//                if ( experiment.getInteractions().size() > 500 ) {
//                    iterator.remove();
//                    continue;
//                }
                long start;

//                start = System.currentTimeMillis();
//                exportExperiment( experiment, PsiVersion.VERSION_1 );
//                timePsi1 += System.currentTimeMillis() - start;

//                start = System.currentTimeMillis();
//                exportExperiment( experiment, PsiVersion.VERSION_2 );
//                timePsi2 += System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                exportExperiment( experiment, PsiVersion.VERSION_25 );
                timePsi25 += System.currentTimeMillis() - start;

                iterator.remove();

                System.out.println( "Generation Time" );
                System.out.println( "PSI v1:  " + timePsi1 );
//                System.out.println( "PSI v2:  " + timePsi2 );
                System.out.println( "PSI v25: " + timePsi25 );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            IntactContext.getCurrentInstance().getDataContext().commitAllActiveTransactions();
        } catch (IntactTransactionException e) {
            e.printStackTrace();
        }

        chrono.stop();
        System.out.println( "Time elapsed: " + chrono );
    }
}