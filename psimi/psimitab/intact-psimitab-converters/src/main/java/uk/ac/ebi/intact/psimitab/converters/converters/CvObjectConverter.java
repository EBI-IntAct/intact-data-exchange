/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.psimitab.converters.converters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import uk.ac.ebi.intact.jami.model.extension.CvTermXref;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;

import java.util.ArrayList;
import java.util.Collection;

/**
 * CvObject Converter.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class CvObjectConverter {

    public static final Log logger = LogFactory.getLog( CvObjectConverter.class );
    private CrossReferenceConverter<CvTermXref> crossRefConverter;

    public CvObjectConverter(){
        this.crossRefConverter = new CrossReferenceConverter<>();
    }

    public CrossReference toCrossReference( IntactCvTerm cvObject )  {
        if ( cvObject == null ) {
            throw new IllegalArgumentException( "CvObject must not be null. " );
        }

        // name of the cv is the fullname
        String text = cvObject.getFullName()!= null ? cvObject.getFullName() : cvObject.getShortName();
        String identity = cvObject.getMIIdentifier();

        if(identity == null ) {
            throw new NullPointerException( cvObject.getClass().getSimpleName() + "("+ text +") didn't have an identity" );
        }

        final CvTermXref idXref = findMatchingIdentityXref(cvObject.getXrefs(), identity); //XrefUtils.getIdentityXref(cvObject, CvDatabase.PSI_MI_MI_REF);

        if (idXref != null){
            try {
                CrossReference ref = crossRefConverter.createCrossReference(idXref, false);
                ref.setText(text);
                return ref;
            } catch ( Exception e ) {
                throw new RuntimeException( "An exception occured while building a cv object : " + text, e );
            }
        }
        else {
            CrossReference ref = new CrossReferenceImpl(CrossReferenceConverter.DATABASE_UNKNOWN, identity, text);
            return ref;
        }
    } 
    
    private CvTermXref findMatchingIdentityXref(Collection<Xref> xrefs, String identity){

        CvTermXref identityRef = null;
        
        Collection<CvTermXref> identities = new ArrayList<>(xrefs.size());
        
        for (Xref xref : xrefs){
             if (xref.getQualifier() != null && Xref.IDENTITY_MI.equalsIgnoreCase(xref.getQualifier().getMIIdentifier())){
                 if (identity.equalsIgnoreCase(xref.getId())){
                     identityRef = (CvTermXref) xref;
                     break;
                 }
             }
            else {
                identities.add((CvTermXref) xref);
            }
        }

        // in case the identity ref is null but we have xrefs matching the identifier, we take the first identity xref
        if (identityRef == null && !identities.isEmpty()){
            identityRef = identities.iterator().next();
        }

        return identityRef;
    }
}
