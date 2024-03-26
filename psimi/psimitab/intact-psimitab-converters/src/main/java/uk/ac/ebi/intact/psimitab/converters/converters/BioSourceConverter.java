package uk.ac.ebi.intact.psimitab.converters.converters;

import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.Organism;
import psidev.psi.mi.tab.model.OrganismImpl;
import uk.ac.ebi.intact.jami.model.extension.IntactOrganism;

/**
 * This class allows to convert a Intact biosource to a MITAB organism
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/07/12</pre>
 */

public class BioSourceConverter {

    public static String TAXID = "taxid";

    public Organism intactToMitab(IntactOrganism organism){
        if (organism != null){
            Organism mitabOrganism = new OrganismImpl();

            String name = organism.getCommonName();
            String fullName = organism.getScientificName();
            String taxId = String.valueOf(organism.getTaxId());

            if (name != null){
                mitabOrganism.addIdentifier(new CrossReferenceImpl(TAXID, taxId, name));
            }

            if (fullName != null){
                mitabOrganism.addIdentifier(new CrossReferenceImpl(TAXID, taxId, fullName));
            }

            return mitabOrganism;
        }

        return null;
    }
}
