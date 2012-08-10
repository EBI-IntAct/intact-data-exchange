package uk.ac.ebi.intact.psimitab.converters.enrichers;

import psidev.psi.mi.tab.model.*;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.InteractorAlias;
import uk.ac.ebi.intact.model.InteractorXref;
import uk.ac.ebi.intact.model.Polymer;
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

public class DefaultInteractorEnricher extends AbstractEnricher{

    public DefaultInteractorEnricher(CrossReferenceConverter<InteractorXref> xrefConv, AliasConverter alisConv) {
        super(xrefConv, alisConv);
    }

    /**
     * Enrich the mitab interactor following data best practices for nucleic acids
     * @param interactor
     * @param mitabInteractor
     * @return the standard InchiKey for the small molecule. Can be null if no standard inchi key available
     */
    public void enrichInteractorFromIntact(uk.ac.ebi.intact.model.Interactor interactor, Interactor mitabInteractor){

        if (interactor != null && mitabInteractor != null){
            Collection<InteractorXref> interactorXrefs = interactor.getXrefs();
            Collection<InteractorAlias> aliases = interactor.getAliases();

            // xrefs
            boolean hasFoundIdentity = processXrefs(mitabInteractor, interactorXrefs);

            // ac and display long
            processAccessionAndDisplay(interactor, mitabInteractor, hasFoundIdentity);

            // convert aliases
            if (!aliases.isEmpty()){

                processAliases(mitabInteractor, aliases);
            }

            // uses crc64 for checksum
            if (interactor instanceof Polymer){
                Polymer polymer = (Polymer) interactor;
                if (polymer.getCrc64() != null){
                    Checksum checksum = new ChecksumImpl(InteractorConverter.CRC64, polymer.getCrc64());
                    mitabInteractor.getChecksums().add(checksum);
                }
            }
        }
    }

    @Override
    protected void processAccessionAndDisplay(uk.ac.ebi.intact.model.Interactor mol, Interactor mitabInteractor, boolean hasFoundIdentity) {

        // the shortlabel is the display short
        psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvDatabase.PSI_MI, mol.getShortLabel(), InteractorConverter.DISPLAY_SHORT );
        mitabInteractor.getAliases().add(displayShort);
        // the shortlabel is a INTACT shortlabel as well
        psidev.psi.mi.tab.model.Alias shortLabel = new AliasImpl( CvDatabase.INTACT, mol.getShortLabel(), InteractorConverter.SHORTLABEL );
        mitabInteractor.getAliases().add(shortLabel);

        // aliases
        if (hasFoundIdentity){
            String identifier = mitabInteractor.getIdentifiers().iterator().next().getIdentifier();

            // the interactor unique id is the display long
            psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvDatabase.PSI_MI, identifier, InteractorConverter.DISPLAY_LONG  );
            mitabInteractor.getAliases().add(displayLong);
            // convert ac as identity or secondary identifier
            if (mol.getAc() != null){

                // add ac as alternative id
                CrossReference acField = createCrossReferenceFromAc(mol);
                mitabInteractor.getAlternativeIdentifiers().add(acField);
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
                psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvDatabase.PSI_MI, mol.getAc(), InteractorConverter.DISPLAY_LONG  );
                mitabInteractor.getAliases().add(displayLong);
            }
            // the shortlabel will be identifier because we need an identifier and will be displayLong as well
            else {
                CrossReference id = new CrossReferenceImpl( CvDatabase.INTACT, mol.getShortLabel());
                mitabInteractor.getIdentifiers().add(id);

                // add shortlabel as display long as well
                psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvDatabase.PSI_MI, mol.getShortLabel(), InteractorConverter.DISPLAY_LONG  );
                mitabInteractor.getAliases().add(displayLong);

            }
        }
    }
}
