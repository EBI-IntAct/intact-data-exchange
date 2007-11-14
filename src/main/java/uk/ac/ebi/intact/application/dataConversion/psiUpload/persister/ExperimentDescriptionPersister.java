/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.persister;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.checker.*;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.AnnotationTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ExperimentDescriptionTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.ExperimentDao;
import uk.ac.ebi.intact.util.cdb.UpdateExperimentAnnotationsFromPudmed;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * That class make the data persitent in the Intact database. <br> That class takes care of an Experiments object. <br>
 * It assumes that the data are already parsed and passed the validity check successfully.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentDescriptionPersister {

    // cache created experiment
    // shortlabel -> Experiment
    private static Map cache = new HashMap();


    /**
     * Make an ExperimentTag persistent as an Intact Experiemnt. <br> (1) check if the experiment is not existing in
     * IntAct, if so, reuse it <br> (2) check if it has been made paersistent already, if so, reuse it. <br> (3) else,
     * make it persistent.
     *
     * @param experimentDescription the data from which we want to make an Experiment persistent
     *
     * @return either an already existing Experiment in IntAct or a brand new one created out of the data present in the
     *         PSI file
     *
     * @throws IntactException
     */
    public static Experiment persist( final ExperimentDescriptionTag experimentDescription )
            throws IntactException {

        ExperimentDao expDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getExperimentDao();
        Institution institution = IntactContext.getCurrentInstance().getInstitution();

        Experiment experiment;
        final String shortlabel = experimentDescription.getShortlabel();

        // (1) check if the experiment is not existing in IntAct, if so, reuse it
        experiment = ExperimentDescriptionChecker.getIntactExperiment( shortlabel );
        if ( experiment != null ) {
            // we made the choice to trust the IntAct database content, hence, not to alter it.
            // altimately, this can be manually edited through the editor afterward.
            return experiment;
        }

        // (2) check if it has been made persistent already, if so, reuse it.
        experiment = (Experiment) cache.get( shortlabel );

        if ( experiment != null ) {
            // already created ... reuse it !
            return experiment;
        }

        // (3) Create a new Experiment
        final BioSource biosource = HostOrganismChecker.getBioSource( experimentDescription.getHostOrganism() );

        experiment = new Experiment( institution, shortlabel, biosource );

        expDao.persist( experiment );

        experiment.setFullName( experimentDescription.getFullname() );

        // CvIdentification
        final String participantDetectionId = experimentDescription.getParticipantDetection().getPsiDefinition().getId();
        final CvIdentification cvIdentification = ParticipantDetectionChecker.getCvIdentification( participantDetectionId );
        experiment.setCvIdentification( cvIdentification );

        // CvInteraction
        final String interactionDetectionId = experimentDescription.getInteractionDetection().getPsiDefinition().getId();
        final CvInteraction cvInteraction = InteractionDetectionChecker.getCvInteraction( interactionDetectionId );
        experiment.setCvInteraction( cvInteraction );

        // Primary Xrefs: pubmed
        final XrefTag bibRef = experimentDescription.getBibRef();
        final ExperimentXref primaryXref = new ExperimentXref( institution,
                                           XrefChecker.getCvDatabase( bibRef.getDb() ),
                                           bibRef.getId(),
                                           bibRef.getSecondary(),
                                           bibRef.getVersion(),
                                           ControlledVocabularyRepository.getPrimaryXrefQualifier() );

        experiment.addXref( primaryXref );
        IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getXrefDao().persist( primaryXref );

        // based on that primary-reference, retreive information from CDB and update experiment's annotations.
        String pubmedId = primaryXref.getPrimaryId();
        System.out.print( "Updating experiment details from CitExplore..." );
        System.out.flush();
        UpdateExperimentAnnotationsFromPudmed.update( experiment, pubmedId );
        System.out.println( "done." );

        // BibRefs: primary and secondary.
        final Collection secondaryPubmedXrefs = experimentDescription.getAdditionalBibRef();
        for ( Iterator iterator = secondaryPubmedXrefs.iterator(); iterator.hasNext(); ) {
            XrefTag xrefTag = (XrefTag) iterator.next();
            ExperimentXref seeAlsoXref = new ExperimentXref( institution,
                                         XrefChecker.getCvDatabase( xrefTag.getDb() ),
                                         xrefTag.getId(),
                                         xrefTag.getSecondary(),
                                         xrefTag.getVersion(),
                                         ControlledVocabularyRepository.getSeeAlsoXrefQualifier() );

            experiment.addXref( seeAlsoXref );
            IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getXrefDao().persist( seeAlsoXref );
        }

        // annotations
        final Collection annotations = experimentDescription.getAnnotations();
        for ( Iterator iterator = annotations.iterator(); iterator.hasNext(); ) {
            final AnnotationTag annotationTag = (AnnotationTag) iterator.next();
            final CvTopic cvTopic = AnnotationChecker.getCvTopic( annotationTag.getType() );

            // search for an annotation to re-use, instead of creating a new one.
            Annotation annotation = searchIntactAnnotation( annotationTag );

            if ( annotation == null ) {
                // doesn't exist, then create a new Annotation
                annotation = new Annotation( institution, cvTopic );
                annotation.setAnnotationText( annotationTag.getText() );
                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getAnnotationDao().persist( annotation );
            }

            experiment.addAnnotation( annotation );
        }

        // other xrefs
        final Collection xrefs = experimentDescription.getXrefs();
        for ( Iterator iterator = xrefs.iterator(); iterator.hasNext(); ) {
            XrefTag xrefTag = (XrefTag) iterator.next();

            ExperimentXref xref = new ExperimentXref( IntactContext.getCurrentInstance().getInstitution(),
                                  XrefChecker.getCvDatabase( xrefTag.getDb() ),
                                  xrefTag.getId(),
                                  xrefTag.getSecondary(),
                                  xrefTag.getVersion(),
                                  null );

            experiment.addXref( xref );
            IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getXrefDao().persist( xref );
        }

        expDao.update( experiment );

        // cache it.
        cache.put( experiment.getShortLabel(), experiment );

        return experiment;
    }

    /**
     * Search in IntAct for an Annotation having the a specific type and annotationText.
     *
     * @param annotationTag the description of the Annotation we are looking for.
     *
     * @return the found Annotation or null if not found.
     *
     * @throws IntactException
     */
    private static Annotation searchIntactAnnotation( final AnnotationTag annotationTag  )
            throws IntactException {

        final String text = annotationTag.getText();

        Collection annotations = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getAnnotationDao().getByTextLike(text);
        Annotation annotation = null;

        if ( annotations != null ) {
            for ( Iterator iterator = annotations.iterator(); iterator.hasNext() && annotation == null; ) {
                Annotation anAnnotation = (Annotation) iterator.next();
                if ( annotationTag.getType().equals( anAnnotation.getCvTopic().getShortLabel() ) ) {
                    annotation = anAnnotation;
                }
            }
        }

        return annotation;
    }
}
