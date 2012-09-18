package uk.ac.ebi.intact.psimitab.converters.enrichers;

import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.irefindex.seguid.RigDataModel;
import uk.ac.ebi.intact.irefindex.seguid.RogidGenerator;
import uk.ac.ebi.intact.irefindex.seguid.SeguidException;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.psimitab.converters.converters.AliasConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.CrossReferenceConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.InteractorConverter;

import java.util.Collection;

/**
 * Converts protein interactors
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/08/12</pre>
 */

public class ProteinConverter extends AbstractEnricher{

    protected RogidGenerator rogidGenerator;
    public static final String UNKNOWN_TAXID = "-3";
    public final static String ROGID = "rogid";

    public ProteinConverter(CrossReferenceConverter<InteractorXref> xrefConv, AliasConverter alisConv){
        super(xrefConv, alisConv);
        this.rogidGenerator = new RogidGenerator();
    }

    /**
     * Enrich the mitab interactor following data best practices for proteins
     * @param polymer
     * @param mitabInteractor
     * @return the RigDataModel for the protein rogid. Can be null if no sequence available
     */
    public RigDataModel enrichProteinFromIntact(Polymer polymer, Interactor mitabInteractor){

        if (polymer != null && mitabInteractor != null){
            Collection<InteractorXref> interactorXrefs = polymer.getXrefs();
            Collection<InteractorAlias> aliases = polymer.getAliases();

            // xrefs
            boolean hasFoundUniprotIdentity = processXrefs(mitabInteractor, interactorXrefs);

            // ac and display long
            processAccessionAndDisplay(polymer, mitabInteractor, hasFoundUniprotIdentity);

            // convert aliases
            if (!aliases.isEmpty()){

                processAliases(mitabInteractor, aliases);
            }

            // convert checksum (rogid/crogid : only if sequence available)
            if (polymer.getSequence() != null){
                RigDataModel rig = buildRigDataModel(polymer);
                if (rig != null){
                    try {
                        final String rogA = rogidGenerator.calculateRogid(rig.getSequence(), rig.getTaxid());
                        mitabInteractor.getChecksums().add(
                                new ChecksumImpl(ROGID, rogA));

                    } catch (SeguidException e) {
                        throw new RuntimeException("An error occured while generating ROG identifier for " +
                                "interactor " + polymer.getAc(), e);
                    }
                }

                return rig;
            }
        }

        return null;
    }

    @Override
    protected void processAccessionAndDisplay(uk.ac.ebi.intact.model.Interactor polymer, Interactor mitabInteractor, boolean hasFoundUniprotIdentity) {
        // if it is a protein from uniprot (found uniprot/refseq identity. If we have refseq, we should have uniprot), assume the short label is the uniprot ID and add it to the
        // aliases
        if (hasFoundUniprotIdentity){
            // the shortlabel is the uniprot ID
            psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvDatabase.PSI_MI, polymer.getShortLabel(), InteractorConverter.DISPLAY_LONG );
            mitabInteractor.getAliases().add(displayLong);
            // the shortlabel is a uniprot shortlabel as well
            psidev.psi.mi.tab.model.Alias shortLabel = new AliasImpl( CvDatabase.UNIPROT, polymer.getShortLabel(), InteractorConverter.SHORTLABEL );
            mitabInteractor.getAliases().add(shortLabel);

            // convert ac as identity or secondary identifier
            if (polymer.getAc() != null){

                // add ac as alternative id
                CrossReference acField = createCrossReferenceFromAc(polymer);
                mitabInteractor.getAlternativeIdentifiers().add(acField);
            }
        }
        // no uniprot identity
        else{

            // ac will be identifier and shortlabel is an alias
            if(polymer.getAc() != null){
                // add shortlabel as intact alias
                psidev.psi.mi.tab.model.Alias altId = new AliasImpl( CvDatabase.INTACT, polymer.getShortLabel(),InteractorConverter.SHORTLABEL );
                mitabInteractor.getAliases().add(altId);

                // add ac as unique id and add it as display_long as well
                CrossReference acField = createCrossReferenceFromAc(polymer);
                mitabInteractor.getIdentifiers().add(acField);

                // add ac as psi display_long alias
                psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvDatabase.PSI_MI, polymer.getAc(),InteractorConverter.DISPLAY_LONG );
                mitabInteractor.getAliases().add(displayLong);
            }
            // the shortlabel will be identifier because we need an identifier and will be displayLong as well
            else {
                CrossReference id = new CrossReferenceImpl( CvDatabase.INTACT, polymer.getShortLabel());
                mitabInteractor.getIdentifiers().add(id);

                psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvDatabase.PSI_MI, polymer.getShortLabel(),InteractorConverter.DISPLAY_LONG );
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
                    // first uniprot or refseq identity
                    if (!hasFoundIdentity && ref.getCvDatabase() != null && (CvDatabase.UNIPROT_MI_REF.equals(ref.getCvDatabase().getIdentifier())
                            || CvDatabase.REFSEQ_MI_REF.equals(ref.getCvDatabase().getIdentifier()))){

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

    @Override
    protected void processAliases(Interactor mitabInteractor, Collection<InteractorAlias> aliases) {
        for (InteractorAlias alias : aliases){
            psidev.psi.mi.tab.model.Alias aliasField = aliasConverter.intactToMitab(alias);

            if (aliasField != null){
                mitabInteractor.getAliases().add(aliasField);

                // create display short which should be gene name or gene name synonym
                if (CvAliasType.GENE_NAME.equals(aliasField.getAliasType())){
                    psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvDatabase.PSI_MI, aliasField.getName(),InteractorConverter.DISPLAY_SHORT );
                    mitabInteractor.getAliases().add(displayShort);
                }
            }
        }
    }

    public RigDataModel buildRigDataModel(Polymer interactor) {

        String taxid;

        if (interactor.getBioSource() != null) {
            taxid = interactor.getBioSource().getTaxId();
        } else {
            taxid = UNKNOWN_TAXID;
        }

        String seq = interactor.getSequence();

        if (seq == null) {
            return null;
        }

        return new RigDataModel(seq, taxid);
    }
}
