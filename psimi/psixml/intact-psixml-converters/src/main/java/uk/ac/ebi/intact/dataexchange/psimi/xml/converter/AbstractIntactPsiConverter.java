package uk.ac.ebi.intact.dataexchange.psimi.xml.converter;

import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.IntactEntry;
import psidev.psi.mi.xml.model.Entry;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractIntactPsiConverter<I, P> implements IntactPsiConverter<I, P> {

    private Institution institution;

    public AbstractIntactPsiConverter(Institution institution) {
        this.institution = institution;
    }

    protected Institution getInstitution() {
        return institution;
    }

    protected void setInstitution(Institution institution)
    {
        this.institution = institution;
    }

    protected void failIfInconsistentConversion(I intactEntry, Entry P) {
        throw new UnsupportedOperationException();
    }

    protected void failIfInconsistentCollectionSize(String type, Collection intactCol, Collection psiCol) {
        if (intactCol.size() > 0 && psiCol.size() > 0 && intactCol.size() != psiCol.size()) {
            throw new InconsistentConversionException(type, intactCol.size(), psiCol.size());
        }
    }
}
