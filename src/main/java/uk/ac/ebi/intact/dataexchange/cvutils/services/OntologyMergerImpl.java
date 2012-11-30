/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.cvutils.services;

import org.obo.datamodel.*;
import org.obo.datamodel.impl.OBORestrictionImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;


/**
 * A class to merge two OBOSession objects based on the criteria in the OntologyMergeConfig object
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
public class OntologyMergerImpl implements OntologyMerger {

    private static final Log log = LogFactory.getLog( OntologyMergerImpl.class );


    OntologyMergeConfig omc;

    public OBOSession merge( OntologyMergeConfig omc, OBOSession source, OBOSession target ) {

        this.omc = omc;

        OBOSession mergedTargedSession;
        mergedTargedSession = getMergedSession( source, target );

        return mergedTargedSession;
    }


    public OBOSession getMergedSession( OBOSession source, OBOSession target ) {
        if ( source == null ) {
            throw new NullPointerException( "You must give a non null source" );
        }

        if ( target == null ) {
            throw new NullPointerException( "You must give a non null target" );
        }

        Collection<IdentifiedObject> allChildObjects = new ArrayList<IdentifiedObject>();

        //get all the term and children for the given sourceTerm
        Collection<IdentifiedObject> children = getTermAndChildren( allChildObjects, source );

        OBOObject targetTerm = ( OBOObject ) target.getObject( omc.getTargetOntologyTerm() );
        OBOObject sourceTerm = ( OBOObject ) source.getObject( omc.getSourceOntologyTerm() );

        if ( log.isDebugEnabled() ) {
            log.debug( " targetTerm ID: " + targetTerm.getID() + " Name: " + targetTerm.getName() + " Children: " + targetTerm.getChildren().size() );
            log.debug( " sourceTerm ID: " + sourceTerm.getID() + " Name: " + sourceTerm.getName() + " Children: " + sourceTerm.getChildren().size() );
            log.debug( " sourceChildren size: " + children.size() );
        }

        //add all the children to the target
        for ( IdentifiedObject child : children ) {
            target.addObject( child );
        }

        if ( omc.isRecursive() ) {
            if ( omc.isIncludeSourceOntologyTerm() ) {
                //set the relationship here directly to the source and target
                OBOClass isAparent = ( OBOClass ) targetTerm;
                Link linkToIsA = new OBORestrictionImpl( sourceTerm );
                linkToIsA.setType( OBOProperty.IS_A );
                isAparent.addChild( linkToIsA );
            } else {
                //set the relationship to the sources children and target
                OBOClass isAparent = ( OBOClass ) targetTerm;
                for ( Link child : sourceTerm.getChildren() ) {
                    Link linkToIsA = new OBORestrictionImpl( child.getChild() );
                    linkToIsA.setType( OBOProperty.IS_A );
                    isAparent.addChild( linkToIsA );

                }
            }
        }

        OBOObject targetTermAfter = ( OBOObject ) target.getObject( omc.getTargetOntologyTerm() );
        if ( log.isDebugEnabled() ) {
            log.debug( " targetTermAfter " + targetTermAfter.getID() + "  " + targetTermAfter.getName() + "  " + targetTermAfter.getChildren().size() );
        }

        return target;
    }


    private Collection<IdentifiedObject> getTermAndChildren( Collection<IdentifiedObject> allChildObjects, OBOSession source ) {

        if ( omc.isIncludeSourceOntologyTerm() ) {
            allChildObjects.add( source.getObject( omc.getSourceOntologyTerm() ) );
        }
        if ( omc.isRecursive() ) {
            allChildObjects.addAll( getChildren( source, omc.getSourceOntologyTerm() ) );
        }
        return allChildObjects;
    }//end method


    private Collection<IdentifiedObject> getChildren( OBOSession source, String miTerm ) {

        OBOObject parentTerm = ( OBOObject ) source.getObject( miTerm );

        Collection<Link> childLinks = parentTerm.getChildren();
        Collection<IdentifiedObject> children = new ArrayList<IdentifiedObject>();

        for ( Link childLink : childLinks ) {
            OBOObject childTerm = ( OBOObject ) childLink.getChild();
            children.add( childTerm );
            children.addAll( getChildren( source, childTerm.getID() ) );
        }

        return children;
    }


    public OntologyMergeConfig getOmc() {
        return omc;
    }

    public void setOmc( OntologyMergeConfig omc ) {
        this.omc = omc;
    }
}
