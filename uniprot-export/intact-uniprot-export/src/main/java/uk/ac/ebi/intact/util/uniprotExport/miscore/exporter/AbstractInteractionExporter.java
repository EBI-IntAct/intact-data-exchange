package uk.ac.ebi.intact.util.uniprotExport.miscore.exporter;

import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.BinaryClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.results.ExportContext;
import uk.ac.ebi.intact.util.uniprotExport.results.IntactCluster;
import uk.ac.ebi.intact.util.uniprotExport.results.UniprotExportResults;

import java.util.HashSet;
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
    @Override
    public void exportInteractionsFrom(UniprotExportResults results) throws UniprotExportException {
        ExportContext context = results.getExportContext();
        IntactCluster cluster = results.getCluster();

        Set<Integer> interactionsPossibleToExport = new HashSet<Integer>();

        if (cluster instanceof BinaryClusterScore){
            BinaryClusterScore clusterScore = (BinaryClusterScore) cluster;

            for (Map.Entry<Integer, BinaryInteraction> entry : clusterScore.getBinaryInteractionCluster().entrySet()){

                if (canExportBinaryInteraction(entry.getValue(), context)){
                    interactionsPossibleToExport.add(entry.getKey());
                }
            }
        }
        else {
            for (Map.Entry<Integer, EncoreInteraction> entry : cluster.getEncoreInteractionCluster().entrySet()){

                if (canExportEncoreInteraction(entry.getValue(), context)){
                    interactionsPossibleToExport.add(entry.getKey());
                }
            }
        }

        results.setInteractionsToExport(interactionsPossibleToExport);
    }
}
