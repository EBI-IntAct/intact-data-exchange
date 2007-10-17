/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import java.util.List;

import psidev.psi.mi.search.column.ColumnSet;
import psidev.psi.mi.search.column.DefaultColumnSet;
import psidev.psi.mi.search.column.PsimiTabColumn;

/**
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id: IntActColumnSet.java 
 */
public class IntActColumnSet extends DefaultColumnSet implements ColumnSet {

	private List<PsimiTabColumn> psimiTabColumns;
	
	public IntActColumnSet(){
		super();
		psimiTabColumns = super.getPsimiTabColumns();
		
		psimiTabColumns.add(new PsimiTabColumn(16, "experimentalRole interactor A", "role_A"));
		psimiTabColumns.add(new PsimiTabColumn(17, "experimentalRole interactor B", "role_B"));
		psimiTabColumns.add(new PsimiTabColumn(18, "properties interactor A", "properties_A"));
		psimiTabColumns.add(new PsimiTabColumn(19, "properties interactor B", "properties_B"));
		psimiTabColumns.add(new PsimiTabColumn(20, "interactorType of A", "type_A"));
		psimiTabColumns.add(new PsimiTabColumn(21, "interactorType of B", "type_B"));
		psimiTabColumns.add(new PsimiTabColumn(22, "hostOrganism", "hostOrganism"));		
	}

}
