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

    private static final double EXPORT_THRESHOLD = 0.40;
    private static final String COLOCALIZATION = "MI:0403";

    public ExporterBasedOnClusterScore(){
    }

    @Override
    public boolean canExportEncoreInteraction(EncoreInteraction encore, ExportContext context) throws UniprotExportException {

        double score = FilterUtils.getMiClusterScoreFor(encore);

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
                        logger.info("The interaction " + encore.getId() + " passed the export rules with score = " + score);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean canExportBinaryInteraction(BinaryInteraction interaction, ExportContext context) throws UniprotExportException {
        double score = FilterUtils.getMiClusterScoreFor(interaction);

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
                        logger.info("The interaction " + ac + " passed the export rules with score = " + score);

                        return true;
                    }
                }
            }
        }

        return false;
    }
}
