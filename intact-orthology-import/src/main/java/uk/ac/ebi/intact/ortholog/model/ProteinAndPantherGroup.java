package uk.ac.ebi.intact.ortholog.model;

import lombok.Value;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;

import java.util.Collection;

@Value
public class ProteinAndPantherGroup {

    IntactProtein protein;
    Collection<String> pantherIds;
}
