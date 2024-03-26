package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Interactor;
import psidev.psi.mi.jami.model.ModelledFeature;
import psidev.psi.mi.jami.model.ModelledParticipant;
import psidev.psi.mi.jami.model.Stoichiometry;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.ComplexSolrEnricher;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractor;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledParticipant;

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
    private IntactComplex complex = null ;
    private Collection<TreeComponents> complexSons = null ;
    private HashMap <String,TreeComponents> map = null ;

    /*************************/
    /*      Constructor      */
    /*************************/
    public TreeComplexComponents(IntactComplex c, ComplexSolrEnricher enricher) {
        complex = c ;
        complexSons = new ArrayList();
        map = new HashMap<String,TreeComponents>();
        for ( ModelledParticipant component : complex.getParticipants() ) {
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

    public void indexFields( SolrInputDocument solrDocument ) throws SolrServerException, JsonProcessingException {
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
        public TreeComponents( ModelledParticipant component_param, ComplexSolrEnricher enricher ) {
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

        public void indexFields( SolrInputDocument solrDocument ) throws SolrServerException, JsonProcessingException {
            root.indexFields( solrDocument );
        }

        /************************************/
        /*      Private TreeNode Class      */
        /************************************/
        private class TreeNode{
            /*****************************************/
            /*      Private TreeNode attributes      */
            /*****************************************/
            private ModelledParticipant component = null ;
            private Collection<TreeComponents> sons = null ;
            private Integer numberOfParticipants = null ;
            private boolean STC;
            private int STC_flag = 0;
            private ComplexSolrEnricher enricher;

            /*******************************/
            /*      TreeNode Constructor   */
            /*******************************/
            public TreeNode( ModelledParticipant component_param, ComplexSolrEnricher enricher) {
                component = component_param ;
                this.enricher = enricher;
                if ( component.getInteractor() instanceof IntactComplex ) {
                    sons = new ArrayList();
//                    System.out.println("Component AC: " + component.getAc());
//                    System.out.println("Component interactor: " + component.getInteractor());
//                    System.out.println("Component interactor compoenents: " + ((InteractionImpl) component.getInteractor()).getComponents()) ;
                    for ( ModelledParticipant comp : ((IntactComplex) component.getInteractor()).getParticipants() ) {
                        IntactModelledParticipant modelledParticipant = (IntactModelledParticipant) comp;
                        TreeComponents tree = null ;
//                        System.out.println("Component: " + comp);
//                        System.out.println("Component son AC: " + comp.getAc());
                        if ( ! map.containsKey ( modelledParticipant.getAc() ) ) {
                            tree = new TreeComponents ( comp, enricher ) ;
                            map.put ( modelledParticipant.getAc(), tree ) ;
                        }
                        else {
                            tree = map.get ( modelledParticipant.getAc() ) ;
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
            public ModelledParticipant          getComponent()  { return component    ; }
            public Collection<TreeComponents>   getSons()       { return sons         ; }

            public int getNumberOfParticipants()   {
                if ( numberOfParticipants == null ) {
                    numberOfParticipants = 0;
                    Stoichiometry stoichiometry = component.getStoichiometry() ;
                    int number = (stoichiometry != null && stoichiometry.getMinValue() > 0) ? stoichiometry.getMinValue() : 1;
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
                    Stoichiometry stoichiometry = component.getStoichiometry();
                    STC = stoichiometry != null && stoichiometry.getMinValue() != 0;
                    if ( hasSons() ){
                        for ( TreeComponents tree : sons ) {
                            STC |= tree.getSTC() ;
                        }
                    }
                    STC_flag = 1 ;
                }

                return STC;
            }

            protected void indexInteractorAC(IntactInteractor interactor,
                                             SolrInputDocument solrDocument){
                // search fields
                solrDocument.addField(ComplexFieldNames.INTERACTOR_ID, interactor.getAc()) ;
                // index source of complex id
                if (interactor instanceof IntactComplex) {
                    IntactComplex intactComplex = (IntactComplex) interactor;
                    if (intactComplex.getSource() != null) {
                        solrDocument.addField(ComplexFieldNames.INTERACTOR_ID, intactComplex.getSource().getShortName());
                        solrDocument.addField(ComplexFieldNames.INTERACTOR_ID, intactComplex.getSource().getShortName() + ":" + interactor.getAc());
                    }
                }
            }

            protected void indexInteractorNames(IntactInteractor interactor,
                                                SolrInputDocument solrDocument){

                // shortname
                solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, interactor.getShortName ( ) ) ;
                // fullname
                if (interactor.getFullName() != null){
                    solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, interactor.getFullName ( ) ) ;
                }
                // aliases
                for ( Alias alias : interactor.getAliases ( ) ) {
                    if (alias.getName() != null){
                        if (alias.getType() != null){
                            CvTerm type = alias.getType();
                            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, type.getShortName() ) ;
                            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, alias.getName() ) ;
                            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, type.getShortName()+":"+alias.getName()) ;
                        }
                        else{
                            solrDocument.addField ( ComplexFieldNames.INTERACTOR_ALIAS, alias.getName());
                        }
                    }
                }
            }

            private void indexFields(SolrInputDocument solrDocument) throws SolrServerException, JsonProcessingException {

                Interactor interactor = component.getInteractor();
                if (interactor != null){
                    IntactInteractor intactInteractor = (IntactInteractor) interactor;

                    // index interactor ac
                    indexInteractorAC(intactInteractor, solrDocument);

                    // index interactor names
                    indexInteractorNames(intactInteractor, solrDocument);

                    // index interactor type
                    enricher.enrichInteractorType((IntactCvTerm) interactor.getInteractorType(), solrDocument);

                    // index and enrich identifiers and xrefs
                    enricher.enrichInteractorXref(interactor.getIdentifiers(), solrDocument);
                    enricher.enrichInteractorXref(interactor.getXrefs(), solrDocument);
                }

                // index biological role
                if (component.getBiologicalRole() != null){
                    enricher.enrichBiologicalRole((IntactCvTerm) component.getBiologicalRole(), solrDocument);
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
                for (ModelledFeature f : component.getFeatures()){
                    if (f.getType() != null){
                        enricher.enrichFeatureType((IntactCvTerm) f.getType(), doc);
                    }
                }
            }
        } // End TreeNode Class

    } // End Tree Class
} // End Wrapper class
