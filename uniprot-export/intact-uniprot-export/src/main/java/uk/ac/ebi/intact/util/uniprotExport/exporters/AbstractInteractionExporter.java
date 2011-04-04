package uk.ac.ebi.intact.util.uniprotExport.exporters;

import org.apache.log4j.Logger;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.results.ExportedClusteredInteractions;
import uk.ac.ebi.intact.util.uniprotExport.results.UniprotExportResults;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.BinaryClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntactCluster;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.ExportContext;

import java.util.Map;
import java.util.Set;

/**
 * Abstract class which implements the InteractionExporter interface
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public abstract class AbstractInteractionExporter implements InteractionExporter {

    private static final Logger logger = Logger.getLogger(AbstractInteractionExporter.class);

    @Override
    public void exportInteractionsFrom(UniprotExportResults results) throws UniprotExportException {
        ExportContext context = results.getExportContext();

        ExportedClusteredInteractions positiveInteractions = results.getPositiveClusteredInteractions();
        ExportedClusteredInteractions negativeInteractions = results.getNegativeClusteredInteractions();

        IntactCluster cluster = positiveInteractions.getCluster();
        IntactCluster negativeCluster = negativeInteractions.getCluster();

        Set<Integer> interactionsPossibleToExport = positiveInteractions.getInteractionsToExport();
        Set<Integer> negativeInteractionsPossibleToExport = negativeInteractions.getInteractionsToExport();

        // process positive interactions
        if (cluster instanceof BinaryClusterScore){
            BinaryClusterScore clusterScore = (BinaryClusterScore) cluster;

            for (Map.Entry<Integer, BinaryInteraction<Interactor>> entry : clusterScore.getBinaryInteractionCluster().entrySet()){

                if (canExportBinaryInteraction(entry.getValue(), context)){
                    interactionsPossibleToExport.add(entry.getKey());
                }
            }
        }
        else {
            for (Map.Entry<Integer, EncoreInteractionForScoring> entry : cluster.getEncoreInteractionCluster().entrySet()){

                if (canExportEncoreInteraction(entry.getValue(), context)){
                    interactionsPossibleToExport.add(entry.getKey());
                }
            }
        }

        // process negative interactions
        if (negativeCluster != null){
            if (negativeCluster instanceof BinaryClusterScore){
                BinaryClusterScore clusterScore = (BinaryClusterScore) negativeCluster;

                for (Map.Entry<Integer, BinaryInteraction<Interactor>> entry : clusterScore.getBinaryInteractionCluster().entrySet()){

                    if (canExportNegativeBinaryInteraction(entry.getValue(), context, positiveInteractions)){
                        negativeInteractionsPossibleToExport.add(entry.getKey());
                    }
                }
            }
            else {
                for (Map.Entry<Integer, EncoreInteractionForScoring> entry : negativeCluster.getEncoreInteractionCluster().entrySet()){

                    if (canExportNegativeEncoreInteraction(entry.getValue(), context, positiveInteractions)){
                        negativeInteractionsPossibleToExport.add(entry.getKey());
                    }
                }
            }
        }
    }
}
