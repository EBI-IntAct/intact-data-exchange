package uk.ac.ebi.intact.dataexchange.uniprotexport.factory;

import uk.ac.ebi.intact.dataexchange.uniprotexport.variables.ExporterRule;
import uk.ac.ebi.intact.util.uniprotExport.exporters.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.exporters.rules.ExporterBasedOnClusterScore;

/**
 * Factory for the exporter rules
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */

public class InteractionExporterFactory {

    public static InteractionExporter createInteractionExporter(ExporterRule rule){
        if (rule == ExporterRule.mi_score) {
            ExporterBasedOnClusterScore exporter = new ExporterBasedOnClusterScore();
            exporter.setPositive_export_threshold(9);
            exporter.setNegative_export_threshold(9);
            return exporter;
        }
        return null;
    }

    public static ExporterRule convertIntoExporterRule(String rule){

        if (rule == null){
            return ExporterRule.none;
        }
        else if (ExporterRule.mi_score.toString().equalsIgnoreCase(rule)){
            return ExporterRule.mi_score;
        }
        else {
            return ExporterRule.none;
        }
    }
}
