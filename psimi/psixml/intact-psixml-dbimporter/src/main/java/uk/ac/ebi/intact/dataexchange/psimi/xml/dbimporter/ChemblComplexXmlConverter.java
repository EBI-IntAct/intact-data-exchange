package uk.ac.ebi.intact.dataexchange.psimi.xml.dbimporter;

import psidev.psi.mi.xml.PsimiXmlForm;
import psidev.psi.mi.xml.PsimiXmlLightweightWriter;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.converter.ConverterContext;
import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IdSequenceGenerator;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvInteractionType;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.CvXrefQualifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Class to convert CHEMBL files to a complex XML file for importing in the database
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>03/12/13</pre>
 */

public class ChemblComplexXmlConverter {

    public static void main(String[] args) throws Exception {

        if (args.length < 4) {
            System.err.println("Usage: ChemblComplexXmlConverter <input.file.components> <input.file.targets> <output.file> <publication.pubmed>");
            System.exit(1);
        }

        File complexFile = new File(args[0]);
        File targetFile = new File(args[1]);
        File outputFile = new File(args[2]);
        String complexPubmedId = args[3];

        BufferedReader complexReader = new BufferedReader(new FileReader(complexFile));
        BufferedReader targetReader = new BufferedReader(new FileReader(targetFile));

        // read complex composition
        Map<String, Collection<ChemblComplexComponent>> complexContent = new HashMap<String, Collection<ChemblComplexComponent>>();
        String line = complexReader.readLine();
        while (line != null){
            String[] lines = line.split("\t");
            String chemblId = lines[0];
            String uniprotId = lines[2];
            String organismName = lines[3];
            String taxid = lines[4];
            String description = lines[5];

            ChemblComplexComponent component = new ChemblComplexComponent(uniprotId, organismName, taxid, description);
            if (complexContent.containsKey(chemblId)){
                complexContent.get(chemblId).add(component);
            }
            else{
                Collection<ChemblComplexComponent> components = new ArrayList<ChemblComplexComponent>();
                components.add(component);
                complexContent.put(chemblId, components);
            }
            line = complexReader.readLine();
        }
        complexReader.close();

        // read complex description
        PsimiXmlLightweightWriter writer = new PsimiXmlLightweightWriter(outputFile, PsimiXmlVersion.VERSION_254);
        writer.writeStartDocument();

        // create source
        Source source = new Source();
        Names names = new Names();
        names.setShortLabel("chembl");
        names.setFullName("CHEMBL database");
        source.setNames(names);
        source.setXref(PsiFactory.createXrefPsiMi("MI:0967"));
        writer.writeStartEntry(source, null);

        // write complexes
        DbReference publication = new DbReference(CvDatabase.PUBMED, CvDatabase.PUBMED_MI_REF, complexPubmedId, CvXrefQualifier.PRIMARY_REFERENCE, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF);

        line = targetReader.readLine();
        Map<ExperimentDescription, ExperimentDescription> experiments = new TreeMap<ExperimentDescription, ExperimentDescription>(new ExperimentComparator());
        Map<ChemblComplexComponent, Interactor> interactors = new TreeMap<ChemblComplexComponent, Interactor>(new InteractorComparator());
        IdSequenceGenerator idGenerator = IdSequenceGenerator.getInstance();
        while (line != null){
            String[] lines = line.split("\t");
            String chemblId = lines[1];
            String preferredName = lines[2];
            String organismName = lines[3];
            String taxid = lines[4];
            String defined = lines[5];

            ChemblComplex complex = new ChemblComplex(chemblId, preferredName, organismName, taxid, defined);
            if (complexContent.containsKey(complex.getChemblId())){
                ConverterContext.getInstance().getConverterConfig().setXmlForm(PsimiXmlForm.FORM_EXPANDED);

                Collection<ChemblComplexComponent> components = complexContent.get(complex.getChemblId());

                // create experiment
                Bibref bibRef = new Bibref(new Xref(publication));
                ExperimentDescription exp = new ExperimentDescription(bibRef, PsiFactory.createInteractionDetectionMethod("MI:0364","inferred by curator"));
                exp.setParticipantIdentificationMethod(PsiFactory.createParticipantIdentificationMethod("MI:0396", "predetermined"));

                // create organism
                Organism organism = new Organism();
                organism.setNcbiTaxId(Integer.parseInt(complex.getTaxid()));
                organism.setNames(new Names());
                organism.getNames().setShortLabel(complex.getOrganismName());
                organism.getNames().setFullName(complex.getOrganismName());
                exp.getHostOrganisms().add(organism);
                exp.setNames(organism.getNames());

                // create interaction
                Interaction interaction = new Interaction();
                interaction.setId(idGenerator.nextId());

                // assign id for experiment
                if (experiments.containsKey(exp)){
                    interaction.getExperiments().add(experiments.get(exp));
                }
                else{
                    exp.setId(idGenerator.nextId());
                    experiments.put(exp, exp);
                    interaction.getExperiments().add(exp);
                }

                // add chembl identity
                interaction.setXref(PsiFactory.createXrefIdentity(complex.getChemblId(),"MI:0967","chembl"));

                // add preferred name
                interaction.setNames(new Names());
                interaction.getNames().setShortLabel(complex.getPreferredName());
                interaction.getNames().getAliases().add(new Alias(complex.getPreferredName(), "complex recommended name", "MI:1315" ));

                // add curated complex
                interaction.getAttributes().add(new Attribute("curated-complex",complex.getPreferredName() + ": CHEMBL XML import"));

                // if not defined, add a caution
                if (!complex.isDefined()){
                    interaction.getAttributes().add(new Attribute(CvTopic.CAUTION_MI_REF,CvTopic.CAUTION,"This complex is not fully defined"));
                }

                // add experimental role neutral component
                ExperimentalRole role = PsiFactory.createExperimentalRole("MI:0497","neutral component");
                // add bio role unspecified
                BiologicalRole bioRole = PsiFactory.createBiologicalRole("MI:0499","unspecified role");
                // add participants
                for (ChemblComplexComponent component : components){
                    Participant participant = new Participant();
                    participant.setId(idGenerator.nextId());
                    participant.getExperimentalRoles().add(role);
                    participant.setBiologicalRole(bioRole);
                    // create interactor
                    if (interactors.containsKey(component)){
                        participant.setInteractor(interactors.get(component));
                    }
                    else{
                        Interactor interactor = PsiFactory.createInteractorUniprotProtein(component.getUniprotId(),
                                PsiFactory.createOrganism(Integer.parseInt(component.getTaxid()), component.getOrganismName()));
                        interactor.getNames().setFullName(component.getDescription());
                        interactor.setId(idGenerator.nextId());
                        participant.setInteractor(interactor);

                        interactors.put(component, interactor);
                    }

                    interaction.getParticipants().add(participant);
                }

                // add interaction type
                if (interaction.getParticipants().size() == 2){
                    interaction.getInteractionTypes().add(PsiFactory.createInteractionType(CvInteractionType.DIRECT_INTERACTION_MI_REF, CvInteractionType.DIRECT_INTERACTION));
                }
                else{
                    interaction.getInteractionTypes().add(PsiFactory.createInteractionType("MI:0915", "physical association"));
                }

                writer.writeInteraction(interaction);
            }
            else {
                System.out.println("Invalid complex "+complex.getChemblId());
            }

            line = targetReader.readLine();
        }
        targetReader.close();
        writer.writeEndEntry(null);
        writer.writeEndDocument();
    }

    private static class ChemblComplex{
        private String chemblId;
        private String preferredName;
        private String organismName;
        private String taxid;
        private boolean isDefined;

        public ChemblComplex(String chemblId, String preferredName, String organismName, String taxid, String defined){
            if (chemblId == null){
                throw new IllegalArgumentException("The CHEMBL id cannot be null");
            }
            this.chemblId = chemblId;
            if (preferredName == null){
                throw new IllegalArgumentException("The preferred name cannot be null");
            }
            this.preferredName = preferredName;
            if (organismName == null){
                throw new IllegalArgumentException("The organism name cannot be null");
            }
            this.organismName = organismName;
            if (taxid == null){
                throw new IllegalArgumentException("The organism name cannot be null");
            }
            this.taxid = taxid;
            if (defined == null){
                throw new IllegalArgumentException("The defined field cannot be null");
            }
            this.isDefined = defined.equals("Y");
        }

        private String getChemblId() {
            return chemblId;
        }

        private String getPreferredName() {
            return preferredName;
        }

        private String getOrganismName() {
            return organismName;
        }

        private String getTaxid() {
            return taxid;
        }

        private boolean isDefined() {
            return isDefined;
        }
    }

    private static class ChemblComplexComponent{
        private String uniprotId;
        private String organismName;
        private String taxid;
        private String description;

        public ChemblComplexComponent(String uniprotId, String organismName, String taxid, String description){
            if (uniprotId == null){
                throw new IllegalArgumentException("The uniprot id cannot be null");
            }
            this.uniprotId = uniprotId;
            if (organismName == null){
                throw new IllegalArgumentException("The organism name cannot be null");
            }
            this.organismName = organismName;
            if (taxid == null){
                throw new IllegalArgumentException("The organism taxid cannot be null");
            }
            this.taxid = taxid;
            if (description == null){
                throw new IllegalArgumentException("The description cannot be null");
            }
            this.description = description;
        }

        private String getUniprotId() {
            return uniprotId;
        }

        private String getOrganismName() {
            return organismName;
        }

        private String getTaxid() {
            return taxid;
        }

        private String getDescription() {
            return description;
        }
    }

    private static class ExperimentComparator implements Comparator<ExperimentDescription> {

        @Override
        public int compare(ExperimentDescription experimentDescription, ExperimentDescription experimentDescription2) {
            Organism host1 = experimentDescription.getHostOrganisms().iterator().next();
            Organism host2 = experimentDescription2.getHostOrganisms().iterator().next();
            // compare organisms
            if (host1.getNcbiTaxId() == host2.getNcbiTaxId()){
                return 0;
            }
            else if (host1.getNcbiTaxId() < host2.getNcbiTaxId()){
                return -1;
            }
            else{
                return 1;
            }
        }
    }

    private static class InteractorComparator implements Comparator<ChemblComplexComponent> {


        @Override
        public int compare(ChemblComplexComponent interactor, ChemblComplexComponent interactor2) {

            // compare organisms
            if (interactor.getTaxid().equals(interactor2.getTaxid())){

                return interactor.getUniprotId().compareTo(interactor2.getUniprotId());
            }
            else{
                return interactor.getTaxid().compareTo(interactor2.getTaxid());
            }
        }
    }
}
