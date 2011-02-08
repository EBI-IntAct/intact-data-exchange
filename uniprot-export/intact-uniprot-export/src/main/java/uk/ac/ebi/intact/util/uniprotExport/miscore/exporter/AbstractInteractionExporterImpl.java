package uk.ac.ebi.intact.util.uniprotExport.miscore.exporter;

import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.IntActInteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiScoreResults;

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

public abstract class AbstractInteractionExporterImpl implements InteractionExporter {
    @Override
    public void exportInteractionsFrom(MiScoreResults results) throws UniprotExportException {
        MiClusterContext context = results.getClusterContext();
        IntActInteractionClusterScore miScore = results.getClusterScore();

        Set<Integer> interactionsPossibleToExport = new HashSet<Integer>();

        for (Map.Entry<Integer, EncoreInteraction> entry : miScore.getInteractionMapping().entrySet()){
            EncoreInteraction encore = entry.getValue();

            if (canExportEncoreInteraction(encore, context)){
                interactionsPossibleToExport.add(entry.getKey());
            }
        }

        results.setInteractionsToExport(interactionsPossibleToExport);
    }

    @Override
    public boolean canExportEncoreInteraction(EncoreInteraction interaction, MiClusterContext context) throws UniprotExportException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
