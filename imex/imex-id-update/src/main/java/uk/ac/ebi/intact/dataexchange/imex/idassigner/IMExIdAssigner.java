package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import uk.ac.ebi.intact.core.IntactException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.filter.IMExFilter;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.helpers.InteractionHelper;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.helpers.PublicationHelper;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;


/**
 * 
 * @author Arnaud Ceol
 *
 * @deprecated this class was superseeded by
 */
@Deprecated
public class IMExIdAssigner {

	public static final String IMExPrefix = "IM-";

	private IMExFilter imexFilter;

	public IMExIdAssigner(IMExFilter imexFilter) {
		this.imexFilter = imexFilter;
	}
	
	/**
	 * Assign an IMEx id to a publication. The IMEx ID is automatically added to all experiments.
	 * The interactions evidence IMEx ID should be added separately. If the publication is not exportable, it is skiped. 
	 * @param publication
	 * @param imexId
	 * @throws IntactException
	 * 	 if the publication is owned by another institution 
	 *  or if the publication or an experiment  has already an IMEx id  
	 */
	public void assignIMExPrimaryId(Publication publication, String imexId) throws IntactException {

		if (publication == null) {
			throw new IntactException("must provide a non null publication");
		}

		if (imexFilter.isExportable(publication)) {
			PublicationHelper.assignIMExId(publication, imexId);
		}
		
	}

	/**
	 * Propagate the IMEx Id of the publication to each experiment. Experiments which already have 
	 * an IMEx ID are skiped. (TO use for instance when an experiment has been added).
	 * @param publication
	 */
	public void updateExperiments(Publication publication) {
		PublicationHelper.updateExperiments(publication);
	}
	
	
	/**
	 * Assign an evidence imex id to all interactions. Interactions which are not exportable are skiped. Interaction which already have an IMEx evidence id are skiped.
	 * Note: a new IMEx evidence Id is assigned if the interaction already had an IMEx primary id, unless such interaction is filtered by the IMExFilter. 
	 * @param publication the publication
	 * @throws IntactException if the publication is null, if the publication has no IMEx ID 
	 */
	public void assignIMExEvidenceIds(Publication publication) throws IntactException {

		if (publication == null) {
			throw new IntactException("must provide a non null publication");
		}

		// the publication must have an IMEx Id
		String imexId = PublicationHelper.getIMExId(publication);

		if (imexId == null) {
			throw new IntactException(
					"an IMEx Id must have been assigned to the publication before to assign IMEx evidence Ids to interactions");
		}

		// get the last id assigned to an interaction within the publication 
		long lastEvidenceIndex = PublicationHelper
				.getLastEvidenceIMExId(publication);

		long currentEvidenceIndex = lastEvidenceIndex;

		for (Experiment experiment : publication.getExperiments()) {
			for (Interaction interaction : experiment.getInteractions()) {
				if (false == InteractionHelper.hasIMExId(interaction)
						&& imexFilter.isExportable(interaction)) {
					String newId = IMExIdTransformer
							.formatIMExId(++currentEvidenceIndex);
					InteractionHelper.addIMExId(interaction, newId);
				}
			}
		}

		// save the last index of IMEx evidence for this publication
		if (currentEvidenceIndex > lastEvidenceIndex) {
			PublicationHelper.setLastEvidenceId(publication,
					currentEvidenceIndex);
		}

	}

	public IMExFilter getImexFilter(IMExFilter imexFilter) {
		return imexFilter;
	}

	public void setImexFilter(IMExFilter imexFilter) {
		this.imexFilter = imexFilter;
	}

	
}
