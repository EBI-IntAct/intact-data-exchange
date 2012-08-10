package uk.ac.ebi.intact.psimitab.converters.enrichers;

import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.psimitab.converters.converters.AliasConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.CrossReferenceConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.InteractorConverter;

import java.util.Collection;

/**
 * Converts Nucleic acids interactors following data best practices
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/08/12</pre>
 */

public class NucleicAcidConverter extends AbstractEnricher{

    public NucleicAcidConverter(CrossReferenceConverter<InteractorXref> xrefConv, AliasConverter alisConv){
        super(xrefConv, alisConv);
    }

    /**
     * Enrich the mitab interactor following data best practices for nucleic acids
     * @param nucleicAcid
     * @param mitabInteractor
     * @return the standard InchiKey for the small molecule. Can be null if no standard inchi key available
     */
    public void enrichNucleicAcidFromIntact(NucleicAcid nucleicAcid, Interactor mitabInteractor){

        if (nucleicAcid != null && mitabInteractor != null){
            Collection<InteractorXref> interactorXrefs = nucleicAcid.getXrefs();
            Collection<InteractorAlias> aliases = nucleicAcid.getAliases();

            // xrefs
            boolean hasFoundEMBLdentity = processXrefs(mitabInteractor, interactorXrefs);

            // ac and display long
            processAccessionAndDisplay(nucleicAcid, mitabInteractor, hasFoundEMBLdentity);

            // convert aliases
            if (!aliases.isEmpty()){

                processAliases(mitabInteractor, aliases);
            }

            // uses crc64 for checksum
            if (nucleicAcid.getCrc64() != null){
                Checksum checksum = new ChecksumImpl(InteractorConverter.CRC64, nucleicAcid.getCrc64());
                mitabInteractor.getChecksums().add(checksum);
            }
        }
    }

    @Override
    protected void processAccessionAndDisplay(uk.ac.ebi.intact.model.Interactor mol, Interactor mitabInteractor, boolean hasFoundEMBLIdentity) {
        // aliases
        if (hasFoundEMBLIdentity){
            String identifier = mitabInteractor.getIdentifiers().iterator().next().getIdentifier();
            // the identifier is the display short
            psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvDatabase.PSI_MI, identifier, InteractorConverter.DISPLAY_SHORT );
            mitabInteractor.getAliases().add(displayShort);
            // the shortlabel is a chebi shortlabel as well
            psidev.psi.mi.tab.model.Alias shortLabel = new AliasImpl( CvDatabase.DDBG, mol.getShortLabel(), InteractorConverter.SHORTLABEL );
            mitabInteractor.getAliases().add(shortLabel);
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
                // add shortlabel as intact alias
                psidev.psi.mi.tab.model.Alias altId = new AliasImpl( CvDatabase.INTACT, mol.getShortLabel(), InteractorConverter.SHORTLABEL  );
                mitabInteractor.getAliases().add(altId);

                // add ac as display short as well
                psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvDatabase.PSI_MI, mol.getAc(), InteractorConverter.DISPLAY_SHORT  );
                mitabInteractor.getAliases().add(displayShort);

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

                // add shortlabel as display short as well
                psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvDatabase.PSI_MI, mol.getShortLabel(), InteractorConverter.DISPLAY_SHORT  );
                mitabInteractor.getAliases().add(displayShort);

                // add shortlabel as display long as well
                psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvDatabase.PSI_MI, mol.getShortLabel(), InteractorConverter.DISPLAY_LONG  );
                mitabInteractor.getAliases().add(displayLong);

            }
        }
    }

    @Override
    protected boolean processXrefs(Interactor mitabInteractor, Collection<InteractorXref> interactorXrefs) {
        boolean hasFoundIdentity = false;

        if (!interactorXrefs.isEmpty()){

            // convert xrefs, and identity
            for (InteractorXref ref : interactorXrefs){

                // identity xrefs
                if (ref.getCvXrefQualifier() != null && CvXrefQualifier.IDENTITY_MI_REF.equals(ref.getCvXrefQualifier().getIdentifier())){
                    // first ddbj/embl/genbank identity
                    if (!hasFoundIdentity && ref.getCvDatabase() != null && CvDatabase.DDBG_MI_REF.equals(ref.getCvDatabase().getIdentifier())){

                        CrossReference identity = xRefConverter.createCrossReference(ref, false);
                        if (identity != null){
                            hasFoundIdentity = true;

                            mitabInteractor.getIdentifiers().add(identity);
                        }
                    }
                    // other identifiers
                    else {
                        CrossReference identity = xRefConverter.createCrossReference(ref, false);
                        if (identity != null){
                            hasFoundIdentity = true;

                            mitabInteractor.getAlternativeIdentifiers().add(identity);
                        }
                    }
                }
                // other xrefs
                else {
                    CrossReference xref = xRefConverter.createCrossReference(ref, true);
                    if (xref != null){
                        hasFoundIdentity = true;

                        mitabInteractor.getXrefs().add(xref);
                    }
                }
            }
        }
        return hasFoundIdentity;
    }
}
