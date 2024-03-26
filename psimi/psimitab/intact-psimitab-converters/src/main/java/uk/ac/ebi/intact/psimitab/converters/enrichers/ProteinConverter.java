package uk.ac.ebi.intact.psimitab.converters.enrichers;

import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.tab.model.AliasImpl;
import psidev.psi.mi.tab.model.ChecksumImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.irefindex.seguid.RigDataModel;
import uk.ac.ebi.intact.irefindex.seguid.RogidGenerator;
import uk.ac.ebi.intact.irefindex.seguid.SeguidException;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactAlias;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.jami.model.extension.ParticipantEvidenceXref;
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

public class ProteinConverter extends AbstractEnricher<IntactProtein> {

    protected RogidGenerator rogidGenerator;
    public static final String UNKNOWN_TAXID = "-3";
    public final static String ROGID = "rogid";
    private boolean hasFoundDisplayShort;

    public ProteinConverter(CrossReferenceConverter<ParticipantEvidenceXref> xrefConv, AliasConverter alisConv){
        super(xrefConv, alisConv);
        this.rogidGenerator = new RogidGenerator();
    }

    public ProteinConverter(CrossReferenceConverter<ParticipantEvidenceXref> xrefConv, AliasConverter alisConv, String defaultInstitution) {
        super(xrefConv, alisConv, defaultInstitution);
        this.rogidGenerator = new RogidGenerator();
    }

    /**
     * Enrich the mitab interactor following data best practices for proteins
     * @param polymer
     * @param mitabInteractor
     * @return the RigDataModel for the protein rogid. Can be null if no sequence available
     */
    public RigDataModel enrichProteinFromIntact(IntactProtein polymer, Interactor mitabInteractor){
        hasFoundDisplayShort = false;

        if (polymer != null && mitabInteractor != null){
            Collection<Xref> interactorXrefs = polymer.getXrefs();
            Collection<Alias> aliases = polymer.getAliases();

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
    protected void processAccessionAndDisplay(IntactProtein polymer, Interactor mitabInteractor, boolean hasFoundUniprotIdentity) {
        // if it is a protein from uniprot (found uniprot/refseq identity. If we have refseq, we should have uniprot), assume the short label is the uniprot ID and add it to the
        // aliases
        if (hasFoundUniprotIdentity){
            // the shortlabel is the uniprot ID
            psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvTerm.PSI_MI, polymer.getShortName(), InteractorConverter.DISPLAY_LONG );
            mitabInteractor.getAliases().add(displayLong);


            // convert ac as identity or secondary identifier
            if (polymer.getAc() != null){

                // add ac as alternative id
                CrossReference acField = createCrossReferenceFromAc(polymer);
                mitabInteractor.getAlternativeIdentifiers().add(0, acField);
            }
        }
        // no uniprot identity
        else{

            // ac will be identifier and shortlabel is an alias
            if(polymer.getAc() != null){
                if (!hasFoundDisplayShort){
                    // add shortlabel as intact alias
                    psidev.psi.mi.tab.model.Alias altId = new AliasImpl( CvTerm.PSI_MI, polymer.getShortName(),InteractorConverter.DISPLAY_SHORT );
                    mitabInteractor.getAliases().add(altId);
                }

                // add ac as unique id and add it as display_long as well
                CrossReference acField = createCrossReferenceFromAc(polymer);
                mitabInteractor.getIdentifiers().add(acField);

                // add ac as psi display_long alias
                psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvTerm.PSI_MI, polymer.getAc(),InteractorConverter.DISPLAY_LONG );
                mitabInteractor.getAliases().add(displayLong);
            }
            // the shortlabel will be identifier because we need an identifier and will be displayLong as well
            else {
                CrossReference id = new CrossReferenceImpl( defaultInstitution, polymer.getShortName());
                mitabInteractor.getIdentifiers().add(id);

                if (!hasFoundDisplayShort){
                    // add shortlabel as intact alias
                    psidev.psi.mi.tab.model.Alias altId = new AliasImpl( CvTerm.PSI_MI, polymer.getShortName(),InteractorConverter.DISPLAY_SHORT );
                    mitabInteractor.getAliases().add(altId);
                }
                else {
                    psidev.psi.mi.tab.model.Alias displayLong = new AliasImpl( CvTerm.PSI_MI, polymer.getShortName(),InteractorConverter.DISPLAY_LONG );
                    mitabInteractor.getAliases().add(displayLong);
                }
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
                    // first uniprot or refseq identity
                    if (!hasFoundIdentity && ref.getDatabase() != null && (Xref.UNIPROTKB_MI.equals(ref.getDatabase().getMIIdentifier())
                            || Xref.REFSEQ_MI.equals(ref.getDatabase().getMIIdentifier()))){

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
                    psidev.psi.mi.tab.model.Alias displayShort = new AliasImpl( CvTerm.PSI_MI, aliasField.getName(),InteractorConverter.DISPLAY_SHORT );
                    mitabInteractor.getAliases().add(displayShort);
                    hasFoundDisplayShort = true;
                }
            }
        }
    }

    public RigDataModel buildRigDataModel(IntactProtein interactor) {

        String taxid;

        if (interactor.getOrganism() != null) {
            taxid = String.valueOf(interactor.getOrganism().getTaxId());
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
