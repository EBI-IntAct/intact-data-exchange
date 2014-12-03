package uk.ac.ebi.intact.dataexchange.cttv.converter;

import org.apache.commons.lang.StringUtils;
import org.cttv.input.model.*;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.XrefUtils;

import java.util.*;

/**
 * Created by maitesin on 13/11/2014.
 */
public class DefaultComplexCttvConverter implements ComplexCttvConverter {
    private String identifiersUrl = "http://identifiers.org/";
    private final String eco = "evidence ontology";
    private final String ecoMI = "MI:1331";
    private final String efo = "efo";
    private final String efoMI = "MI:1337";
    private final String orphanet = "orphanet";
    private final String orphanetMI = "IA:2828";
    private final String intact = "intact";
    private final String intactMI = "MI:0469";

    @Override
    public EvidenceString convertToEvidenceStringFromComplex(Complex complex) {
        //
        //Biological Subject
        //
        List<String> about = new ArrayList<String>(complex.getParticipants().size());
        for (Participant participant : complex.getParticipants()) {
            Interactor interactor = participant.getInteractor();
            if (interactor instanceof Protein) {
                about.add(this.identifiersUrl + "uniprot/" + interactor.getPreferredIdentifier().getId());
            }
            else if (interactor instanceof BioactiveEntity) {
                about.add(this.identifiersUrl + "chembl/" + interactor.getPreferredIdentifier().getId());
            }
        }
        //TODO
        BiologicalSubjectProperties properties = new BiologicalSubjectProperties(/*Ask Curators about Association Context*/ AssociationContext.Protein_complex_heteropolymer, /*Ask Curators about activity*/ Activity.LOSS_OF_FUNCTION);
        BiologicalSubject biologicalSubject = new BiologicalSubject(about, properties);
        //
        //Evidence (Before known as Provenance)
        //
        ProvenanceUrls provenanceUrls = new ProvenanceUrls();
        provenanceUrls.addLinkOut(new LinkOut("IntAct Complex Portal", "http://www.ebi.ac.uk/intact/complex-ws/details/" + getIntActID(complex).getId()));
        ProvenanceType provenanceType = new ProvenanceType(new ProvenanceLiterature(), new ProvenanceExpert(true), new ProvenanceDatabase("IntAct", "1.0")); //This version is hardcoded
        List<String> evidenceCodes = new ArrayList<String>();
        //This two are hardcoded
        evidenceCodes.add(this.identifiersUrl + "eco/ECO:0000205");
        evidenceCodes.add(this.identifiersUrl + "eco/ECO:0000001");
        //The ECO code of the complex
        for (Xref xref_eco : XrefUtils.collectAllXrefsHavingDatabase(complex.getEvidenceType().getIdentifiers(), ecoMI, eco)) {
            evidenceCodes.add(this.identifiersUrl + "eco/" + xref_eco.getId());
        }
        Evidence evidence = new Evidence(complex.getUpdatedDate(), true, provenanceType, evidenceCodes, provenanceUrls);
        //
        //Biological Object
        //
        BiologicalObject biologicalObject = null;
        for (Xref xref : XrefUtils.collectAllXrefsHavingDatabase(complex.getIdentifiers(), efoMI, efo)) {
            if (biologicalObject == null) {
                biologicalObject = new BiologicalObject(this.identifiersUrl + "efo/" + xref.getId());
            }
            else {
                biologicalObject.addAbout(this.identifiersUrl + "efo/" + xref.getId());
            }
        }
        if (biologicalObject == null) {
            biologicalObject = new BiologicalObject(this.identifiersUrl + "efo/EFO:0000000");
        }
        for (Xref xref : XrefUtils.collectAllXrefsHavingDatabase(complex.getXrefs(), orphanetMI, orphanet)) {
            biologicalObject.addAbout(this.identifiersUrl + "orphanet/" + xref.getId());
        }
        Map<String, String> uniqueAssociationFields = new HashMap<String, String>();
        uniqueAssociationFields.put("intact_id", this.identifiersUrl + "intact/" + getIntActID(complex).getId());
        uniqueAssociationFields.put("biological_subjects", StringUtils.join(biologicalSubject.getAbout(), ","));
        uniqueAssociationFields.put("biological_objects", StringUtils.join(biologicalObject.getAbout(), ","));
        return new EvidenceString(biologicalSubject, evidence, biologicalObject, uniqueAssociationFields);
    }

    private Xref getIntActID(Complex complex) {
        return XrefUtils.collectFirstIdentifierWithDatabase(complex.getIdentifiers(), this.intactMI, this.intact);
    }
}
