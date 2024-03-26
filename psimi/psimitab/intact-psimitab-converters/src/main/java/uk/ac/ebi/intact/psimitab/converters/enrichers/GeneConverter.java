package uk.ac.ebi.intact.psimitab.converters.enrichers;

import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.tab.model.AliasImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactAlias;
import uk.ac.ebi.intact.jami.model.extension.IntactGene;
import uk.ac.ebi.intact.jami.model.extension.ParticipantEvidenceXref;
import uk.ac.ebi.intact.psimitab.converters.converters.AliasConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.CrossReferenceConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.InteractorConverter;

import java.util.Collection;

/**
 * Converts Gene interactor following data best practices
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/08/12</pre>
 */

public class GeneConverter extends AbstractEnricher<IntactGene> {

    private static final String ENSEMBL_GENOME_MI = "MI:1013";
    private boolean hasFoundDisplayShort = false;

    public GeneConverter(CrossReferenceConverter<ParticipantEvidenceXref> xrefConv, AliasConverter alisConv){
        super(xrefConv, alisConv);
    }

    public GeneConverter(CrossReferenceConverter<ParticipantEvidenceXref> xrefConv, AliasConverter alisConv, String defaultInstitution) {
        super(xrefConv, alisConv, defaultInstitution);
    }

    /**
     * Enrich the mitab interactor following data best practices for nucleic acids
     * @param gene
     * @param mitabInteractor
     * @return the standard InchiKey for the small molecule. Can be null if no standard inchi key available
     */
    public void enrichGeneFromIntact(IntactGene gene, Interactor mitabInteractor){
        hasFoundDisplayShort = false;

        if (gene != null && mitabInteractor != null){
            Collection<Xref> interactorXrefs = gene.getXrefs();
            Collection<Alias> aliases = gene.getAliases();

            // xrefs
            boolean hasFoundENSEMBLIdentity = processXrefs(mitabInteractor, interactorXrefs);

            // convert aliases
            if (!aliases.isEmpty()){

                processAliases(mitabInteractor, aliases);
            }

            // ac and display long
            processAccessionAndDisplay(gene, mitabInteractor, hasFoundENSEMBLIdentity);
        }
    }

    @Override
    protected void processAccessionAndDisplay(IntactGene mol, Interactor mitabInteractor, boolean hasFoundEMBLIdentity) {
        // do display short has been found so far, we need to add the shortlabel as display short
        if (!hasFoundDisplayShort){
            psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvTerm.PSI_MI, mol.getShortName(), InteractorConverter.DISPLAY_SHORT  );
            mitabInteractor.getAliases().add(displayLong);
        }

        // if it is a small molecule from CHEBI, Assume the short label is the molecule name
        // aliases
        if (hasFoundEMBLIdentity){
            String identifier = mitabInteractor.getIdentifiers().iterator().next().getIdentifier();
            // we have a display short so the current shortlabel has not been exported as display_short
            if (hasFoundDisplayShort){
                // the shortlabel is a ENSEMBL shortlabel as well
                psidev.psi.mi.tab.model.Alias shortLabel = new AliasImpl( Xref.ENSEMBL, mol.getShortName(), InteractorConverter.SHORTLABEL );
                mitabInteractor.getAliases().add(shortLabel);
            }

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
        // no CHEBI identity
        else{

            // ac will be identifier and shortlabel is an alias
            if(mol.getAc() != null){
                // add shortlabel as intact alias only if the display_short is not the shortlabel to not duplicate aliases
                if (hasFoundDisplayShort){
                    psidev.psi.mi.tab.model.Alias altId = new AliasImpl( defaultInstitution, mol.getShortName(), InteractorConverter.SHORTLABEL  );
                    mitabInteractor.getAliases().add(altId);
                }

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
                    if (!hasFoundIdentity && ref.getDatabase() != null && (
                            Xref.ENSEMBL_MI.equals(ref.getDatabase().getMIIdentifier()) || Xref.ENTREZ_GENE_MI.equals(ref.getDatabase().getMIIdentifier())
                    || ENSEMBL_GENOME_MI.equals(ref.getDatabase().getMIIdentifier()))){

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

    @Override
    protected void processAliases(Interactor mitabInteractor, Collection<Alias> aliases) {
        for (Alias alias : aliases){
            psidev.psi.mi.tab.model.Alias aliasField = aliasConverter.intactToMitab((AbstractIntactAlias) alias);

            if (aliasField != null){
                mitabInteractor.getAliases().add(aliasField);

                // create display short which should be gene name or gene name synonym
                if (Alias.GENE_NAME.equals(aliasField.getAliasType())){
                    hasFoundDisplayShort = true;
                    psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvTerm.PSI_MI, aliasField.getName(),InteractorConverter.DISPLAY_SHORT );
                    mitabInteractor.getAliases().add(displayShort);
                }
            }
        }
    }
}
