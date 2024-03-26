package uk.ac.ebi.intact.psimitab.converters.enrichers;

import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactAlias;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractor;
import uk.ac.ebi.intact.jami.model.extension.ParticipantEvidenceXref;
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

public abstract class AbstractEnricher<T extends IntactInteractor> {

    protected CrossReferenceConverter<ParticipantEvidenceXref> xRefConverter;
    protected AliasConverter aliasConverter;
    protected String defaultInstitution = "intact";
    protected String intactSecondary = "intact-secondary";

    public AbstractEnricher(CrossReferenceConverter<ParticipantEvidenceXref> xrefConv, AliasConverter alisConv){
        xRefConverter = xrefConv != null ? xrefConv : new CrossReferenceConverter<>();
        aliasConverter = alisConv != null ? alisConv : new AliasConverter();
    }

    public AbstractEnricher(CrossReferenceConverter<ParticipantEvidenceXref> xrefConv, AliasConverter alisConv, String defaultInstitution){
        xRefConverter = xrefConv != null ? xrefConv : new CrossReferenceConverter<>();
        aliasConverter = alisConv != null ? alisConv : new AliasConverter();
        if (defaultInstitution != null){
            this.defaultInstitution = defaultInstitution;
        }
    }

    protected CrossReference createCrossReferenceFromAc(T mol) {
        CrossReference acField = new CrossReferenceImpl();

        String db = defaultInstitution;

        acField.setDatabase(db);
        acField.setIdentifier(mol.getAc());

        return acField;
    }

    protected void processAliases(Interactor mitabInteractor, Collection<Alias> aliases) {
        for (Alias alias : aliases){
            psidev.psi.mi.tab.model.Alias aliasField = aliasConverter.intactToMitab((AbstractIntactAlias) alias);

            if (aliasField != null){
                mitabInteractor.getAliases().add(aliasField);
            }
        }
    }

    protected boolean processXrefs(Interactor mitabInteractor, Collection<Xref> interactorXrefs) {
        boolean hasFoundIdentity = false;

        if (!interactorXrefs.isEmpty()){

            // convert xrefs, and identity
            for (Xref ref : interactorXrefs){

                // identity xrefs
                if (ref.getQualifier() != null && Xref.IDENTITY_MI.equals(ref.getQualifier().getMIIdentifier())){
                    // first ddbj/embl/genbank identity
                    if (!hasFoundIdentity){

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

    protected abstract void processAccessionAndDisplay(T mol, Interactor mitabInteractor, boolean hasFoundIdentity);
}
