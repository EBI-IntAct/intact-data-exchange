package uk.ac.ebi.intact.util.uniprotExport.miscore;

import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.score.InteractionClusterScore;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.converters.Intact2BinaryInteractionConverter;
import uk.ac.ebi.intact.psimitab.converters.InteractionConverter;
import uk.ac.ebi.intact.psimitab.converters.expansion.NotExpandableInteractionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15-Sep-2010</pre>
 */

public class MiScoreClient {

    private InteractionClusterScore interactionClusterScore;
    private Intact2BinaryInteractionConverter interactionConverter;

    public MiScoreClient(){
        this.interactionConverter = new Intact2BinaryInteractionConverter();
        this.interactionClusterScore = new InteractionClusterScore();
    }

    public void computeMiScoresFor(List<String> interactions){

        int i = 0;
        List<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();

        while (i < interactions.size()){
            binaryInteractions.clear();

            for (int j = i; j<i+200 && j<interactions.size();j++){
                String interactionAc = interactions.get(j);
                Interaction intactInteraction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionAc);

                if (this.interactionConverter.getExpansionStrategy().isExpandable(intactInteraction)){
                    try {
                        Collection<IntactBinaryInteraction> toBinary = this.interactionConverter.convert(intactInteraction);
                        binaryInteractions.addAll(toBinary);
                    } catch (NotExpandableInteractionException e) {
                        System.out.println("The interaction " + interactionAc + ", " + intactInteraction.getShortLabel() + " cannot be converted into binary interactions and is excluded.");
                    }
                }
                else {
                    System.out.println("The interaction " + interactionAc + ", " + intactInteraction.getShortLabel() + " cannot be converted into binary interactions and is excluded.");
                }
            }
            this.interactionClusterScore.setBinaryInteractionList(binaryInteractions);
            this.interactionClusterScore.runService();

            i += 200;
        }

        this.interactionClusterScore.saveScores("/home/marine/Desktop/miScores_1");
    }
}
