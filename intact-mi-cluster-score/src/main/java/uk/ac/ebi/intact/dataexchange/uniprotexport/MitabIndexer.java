package uk.ac.ebi.intact.dataexchange.uniprotexport;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.enfin.mi.cluster.ClusterContext;
import uk.ac.ebi.enfin.mi.cluster.InteractionClusterAdv;
import uk.ac.ebi.enfin.mi.cluster.cache.CacheStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class generates a clustered mitab file using the mi cluster.
 *
 * The possible arguments are :
 * - mitab : the mitab file to read and cluster
 * - clustered : the name of the clustered mitab file to generate
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/03/11</pre>
 */

public class MitabIndexer {

    /**
     * Cluster all the binary interactions in the mitab file and save in clustered mitab file
     * @param mitabFile
     * @param clusteredMitabFile
     * @throws IOException
     * @throws psidev.psi.mi.xml.converter.ConverterException
     */
    public static void clusterAllInteractions(String mitabFile, String clusteredMitabFile) throws IOException, ConverterException {
        ClusterContext context = ClusterContext.getInstance();
        context.setCacheStrategy(CacheStrategy.ON_DISK);

        InteractionClusterAdv cluster = new InteractionClusterAdv();

        PsimiTabReader mitabReader = new PsimiTabReader();
        File mitabAsFile = new File(mitabFile);

        FileInputStream inputStream = new FileInputStream(mitabAsFile);

        try{
            Iterator<psidev.psi.mi.tab.model.BinaryInteraction> iterator = mitabReader.iterate(inputStream);

            // the binary interactions to cluster
            List<psidev.psi.mi.tab.model.BinaryInteraction> interactionToProcess = new ArrayList<BinaryInteraction>();

            while (iterator.hasNext()){
                interactionToProcess.clear();
                while (interactionToProcess.size() < 200 && iterator.hasNext()){
                    BinaryInteraction interaction = iterator.next();

                    interactionToProcess.add(interaction);
                }

                cluster.setBinaryInteractionList(interactionToProcess);
                cluster.runService();
            }

            cluster.saveScoreInMitab(clusteredMitabFile);
        }
        finally {
            inputStream.close();
        }
    }

    public static void main( String[] args ) throws IOException {

        // 2 possible arguments
        if( args.length != 2 ) {
            System.err.println( "Usage: MitabIndexer <mitab> <clustered>" );
            System.err.println( "Usage: <mitab> is the mitab file" );
            System.err.println( "Usage: <clustered> is the clustered mitab file.");
                    System.exit( 1 );
        }

        String mitabFile = args[0];
        String intactClustered = args[1];

        System.out.println( "MITAB file = " + mitabFile );
        System.out.println( "Clustered MITAB file = " + intactClustered );

        try {
            clusterAllInteractions(mitabFile, intactClustered);
        } catch (ConverterException e) {
            e.printStackTrace();
        }

    }
}
