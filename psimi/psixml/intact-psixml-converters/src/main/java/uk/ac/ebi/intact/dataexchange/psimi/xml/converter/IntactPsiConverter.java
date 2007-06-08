package uk.ac.ebi.intact.dataexchange.psimi.xml.converter;

/**
 * Converter interface, which converters Intact<->Psi must implement
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface IntactPsiConverter<I, P> {

    I psiToIntact(P psiObject);

    P intactToPsi(I intactObject);
}
