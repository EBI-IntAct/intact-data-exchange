package uk.ac.ebi.intact.dataexchange.uniprotexport.factory;

import uk.ac.ebi.intact.dataexchange.uniprotexport.variables.ExporterRule;
import uk.ac.ebi.intact.util.uniprotExport.exporters.rules.ExporterBasedOnClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.exporters.rules.ExporterBasedOnDetectionMethod;
import uk.ac.ebi.intact.util.uniprotExport.exporters.InteractionExporter;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */

public class InteractionExporterFactory {

    public static InteractionExporter createInteractionExporter(ExporterRule rule){
        switch (rule) {
            case detection_method:
                return new ExporterBasedOnDetectionMethod();
            case mi_score:
                ExporterBasedOnClusterScore exporter = new ExporterBasedOnClusterScore();
                exporter.setExport_threshold(0.42);
                return exporter;
            default:
                return null;
        }
    }

    public static ExporterRule convertIntoExporterRule(String rule){

        if (rule == null){
            return ExporterRule.none;
        }
        else if (ExporterRule.detection_method.toString().equalsIgnoreCase(rule)){
            return ExporterRule.detection_method;
        }
        else if (ExporterRule.mi_score.toString().equalsIgnoreCase(rule)){
            return ExporterRule.mi_score;
        }
        else {
            return ExporterRule.none;
        }
    }
}
