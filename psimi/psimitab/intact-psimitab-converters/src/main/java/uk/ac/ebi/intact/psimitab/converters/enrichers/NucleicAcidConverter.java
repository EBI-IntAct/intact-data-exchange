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
import uk.ac.ebi.intact.jami.model.extension.IntactNucleicAcid;
import uk.ac.ebi.intact.jami.model.extension.ParticipantEvidenceXref;
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

public class NucleicAcidConverter extends AbstractEnricher<IntactNucleicAcid> {

    public NucleicAcidConverter(CrossReferenceConverter<ParticipantEvidenceXref> xrefConv, AliasConverter alisConv){
        super(xrefConv, alisConv);
    }

    public NucleicAcidConverter(CrossReferenceConverter<ParticipantEvidenceXref> xrefConv, AliasConverter alisConv, String defaultInstitution) {
        super(xrefConv, alisConv, defaultInstitution);
    }

    /**
     * Enrich the mitab interactor following data best practices for nucleic acids
     * @param nucleicAcid
     * @param mitabInteractor
     * @return the standard InchiKey for the small molecule. Can be null if no standard inchi key available
     */
    public void enrichNucleicAcidFromIntact(IntactNucleicAcid nucleicAcid, Interactor mitabInteractor){

        if (nucleicAcid != null && mitabInteractor != null){
            Collection<Xref> interactorXrefs = nucleicAcid.getXrefs();
            Collection<Alias> aliases = nucleicAcid.getAliases();

            // xrefs
            boolean hasFoundEMBLdentity = processXrefs(mitabInteractor, interactorXrefs);

            // ac and display long
            processAccessionAndDisplay(nucleicAcid, mitabInteractor, hasFoundEMBLdentity);

            // convert aliases
            if (!aliases.isEmpty()){

                processAliases(mitabInteractor, aliases);
            }

            // uses crc64 for checksum
            psidev.psi.mi.jami.model.Checksum crc64 = ChecksumUtils.collectFirstChecksumWithMethod(nucleicAcid.getChecksums(), null, "crc64");
            if (crc64 != null) {
                Checksum checksum = new ChecksumImpl(InteractorConverter.CRC64, crc64.getValue());
                mitabInteractor.getChecksums().add(checksum);
            }
        }
    }

    @Override
    protected void processAccessionAndDisplay(IntactNucleicAcid mol, Interactor mitabInteractor, boolean hasFoundEMBLIdentity) {
        // the shortlabel is the display short
        psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvTerm.PSI_MI, mol.getShortName(), InteractorConverter.DISPLAY_SHORT );
        mitabInteractor.getAliases().add(displayShort);
        // aliases
        if (hasFoundEMBLIdentity){
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
                CrossReference id = new CrossReferenceImpl( defaultInstitution, mol.getShortName());
                mitabInteractor.getIdentifiers().add(id);

                // add shortlabel as display long as well
                psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvTerm.PSI_MI, mol.getShortName(), InteractorConverter.DISPLAY_LONG  );
                mitabInteractor.getAliases().add(displayLong);

            }
        }
    }

    @Override
    protected boolean processXrefs(Interactor mitabInteractor, Collection<Xref> interactorXrefs) {
        boolean hasFoundIdentity = false;

        if (!interactorXrefs.isEmpty()){

            // convert xrefs, and identity
            for (Xref ref : interactorXrefs){

                // identity xrefs
                if (ref.getQualifier() != null && Xref.IDENTITY_MI.equals(ref.getQualifier().getMIIdentifier())){
                    // first ddbj/embl/genbank identity
                    if (!hasFoundIdentity && ref.getDatabase() != null && Xref.DDBJ_EMBL_GENBANK_MI.equals(ref.getDatabase().getMIIdentifier())){

                        CrossReference identity = xRefConverter.createCrossReference((ParticipantEvidenceXref) ref, false);
                        if (identity != null){
                            hasFoundIdentity = true;

                            mitabInteractor.getIdentifiers().add(identity);
                        }
                    }
                    // other identifiers
                    else {
                        CrossReference identity = xRefConverter.createCrossReference((ParticipantEvidenceXref) ref, false);
                        if (identity != null){
                            mitabInteractor.getAlternativeIdentifiers().add(identity);
                        }
                    }
                }
                // secondary acs are alternative identifiers
                else if (ref.getQualifier() != null && (Xref.SECONDARY_MI.equals(ref.getQualifier().getMIIdentifier())
                        || intactSecondary.equals(ref.getQualifier().getShortName()))){
                    CrossReference identity = xRefConverter.createCrossReference((ParticipantEvidenceXref) ref, false);
                    if (identity != null){
                        mitabInteractor.getAlternativeIdentifiers().add(identity);
                    }
                }
                // other xrefs
                else {
                    CrossReference xref = xRefConverter.createCrossReference((ParticipantEvidenceXref) ref, true);
                    if (xref != null){
                        mitabInteractor.getXrefs().add(xref);
                    }
                }
            }
        }
        return hasFoundIdentity;
    }
}
