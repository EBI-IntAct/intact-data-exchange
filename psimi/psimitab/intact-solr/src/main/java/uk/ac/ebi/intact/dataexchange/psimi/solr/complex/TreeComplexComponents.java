package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.hibernate.Hibernate;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.ComplexSolrEnricher;
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

    /********************************/
    /*      Private attributes      */
    /********************************/
    private InteractionImpl complex = null ;
    private Collection<TreeComponents> complexSons = null ;
    private HashMap <String,TreeComponents> map = null ;

    /*************************/
    /*      Constructor      */
    /*************************/
    public TreeComplexComponents(InteractionImpl c, ComplexSolrEnricher enricher) {
        complex = c ;
        complexSons = new ArrayList();
        map = new HashMap<String,TreeComponents>();
        for ( Component component : complex.getComponents() ) {
            complexSons.add ( new TreeComponents ( component, enricher ) );
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

    public void indexFields( SolrInputDocument solrDocument ) throws SolrServerException {
        for (TreeComponents comp : complexSons){
            comp.indexFields(solrDocument);
        }
    }

    /*********************************/
    /*      Private Tree Class      */
    /*********************************/
    private class TreeComponents {

        /*************************************/
        /*      Private Tree attributes      */
        /*************************************/
        private TreeNode root = null ;

        /*******************************/
        /*      Tree Constructor       */
        /*******************************/
        public TreeComponents( Component component_param, ComplexSolrEnricher enricher ) {
            root = new TreeNode ( component_param, enricher ) ;
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

        public void indexFields( SolrInputDocument solrDocument ) throws SolrServerException {
            root.indexFields( solrDocument );
        }

        /************************************/
        /*      Private TreeNode Class      */
        /************************************/
        private class TreeNode{
            /*****************************************/
            /*      Private TreeNode attributes      */
            /*****************************************/
            private Component component = null ;
            private Collection<TreeComponents> sons = null ;
            private Integer numberOfParticipants = null ;
            private boolean STC;
            private int STC_flag = 0;
            private ComplexSolrEnricher enricher;

            /*******************************/
            /*      TreeNode Constructor   */
            /*******************************/
            public TreeNode( Component component_param, ComplexSolrEnricher enricher) {
                component = component_param ;
                this.enricher = enricher;
                if ( component.getInteractor() instanceof InteractionImpl ) {
                    Hibernate.initialize(((InteractionImpl) component.getInteractor()).getComponents());
                    sons = new ArrayList();
//                    System.out.println("Component AC: " + component.getAc());
//                    System.out.println("Component interactor: " + component.getInteractor());
//                    System.out.println("Component interactor compoenents: " + ((InteractionImpl) component.getInteractor()).getComponents()) ;
                    for ( Component comp : ((InteractionImpl) component.getInteractor()).getComponents() ) {
                        TreeComponents tree = null ;
//                        System.out.println("Component: " + comp);
//                        System.out.println("Component son AC: " + comp.getAc());
                        if ( ! map.containsKey ( comp.getAc() ) ) {
                            tree = new TreeComponents ( comp, enricher ) ;
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

            public int getNumberOfParticipants()   {
                if ( numberOfParticipants == null ) {
                    numberOfParticipants = 0;
                    int stoichiometry = (int) component.getStoichiometry() ;
                    int number = stoichiometry == 0 ? 1 : stoichiometry ;
                    if ( hasSons() ) {
                        for ( TreeComponents tree : sons ) {
                            numberOfParticipants += number * tree.getNumberOfParticipants();
                        }
                    }
                    else{
                        numberOfParticipants = number;
                    }
                }
                return numberOfParticipants;
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

            protected void indexInteractorAC(Interactor interactor,
                                             SolrInputDocument solrDocument){
                // search fields
                solrDocument.addField(ComplexFieldNames.INTERACTOR_ID, interactor.getAc()) ;
                // index source of complex id
                if ( interactor.getOwner() != null ) {
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, interactor.getOwner().getShortLabel() ) ;
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_ID, interactor.getOwner().getShortLabel() + ":" + interactor.getAc() ) ;
                }
            }

            protected void indexInteractorNames(Interactor interactor,
                                             SolrInputDocument solrDocument){

                // shortname
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, interactor.getShortLabel ( ) ) ;
                // fullname
                if (interactor.getFullName() != null){
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, interactor.getFullName ( ) ) ;
                }
                // aliases
                for ( Alias alias : interactor.getAliases ( ) ) {
                    if (alias.getName() != null){
                        if (alias.getCvAliasType() != null){
                            CvAliasType type = alias.getCvAliasType();
                            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, type.getShortLabel() ) ;
                            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, alias.getName() ) ;
                            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, type.getShortLabel()+":"+alias.getName()) ;
                        }
                        else{
                            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, alias.getName());
                        }
                    }
                }
            }

            private void indexFields(SolrInputDocument solrDocument) throws SolrServerException {

                Interactor interactor = component.getInteractor();
                if (interactor != null){
                    // index interactor ac
                    indexInteractorAC(interactor, solrDocument);

                    // index interactor names
                    indexInteractorNames(interactor, solrDocument);

                    // index interactor type
                    enricher.enrichInteractorType(interactor.getCvInteractorType(), solrDocument);

                    // index and enrich xrefs
                    enricher.enrichInteractorXref(interactor.getXrefs(), solrDocument);
                }

                // index biological role
                if (component.getCvBiologicalRole() != null){
                    enricher.enrichBiologicalRole(component.getCvBiologicalRole(), solrDocument);
                }

                // index features
                indexfeatures(solrDocument);

                // enrich sons if necessary
                if (hasSons()){
                    for (TreeComponents t : sons){
                         t.indexFields(solrDocument);
                    }
                }
            }

            protected void indexfeatures(SolrInputDocument doc) {
                for (Feature f : component.getFeatures()){
                    if (f.getCvFeatureType() != null){
                        enricher.enrichFeatureType(f.getCvFeatureType(), doc);
                    }
                }
            }
        } // End TreeNode Class

    } // End Tree Class
} // End Wrapper class
