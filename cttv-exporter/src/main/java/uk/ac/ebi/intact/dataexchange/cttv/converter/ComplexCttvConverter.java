package uk.ac.ebi.intact.dataexchange.cttv.converter;

import org.cttv.input.model.EvidenceString;
import psidev.psi.mi.jami.model.Complex;

/**
 * Created by maitesin on 13/11/2014.
 */
public interface ComplexCttvConverter {
    public EvidenceString convertToEvidenceStringFromComplex(Complex complex);
}
