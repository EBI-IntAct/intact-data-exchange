package uk.ac.ebi.intact.psimitab.converters.enrichers;

import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.utils.ChecksumUtils;
import psidev.psi.mi.tab.model.AliasImpl;
import psidev.psi.mi.tab.model.Checksum;
import psidev.psi.mi.tab.model.ChecksumImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractor;
import uk.ac.ebi.intact.jami.model.extension.IntactPolymer;
import uk.ac.ebi.intact.jami.model.extension.ParticipantEvidenceXref;
import uk.ac.ebi.intact.psimitab.converters.converters.AliasConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.CrossReferenceConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.InteractorConverter;

import java.util.Collection;

/**
 * If the interactor type is not known, we should use this converter to enrich mitab interactor from intact
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/08/12</pre>
 */

public class DefaultInteractorEnricher extends AbstractEnricher<IntactInteractor> {

    public DefaultInteractorEnricher(CrossReferenceConverter<ParticipantEvidenceXref> xrefConv, AliasConverter alisConv) {
        super(xrefConv, alisConv);
    }

    public DefaultInteractorEnricher(CrossReferenceConverter<ParticipantEvidenceXref> xrefConv, AliasConverter alisConv, String defaultInstitution) {
        super(xrefConv, alisConv, defaultInstitution);
    }

    /**
     * Enrich the mitab interactor following data best practices for nucleic acids
     * @param interactor
     * @param mitabInteractor
     * @return the standard InchiKey for the small molecule. Can be null if no standard inchi key available
     */
    public void enrichInteractorFromIntact(IntactInteractor interactor, Interactor mitabInteractor){

        if (interactor != null && mitabInteractor != null){
            Collection<Xref> interactorXrefs = interactor.getXrefs();
            Collection<Alias> aliases = interactor.getAliases();

            // xrefs
            boolean hasFoundIdentity = processXrefs(mitabInteractor, interactorXrefs);

            // ac and display long
            processAccessionAndDisplay(interactor, mitabInteractor, hasFoundIdentity);

            // convert aliases
            if (!aliases.isEmpty()){

                processAliases(mitabInteractor, aliases);
            }

            // uses crc64 for checksum
            if (interactor instanceof IntactPolymer){
                IntactPolymer polymer = (IntactPolymer) interactor;
                psidev.psi.mi.jami.model.Checksum crc64 = ChecksumUtils.collectFirstChecksumWithMethod(polymer.getChecksums(), null, "crc64");
                if (crc64 != null) {
                    Checksum checksum = new ChecksumImpl(InteractorConverter.CRC64, crc64.getValue());
                    mitabInteractor.getChecksums().add(checksum);
                }
            }
        }
    }

    @Override
    protected void processAccessionAndDisplay(IntactInteractor mol, Interactor mitabInteractor, boolean hasFoundIdentity) {

        // the shortlabel is the display short
        psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvTerm.PSI_MI, mol.getShortName(), InteractorConverter.DISPLAY_SHORT );
        mitabInteractor.getAliases().add(displayShort);

        // aliases
        if (hasFoundIdentity){
            String identifier = mitabInteractor.getIdentifiers().iterator().next().getIdentifier();

            // the interactor unique id is the display long
            psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvTerm.PSI_MI, identifier, InteractorConverter.DISPLAY_LONG  );
            mitabInteractor.getAliases().add(displayLong);
            // convert ac as identity or secondary identifier
            if (mol.getAc() != null){

                // add ac as alternative id
                CrossReference acField = createCrossReferenceFromAc(mol);
                mitabInteractor.getAlternativeIdentifiers().add(0, acField);
            }
        }
        // no identity
        else{

            // ac will be identifier and shortlabel is an alias
            if(mol.getAc() != null){

                // add ac as unique id and add it as display_long as well
                CrossReference acField = createCrossReferenceFromAc(mol);
                mitabInteractor.getIdentifiers().add(acField);

                // add ac as psi display_long alias
                psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvTerm.PSI_MI, mol.getAc(), InteractorConverter.DISPLAY_LONG  );
                mitabInteractor.getAliases().add(displayLong);
            }
            // the shortlabel will be identifier because we need an identifier and will be displayLong as well
            else {
                CrossReference id = new CrossReferenceImpl( this.defaultInstitution, mol.getShortName());
                mitabInteractor.getIdentifiers().add(id);

                // add shortlabel as display long as well
                psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvTerm.PSI_MI, mol.getShortName(), InteractorConverter.DISPLAY_LONG  );
                mitabInteractor.getAliases().add(displayLong);

            }
        }
    }
}
