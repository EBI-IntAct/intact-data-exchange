package uk.ac.ebi.intact.psimitab.converters.enrichers;

import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.tab.model.AliasImpl;
import psidev.psi.mi.tab.model.ChecksumImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.jami.model.extension.IntactMolecule;
import uk.ac.ebi.intact.jami.model.extension.ParticipantEvidenceXref;
import uk.ac.ebi.intact.psimitab.converters.converters.AliasConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.CrossReferenceConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.InteractorConverter;
import uk.ac.ebi.intact.psimitab.converters.util.PsimitabTools;

import java.util.Collection;

/**
 * Converts SmallMolecule interactors following data best practices
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/08/12</pre>
 */

public class SmallMoleculeConverter extends AbstractEnricher<IntactMolecule> {

    public final static String STANDARD_INCHI_KEY = "standard inchi key";
    public final static String INCHI_MI_REF ="MI:2010";

    public SmallMoleculeConverter(CrossReferenceConverter<ParticipantEvidenceXref> xrefConv, AliasConverter alisConv){
        super(xrefConv, alisConv);
    }

    public SmallMoleculeConverter(CrossReferenceConverter<ParticipantEvidenceXref> xrefConv, AliasConverter alisConv, String defaultInstitution) {
        super(xrefConv, alisConv, defaultInstitution);
    }

    /**
     * Enrich the mitab interactor following data best practices for small molecules
     * @param smallMolecule
     * @param mitabInteractor
     * @return the standard InchiKey for the small molecule. Can be null if no standard inchi key available
     */
    public String enrichSmallMoleculeFromIntact(IntactMolecule smallMolecule, Interactor mitabInteractor){

        if (smallMolecule != null && mitabInteractor != null){
            Collection<Xref> interactorXrefs = smallMolecule.getXrefs();
            Collection<Alias> aliases = smallMolecule.getAliases();

            // xrefs
            boolean hasFoundCHEBIdentity = processXrefs(mitabInteractor, interactorXrefs);

            // ac and display long
            processAccessionAndDisplay(smallMolecule, mitabInteractor, hasFoundCHEBIdentity);

            // convert aliases
            if (!aliases.isEmpty()){

                processAliases(mitabInteractor, aliases);
            }

            // convert checksum (standard inchi key : only if available in annotation)
            String inchiKey = extractStandardInchiKeyFrom(smallMolecule);

            if (inchiKey != null){
                mitabInteractor.getChecksums().add(
                        new ChecksumImpl(STANDARD_INCHI_KEY, inchiKey));

            }

            return inchiKey;
        }

        return null;
    }

    @Override
    protected void processAccessionAndDisplay(IntactMolecule mol, Interactor mitabInteractor, boolean hasFoundCHEBIIdentity) {
        // if it is a small molecule from CHEBI, Assume the short label is the molecule name
        // aliases
        if (hasFoundCHEBIIdentity){
            // the shortlabel is the display short
            psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvTerm.PSI_MI, mol.getShortName(), InteractorConverter.DISPLAY_SHORT );
            mitabInteractor.getAliases().add(displayShort);
            // the interactor unique id is the display long
            psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvTerm.PSI_MI, mitabInteractor.getIdentifiers().iterator().next().getIdentifier(), InteractorConverter.DISPLAY_LONG  );
            mitabInteractor.getAliases().add(displayLong);

            // convert ac as identity or secondary identifier
            if (mol.getAc() != null){

                // add ac as alternative id
                CrossReference acField = createCrossReferenceFromAc(mol);
                mitabInteractor.getAlternativeIdentifiers().add(0, acField);
            }
        }
        // no CHEBI identity
        else{

            // ac will be identifier and shortlabel is an alias
            if(mol.getAc() != null){

                // add shortlabel as display short as well
                psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvTerm.PSI_MI, mol.getShortName(), InteractorConverter.DISPLAY_SHORT  );
                mitabInteractor.getAliases().add(displayShort);

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

                // add shortlabel as display short as well
                psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvTerm.PSI_MI, mol.getShortName(), InteractorConverter.DISPLAY_SHORT  );
                mitabInteractor.getAliases().add(displayShort);

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
                    // first CHEBI identity
                    if (!hasFoundIdentity && ref.getDatabase() != null && Xref.CHEBI_MI.equals(ref.getDatabase().getMIIdentifier())){

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

    public String extractStandardInchiKeyFrom(IntactMolecule interactor) {

        // find INCHI key
        final Annotation annotation = PsimitabTools.findAnnotationByTopicMiOrLabel(interactor.getAnnotations(), INCHI_MI_REF);// INCHI_MI_REF

        if (annotation != null){
            return  annotation.getValue();
        }

        return null;
    }
}
