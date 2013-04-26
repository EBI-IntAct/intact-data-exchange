package uk.ac.ebi.intact.dataexchange.imex.idassigner.filter;

import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;



public interface IMExFilter {

	public boolean isExportable(Publication publication);
	
	public boolean isExportable(Interaction interaction);
	
}
