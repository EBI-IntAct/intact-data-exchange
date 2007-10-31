/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import psidev.psi.mi.search.column.DefaultColumnSet;
import psidev.psi.mi.search.query.SearchQuery;

/**
 * TODO comment this
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 1.5.0
 */
public class IntActSearchQuery extends SearchQuery{
    
    public IntActSearchQuery(String query){
		super(query);
	}
	
	public IntActSearchQuery(String query, String ... fields){
		super(query, fields);
	}
	
	public IntActSearchQuery(SearchQuery searchQuery){
		super(searchQuery.getQuery());
	}
	
    private static final String[] DEFAULT_FIELDS = {"identifiers",
        	DefaultColumnSet.PUB_ID.getShortName(),
        	DefaultColumnSet.PUB_1ST_AUTHORS.getShortName(),
        	"species",
        	DefaultColumnSet.INTERACTION_TYPES.getShortName(),
        	DefaultColumnSet.INTER_DETECTION_METHODS.getShortName(),
        	DefaultColumnSet.INTERACTION_ID.getShortName(),
        	"properties",
        	IntActColumnSet.HOSTORGANISM.getShortName(),
        	IntActColumnSet.EXPANSION_METHOD.getShortName()};

    private String[] fields = DEFAULT_FIELDS;
    
    @Override
    public String[] getFields() {
        return fields;
    }

}
