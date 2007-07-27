/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.persister;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.checker.ControlledVocabularyRepository;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.checker.FeatureChecker;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.checker.XrefChecker;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.FeatureTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.LocationTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;

import java.util.Iterator;

/**
 * That class persists a feature and its ranges if any.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class FeaturePersister {

    public static void persist( final FeatureTag featureTag,
                                final Component component,
                                final Protein protein ) throws IntactException {

        String typeId = featureTag.getFeatureType().getPsiDefinition().getId();
        CvFeatureType featureType = FeatureChecker.getCvFeatureType( typeId );

        Institution institution = IntactContext.getCurrentInstance().getInstitution();

        Feature feature = new Feature( institution,
                                       featureTag.getShortlabel(),
                                       component,
                                       featureType );

        feature.setFullName( featureTag.getFullname() );

        String detectionId = null;
        if ( featureTag.hasFeatureDetection() ) {
            detectionId = featureTag.getFeatureDetection().getPsiDefinition().getId();
            CvFeatureIdentification featureDetection = FeatureChecker.getCvFeatureIdentification( detectionId );
            feature.setCvFeatureIdentification( featureDetection );
        }

        // persist the object
        IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getFeatureDao().persist( feature );

        // add xrefs if any
        for ( Iterator iterator1 = featureTag.getXrefs().iterator(); iterator1.hasNext(); ) {
            XrefTag xrefTag = (XrefTag) iterator1.next();

            CvXrefQualifier qualifier = null;
            if ( xrefTag.isPrimaryRef() ) {
                qualifier = ControlledVocabularyRepository.getPrimaryXrefQualifier();
            }

            final FeatureXref xref = new FeatureXref( institution,
                                        XrefChecker.getCvDatabase( xrefTag.getDb() ),
                                        xrefTag.getId(),
                                        xrefTag.getSecondary(),
                                        xrefTag.getVersion(),
                                        qualifier );
            feature.addXref( xref );
            IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getXrefDao().persist( xref );
        }

        LocationTag location = featureTag.getLocation();
        if ( location != null ) {
            // create a range

            // extract the associated subsequence from the protein
            String sequence = null;
            if ( protein != null ) {
                sequence = protein.getSequence();
            }

            Range range = new Range( institution,
                                     (int) location.getFromIntervalStart(),
                                     (int) location.getFromIntervalEnd(),
                                     (int) location.getToIntervalStart(),
                                     (int) location.getToIntervalEnd(),
                                     sequence );

            // right now we are dealing with simple feature, they don't interact together
            range.setLinked( false );

            // this must be done after setting the fyzzy types
            range.setUndetermined();

            // associate the range to a feature
            feature.addRange( range );

            IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getRangeDao().persist( range );
        }
    }
}