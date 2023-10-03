package uk.ac.ebi.intact.dataexchange.enricher.fetch;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;

@Component("intactNucleicAcidFetcher")
@Lazy
public class NucleicAcidFetcher extends psidev.psi.mi.jami.bridges.rna.central.RNACentralFetcher {
    public NucleicAcidFetcher() throws BridgeFailedException {
    }
}
