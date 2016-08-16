package uk.ac.ebi.intact.export.mutation.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.ParticipantEvidence;
import uk.ac.ebi.intact.export.mutation.MutationExportConfig;
import uk.ac.ebi.intact.export.mutation.MutationExportContext;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;

import java.util.Collection;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class MutationExportDaoImpl implements MutationExportDao {
    private static final Log log = LogFactory.getLog(MutationExportDaoImpl.class);

    private MutationExportConfig config = MutationExportContext.getInstance().getConfig();

    @Override
    public Collection<IntactFeatureEvidence> getFeatureEvidenceByType(String term) {
        return config.getIntactDao().getFeatureEvidenceDao().getByFeatureType(null, term);
    }

    @Override
    public ParticipantEvidence getParticipantEvidence(String ac) {
        return config.getIntactDao().getParticipantEvidenceDao().getByAc(ac);
    }

    @Override
    public CvTerm getCVTermByAc(String ac) {
        return config.getIntactDao().getCvTermDao().getByAc(ac);
    }
}
