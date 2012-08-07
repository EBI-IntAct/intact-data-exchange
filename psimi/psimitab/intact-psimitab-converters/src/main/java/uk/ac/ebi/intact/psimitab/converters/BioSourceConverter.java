package uk.ac.ebi.intact.psimitab.converters;

import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.Organism;
import psidev.psi.mi.tab.model.OrganismImpl;
import uk.ac.ebi.intact.model.BioSource;

/**
 * This class allows to convert a Intact biosource to a MITAB organism
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/07/12</pre>
 */

public class BioSourceConverter {

    public static String TAXID = "taxid";

    public Organism intactToMitab(BioSource organism){
        if (organism != null && organism.getTaxId() != null){
            Organism mitabOrganism = new OrganismImpl();

            String name = organism.getShortLabel();
            String fullName = organism.getFullName();
            String taxId = organism.getTaxId();

            if (name != null){
                mitabOrganism.addIdentifier(new CrossReferenceImpl(TAXID, organism.getTaxId(), name));
            }

            if (fullName != null){
                mitabOrganism.addIdentifier(new CrossReferenceImpl(TAXID, organism.getTaxId(), fullName));
            }

            return mitabOrganism;
        }

        return null;
    }
}
