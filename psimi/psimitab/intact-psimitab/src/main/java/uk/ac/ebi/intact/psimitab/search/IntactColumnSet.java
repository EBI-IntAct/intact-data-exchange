/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import psidev.psi.mi.search.column.ColumnSet;
import psidev.psi.mi.search.column.DefaultColumnSet;
import psidev.psi.mi.search.column.PsimiTabColumn;

import java.util.List;

/**
 * Define all addidional Columns of the IntactBinaryInteraction for indexing.
 * 
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntactColumnSet extends DefaultColumnSet implements ColumnSet {
	
	public static final PsimiTabColumn EXPERIMENTAL_ROLE_A = new PsimiTabColumn(15, "Experimental role(s) interactor A", "experimentalRoleA");
	public static final PsimiTabColumn EXPERIMENTAL_ROLE_B = new PsimiTabColumn(16, "Experimental role(s) interactor B", "experimentalRoleB");
    public static final PsimiTabColumn BIOLOGICAL_ROLE_A = new PsimiTabColumn(17, "Biological role(s) interactor A", "biologicalRoleA");
    public static final PsimiTabColumn BIOLOGICAL_ROLE_B = new PsimiTabColumn(18, "Biological role(s) interactor B", "biologicalRoleB");
    public static final PsimiTabColumn PROPERTIES_A = new PsimiTabColumn(19, "Properties interactor A", "propertiesA");
	public static final PsimiTabColumn PROPERTIES_B = new PsimiTabColumn(20, "Properties interactor B", "propertiesB");
	public static final PsimiTabColumn INTERACTOR_TYPE_A = new PsimiTabColumn(21, "Type(s) interactor A", "typeA");
	public static final PsimiTabColumn INTERACTOR_TYPE_B = new PsimiTabColumn(22, "Type(s) interactor B", "typeB");
	public static final PsimiTabColumn HOSTORGANISM = new PsimiTabColumn(23, "HostOrganism(s)", "hostOrganism");
	public static final PsimiTabColumn EXPANSION_METHOD = new PsimiTabColumn(24, "Expansion method(s)", "expansion");
    public static final PsimiTabColumn DATASET = new PsimiTabColumn( 25, "Dataset name(s)", "dataset");
	
	
	public IntactColumnSet(){
		super();
		List<PsimiTabColumn> psimiTabColumns = super.getPsimiTabColumns();
		
		psimiTabColumns.add( EXPERIMENTAL_ROLE_A );
		psimiTabColumns.add( EXPERIMENTAL_ROLE_B );
		psimiTabColumns.add( BIOLOGICAL_ROLE_A );
		psimiTabColumns.add( BIOLOGICAL_ROLE_B );
        psimiTabColumns.add( PROPERTIES_A );
		psimiTabColumns.add( PROPERTIES_B );
		psimiTabColumns.add( INTERACTOR_TYPE_A );
		psimiTabColumns.add( INTERACTOR_TYPE_B );
		psimiTabColumns.add( HOSTORGANISM );
		psimiTabColumns.add( EXPANSION_METHOD );
        psimiTabColumns.add( DATASET );
    }

}
