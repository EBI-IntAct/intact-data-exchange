package uk.ac.ebi.intact.psixml.converter.shared;

import psidev.psi.mi.xml.model.Organism;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.psixml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.psixml.converter.annotation.PsiConverter;
import uk.ac.ebi.intact.psixml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.psixml.converter.util.PsiConverterUtils;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@PsiConverter(intactObjectType = BioSource.class, psiObjectType = Organism.class)
public class OrganismConverter extends AbstractIntactPsiConverter<BioSource, Organism> {

    public OrganismConverter(Institution institution) {
        super(institution);
    }

    public BioSource psiToIntact(Organism psiObject) {
        if (psiObject == null) return null;

        String shortLabel = IntactConverterUtils.getShortLabelFromNames(psiObject.getNames());
        int taxId = psiObject.getNcbiTaxId();

        BioSource bioSource = new BioSource(getInstitution(), shortLabel, String.valueOf(taxId));
        IntactConverterUtils.populateNames(psiObject.getNames(), bioSource);

        return bioSource;
    }

    public Organism intactToPsi(BioSource intactObject) {
        Organism organism = new Organism();
        PsiConverterUtils.populate(intactObject, organism);

        organism.setNcbiTaxId(Integer.valueOf(intactObject.getTaxId()));

        return organism;
    }
}
