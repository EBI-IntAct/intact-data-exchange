package uk.ac.ebi.intact.psimitab.converters.enrichers;

import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.psimitab.converters.converters.AliasConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.CrossReferenceConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.InteractorConverter;

import java.util.Collection;

/**
 * Converts SmallMolecule interactors following data best practices
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/08/12</pre>
 */

public class SmallMoleculeConverter extends AbstractEnricher{

    public final static String STANDARD_INCHI_KEY = "standard inchi key";
    public final static String INCHI_MI_REF ="MI:2010";

    public SmallMoleculeConverter(CrossReferenceConverter<InteractorXref> xrefConv, AliasConverter alisConv){
        super(xrefConv, alisConv);
    }

    public SmallMoleculeConverter(CrossReferenceConverter<InteractorXref> xrefConv, AliasConverter alisConv, String defaultInstitution) {
        super(xrefConv, alisConv, defaultInstitution);
    }

    /**
     * Enrich the mitab interactor following data best practices for small molecules
     * @param smallMolecule
     * @param mitabInteractor
     * @return the standard InchiKey for the small molecule. Can be null if no standard inchi key available
     */
    public String enrichSmallMoleculeFromIntact(SmallMolecule smallMolecule, Interactor mitabInteractor){

        if (smallMolecule != null && mitabInteractor != null){
            Collection<InteractorXref> interactorXrefs = smallMolecule.getXrefs();
            Collection<InteractorAlias> aliases = smallMolecule.getAliases();

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
    protected void processAccessionAndDisplay(uk.ac.ebi.intact.model.Interactor mol, Interactor mitabInteractor, boolean hasFoundCHEBIIdentity) {
        // if it is a small molecule from CHEBI, Assume the short label is the molecule name
        // aliases
        if (hasFoundCHEBIIdentity){
            // the shortlabel is the display short
            psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvDatabase.PSI_MI, mol.getShortLabel(), InteractorConverter.DISPLAY_SHORT );
            mitabInteractor.getAliases().add(displayShort);
            // the interactor unique id is the display long
            psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvDatabase.PSI_MI, mitabInteractor.getIdentifiers().iterator().next().getIdentifier(), InteractorConverter.DISPLAY_LONG  );
            mitabInteractor.getAliases().add(displayLong);

            // convert ac as identity or secondary identifier
            if (mol.getAc() != null){

                // add ac as alternative id
                CrossReference acField = createCrossReferenceFromAc(mol);
                mitabInteractor.getAlternativeIdentifiers().add(acField);
            }
        }
        // no CHEBI identity
        else{

            // ac will be identifier and shortlabel is an alias
            if(mol.getAc() != null){

                // add shortlabel as display short as well
                psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvDatabase.PSI_MI, mol.getShortLabel(), InteractorConverter.DISPLAY_SHORT  );
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
                CrossReference id = new CrossReferenceImpl( defaultInstitution, mol.getShortLabel());
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
                    // first CHEBI identity
                    if (!hasFoundIdentity && ref.getCvDatabase() != null && CvDatabase.CHEBI_MI_REF.equals(ref.getCvDatabase().getIdentifier())){

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
                            mitabInteractor.getAlternativeIdentifiers().add(identity);
                        }
                    }
                }
                // secondary acs are alternative identifiers
                else if (ref.getCvXrefQualifier() != null && (CvXrefQualifier.SECONDARY_AC_MI_REF.equals(ref.getCvXrefQualifier().getIdentifier())
                        || intactSecondary.equals(ref.getCvXrefQualifier().getShortLabel()))){
                    CrossReference identity = xRefConverter.createCrossReference(ref, false);
                    if (identity != null){
                        mitabInteractor.getAlternativeIdentifiers().add(identity);
                    }
                }
                // other xrefs
                else {
                    CrossReference xref = xRefConverter.createCrossReference(ref, true);
                    if (xref != null){
                        mitabInteractor.getXrefs().add(xref);
                    }
                }
            }
        }
        return hasFoundIdentity;
    }

    public String extractStandardInchiKeyFrom(SmallMolecule interactor) {

        // find INCHI key
        final Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(interactor, INCHI_MI_REF);// INCHI_MI_REF

        if (annotation != null){
            return  annotation.getAnnotationText();
        }

        return null;
    }
}
