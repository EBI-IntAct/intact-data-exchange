package uk.ac.ebi.intact.dataexchange.psimi.xml.dbimporter;

import psidev.psi.mi.xml.PsimiXmlForm;
import psidev.psi.mi.xml.PsimiXmlLightweightWriter;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.converter.ConverterContext;
import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IdSequenceGenerator;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvInteractionType;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.CvXrefQualifier;

import java.io.File;
import java.util.*;

/**
 * Class to convert PDB file to a PDB complex file for importing in the database
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>03/12/13</pre>
 */

public class PDBComplexXmlConverter {

    public static void main(String[] args) throws Exception {

        if (args.length < 3) {
            System.err.println("Usage: PDBComplexXmlConverter <input.file> <output.file> <publication.pubmed>");
            System.exit(1);
        }

        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);
        String complexPubmedId = args[2];

        PsimiXmlReader reader = new PsimiXmlReader();
        PsimiXmlLightweightWriter writer = new PsimiXmlLightweightWriter(outputFile, PsimiXmlVersion.VERSION_254);

        writer.writeStartDocument();

        EntrySet entrySet = reader.read(inputFile);

        Map<ExperimentDescription, ExperimentDescription> experiments = new TreeMap<ExperimentDescription, ExperimentDescription>(new ExperimentComparator());
        Map<Interactor, Interactor> interactors = new TreeMap<Interactor, Interactor>(new InteractorComparator());
        IdSequenceGenerator idGenerator = IdSequenceGenerator.getInstance();

        for (Entry entry : entrySet.getEntries()){
            writer.writeStartEntry(entry.getSource(), null);

            Iterator<Interaction> interactionIterator = entry.getInteractions().iterator();
            DbReference publication = new DbReference(CvDatabase.PUBMED, CvDatabase.PUBMED_MI_REF, complexPubmedId, CvXrefQualifier.PRIMARY_REFERENCE, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF);

            while (interactionIterator.hasNext()){
                ConverterContext.getInstance().getConverterConfig().setXmlForm(PsimiXmlForm.FORM_EXPANDED);

                Interaction interaction = interactionIterator.next();
                interaction.setId(idGenerator.nextId());

                // shortlabel is recommended name
                interaction.getNames().getAliases().add(new Alias(interaction.getNames().getShortLabel(), "complex recommended name", "MI:1315" ));

                // add curated complex
                interaction.getAttributes().add(new Attribute("curated-complex",interaction.getNames().getShortLabel() + ": PDBe XML import"));

                // add an annotation with id
                interaction.getAttributes().add(new Attribute(CvTopic.COMMENT_MI_REF, CvTopic.COMMENT,"PSI-XML 2.5 interaction id "+interaction.getId()));

                // add experimental role neutral component
                ExperimentalRole role = PsiFactory.createExperimentalRole("MI:0497","neutral component");
                // add bio role unspecified
                BiologicalRole bioRole = PsiFactory.createBiologicalRole("MI:0499","unspecified role");
                // add missing host organism
                Organism host = null;
                int taxid = 0;
                for (Participant participant : interaction.getParticipants()){
                    participant.setId(idGenerator.nextId());
                    // add exp role
                    if (participant.getExperimentalRoles().isEmpty()){
                        participant.getExperimentalRoles().add(role);
                    }
                    // add bio role
                    if (participant.getBiologicalRole() == null){
                        participant.setBiologicalRole(bioRole);
                    }

                    // check organism
                    if (participant.getInteractor() != null){
                        Interactor interactor = participant.getInteractor();
                        if (interactor.getOrganism() != null){
                            if (taxid == 0){
                                host = interactor.getOrganism();
                                taxid = interactor.getOrganism().getNcbiTaxId();
                            }
                            else if (taxid != interactor.getOrganism().getNcbiTaxId()){
                                System.out.println("ERROR: two interactors from the same complex do have different taxids." + taxid + ", "+interactor.getOrganism().getNcbiTaxId());
                                host = new Organism();
                                host.setNcbiTaxId(-4);
                                host.setNames(new Names());
                                host.getNames().setShortLabel("in vivo");
                            }
                        }
                        if (interactors.containsKey(interactor)){
                            participant.setInteractor(interactors.get(interactor));
                        }
                        else{
                            interactor.setId(idGenerator.nextId());
                            interactors.put(interactor, interactor);
                        }
                    }
                }

                // direct interaction if two participants, otherwise physical
                if (interaction.getInteractionTypes().isEmpty()){
                    if (interaction.getParticipants().size() == 2){
                        interaction.getInteractionTypes().add(PsiFactory.createInteractionType(CvInteractionType.DIRECT_INTERACTION_MI_REF, CvInteractionType.DIRECT_INTERACTION));
                    }
                    else{
                        interaction.getInteractionTypes().add(PsiFactory.createInteractionType("MI:0915", "physical association"));
                    }
                }

                // remove db ref incompleteData and add caution
                if (interaction.getXref() != null){
                    //<secondaryRef refType="identity" refTypeAc="MI:0356" db="PDBe" id="Incomplete_Data" dbAc="MI:0472"/>
                    DbReference incompleteData = null;
                    for (DbReference ref : interaction.getXref().getAllDbReferences()){
                        if (ref.getDbAc() != null && "MI:0472".equals(ref.getDbAc())
                                && ref.getDb() != null && "PDBe".equalsIgnoreCase(ref.getDb())
                                && ref.getRefTypeAc() != null && CvXrefQualifier.IDENTITY_MI_REF.equals(ref.getRefTypeAc())
                                && ref.getRefType() != null && CvXrefQualifier.IDENTITY.equalsIgnoreCase(ref.getRefType())
                                && "Incomplete_Data".equalsIgnoreCase(ref.getId())){
                            incompleteData = ref;
                        }
                    }

                    // add caution if incomplete data
                    if (incompleteData != null){
                        interaction.getXref().getSecondaryRef().remove(incompleteData);
                        interaction.getAttributes().add(new Attribute(CvTopic.CAUTION_MI_REF,CvTopic.CAUTION,"Incomplete Data"));
                    }
                }

                // add pdb pubmed id
                Collection<ExperimentDescription> newExperiments = new ArrayList<ExperimentDescription>();
                for (ExperimentDescription exp : interaction.getExperiments()){
                    // add host organism
                    if (exp.getHostOrganisms().isEmpty()){
                        exp.getHostOrganisms().add(host);
                    }
                    // add primary ref in experiment Xref
                    if (exp.getXref() == null){
                        exp.setXref(new Xref(publication));
                    }
                    else{
                        // reset qualifier of all primary references
                        for (DbReference db : exp.getXref().getAllDbReferences()){
                            if (db.getDbAc() != null && CvDatabase.PUBMED_MI_REF.equals(db.getDbAc())
                                    && db.getDb() != null && CvDatabase.PUBMED.equalsIgnoreCase(db.getDb())
                                    && db.getRefTypeAc() != null && CvXrefQualifier.PRIMARY_REFERENCE_MI_REF.equals(db.getRefTypeAc())
                                    && db.getRefType() != null && CvXrefQualifier.PRIMARY_REFERENCE.equalsIgnoreCase(db.getRefType())){
                                db.setRefType(CvXrefQualifier.SEE_ALSO);
                                db.setRefTypeAc(CvXrefQualifier.SEE_ALSO_MI_REF);
                            }
                        }

                        DbReference oldPrimary = exp.getXref().getPrimaryRef();
                        exp.getXref().setPrimaryRef(publication);
                        exp.getXref().getSecondaryRef().add(oldPrimary);
                    }

                    // add primary ref in bibref
                    Bibref bibref = exp.getBibref();
                    if (bibref.getXref() == null){
                        bibref.setXref(new Xref(publication));
                    }
                    else{
                        // reset qualifier of all primary references
                        for (DbReference db : bibref.getXref().getAllDbReferences()){
                            if (db.getDbAc() != null && CvDatabase.PUBMED_MI_REF.equals(db.getDbAc())
                                    && db.getDb() != null && CvDatabase.PUBMED.equalsIgnoreCase(db.getDb())
                                    && db.getRefTypeAc() != null && CvXrefQualifier.PRIMARY_REFERENCE_MI_REF.equals(db.getRefTypeAc())
                                    && db.getRefType() != null && CvXrefQualifier.PRIMARY_REFERENCE.equalsIgnoreCase(db.getRefType())){
                                db.setRefType(CvXrefQualifier.SEE_ALSO);
                                db.setRefTypeAc(CvXrefQualifier.SEE_ALSO_MI_REF);
                            }
                        }

                        DbReference oldPrimary = bibref.getXref().getPrimaryRef();
                        bibref.getXref().setPrimaryRef(publication);
                        bibref.getXref().getSecondaryRef().add(oldPrimary);
                    }

                    if (experiments.containsKey(exp)){
                        newExperiments.add(experiments.get(exp));
                    }
                    else{
                        exp.setId(idGenerator.nextId());
                        experiments.put(exp, exp);
                        newExperiments.add(exp);
                    }

                }

                interaction.getExperiments().clear();
                interaction.getExperiments().addAll(newExperiments);

                writer.writeInteraction(interaction);
            }

            writer.writeEndEntry(null);
        }

        writer.writeEndDocument();
    }

    private static class ExperimentComparator implements Comparator<ExperimentDescription> {

        @Override
        public int compare(ExperimentDescription experimentDescription, ExperimentDescription experimentDescription2) {
            Organism host1 = experimentDescription.getHostOrganisms().iterator().next();
            Organism host2 = experimentDescription2.getHostOrganisms().iterator().next();
            // compare organisms
            if (host1.getNcbiTaxId() == host2.getNcbiTaxId()){

                // compare bibref
                Bibref bibRef1 = experimentDescription.getBibref();
                Bibref bibRef2 = experimentDescription2.getBibref();

                Collection<DbReference> xref1 = bibRef1.getXref().getAllDbReferences();
                Collection<DbReference> xref2 = bibRef2.getXref().getAllDbReferences();

                if (xref1.size() == xref2.size()){
                    if (org.apache.commons.collections.CollectionUtils.isEqualCollection(xref1, xref2)){
                        return 0;
                    }
                    else{
                        Iterator<DbReference> iterator1 = xref1.iterator();
                        Iterator<DbReference> iterator2 = xref2.iterator();

                        while (iterator1.hasNext() && iterator2.hasNext()){
                            DbReference ref1 = iterator1.next();
                            DbReference ref2 = iterator2.next();

                            int comp1 = ref1.getId().compareTo(ref2.getId());
                            if (comp1 != 0){
                                return comp1;
                            }
                            comp1 = ref1.getDb().compareTo(ref2.getDb());
                            if (comp1 != 0){
                                return comp1;
                            }
                            comp1 = ref1.getDbAc().compareTo(ref2.getDbAc());
                            if (comp1 != 0){
                                return comp1;
                            }
                            comp1 = ref1.getRefType().compareTo(ref2.getRefType());
                            if (comp1 != 0){
                                return comp1;
                            }
                            comp1 = ref1.getRefTypeAc().compareTo(ref2.getRefTypeAc());
                            if (comp1 != 0){
                                return comp1;
                            }
                        }

                        return 0;
                    }
                }
                else if (xref1.size() < xref2.size()){
                    return -1;
                }
                else{
                    return 1;
                }
            }
            else if (host1.getNcbiTaxId() < host2.getNcbiTaxId()){
                return -1;
            }
            else{
                return 1;
            }
        }
    }

    private static class InteractorComparator implements Comparator<Interactor> {


        @Override
        public int compare(Interactor interactor, Interactor interactor2) {

            Organism host1 = interactor.getOrganism();
            Organism host2 = interactor2.getOrganism();
            // compare organisms
            if (host1.getNcbiTaxId() == host2.getNcbiTaxId()){

                // compare xref
                Collection<DbReference> xref1 = interactor.getXref().getAllDbReferences();
                Collection<DbReference> xref2 = interactor2.getXref().getAllDbReferences();

                if (xref1.size() == xref2.size()){
                    if (org.apache.commons.collections.CollectionUtils.isEqualCollection(xref1, xref2)){
                        return 0;
                    }
                    else{
                        Iterator<DbReference> iterator1 = xref1.iterator();
                        Iterator<DbReference> iterator2 = xref2.iterator();

                        while (iterator1.hasNext() && iterator2.hasNext()){
                            DbReference ref1 = iterator1.next();
                            DbReference ref2 = iterator2.next();

                            int comp1 = ref1.getId().compareTo(ref2.getId());
                            if (comp1 != 0){
                                return comp1;
                            }
                            comp1 = ref1.getDb().compareTo(ref2.getDb());
                            if (comp1 != 0){
                                return comp1;
                            }
                            comp1 = ref1.getDbAc().compareTo(ref2.getDbAc());
                            if (comp1 != 0){
                                return comp1;
                            }
                            comp1 = ref1.getRefType().compareTo(ref2.getRefType());
                            if (comp1 != 0){
                                return comp1;
                            }
                            comp1 = ref1.getRefTypeAc().compareTo(ref2.getRefTypeAc());
                            if (comp1 != 0){
                                return comp1;
                            }
                        }

                        return 0;
                    }
                }
                else if (xref1.size() < xref2.size()){
                    return -1;
                }
                else{
                    return 1;
                }
            }
            else if (host1.getNcbiTaxId() < host2.getNcbiTaxId()){
                return -1;
            }
            else{
                return 1;
            }
        }
    }
}
