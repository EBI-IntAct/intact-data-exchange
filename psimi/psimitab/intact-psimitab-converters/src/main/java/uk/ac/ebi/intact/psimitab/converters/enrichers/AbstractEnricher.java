package uk.ac.ebi.intact.psimitab.converters.enrichers;

import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.InstitutionUtils;
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

    public AbstractEnricher(CrossReferenceConverter<InteractorXref> xrefConv, AliasConverter alisConv){
        xRefConverter = xrefConv != null ? xrefConv : new CrossReferenceConverter<InteractorXref>();
        aliasConverter = alisConv != null ? alisConv : new AliasConverter();
    }

    protected CrossReference createCrossReferenceFromAc(uk.ac.ebi.intact.model.Interactor mol) {
        CrossReference acField = new CrossReferenceImpl();

        String db = CvDatabase.INTACT;

        acField.setDatabase(db);
        acField.setIdentifier(mol.getAc());

        if (mol.getOwner() != null){
            Institution institution = mol.getOwner();

            CvDatabase database = InstitutionUtils.retrieveCvDatabase(IntactContext.getCurrentInstance(), institution);

            if (database != null && database.getShortLabel() != null){
                acField.setDatabase(database.getShortLabel());
            }
        }
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

    protected abstract void processAccessionAndDisplay(uk.ac.ebi.intact.model.Interactor mol, Interactor mitabInteractor, boolean hasFoundIdentity);
}
