package uk.ac.ebi.intact.export.mutation.helper;

import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.FeatureEvidence;
import psidev.psi.mi.jami.model.ParticipantEvidence;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;

import java.util.List;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public interface MutationExportDao {

    public List<String> getFeatureEvidenceByType(String term);

    public IntactFeatureEvidence getFeature(String ac);

    public ParticipantEvidence getParticipantEvidence(String ac);

    public CvTerm getCVTermByAc(String ac);
}
