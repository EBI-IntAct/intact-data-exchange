package uk.ac.ebi.intact.dataexchange.enricher.fetch;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;

@Component("intactEnsemblNucleicAcidFetcher")
@Lazy
public class EnsemblNucleicAcidFetcher extends psidev.psi.mi.jami.bridges.ensembl.EnsemblNucleicAcidFetcher {
    public EnsemblNucleicAcidFetcher() throws BridgeFailedException {
    }
}
