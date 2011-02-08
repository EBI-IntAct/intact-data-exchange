package uk.ac.ebi.intact.util.uniprotExport.miscore.exporter;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Confidence;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.filter.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiClusterContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This exporter is respecting the following rules :
 * - a binary interaction is eligible for uniprot export if has a MI score superior or equal to a threshold value
 * - the binary interaction must have at least one evidence that this interaction is a true binary interaction (and not spoke expanded)
 * and this evidence must be different from colocalization
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16-Sep-2010</pre>
 */

public class ExporterBasedOnClusterScore extends AbstractInteractionExporterImpl {

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
        return extractMiClusterScoreFrom(confidenceValues);
    }

    private double extractMiClusterScoreFrom(List<Confidence> confidenceValues) {
        double score = 0;
        for(Confidence confidenceValue:confidenceValues){
            if(confidenceValue.getType().equalsIgnoreCase(CONFIDENCE_NAME)){
                score = Double.parseDouble(confidenceValue.getValue());
            }
        }

        return score;
    }

    /**
     *
     * @param interaction
     * @return the computed Mi cluster score for this interaction
     */
    private double getMiClusterScoreFor(BinaryInteraction interaction){
        List<psidev.psi.mi.tab.model.Confidence> confidenceValues = interaction.getConfidenceValues();
        return extractMiClusterScoreFrom(confidenceValues);
    }

    @Override
    public boolean canExportEncoreInteraction(EncoreInteraction encore, MiClusterContext context) throws UniprotExportException {

        double score = getMiClusterScoreFor(encore);

        if (score >= EXPORT_THRESHOLD){

            if (encore.getExperimentToDatabase() == null){
                throw new UniprotExportException("The interaction " + encore.getId() + ":" + encore.getInteractorA() + "-" + encore.getInteractorB() +" doesn't have any references to IntAct.");
            }

            Set<String> intactInteractions = new HashSet<String>();

            intactInteractions.addAll(encore.getExperimentToPubmed().keySet());

            if (intactInteractions.isEmpty()){
                throw new UniprotExportException("The interaction " + encore.getId() + ":" + encore.getInteractorA() + "-" + encore.getInteractorB() +" doesn't have any references to IntAct.");
            }

            for (String ac : intactInteractions){
                if (!context.getSpokeExpandedInteractions().contains(ac)){

                    String method = context.getInteractionToMethod_type().get(ac).getMethod();

                    if (!method.equals(COLOCALIZATION)){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean canExportEBinaryInteraction(BinaryInteraction interaction, MiClusterContext context) throws UniprotExportException {
        double score = getMiClusterScoreFor(interaction);

        if (score >= EXPORT_THRESHOLD){

            Set<String> intactInteractions = new HashSet<String>();

            intactInteractions.addAll(FilterUtils.extractIntactAcFrom(interaction.getInteractionAcs()));

            if (intactInteractions.isEmpty()){
                throw new UniprotExportException("The interaction :" + interaction.getInteractorA().toString() + "-" + interaction.getInteractorB().toString() +" doesn't have any references to IntAct.");
            }

            for (String ac : intactInteractions){
                if (!context.getSpokeExpandedInteractions().contains(ac)){

                    String method = context.getInteractionToMethod_type().get(ac).getMethod();

                    if (!method.equals(COLOCALIZATION)){
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
