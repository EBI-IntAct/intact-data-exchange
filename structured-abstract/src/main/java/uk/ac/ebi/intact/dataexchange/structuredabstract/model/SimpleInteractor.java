package uk.ac.ebi.intact.dataexchange.structuredabstract.model;

import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.Interactor;
import psidev.psi.mi.jami.model.Participant;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.utils.AliasUtils;
import psidev.psi.mi.jami.utils.XrefUtils;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractor;

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

    public SimpleInteractor(Participant component){

        if (component == null){
            throw new IllegalArgumentException("The component cannot be null.");
        }

        Interactor interactor = component.getInteractor();
        if (interactor == null){
            throw new IllegalArgumentException("The component must have a non null interactor.");
        }

        // ac
        this.ac = (interactor instanceof IntactInteractor) ? ((IntactInteractor)interactor).getAc() : interactor.getShortName();

        // shortlabel is interactor shortlabel
        this.shortName = interactor.getShortName();

        Xref uniprot = null;
        Xref refseq = null;
        Xref lastIdentifier = null;

        // xref is uniprot/refseq or identity
        for (Xref xref : interactor.getIdentifiers()) {
            if (XrefUtils.isXrefFromDatabase(xref, Xref.UNIPROTKB_MI, Xref.UNIPROTKB)
                    && XrefUtils.doesXrefHaveQualifier(xref, Xref.IDENTITY_MI, Xref.IDENTITY)){
                uniprot = xref;
            }
            else if (XrefUtils.isXrefFromDatabase(xref, Xref.REFSEQ_MI, Xref.REFSEQ)
                    && XrefUtils.doesXrefHaveQualifier(xref, Xref.IDENTITY_MI, Xref.IDENTITY)){
                refseq = xref;
            }
            else if (XrefUtils.doesXrefHaveQualifier(xref, Xref.IDENTITY_MI, Xref.IDENTITY)){
                lastIdentifier = xref;
            }
        }

        // alias is gene name
        for (Alias alias : interactor.getAliases()) {
            if (AliasUtils.doesAliasHaveType(alias, Alias.GENE_NAME_MI, Alias.GENE_NAME)){
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
        for (Object alias : component.getAliases()) {
            if (AliasUtils.doesAliasHaveType((Alias)alias, Alias.AUTHOR_ASSIGNED_NAME_MI, Alias.AUTHOR_ASSIGNED_NAME)) {
                this.authorAssignedName = ((Alias)alias).getName();
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
