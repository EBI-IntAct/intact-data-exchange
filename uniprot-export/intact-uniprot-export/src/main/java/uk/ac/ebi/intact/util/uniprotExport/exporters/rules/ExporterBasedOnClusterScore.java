package uk.ac.ebi.intact.util.uniprotExport.exporters.rules;

import org.apache.log4j.Logger;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.exporters.AbstractInteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.ExportContext;

import java.util.HashSet;
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

public class ExporterBasedOnClusterScore extends AbstractInteractionExporter {
    private static final Logger logger = Logger.getLogger(ExporterBasedOnClusterScore.class);

    /**
     * The score threshold for positive interactions
     */
    private double positive_export_threshold = 0.43;

    /**
     * The score threshold for negative interactions
     */
    private double negative_export_threshold = 0.43;

    private static final String COLOCALIZATION = "MI:0403";

    public ExporterBasedOnClusterScore(){
    }

    @Override
    public boolean canExportEncoreInteraction(EncoreInteraction encore, ExportContext context, boolean isNegative) throws UniprotExportException {

        double score = FilterUtils.getMiClusterScoreFor(encore);

        double threshold = isNegative ? negative_export_threshold : positive_export_threshold;

        if (score >= threshold){

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
                        logger.info("The interaction " + encore.getId() + " passed the export rules with score = " + score);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean canExportBinaryInteraction(BinaryInteraction interaction, ExportContext context, boolean isNegative) throws UniprotExportException {
        double score = FilterUtils.getMiClusterScoreFor(interaction);

        double threshold = isNegative ? negative_export_threshold : positive_export_threshold;

        if (score >= threshold){

            Set<String> intactInteractions = new HashSet<String>();

            intactInteractions.addAll(FilterUtils.extractIntactAcFrom(interaction.getInteractionAcs()));

            if (intactInteractions.isEmpty()){
                throw new UniprotExportException("The interaction :" + interaction.getInteractorA().toString() + "-" + interaction.getInteractorB().toString() +" doesn't have any references to IntAct.");
            }

            for (String ac : intactInteractions){
                if (!context.getSpokeExpandedInteractions().contains(ac)){

                    String method = context.getInteractionToMethod_type().get(ac).getMethod();

                    if (!method.equals(COLOCALIZATION)){
                        logger.info("The interaction " + ac + " passed the export rules with score = " + score);

                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void setPositive_export_threshold(double positive_export_threshold) {
        this.positive_export_threshold = positive_export_threshold;
    }

    public double getPositive_export_threshold() {
        return positive_export_threshold;
    }

    public double getNegative_export_threshold() {
        return negative_export_threshold;
    }

    public void setNegative_export_threshold(double negative_export_threshold) {
        this.negative_export_threshold = negative_export_threshold;
    }
}
