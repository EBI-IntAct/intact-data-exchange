package uk.ac.ebi.intact.export.mutation.helper;

import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.ParticipantEvidence;

import java.util.List;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public interface MutationExportDao {

    public List<String> getFeatureEvidenceByType(String term);

    public ParticipantEvidence getParticipantEvidence(String ac);

    public CvTerm getCVTermByAc(String ac);
}
