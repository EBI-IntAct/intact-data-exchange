package uk.ac.ebi.intact.dataexchange.psimi.xml.converter;

import uk.ac.ebi.intact.model.Institution;

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
}
