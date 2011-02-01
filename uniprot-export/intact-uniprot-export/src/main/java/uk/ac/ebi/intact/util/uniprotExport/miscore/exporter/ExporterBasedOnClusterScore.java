package uk.ac.ebi.intact.util.uniprotExport.miscore.exporter;

import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.LineExport;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.IntActInteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiClusterContext;

import java.util.*;

/**
 * This class is extracting interactions in Intact which are only PPI interactions, non negative and dr-uniprot-export annotation is taken into account.
 * It is also possible to extract the interactions exported in uniprot with current rules on the interaction detection method.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16-Sep-2010</pre>
 */

public class ExporterBasedOnClusterScore extends LineExport implements InteractionExporter{

    private static final double EXPORT_THRESHOLD = 0.43;
    private static final String CONFIDENCE_NAME = "intactPsiscore";
    private static final String COLOCALIZATION = "MI:0403";

    public ExporterBasedOnClusterScore(){
    }

    /**
     *
     * @param interaction
     * @return the computed Mi cluster score for this interaction
     */
    private double getMiClusterScoreFor(EncoreInteraction interaction){
        List<psidev.psi.mi.tab.model.Confidence> confidenceValues = interaction.getConfidenceValues();
        double score = 0;
        for(psidev.psi.mi.tab.model.Confidence confidenceValue:confidenceValues){
            if(confidenceValue.getType().equalsIgnoreCase(CONFIDENCE_NAME)){
                score = Double.parseDouble(confidenceValue.getValue());
            }
        }

        return score;
    }

    /**
     * For each binary interaction in the intactMiClusterScore : filter on a threshold value of the score and then, depending on 'filterBinary',
     * will add a filter on true binary interaction
     * @param filterBinary
     * @return
     * @throws UniprotExportException
     */
    public Set<Integer> processExport(MiScoreResults results, boolean filterBinary) throws UniprotExportException {
        MiClusterContext context = results.getClusterContext();
        IntActInteractionClusterScore miScore = results.getClusterScore();

        Set<Integer> interactionsPossibleToExport = new HashSet<Integer>();
        Map<String, Map.Entry<String, String>> interactionType_Method = context.getInteractionToType_Method();
        List<String> spokeExpandedInteractions = context.getSpokeExpandedInteractions();

        for (Map.Entry<Integer, EncoreInteraction> entry : miScore.getInteractionMapping().entrySet()){
            EncoreInteraction encore = entry.getValue();

            double score = getMiClusterScoreFor(encore);

            if (score >= EXPORT_THRESHOLD){

                if (encore.getExperimentToDatabase() == null){
                    throw new UniprotExportException("The interaction " + entry.getKey() + ":" + encore.getInteractorA() + "-" + encore.getInteractorB() +" doesn't have any references to IntAct.");
                }
                List<String> intactInteractions = new ArrayList<String>();
                
                intactInteractions.addAll(encore.getExperimentToPubmed().keySet());

                if (intactInteractions.isEmpty()){
                    throw new UniprotExportException("The interaction " + entry.getKey() + ":" + encore.getInteractorA() + "-" + encore.getInteractorB() +" doesn't have any references to IntAct.");
                }

                for (String ac : intactInteractions){
                    if (filterBinary){
                        if (!spokeExpandedInteractions.contains(ac)){

                            String method = interactionType_Method.get(ac).getKey();

                            if (!method.equals(COLOCALIZATION)){
                                interactionsPossibleToExport.add(entry.getKey());
                                break;
                            }
                        }
                    }
                    else{
                        interactionsPossibleToExport.add(entry.getKey());
                    }
                }
            }
        }

        return interactionsPossibleToExport;
    }

    @Override
    public void exportInteractionsFrom(MiScoreResults results) throws UniprotExportException {
        results.setInteractionsToExport(processExport(results, true));
    }
}
