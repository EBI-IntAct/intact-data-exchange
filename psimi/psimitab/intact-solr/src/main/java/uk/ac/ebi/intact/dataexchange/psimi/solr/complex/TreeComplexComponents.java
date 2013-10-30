package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.solr.common.SolrInputDocument;
import uk.ac.ebi.intact.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 *  This class is a wrapper for the TreeComponents class
 *  because the first level is an special case.
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 29/10/13
 */

public class TreeComplexComponents {

    /*************************/
    /*      Constructor      */
    /*************************/
    public TreeComplexComponents(InteractionImpl c) {
        complex = c ;
        complexSons = new ArrayList();
        for ( Component component : complex.getComponents() ) {
            complexSons.add ( new TreeComponents ( component ) );
        }
    }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    public int getNumberOfParticipants() {
        int number = 0 ;
        for ( TreeComponents tree : complexSons ) {
            number += tree.getNumberOfParticipants() ;
        }
        return number ;
    }

    public boolean getSTC() {
        boolean STC = false ;
        for ( TreeComponents tree : complexSons ) {
            STC |= tree.getSTC() ;
        }
        return STC ;
    }

    public SolrInputDocument setFields( SolrInputDocument solrDocument ) {
        for ( TreeComponents tree : complexSons ) {
            tree.setFields(solrDocument);
        }
        return setFields(solrDocument, complex);
    }

    /*********************************/
    /*      Private Tree Class      */
    /*********************************/
    private class TreeComponents {

        /*******************************/
        /*      Tree Constructor       */
        /*******************************/
        public TreeComponents( Component component_param ) {
            root = new TreeNode ( component_param ) ;
        }

        /***************************************/
        /*       Tree Getters and Setters      */
        /***************************************/
        public int getNumberOfParticipants(){
            return root.getNumberOfParticipants() ;
        }

        public boolean getSTC() {
            return root.getSTC() ;
        }

        public SolrInputDocument setFields( SolrInputDocument solrDocument ) {
            return root.setFields ( solrDocument ) ;
        }

        /************************************/
        /*      Private TreeNode Class      */
        /************************************/
        private class TreeNode{

            /*******************************/
            /*      TreeNode Constructor   */
            /*******************************/
            public TreeNode( Component component_param) {
                component = component_param ;
                if ( component.getInteractor() instanceof InteractionImpl ) {
                    sons = new ArrayList();
                    for ( Component comp : ((InteractionImpl) component.getInteractor()).getComponents() ) {
                        TreeComponents tree = null ;
                        if ( ! map.containsKey ( comp.getAc() ) ) {
                            tree = new TreeComponents ( comp ) ;
                            map.put ( comp.getAc(), tree ) ;
                        }
                        else {
                            tree = map.get ( comp.getAc() ) ;
                        }
                        sons.add ( tree ) ;
                    }
                }
                else {
                    sons = null ;
                }
            }

            /*******************************************/
            /*       TreeNode Getters and Setters      */
            /*******************************************/
            public boolean                      hasSons()       { return sons != null ; }
            public Component                    getComponent()  { return component    ; }
            public Collection<TreeComponents>   getSons()       { return sons         ; }

            public int                          getNumberOfParticipants()   {
                if ( numberOfParticipants == null ) {
                    int stoichiometry = (int) component.getStoichiometry() ;
                    numberOfParticipants = stoichiometry == 0 ? 1 : stoichiometry ;
                    if ( hasSons() ) {
                        for ( TreeComponents tree : sons ) {
                            numberOfParticipants += tree.getNumberOfParticipants();
                        }
                    }
                }
                return numberOfParticipants ;
            }

            public boolean getSTC() {
                if ( STC_flag == 0 ) {
                    STC = component.getStoichiometry() != 0.0f ;
                    if ( hasSons() ){
                        for ( TreeComponents tree : sons ) {
                            STC |= tree.getSTC() ;
                        }
                    }
                    STC_flag = 1 ;
                }

                return STC;
            }

            public SolrInputDocument setFields(SolrInputDocument solrDocument) {
                return TreeComplexComponents.setFields(solrDocument, component.getInteractor());
            }

            /*****************************************/
            /*      Private TreeNode attributes      */
            /*****************************************/
            private Component component = null ;
            private Collection<TreeComponents> sons = null ;
            private Integer numberOfParticipants = null ;
            private boolean STC;
            private int STC_flag = 0;
        } // End TreeNode Class

        /*************************************/
        /*      Private Tree attributes      */
        /*************************************/
        private TreeNode root = null ;
    } // End Tree Class


    /***********************************/
    /*      Private static method      */
    /***********************************/
    private static SolrInputDocument setFields(SolrInputDocument solrDocument, Interactor interactor) {
        String shortLabel = null ;
        String ID = null;
        CvXrefQualifier cvXrefQualifier = null ;
        solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, interactor.getAc ( ) ) ;
        for ( InteractorXref xref : interactor.getXrefs ( ) ) {
            cvXrefQualifier = xref.getCvXrefQualifier ( ) ;
            if ( cvXrefQualifier != null &&
                    (  cvXrefQualifier.getIdentifier ( ) .equals ( CvXrefQualifier.IDENTITY_MI_REF )
                            || cvXrefQualifier.getIdentifier ( ) .equals ( CvXrefQualifier.SECONDARY_AC_MI_REF )
                            || cvXrefQualifier.getShortLabel ( ) .equalsIgnoreCase ( "intact-secondary" ) ) ) {
                shortLabel = xref.getCvDatabase ( ) .getShortLabel ( ) ;
                ID = xref.getPrimaryId() ;
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, ID ) ;
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, shortLabel ) ;
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID,
                        new StringBuilder ( ) .append ( shortLabel ) .append ( ":" ) .append ( ID ) .toString ( ) ) ;
            }
            else {
                if ( xref.getCvDatabase ( ) .getIdentifier() .equals(CvDatabase.GO_MI_REF) ) {
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF_ONTOLOGY, ID ) ;
                }
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF, ID ) ;
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF, shortLabel ) ;
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF,
                        new StringBuilder ( ) .append ( shortLabel ) .append ( ":" ) .append ( ID ) .toString ( ) ) ;
                if ( xref.getCvXrefQualifier ( ) != null ){
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_XREF, cvXrefQualifier.getShortLabel() ) ;
                }
            }
        }
        return solrDocument;
    }

    /********************************/
    /*      Private attributes      */
    /********************************/
    private InteractionImpl complex = null ;
    private Collection<TreeComponents> complexSons = null ;
    private HashMap <String,TreeComponents> map = null ;
} // End Wrapper class
