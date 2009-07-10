/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 *//**
  * That class keeps some required IntAct object such as some controlled vacabulary terms.
  * This is a singleton.
  *
  * @author Samuel Kerrien (skerrien@ebi.ac.uk)
  * @version $Id$
  */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.context.CvContext;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvAliasType;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.CvXrefQualifier;

public class ControlledVocabularyRepository {

    private static boolean initialisationDone = false;

    private static CvTopic authorConfidenceTopic = null;
    private static CvTopic noUniprotUpdate = null;
    private static CvXrefQualifier primaryReferenceXrefQualifier = null;
    private static CvXrefQualifier identityXrefQualifier = null;
    private static CvXrefQualifier seeAlsoXrefQualifier;
    private static CvAliasType geneName;


    public static void check( ) {
        initialise( );
    }


    /////////////////////////
    // Getters
    public static CvTopic getAuthorConfidenceTopic() {
        return authorConfidenceTopic;
    }

    public static CvTopic getNoUniprotUpdateTopic() {
        return noUniprotUpdate;
    }

    public static CvXrefQualifier getPrimaryXrefQualifier() {
        return primaryReferenceXrefQualifier;
    }

    public static CvXrefQualifier getSeeAlsoXrefQualifier() {
        return seeAlsoXrefQualifier;
    }

    public static CvXrefQualifier getIdentityQualifier() {
        return identityXrefQualifier;
    }

    public static CvAliasType getGeneNameAliasType() {
        return geneName;
    }

    /////////////////////////
    // Init
    private static void initialise( ) {

        if ( initialisationDone == false ) {

            CvContext cvContext = IntactContext.getCurrentInstance().getCvContext();

            // load CVs by shortlabel
            authorConfidenceTopic = cvContext.getByMiRef(CvTopic.class, CvTopic.AUTHOR_CONFIDENCE_MI_REF);
            noUniprotUpdate = cvContext.getByLabel(CvTopic.class, CvTopic.NON_UNIPROT);

            // load CVs by MI reference
            primaryReferenceXrefQualifier = cvContext.getByMiRef( CvXrefQualifier.class, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF );
            seeAlsoXrefQualifier = cvContext.getByMiRef( CvXrefQualifier.class, CvXrefQualifier.SEE_ALSO_MI_REF );
            identityXrefQualifier = cvContext.getByMiRef( CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF );
            geneName = cvContext.getByMiRef(CvAliasType.class, CvAliasType.GENE_NAME_MI_REF );

            initialisationDone = true;
        }
    } // init


}