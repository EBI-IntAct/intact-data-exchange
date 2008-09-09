/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.search.util.AbstractInteractionDocumentBuilder;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import psidev.psi.mi.tab.model.builder.Column;
import psidev.psi.mi.tab.model.builder.Row;
import psidev.psi.mi.tab.model.builder.RowBuilder;
import psidev.psi.mi.tab.model.builder.MitabDocumentDefinition;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import uk.ac.ebi.intact.util.ols.OlsUtils;
import uk.ac.ebi.intact.util.ols.Term;
import uk.ac.ebi.intact.util.ols.OlsClient;
import uk.ac.ebi.ook.web.services.Query;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.rmi.RemoteException;

/**
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntactDocumentBuilder extends AbstractInteractionDocumentBuilder<IntactBinaryInteraction> {

    private static final Log log = LogFactory.getLog( IntactDocumentBuilder.class );
    private boolean includeParentsForCvTerms = false;

    public IntactDocumentBuilder(boolean includeParentsForCvTerms){
      super(new IntactDocumentDefinition());
      this.includeParentsForCvTerms = includeParentsForCvTerms;

    }

    public IntactDocumentBuilder() {
        super(new IntactDocumentDefinition());
    }

    public Document createDocumentFromPsimiTabLine(String psiMiTabLine) throws MitabLineException
	{
        Document doc = super.createDocumentFromPsimiTabLine( psiMiTabLine );

        final RowBuilder rowBuilder = getDocumentDefinition().createRowBuilder();
        final Row row = rowBuilder.createRow(psiMiTabLine);

        if (row.getColumnCount() <= IntactDocumentDefinition.EXPERIMENTAL_ROLE_A) {
            return doc;
        }

        // raw fields
		Column experimentRoleA = row.getColumnByIndex(IntactDocumentDefinition.EXPERIMENTAL_ROLE_A);
		Column experimentRoleB = row.getColumnByIndex(IntactDocumentDefinition.EXPERIMENTAL_ROLE_B);
        Column biologicalRoleA = row.getColumnByIndex(IntactDocumentDefinition.BIOLOGICAL_ROLE_A);
        Column biologicalRoleB = row.getColumnByIndex(IntactDocumentDefinition.BIOLOGICAL_ROLE_B);
        Column propertiesA = row.getColumnByIndex(IntactDocumentDefinition.PROPERTIES_A);
		Column propertiesB = row.getColumnByIndex(IntactDocumentDefinition.PROPERTIES_B);
		Column typeA = row.getColumnByIndex(IntactDocumentDefinition.INTERACTOR_TYPE_A);
		Column typeB = row.getColumnByIndex(IntactDocumentDefinition.INTERACTOR_TYPE_B);
		Column hostOrganism = row.getColumnByIndex(IntactDocumentDefinition.HOST_ORGANISM);
		Column expansion = row.getColumnByIndex(IntactDocumentDefinition.EXPANSION_METHOD);
        Column dataset = row.getColumnByIndex(IntactDocumentDefinition.DATASET);

        // other columns, such as the annotations (corresponding to a second extension of the format)
        // after next section

		doc.add(new Field("roles", isolateDescriptions( experimentRoleA ) + " " + isolateDescriptions( experimentRoleB )
                + " " + isolateDescriptions( biologicalRoleA ) + " " + isolateDescriptions( biologicalRoleB),
				Field.Store.NO,
				Field.Index.TOKENIZED));
		
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.EXPERIMENTAL_ROLE_A), experimentRoleA);
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.EXPERIMENTAL_ROLE_B), experimentRoleB);
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.BIOLOGICAL_ROLE_A), biologicalRoleA);
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.BIOLOGICAL_ROLE_B), biologicalRoleB);

        String value = isolateValue(propertiesA) + " " + isolateValue(propertiesB);
		doc.add(new Field("properties_exact", value,
				Field.Store.NO,
				Field.Index.TOKENIZED));

		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.PROPERTIES_A), propertiesA);
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.PROPERTIES_B), propertiesB);
		
		doc.add(new Field("interactor_types", isolateDescriptions(typeA) + " " + isolateDescriptions(typeB),
				Field.Store.NO,
				Field.Index.TOKENIZED));
		
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.INTERACTOR_TYPE_A), typeA);
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.INTERACTOR_TYPE_B), typeB);
		
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.HOST_ORGANISM), hostOrganism);
		
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.EXPANSION_METHOD), expansion);
        addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.DATASET), dataset);


        // second extension

        if (row.getColumnCount() > IntactDocumentDefinition.ANNOTATIONS_B) {
            Column annotationsA = row.getColumnByIndex(IntactDocumentDefinition.ANNOTATIONS_A);
            Column annotationsB = row.getColumnByIndex(IntactDocumentDefinition.ANNOTATIONS_B);

            addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.ANNOTATIONS_A), annotationsA);
            addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.ANNOTATIONS_B), annotationsB);

            doc.add(new Field("annotation", isolateValue(annotationsA)+" "+isolateValue(annotationsB),
                    Field.Store.NO,
                    Field.Index.TOKENIZED));
        }

        if (row.getColumnCount() > IntactDocumentDefinition.PARAMETERS_INTERACTION) {
            Column parametersA = row.getColumnByIndex(IntactDocumentDefinition.PARAMETERS_A);
            Column parametersB = row.getColumnByIndex(IntactDocumentDefinition.PARAMETERS_B);
            Column parametersInteraction = row.getColumnByIndex(IntactDocumentDefinition.PARAMETERS_INTERACTION);


            addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.PARAMETERS_A), parametersA);
            addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.PARAMETERS_B), parametersB);
            addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.PARAMETERS_INTERACTION), parametersInteraction);

            doc.add(new Field("parameter", isolateValue(parametersA)+" "+isolateValue(parametersB)+" "+isolateValue(parametersInteraction),
                    Field.Store.NO,
                    Field.Index.TOKENIZED));
        }


        if(this.includeParentsForCvTerms){
            if ( log.isDebugEnabled() ) {
                log.debug( "including parents as includeParentsForCvTerms value is "+this.includeParentsForCvTerms );
            }

            //Expand interaction detection method and interaction type columns by including parent cv terms only for search, no store
        Column detMethod_exact = row.getColumnByIndex( MitabDocumentDefinition.INT_DET_METHOD );

        Column detMethodWithParents = getColumnWithParents( detMethod_exact,OlsUtils.PSI_MI_ONTOLOGY );
        String detMethodValue = isolateIdentifier(detMethodWithParents) + " " + isolateDescriptions(detMethodWithParents);
            if ( log.isTraceEnabled() ) {
                log.trace( "detmethod -> "+detMethodValue );
            }

            doc.add( new Field( "detmethod", detMethodValue,
                            Field.Store.YES,
                            Field.Index.TOKENIZED ) );


        Column interactionTypes_exact = row.getColumnByIndex( MitabDocumentDefinition.INT_TYPE );

        Column interactionTypesWithParents = getColumnWithParents( interactionTypes_exact,OlsUtils.PSI_MI_ONTOLOGY );
        String intTypeValue = isolateIdentifier(interactionTypesWithParents) + " " + isolateDescriptions(interactionTypesWithParents);

        if ( log.isTraceEnabled() ) {
                log.trace( "type -> "+intTypeValue );
            }
        doc.add( new Field( "type", intTypeValue,
                            Field.Store.YES,
                            Field.Index.TOKENIZED ) );


        //Expand properties field columns to by including the parent terms
        Column propertiesA_exact = row.getColumnByIndex(IntactDocumentDefinition.PROPERTIES_A);
        if ( log.isTraceEnabled() ) {
            log.trace( "propertiesA_exact: "+propertiesA_exact.toString() );
        }


        Column propertiesAWithParents = getColumnWithParents(propertiesA_exact,OlsUtils.GO_ONTOLOGY);
        if ( log.isTraceEnabled() ) {
            log.trace( "propertisAWithParents: "+propertiesAWithParents.toString() );
        }


        String propertiesAValue = isolateIdentifier( propertiesAWithParents )+" " + isolateDescriptions( propertiesAWithParents );

        Column propertiesB_exact = row.getColumnByIndex(IntactDocumentDefinition.PROPERTIES_B);
        if ( log.isTraceEnabled() ) {
            log.trace( "propertiesB_exact: "+propertiesB_exact.toString() );
        }

        Column propertiesBWithParents = getColumnWithParents(propertiesB_exact,OlsUtils.GO_ONTOLOGY);
        if ( log.isTraceEnabled() ) {
            log.trace( "propertiesBWithParents: "+propertiesBWithParents );
        }


        String propertiesBValue = isolateIdentifier( propertiesBWithParents )+" " + isolateDescriptions( propertiesBWithParents );

        String propertiesValue = propertiesAValue + " " + propertiesBValue;
        if ( log.isTraceEnabled() ) {
            log.trace("propertiesValue  "+ propertiesValue );
        }

        doc.add( new Field( "properties", propertiesValue,
                                    Field.Store.YES,
                                    Field.Index.TOKENIZED ) );


        }else{

            if ( log.isDebugEnabled() ) {
                log.debug( "excluding parents as  includeParentsForCvTerms value is  "+this.includeParentsForCvTerms );
            }

            //Do not Expand interaction detection method and interaction type columns by including parent cv terms, use it as exact
            Column detMethod_exact = row.getColumnByIndex( MitabDocumentDefinition.INT_DET_METHOD );
            String detMethodValue = isolateIdentifier( detMethod_exact ) + " " + isolateDescriptions( detMethod_exact );

             if ( log.isTraceEnabled() ) {
                log.trace( "detmethod -> "+detMethodValue );
            }

            doc.add( new Field( "detmethod", detMethodValue,
                                Field.Store.NO,
                                Field.Index.TOKENIZED ) );


            Column interactionTypes_exact = row.getColumnByIndex( MitabDocumentDefinition.INT_TYPE );
            String intTypeValue = isolateIdentifier( interactionTypes_exact ) + " " + isolateDescriptions( interactionTypes_exact );

            if ( log.isTraceEnabled() ) {
                log.trace( "type -> "+intTypeValue );
            }

            doc.add( new Field( "type", intTypeValue,
                                Field.Store.NO,
                                Field.Index.TOKENIZED ) );

            //Do not Expand properties field columns to by including the parent terms
            Column propertiesA_exact = row.getColumnByIndex( IntactDocumentDefinition.PROPERTIES_A );
            String propertiesAValue = isolateIdentifier( propertiesA_exact ) + " " + isolateDescriptions( propertiesA_exact );

            Column propertiesB_exact = row.getColumnByIndex( IntactDocumentDefinition.PROPERTIES_B );
            String propertiesBValue = isolateIdentifier( propertiesB_exact ) + " " + isolateDescriptions( propertiesB_exact );

            String propertiesValue = propertiesAValue + " " + propertiesBValue;
            if ( log.isDebugEnabled() ) {
                log.debug( "propertiesValue  " + propertiesValue );
            }

            doc.add( new Field( "properties", propertiesValue,
                                Field.Store.NO,
                                Field.Index.TOKENIZED ) );


        }



        return doc;
	}


    public String isolateIdentifier( Column column ) {
        StringBuilder sb = new StringBuilder( 256 );

        for ( Iterator<psidev.psi.mi.tab.model.builder.Field> iterator = column.getFields().iterator(); iterator.hasNext(); ) {
            psidev.psi.mi.tab.model.builder.Field field = iterator.next();

            sb.append( field.getType() ).append( ":" ).append( field.getValue() );


            if ( iterator.hasNext() ) {
                sb.append( " " );
            }
        }

        return sb.toString();
    }

     public Column getColumnWithParents( Column cvColumn, String ontology ) {
         //default exclude true
         return getColumnWithParents(cvColumn,ontology,true);
     }

    public Column getColumnWithParents( Column cvColumn, String ontology, boolean excludeRoot ) {

        if ( log.isDebugEnabled() && cvColumn!=null) {
            log.debug( "cvColumn field size "+cvColumn.getFields().size() );
        }


        List<psidev.psi.mi.tab.model.builder.Field> allFieldsList = new ArrayList<psidev.psi.mi.tab.model.builder.Field>();
        for ( psidev.psi.mi.tab.model.builder.Field field : cvColumn.getFields() ) {

            if ( log.isTraceEnabled() ) {
                log.trace( "FIELD: type-> " + field.getType() + " value ->  " + field.getValue() + " desc->  " + field.getDescription() );
            }


            List<psidev.psi.mi.tab.model.builder.Field> currentFieldListWithParents = new ArrayList<psidev.psi.mi.tab.model.builder.Field>();

            String miIdentifier = null;

            if ( OlsUtils.PSI_MI_ONTOLOGY.equals( ontology ) ) {
                //handles of type MI:0407(direct interaction)
                if ( OlsUtils.PSI_MI_ONTOLOGY.equalsIgnoreCase( field.getType() ) ) {
                    miIdentifier = OlsUtils.PSI_MI_ONTOLOGY + ":" + field.getValue();
                } else if ( "psi-mi".equalsIgnoreCase( field.getType() ) ) {//handles of type psi-mi:"MI:1002"
                    miIdentifier = field.getValue();
                }

                //get Parent CVS for the given MI Identifier
                currentFieldListWithParents = getListOfFieldsWithParents( miIdentifier, ontology, excludeRoot );
            } else if ( OlsUtils.GO_ONTOLOGY.equals( ontology ) ) {
                if ( OlsUtils.GO_ONTOLOGY.equalsIgnoreCase( field.getType() ) ) {

                    if ( field.getType() != null ) {
                        if ( field.getValue().startsWith( "GO:" ) ) {
                            miIdentifier = field.getValue();
                        } else {
                            miIdentifier = OlsUtils.GO_ONTOLOGY + ":" + field.getValue();
                        }
                    }
                    currentFieldListWithParents = getListOfFieldsWithParents( miIdentifier, ontology, excludeRoot );
                }


            } else {
                throw new IllegalArgumentException( "Unsupported ontology" );
            }

            if ( currentFieldListWithParents.size() > 0 ) {
                allFieldsList.addAll( currentFieldListWithParents );
            }

        }//end for


        return new Column( allFieldsList );
    }

    private Term getCvTerm( String miIdentifier,String ontology )  {
        Term term = null;
        try {
            if(OlsUtils.PSI_MI_ONTOLOGY.equals( ontology )){
            term = OlsUtils.getMiTerm( miIdentifier );
            }else if(OlsUtils.GO_ONTOLOGY.equals( ontology )){
            term = OlsUtils.getGoTerm( miIdentifier );  
            }
        } catch ( RemoteException e ) {
            e.printStackTrace();
        }
        return term;
    }

    /**
     *
     * @param identifier   Cv identifier 
     * @param ontology     where MI or GO etc.,
     * @param excludeRoot  whether to exclude the root term or the cv
     * @return list of cv terms with parents and itself
     * @throws RemoteException thrown by OlsUtils
     */
    private List<Term> getAllParents( String identifier,String ontology,boolean excludeRoot ) throws RemoteException {
        List<Term> allParents = null;
        try{
        OlsClient olsClient = new OlsClient();
        Query ontologyQuery = olsClient.getOntologyQuery();
        if(OlsUtils.PSI_MI_ONTOLOGY.equals( ontology )){
        allParents = OlsUtils.getAllParents( identifier, OlsUtils.PSI_MI_ONTOLOGY, ontologyQuery, new ArrayList<Term>(), excludeRoot );
        }else if(OlsUtils.GO_ONTOLOGY.equals( ontology )){
         allParents = OlsUtils.getAllParents( identifier, OlsUtils.GO_ONTOLOGY, ontologyQuery, new ArrayList<Term>(), excludeRoot );

        }
        } catch ( RemoteException e ) {
            e.printStackTrace();
        }
        return allParents;

    }

    public List<psidev.psi.mi.tab.model.builder.Field> getListOfFieldsWithParents( String identifier,String ontology,boolean excludeRoot ) {
        if ( identifier == null ) {
            throw new NullPointerException( "You must give a non null identifier" );
        }

        if ( ontology == null ) {
            throw new NullPointerException( "You must give a non null ontology" );
        }




        Term term = getCvTerm( identifier,ontology );
        if ( term == null ) {
            throw new IllegalArgumentException( "No cv term found for the givem MI -> " + identifier );
        }

        List<psidev.psi.mi.tab.model.builder.Field> fields = new ArrayList<psidev.psi.mi.tab.model.builder.Field>();

        //Construct new field
        final String[] strings = term.getId().split( ":" );
        String id = strings[0];
        String value = strings[1];
        String description = term.getName();
        psidev.psi.mi.tab.model.builder.Field currentCv = new psidev.psi.mi.tab.model.builder.Field( id, value, description );
        fields.add( currentCv );
        if ( log.isTraceEnabled() ) {
            log.trace( "currentCv " + currentCv.toString() );
        }

        //deal with parents
        List<Term> allParents = null;
        try {
            allParents = getAllParents( identifier,ontology, excludeRoot );
        } catch ( RemoteException e ) {
            e.printStackTrace();
        }


        if ( allParents != null ) {
            for ( Term parentTerm : allParents ) {
                final String[] strings_ = parentTerm.getId().split( ":" );
                String parentId = strings_[0];
                String parentValue = strings_[1];
                String parentDescription = parentTerm.getName();
                psidev.psi.mi.tab.model.builder.Field parentCv = new psidev.psi.mi.tab.model.builder.Field( parentId, parentValue, parentDescription );
                fields.add( parentCv );
                if ( log.isTraceEnabled() ) {
                log.trace( "parentCv " + parentCv.toString() );
                }
            }
        }

        return fields;
    }



    /**
     * Gets only the value part surround with brackets
     * @param column the Column with a collection of fields from which the desc has to be isolated
     * @return  a concatinated String of desc
     */
    public String isolateDescriptions(Column column)
    {
        StringBuilder sb = new StringBuilder(256);

        for (Iterator<psidev.psi.mi.tab.model.builder.Field> iterator = column.getFields().iterator(); iterator.hasNext();) {
            psidev.psi.mi.tab.model.builder.Field field = iterator.next();

            if (field.getDescription() != null) {
                sb.append(field.getDescription());
            }

            if (iterator.hasNext()) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }
}
