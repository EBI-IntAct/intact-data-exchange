package uk.ac.ebi.intact.export.mutation.helper;

import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.ParticipantEvidence;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;

import java.util.Collection;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public interface MutationExportDao {

    public Collection<IntactFeatureEvidence> getFeatureEvidenceByType(String term);

    public ParticipantEvidence getParticipantEvidence(String ac);

    public CvTerm getCVTermByAc(String ac);
}
