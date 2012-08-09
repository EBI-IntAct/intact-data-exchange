package uk.ac.ebi.intact.psimitab.converters.enrichers;

import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.psimitab.converters.AliasConverter;
import uk.ac.ebi.intact.psimitab.converters.CrossReferenceConverter;
import uk.ac.ebi.intact.psimitab.converters.InteractorConverter;

import java.util.Collection;

/**
 * Converts Gene interactor following data best practices
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/08/12</pre>
 */

public class GeneConverter extends AbstractEnricher{

    private static final String ENSEMBL_GENOME_MI = "MI:1013";

    public GeneConverter(CrossReferenceConverter<InteractorXref> xrefConv, AliasConverter alisConv){
        super(xrefConv, alisConv);
    }

    /**
     * Enrich the mitab interactor following data best practices for nucleic acids
     * @param gene
     * @param mitabInteractor
     * @return the standard InchiKey for the small molecule. Can be null if no standard inchi key available
     */
    public void enrichGeneFromIntact(uk.ac.ebi.intact.model.Interactor gene, Interactor mitabInteractor){

        if (gene != null && mitabInteractor != null){
            Collection<InteractorXref> interactorXrefs = gene.getXrefs();
            Collection<InteractorAlias> aliases = gene.getAliases();

            // xrefs
            boolean hasFoundENSEMBLIdentity = processXrefs(mitabInteractor, interactorXrefs);

            // ac and display long
            processAccessionAndDisplay(gene, mitabInteractor, hasFoundENSEMBLIdentity);

            // convert aliases
            if (!aliases.isEmpty()){

                processAliases(mitabInteractor, aliases);
            }

            // uses crc64 for checksum
            if (gene instanceof Polymer){
                Polymer polymer = (Polymer) gene;
                if (polymer.getCrc64() != null){
                    Checksum checksum = new ChecksumImpl(InteractorConverter.CRC64, polymer.getCrc64());
                    mitabInteractor.getChecksums().add(checksum);
                }
            }
        }
    }

    @Override
    protected void processAccessionAndDisplay(uk.ac.ebi.intact.model.Interactor mol, Interactor mitabInteractor, boolean hasFoundEMBLIdentity) {
        // if it is a small molecule from CHEBI, Assume the short label is the molecule name
        // aliases
        if (hasFoundEMBLIdentity){
            String identifier = mitabInteractor.getIdentifiers().iterator().next().getIdentifier();
            // the shortlabel is a ENSEMBL shortlabel as well
            psidev.psi.mi.tab.model.Alias shortLabel = new AliasImpl( CvDatabase.ENSEMBL, mol.getShortLabel(), InteractorConverter.SHORTLABEL );
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
        // no CHEBI identity
        else{

            // ac will be identifier and shortlabel is an alias
            if(mol.getAc() != null){
                // add shortlabel as intact alias
                psidev.psi.mi.tab.model.Alias altId = new AliasImpl( CvDatabase.INTACT, mol.getShortLabel(), InteractorConverter.SHORTLABEL  );
                mitabInteractor.getAliases().add(altId);

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
                    if (!hasFoundIdentity && ref.getCvDatabase() != null && (
                            CvDatabase.ENSEMBL_MI_REF.equals(ref.getCvDatabase().getIdentifier()) || CvDatabase.ENTREZ_GENE_MI_REF.equals(ref.getCvDatabase().getIdentifier())
                    || ENSEMBL_GENOME_MI.equals(ref.getCvDatabase().getIdentifier()))){

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

    @Override
    protected void processAliases(Interactor mitabInteractor, Collection<InteractorAlias> aliases) {
        for (InteractorAlias alias : aliases){
            psidev.psi.mi.tab.model.Alias aliasField = aliasConverter.intactToMitab(alias);

            if (aliasField != null){
                mitabInteractor.getAliases().add(aliasField);

                // create display short which should be gene name or gene name synonym
                if (CvAliasType.GENE_NAME.equals(aliasField.getAliasType()) || CvAliasType.GENE_NAME_SYNONYM.equals(aliasField.getAliasType())){
                    psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvDatabase.PSI_MI, aliasField.getName(),InteractorConverter.DISPLAY_SHORT );
                    mitabInteractor.getAliases().add(displayShort);
                }
            }
        }
    }
}
