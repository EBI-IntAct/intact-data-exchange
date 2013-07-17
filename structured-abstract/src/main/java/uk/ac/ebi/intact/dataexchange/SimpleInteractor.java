package uk.ac.ebi.intact.dataexchange;

import uk.ac.ebi.intact.model.*;

/**
 * A simple interactor has a name and a Xref
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15/07/13</pre>
 */

public class SimpleInteractor {

    public static final String IPI = "MI:0675";

    private String ac;
    private String shortName;
    private Xref xref;
    private String authorAssignedName;

    public SimpleInteractor(Component component){

        if (component == null){
            throw new IllegalArgumentException("The component cannot be null.");
        }

        Interactor interactor = component.getInteractor();
        if (interactor == null){
            throw new IllegalArgumentException("The component must have a non null interactor.");
        }

        // ac
        this.ac = interactor.getAc();

        // shortlabel is interactor shortlabel
        this.shortName = interactor.getShortLabel();

        Xref uniprot = null;
        Xref refseq = null;
        Xref lastIdentifier = null;

        // xref is uniprot/refseq or identity
        for (Xref xref : interactor.getXrefs()) {
            if (xref.getCvXrefQualifier() != null && xref.getCvXrefQualifier().getIdentifier() != null
                    && xref.getCvDatabase() != null && xref.getCvDatabase().getIdentifier() != null
                    && xref.getCvXrefQualifier().getIdentifier().equals(CvXrefQualifier.IDENTITY_MI_REF)){
                if (xref.getCvDatabase().getIdentifier().equals(
                        CvDatabase.UNIPROT_MI_REF)) {
                    uniprot = xref;
                    break;
                }
                else if (xref.getCvDatabase().getIdentifier()
                        .equals(CvDatabase.REFSEQ_MI_REF) || xref
                        .getCvDatabase().getIdentifier()
                        .equals(IPI)) {
                    refseq = xref;
                }
                else {
                    lastIdentifier = xref;
                }
            }
        }

        // alias is gene name
        for (Alias alias : interactor.getAliases()) {
            if (alias.getCvAliasType() != null && alias.getCvAliasType().getIdentifier() != null
                    && alias.getCvAliasType().getIdentifier().equalsIgnoreCase(CvAliasType.GENE_NAME_MI_REF)
                    && alias.getName() != null && alias.getName().length() > 0){
                this.shortName = alias.getName();
                break;
            }
        }

        if (uniprot != null){
           this.xref = uniprot;
        }
        else if (refseq != null){
           this.xref = refseq;
        }
        else {
            this.xref = lastIdentifier;
        }

        // get author assigned name
        for (ComponentAlias alias : component.getAliases()) {
            if (alias.getCvAliasType() != null && alias.getCvAliasType().getIdentifier() != null
                && alias.getCvAliasType().getIdentifier().equals(
                    CvAliasType.AUTHOR_ASSIGNED_NAME_MI_REF) && alias.getName() != null) {
                this.authorAssignedName = alias.getName();
                break;
            }
        }
    }

    public String getShortName() {
        return shortName;
    }

    public Xref getXref() {
        return xref;
    }

    public String getAuthorAssignedName() {
        return authorAssignedName;
    }

    public String getAc() {
        return ac;
    }
}
