package uk.ac.ebi.intact.psimitab.converters.enrichers;

import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.InteractorAlias;
import uk.ac.ebi.intact.model.InteractorXref;
import uk.ac.ebi.intact.psimitab.converters.converters.AliasConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.CrossReferenceConverter;

import java.util.Collection;

/**
 * Abstract class for the enrichers following data best practices
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/08/12</pre>
 */

public abstract class AbstractEnricher {

    protected CrossReferenceConverter<InteractorXref> xRefConverter;
    protected AliasConverter aliasConverter;
    protected String defaultInstitution = CvDatabase.INTACT;
    protected String intactSecondary = "intact-secondary";

    public AbstractEnricher(CrossReferenceConverter<InteractorXref> xrefConv, AliasConverter alisConv){
        xRefConverter = xrefConv != null ? xrefConv : new CrossReferenceConverter<InteractorXref>();
        aliasConverter = alisConv != null ? alisConv : new AliasConverter();
    }

    public AbstractEnricher(CrossReferenceConverter<InteractorXref> xrefConv, AliasConverter alisConv, String defaultInstitution){
        xRefConverter = xrefConv != null ? xrefConv : new CrossReferenceConverter<InteractorXref>();
        aliasConverter = alisConv != null ? alisConv : new AliasConverter();
        if (defaultInstitution != null){
            this.defaultInstitution = defaultInstitution;
        }
    }

    protected CrossReference createCrossReferenceFromAc(uk.ac.ebi.intact.model.Interactor mol) {
        CrossReference acField = new CrossReferenceImpl();

        String db = defaultInstitution;

        acField.setDatabase(db);
        acField.setIdentifier(mol.getAc());

        return acField;
    }

    protected void processAliases(Interactor mitabInteractor, Collection<InteractorAlias> aliases) {
        for (InteractorAlias alias : aliases){
            psidev.psi.mi.tab.model.Alias aliasField = aliasConverter.intactToMitab(alias);

            if (aliasField != null){
                mitabInteractor.getAliases().add(aliasField);
            }
        }
    }

    protected boolean processXrefs(Interactor mitabInteractor, Collection<InteractorXref> interactorXrefs) {
        boolean hasFoundIdentity = false;

        if (!interactorXrefs.isEmpty()){

            // convert xrefs, and identity
            for (InteractorXref ref : interactorXrefs){

                // identity xrefs
                if (ref.getCvXrefQualifier() != null && CvXrefQualifier.IDENTITY_MI_REF.equals(ref.getCvXrefQualifier().getIdentifier())){
                    // first ddbj/embl/genbank identity
                    if (!hasFoundIdentity){

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

    protected abstract void processAccessionAndDisplay(uk.ac.ebi.intact.model.Interactor mol, Interactor mitabInteractor, boolean hasFoundIdentity);
}
