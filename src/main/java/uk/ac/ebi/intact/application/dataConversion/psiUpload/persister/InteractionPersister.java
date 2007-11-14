/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.persister;

import uk.ac.ebi.intact.application.commons.go.GoXrefHelper;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.checker.*;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.*;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.CommandLineOptions;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.util.SearchReplace;

import java.util.*;

/**
 * That class make the data persitent in the Intact database. <br> That class takes care of an Interaction. <br> It
 * assumes that the data are already parsed and passed the validity check successfully.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class InteractionPersister {

    ////////////////////////////////
    // Constants

    private static int MAX_LENGTH_INTERACTION_SHORTLABEL = 20;

    private static boolean DEBUG = CommandLineOptions.getInstance().isDebugEnabled();
    private static final String NEW_LINE = System.getProperty( "line.separator" );

    ////////////////////////////////
    // Private classes

    /**
     * Carries the result of the search for the non already existing interaction based on the data found in the PSI
     * file. it contains: - a new shortlabel if the interaction doesn't exists. - the experiment to link to.
     */
    public static class ExperimentWrapper {

        private String shortlabel = null;
        private final ExperimentDescriptionTag experiment;

        public ExperimentWrapper( ExperimentDescriptionTag experiment ) {
            this.experiment = experiment;
        }

        public ExperimentDescriptionTag getExperiment() {
            return experiment;
        }

        public void setShortlabel( String shortlabel ) {
            this.shortlabel = shortlabel;
        }

        public String getShortlabel() {
            return shortlabel;
        }

        public boolean hasShortlabel() {
            return shortlabel != null;
        }

        public String toString() {
            return "ExperimentWrapper{" +
                   "experiment=" + experiment.getShortlabel() +
                   ", shortlabel='" + shortlabel + "'" +
                   "}";
        }
    }

    /////////////////////////////
    // Methods

    /**
     * Persist the Interaction according to its PSI description. <br> A Shortlabel is automatically generated based on
     * the gene names of the participant, if no gene name are available we use the protein AC. <br> <br> An Interaction
     * can be linked to multiple experiment in PSI, we do take it into account and we manages cases where an interaction
     * already exists, attached to one Experiment (let's call it E1) and the same interaction is also declared in the
     * PSI file as interacting in E1 and E2, in which case we just create an extra interaction and link it to E2. <br>
     * So far, we do not automatically link several Experiment to a single Interaction using the PSI loader. <br> <br>
     *
     * @param interactionTag the PSI definition of the Interaction
     *
     * @return the collection of created IntAct Interaction.
     *
     * @throws IntactException if an error occurs while persisting the data in IntAct.
     */
    public static Collection persist( final InteractionTag interactionTag )
            throws IntactException {

        Collection interactions = new ArrayList( 1 );

        Institution institution = IntactContext.getCurrentInstance().getInstitution();

        // Generating shortlabels
        Collection e = interactionTag.getExperiments();
        Collection experiments = new ArrayList( e.size() );
        for ( Iterator iterator = e.iterator(); iterator.hasNext(); ) {
            ExperimentDescriptionTag experimentDescription = (ExperimentDescriptionTag) iterator.next();
            experiments.add( new ExperimentWrapper( experimentDescription ) );
        }

        if ( DEBUG ) {
            System.out.println( "Before createShortlabel() " );
            for ( Iterator iterator = experiments.iterator(); iterator.hasNext(); ) {
                ExperimentWrapper experimentWrapper = (ExperimentWrapper) iterator.next();
                System.out.println( experimentWrapper );
            }
        }

        // that updates the experiment collection and only leaves those for which we have to create a new interaction
        createShortlabel( interactionTag, experiments );

        if ( DEBUG ) {
            System.out.println( "After createShortlabel() " );
            for ( Iterator iterator = experiments.iterator(); iterator.hasNext(); ) {
                ExperimentWrapper experimentWrapper = (ExperimentWrapper) iterator.next();
                System.out.println( experimentWrapper );
            }
        }

        // LOOP HERE OVER THE RESULT COLLECTION(shortlabel, experiment)
        for ( Iterator iterator = experiments.iterator(); iterator.hasNext(); ) {
            ExperimentWrapper experimentWrapper = (ExperimentWrapper) iterator.next();

            ExperimentDescriptionTag psiExperiment = experimentWrapper.getExperiment();
            Experiment intactExperiment = ExperimentDescriptionPersister.persist( psiExperiment );

            Collection myExperiments = new ArrayList( 1 );
            myExperiments.add( intactExperiment );

            final String shortlabel = experimentWrapper.getShortlabel();

            /* Components
            * The way to create an Interaction is NOT TRIVIAL: we have firstly to create an
            * Interaction Object with an empty collection of Component. and Then we have to
            * use the interaction.addComponent( Component c ) to fill it up.
            */
            final Collection components = new ArrayList( interactionTag.getParticipants().size() );

            // CvInteractionType
            final String cvInteractionTypeId = interactionTag.getInteractionType().getPsiDefinition().getId();
            CvInteractionType cvInteractionType = InteractionTypeChecker.getCvInteractionType( cvInteractionTypeId );

            // Creation of the Interaction
            Interaction interaction = new InteractionImpl( myExperiments,
                                                           components,
                                                           cvInteractionType,
                                                           InteractionChecker.getCvInteractionType(),
                                                           shortlabel,
                                                           institution );

            // no other choice in PSI
            interaction.setBioSource( intactExperiment.getBioSource() );

            interaction.setFullName( interactionTag.getFullname() );

            // check if there is a Kd value available
            Collection annotations = interactionTag.getAnnotations();
            for ( Iterator iterator1 = annotations.iterator(); iterator1.hasNext(); ) {
                AnnotationTag annotationTag = (AnnotationTag) iterator1.next();

                if ( annotationTag.isDissociationConstant() ) {
                    Float kd = new Float( annotationTag.getText() );
                    interaction.setKD( kd );
                }
            }

            IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInteractionDao().persist((InteractionImpl) interaction );

            interactions.add( interaction );
            System.out.println( "Interaction " + shortlabel + " created under experiment " +
                                intactExperiment.getShortLabel() );

            // Annotations
            annotations = interactionTag.getAnnotations();
            for ( Iterator iterator2 = annotations.iterator(); iterator2.hasNext(); ) {
                final AnnotationTag annotationTag = (AnnotationTag) iterator2.next();
                final CvTopic cvTopic = AnnotationChecker.getCvTopic( annotationTag.getType() );

                if ( cvTopic != null ) {
                    // search for an annotation to re-use, instead of creating a new one.
                    Annotation annotation = searchIntactAnnotation( annotationTag );

                    if ( annotation == null ) {
                        // doesn't exist, then create a new Annotation
                        annotation = new Annotation( institution, cvTopic );
                        annotation.setAnnotationText( annotationTag.getText() );
                        IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getAnnotationDao().persist( annotation );
                    }

                    interaction.addAnnotation( annotation );
                }

                // no need for an else, the additional annotation topic have been controlled by the checker.
            }

            //Xref
            Collection xrefs = interactionTag.getXrefs();
            for ( Iterator iterator1 = xrefs.iterator(); iterator1.hasNext(); ) {
                final XrefTag xrefTag = (XrefTag) iterator1.next();
                final CvDatabase cvDatabase = XrefChecker.getCvDatabase( xrefTag.getDb() );
                if ( cvDatabase != null ) {
                    CvXrefQualifier cvXrefQualifier = null;
                    String secondaryId = null;
                    if ( cvDatabase.getShortLabel().equals( CvDatabase.GO ) ) {
                        GoXrefHelper goXrefHelper = new GoXrefHelper( xrefTag.getId() );
                        if ( goXrefHelper.getQualifier() != null ) {
                            cvXrefQualifier = IntactContext.getCurrentInstance().getCvContext().getByLabel(CvXrefQualifier.class, goXrefHelper.getQualifier());
                        }
                        if ( goXrefHelper.getSecondaryId() != null ) {
                            secondaryId = goXrefHelper.getSecondaryId();
                        }
                    }

                    InteractorXref xref = new InteractorXref( institution, cvDatabase, xrefTag.getId(), secondaryId, null, cvXrefQualifier );
                    interaction.addXref( xref );
                    IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getXrefDao().persist( xref );
                }
            }

            // TODO clean that thing. Right now we are most of the time stuffing the author confidence as annotation
            // Confidence data
            final ConfidenceTag confidence = interactionTag.getConfidence();
            if ( confidence != null ) {
                // TODO look after that unit parameter, we might have to adapt the CvTopic accordingly later.
                final CvTopic authorConfidence = ControlledVocabularyRepository.getAuthorConfidenceTopic();

                // check if that annotation could not be shared.
                Collection _annotations = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getAnnotationDao().getByTextLike(confidence.getValue());

                Annotation annotation = null;
                for ( Iterator iterator3 = _annotations.iterator(); iterator3.hasNext() && annotation == null; ) {
                    Annotation _annotation = (Annotation) iterator3.next();
                    if ( authorConfidence.equals( _annotation.getCvTopic() ) ) {
                        annotation = _annotation;
                    }
                }

                if ( annotation == null ) {
                    // create it !
                    annotation = new Annotation( institution, authorConfidence );
                    annotation.setAnnotationText( confidence.getValue() );
                    IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getAnnotationDao().persist( annotation );
                }

                interaction.addAnnotation( annotation );
            }

            IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInteractionDao().update( (InteractionImpl) interaction );

            // Now process the components...
            final Collection participants = interactionTag.getParticipants();
            for ( Iterator iterator4 = participants.iterator(); iterator4.hasNext(); ) {
                ProteinParticipantTag proteinParticipant = (ProteinParticipantTag) iterator4.next();
                ProteinParticipantPersister.persist( proteinParticipant, interaction );
            }
        } // experiments

        return interactions;
    }

    private static class GeneNameIgnoreCaseComparator implements Comparator {

        ////////////////////////////////////////////////
        // Implementation of the Comparable interface
        ////////////////////////////////////////////////

        /**
         * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive
         * integer as this object is less than, equal to, or greater than the specified object.<p>
         *
         * @param o1 the Object to be compared.
         * @param o2 the Object to compare with.
         *
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater
         *         than the specified object.
         *
         * @throws ClassCastException if the specified object's type prevents it from being compared to this Object.
         */
        public final int compare( final Object o1, final Object o2 ) {

            final String s1 = ( (String) o1 ).toLowerCase();
            final String s2 = ( (String) o2 ).toLowerCase();

            // the current string comes first if it's before in the alphabetical order
            if ( !( s1.equals( s2 ) ) ) {
                return s1.compareTo( s2 );
            } else {
                return 0;
            }
        }

    }

    /**
     * Create an IntAct shortlabel for a given interaction (ie. a set of [protein, role] ).
     * <p/>
     * - Stategy -
     * <p/>
     * Protein's role can be either: bait, prey or neutral the interaction shortlabel has the following patter: X-Y-Z
     * with a limit in length of 20 caracters. TODO: do not hard code that ! Could be fetched from the
     * DatabaseInspector.
     * <p/>
     * X is (in order of preference): 1. the gene name of the bait protein 2. the gene name of a prey protein (the first
     * one in alphabetical order) 3. the gene name of a neutral protein (the first one in alphabetical order)
     * <p/>
     * Y is : 1. the gene name of a prey protein (the first one in alphabetical order or second if the first has been
     * used already) 2. the gene name of a neutral protein (the first one in alphabetical order or second if the first
     * has been used already) Z is : an Integer that gives the number of occurence in intact.
     * <p/>
     * eg. 1. bait(baaa), prey(paaa, pbbb, pccc), neutral(naaa) should gives us: baaa-paaa-1
     * <p/>
     * 2. bait(baaa), prey(), neutral(naaa) should gives us: baaa-naaa-1
     * <p/>
     * 3. bait(), prey(paaa, pbbb, pccc), neutral(naaa) should gives us: paaa-pbbb-1
     * <p/>
     * 4. bait(), prey(paaa), neutral(naaa) should gives us: paaa-naaa-1
     *
     * @param interaction
     *
     * @throws IntactException
     */
    private static void createShortlabel( final InteractionTag interaction,
                                          final Collection experiments )
            throws IntactException {

        Collection baits = new ArrayList( 2 );
        Collection preys = new ArrayList( 2 );
        Collection neutrals = new ArrayList( 2 );

        /**
         * Search for a gene name in the set, if none exist, take the protein ID.
         */
        final Collection proteins = interaction.getParticipants();
        for ( Iterator iterator = proteins.iterator(); iterator.hasNext(); ) {
            ProteinParticipantTag proteinParticipant = (ProteinParticipantTag) iterator.next();
            final String role = proteinParticipant.getRole();
            final ProteinHolder proteinHolder = getProtein( proteinParticipant );

            // the gene name is held in the master protein, not the splice variant.
            final String geneName = getGeneName( proteinHolder );

            // TODO load default CVs via MI reference and use the CvObject.equals() !!!!
            if ( role.equals( "prey" ) ) { // most numerous role, cut down the number of test
                preys.add( geneName );
            } else if ( role.equals( "bait" ) ) {
                baits.add( geneName );
            } else if ( role.equals( "unspecified" ) ) {
                neutrals.add( geneName );
            } else if ( role.equals( "neutral component" ) ) {
                neutrals.add( geneName );
            } else if ( role.equals( "neutral" ) ) {
                // we have changed the name of that CV, from neutral to 'neutral component'
                // for the time being allow both ... PSI 1.0 allow only neutral
                // TODO program a switch, if the database contain only 'neutral component'
                neutrals.add( geneName );
            } else {
                // we should never get in here if RoleChecker plays its role !
                throw new IllegalStateException( "Found role: " + role + " which is not supported at the moment (" +
                                                 "so far only bait, prey and 'neutral component' are). abort." );

            }
        } // for proteins

        // we can have either 1..n bait with 1..n prey
        // or 2..n neutral
        String baitShortlabel = null;
        String preyShortlabel = null;

        if ( baits.isEmpty() && preys.isEmpty() ) {
            // we have only neutral

            Object[] _geneNames = neutrals.toArray();
            Arrays.sort( _geneNames, new GeneNameIgnoreCaseComparator() );

            baitShortlabel = (String) _geneNames[ 0 ];

            if ( _geneNames.length > 2 ) {
                // if more than 2 components, get one and add the cound of others.
                preyShortlabel = ( _geneNames.length - 1 ) + "";
            } else {
                preyShortlabel = (String) _geneNames[ 1 ];
            }

        } else {

            // bait-prey
            baitShortlabel = getLabelFromCollection( baits, true ); // fail on error
            preyShortlabel = getLabelFromCollection( preys, false ); // don't fail on error
            if ( preyShortlabel == null ) {
                preyShortlabel = getLabelFromCollection( neutrals, true ); // fail on error
            }
        }

        // that updates the experiment collection and only leaves those for which we have to create a new interaction
        createInteractionShortLabels( interaction, experiments, baitShortlabel, preyShortlabel );
    }

    /**
     * Search for the first string (in alphabetical order).
     *
     * @param geneNames   a collection of non ordered gene names.
     * @param failOnError if <code>true</code> throw an IntactException when no gene name is found, if
     *                    <code>false</code> sends back <code>null</code>.
     *
     * @return either a String or null according to the failOnError parameter.
     *
     * @throws IntactException thrown when the failOnError parameter is true and no string can be returned.
     * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.persister.InteractionPersister.GeneNameIgnoreCaseComparator
     */
    private static String getLabelFromCollection( Collection geneNames, boolean failOnError ) throws IntactException {
        String shortlabel = null;

        if ( geneNames == null ) {
            throw new IllegalArgumentException( "You must give a non null collection of gene name." );
        }

        switch ( geneNames.size() ) {
            case 0:
                // ERROR, we should have a bait.
                // This should have been detected during step 1 or 2.
                if ( failOnError ) {
                    throw new IntactException( "Could not find gene name for that interaction." );
                }
                break;
            case 1:
                shortlabel = (String) geneNames.iterator().next();
                break;

            default:
                // more than one ... need sorting
                Object[] _geneNames = geneNames.toArray();
                Arrays.sort( _geneNames, new GeneNameIgnoreCaseComparator() );
                shortlabel = (String) _geneNames[ 0 ];
                break;
        }

        return shortlabel;
    }

    /**
     * Create an interaction shortlabel out of two shortlabels. <br> Take care about the maximum length of the field.
     * <br> It checks as well if the generated shortlabel as already been associated to an other Interaction.
     *
     * @param psiInteraction The interaction we are investigating on.
     * @param experiments    Collection in which after processing we have all ExperimentWrapper (shortlabel +
     *                       experimentDescription) in which the interaction hasn't been created yet.
     * @param bait           the label for the bait (could be gene name or SPTR entry AC)
     * @param prey           the label for the prey (could be gene name or SPTR entry AC)
     */
    private static void createInteractionShortLabels( final InteractionTag psiInteraction,
                                                      final Collection experiments,
                                                      String bait,
                                                      String prey )
            throws IntactException {

        // convert bad characters ('-', ' ', '.') to '_'
        bait = bait.toLowerCase();
        bait = SearchReplace.replace( bait, "-", "_" );
        bait = SearchReplace.replace( bait, " ", "_" );
        bait = SearchReplace.replace( bait, ".", "_" );

        prey = prey.toLowerCase();
        prey = SearchReplace.replace( prey, "-", "_" );
        prey = SearchReplace.replace( prey, " ", "_" );
        prey = SearchReplace.replace( prey, ".", "_" );

        int count = 0;
        String _bait = bait;
        String _prey = prey;
        boolean allLabelFound = false;
        String label = null;
        String suffix = null;

        // check out the curation rules to know how to create an interaction shortlabel.
        // http://www3.ebi.ac.uk/internal/seqdb/curators/intact/Intactcurationrules_000.htm

        while ( !allLabelFound ) {

            if ( count == 0 ) {
                suffix = null;
                label = _bait + "-" + _prey;
            } else {
                suffix = "-" + count;
                label = _bait + "-" + _prey + suffix;
            }

            count = ++count;

            // check if truncation needed.
            // if so, remove one character from the longest between bait and prey ... until the length is right.
            while ( label.length() > MAX_LENGTH_INTERACTION_SHORTLABEL ) {
                if ( _bait.length() > _prey.length() ) {
                    _bait = _bait.substring( 0, _bait.length() - 1 ); // truncate, remove last charachter (from bait)
                } else {
                    _prey = _prey.substring( 0, _prey.length() - 1 ); // truncate, remove last charachter (from prey)
                }

                if ( suffix == null ) {
                    label = _bait + "-" + _prey;
                } else {
                    label = _bait + "-" + _prey + suffix;
                }
            } // while

            // we have the right label's size now ... search for existing one !
            if ( DEBUG ) {
                System.out.println( "Search interaction by label: " + label );
            }
            Collection interactions = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInteractionDao().getByShortLabelLike(label);

            if ( interactions.size() == 0 ) {

                if ( DEBUG ) {
                    System.out.println( "No interaction found with the label: " + label );
                }

                // Give the remaining experiment a shortlabel.
                // takes care of gaps in the shortlabel sequence (label-1, label-2, label-3 ...).
                // could create new gaps if some already exists.
                boolean atLeastOneInteractionWithoutShortlabel = false;
                boolean oneExperimentHasAlreadyBeenUpdated = false;
                for ( Iterator iterator = experiments.iterator(); iterator.hasNext() && !atLeastOneInteractionWithoutShortlabel; )
                {
                    ExperimentWrapper experimentWrapper = (ExperimentWrapper) iterator.next();
                    // we want to associate only one shortlabel per loop and check if there is at least one
                    // more experiment to update.
                    if ( DEBUG ) {
                        System.out.println( "Work on " + experimentWrapper );
                    }
                    if ( oneExperimentHasAlreadyBeenUpdated ) {
                        if ( !experimentWrapper.hasShortlabel() ) {
                            atLeastOneInteractionWithoutShortlabel = true; // exit the loop.
                            if ( DEBUG ) {
                                System.out.println( "At least one more experiment to which we have to give a shortlabel" );
                            }
                        } else {
                            if ( DEBUG ) {
                                System.out.println( "has already a shortlabel" );
                            }
                        }
                    } else {
                        if ( !experimentWrapper.hasShortlabel() ) {
                            experimentWrapper.setShortlabel( label );
                            oneExperimentHasAlreadyBeenUpdated = true;
                            if ( DEBUG ) {
                                System.out.println( "Experiment " + experimentWrapper.getExperiment().getShortlabel()
                                                    + " has been given the interaction shortlabel: " + label );
                            }
                        } else {
                            if ( DEBUG ) {
                                System.out.println( "none has been set up to now and the current one has already a shortlabel" );
                            }
                        }
                    }
                }

                if ( DEBUG ) {
                    if ( atLeastOneInteractionWithoutShortlabel == true ) {
                        System.out.println( "All experiment have been given an interaction shortlabel." );
                    }
                }

                allLabelFound = !atLeastOneInteractionWithoutShortlabel;
            } else {

                if ( DEBUG ) {
                    System.out.println( interactions.size() + " interactions found with the label: " + label );
                }

                /**
                 * An interaction already exists in an experiment if:
                 *       (1) The shortlabel has the prefix bait-prey
                 *       (2) if components involved (Protein + Role) are identical.
                 *           If the components are somehow different, a new Interaction should be created
                 *           with the same prefix and a suffix that is not already in use.
                 *           eg.
                 *                We have 3 Proteins:
                 *                    - P1: gene-name -> gene1
                 *                    - P1-1: gene-name -> gene1 (got from P1)
                 *                    - P2: gene-name -> gene2
                 *
                 *                We have 2 interactions
                 *                    - Interaction 1 have interaction between P1(bait) and P2(prey)
                 *                      gives us the shortlabel gene1-gene2-1
                 *                    - Interaction 1 have interaction between P1-1(bait) and P2(prey)
                 *                      gives us the shortlabel gene1-gene2-2
                 *                      (!) the components involved are different even if the gene name are identical
                 *                          hence, we get same interaction name with suffixes 1 and 2.
                 */

                for ( Iterator iterator = interactions.iterator(); iterator.hasNext(); ) {
                    Interaction intactInteraction = (Interaction) iterator.next();

                    // that updates the experiment collection and only leaves those for which we have to create a new interaction
                    alreadyExistsInIntact( psiInteraction, experiments, intactInteraction ); // update experiments !

                } // intact interaction
            }
        } // while
    }

    /**
     * compare two ranges.
     *
     * @param range
     * @param location
     *
     * @return
     */
    public static boolean areRangeEquals( Range range, LocationTag location ) {

        boolean equals = ( range.getFromIntervalStart() == location.getFromIntervalStart() &&
                           range.getFromIntervalEnd() == location.getFromIntervalEnd() &&
                           range.getToIntervalStart() == location.getToIntervalStart() &&
                           range.getToIntervalEnd() == location.getToIntervalEnd()
        );

        if ( DEBUG ) {
            System.out.print( "RANGE from    " + range.getFromIntervalStart() + ".." + range.getFromIntervalEnd() );
            System.out.println( " to " + range.getToIntervalStart() + ".." + range.getToIntervalEnd() );

            System.out.print( "LOCATION from " + location.getFromIntervalStart() + ".." + location.getFromIntervalEnd() );
            System.out.println( " to " + location.getToIntervalStart() + ".." + location.getToIntervalEnd() );

            if ( equals ) {
                System.out.println( "EQUALS" );
            } else {
                System.out.println( "DIFFERENT" );
            }
        }

        return equals;
    }

    /**
     * Retreive (if any) the PSI ID (MI:xxxx) of a Controlled Vocabulary item.
     *
     * @param cv the CV we want to get the PSI ID from.
     *
     * @return a PSI ID or null if none is found.
     */
    public static String getPsiID( CvObject cv ) {

        if ( cv == null ) {
            throw new IllegalArgumentException( "You must give a non null argument." );
        }

        if ( cv.getXrefs() == null ) {
            return null;
        }

        for ( Iterator iterator = cv.getXrefs().iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            // TODO shouldn't they be CvXrefQualifier = identity ?

            if ( CvDatabase.PSI_MI.equals( xref.getCvDatabase().getShortLabel() ) ) {
                return xref.getPrimaryId();
            }
        }

        return null;
    }

    /**
     * Assess if a IntAct Component's feature 'equals' a PSI definition.
     * <p/>
     * <pre>
     * algo sketch:
     *   1. compare the features (all of them shoul dbe found identical to assess that the components are equals)
     *      1.1. check feature type
     *      1.2. check detection type
     *      1.3. check the Range (IntAct) against Location (PSI)
     * <p/>
     * Remark: in PSI version 1 a feature contains a single Location (Range in IntAct) although Intact's Feature
     * ------  can have multiple Ranges, in order to map that we assume that if the user wants to store multiple
     *         Range into an IntAct's Feature he will add an Xref at the feature level, that xref id will be used
     *         to group multiple Location. Hence we can create from PSI a Feature having multiple Ranges.
     *         Hopefully, PSI version 2 fixes that problem and introduce the needed types.
     * <p/>
     * </pre>
     *
     * @param intactComponent an IntAct Component to compare with a PSI definition.
     * @param psiComponent    a PSI definition of a Component.
     *
     * @return true if the PSI definition reflects the content of the given component, otherwise false.
     */
    public static boolean featureAreEquals( Component intactComponent, ProteinParticipantTag psiComponent ) {

        // TODO when reading the PSI XML we could already load the FeatureTag with multiple Location based on the xref.id

        if ( DEBUG ) {
            System.out.println( "\n\tCompare IntAct Component: " + intactComponent + "\n\tand\n\t" + psiComponent );
        }
        // Component and proteinParticipant are different as soon as we discover one difference
        boolean found = true;

        // check on the Features
        Collection bindingDomains = intactComponent.getBindingDomains();
        if ( psiComponent.hasFeature() && bindingDomains.isEmpty() == false ) {

            if ( DEBUG ) {
                System.out.println( "\tBoth of them have features" );
            }

            // group PSI feature in order to reflect the relationship 1 feature -> 0..n Ranges
            Map featureMap = psiComponent.getClusteredFeatures();

            if ( bindingDomains.size() != featureMap.size() ) {

                if ( DEBUG ) {
                    System.out.println( "\tAfter clustering, they don't have the same count of features" );
                }

                return false;
            }

            if ( DEBUG ) {
                System.out.println( "\tAfter clustering, they have the same count of features" );
            }

            // now check that for each PSI cluster, there is one intact feature with the same Ranges.
            for ( Iterator iterator = featureMap.keySet().iterator(); iterator.hasNext() && found == true; ) {

                String clusterID = (String) iterator.next();
                Collection psiFeatures = (Collection) featureMap.get( clusterID );

                if ( DEBUG ) {
                    System.out.println( "\tselect a PSI cluster(id:" + clusterID + "), it contains " + psiFeatures.size() + " feature(s)." );
                }

                boolean featureFound = false;

                // Check all PSI feature can be found in a single intact Feature (incl. Range Vs Location)
                // We leave the loop as soon as one of them is not found.
                for ( Iterator iterator2 = bindingDomains.iterator(); iterator2.hasNext() && featureFound == false; ) {
                    Feature intactFeature = (Feature) iterator2.next();

                    if ( DEBUG ) {
                        System.out.println( "\t\tSelect an intact feature, shortlabel=" + intactFeature.getShortLabel() );
                    }

                    // Compare count of PSI Location and IntAct Range
                    if ( intactFeature.getRanges().size() != psiFeatures.size() ) {
                        // not the same, skip
                        if ( DEBUG ) {
                            System.out.println( "\t\tTheir count of range/Location is different, skip." );
                        }

                        continue; // next intact feature
                    }

                    if ( DEBUG ) {
                        System.out.println( "\t\tTheir count of range/Location is identical, compare them." );
                    }

                    // we have the same number of locations/ranges

                    // allow to leave the loop if one difference is found.
                    boolean allAttributesFound = true;

                    for ( Iterator iterator1 = psiFeatures.iterator(); iterator1.hasNext() && allAttributesFound; ) {
                        FeatureTag psiFeature = (FeatureTag) iterator1.next();

                        if ( DEBUG ) {
                            System.out.println( "\t\t\tSelect PSI feature, shortlabel: " + psiFeature.getShortlabel() );
                        }

                        // Compare featureType, via MI reference (mandatory attribute in IntAct and PSI)
                        String psiMI = psiFeature.getFeatureType().getPsiDefinition().getId();
                        String intactMI = getPsiID( intactFeature.getCvFeatureType() );

                        // this only works is the check step has been done before !!
                        if ( !intactMI.equals( psiMI ) ) {
                            // not equals - go to next feature
                            allAttributesFound = false;
                            if ( DEBUG ) {
                                System.out.println( "\t\t\tFeature Type are different: PSI:" + psiMI + " Intact: " + intactMI );
                            }

                            continue; // next psi Feature

                        } else {

                            if ( DEBUG ) {
                                System.out.println( "\t\t\tFeature Type is the same: " + psiMI );
                            }
                        }

                        psiMI = null;
                        intactMI = null;

                        // Compare featureIdentification, via MI reference (not mandatory in IntAct and PSI)
                        if ( psiFeature.hasFeatureDetection() ) {

                            psiMI = psiFeature.getFeatureDetection().getPsiDefinition().getId();
                            if ( intactFeature.getCvFeatureIdentification() != null ) {

                                intactMI = getPsiID( intactFeature.getCvFeatureIdentification() );

                                if ( !intactMI.equals( psiMI ) ) {
                                    // not equals
                                    allAttributesFound = false;
                                    if ( DEBUG ) {
                                        System.out.println( "Feature identification are different" );
                                    }
                                    continue; // next psi Feature
                                }

                            } else {

                                if ( DEBUG ) {
                                    System.out.println( "Feature identification are different" );
                                }
                                allAttributesFound = false;
                                continue; // next psi Feature
                            }

                        } else {

                            // The PSI feature doesn't have a feature detection
                            if ( intactFeature.getCvFeatureIdentification() != null ) {

                                if ( DEBUG ) {
                                    System.out.println( "Feature identification are different" );
                                }

                                allAttributesFound = false;
                                continue; // next psi Feature
                            }
                        }

                        if ( DEBUG ) {
                            System.out.println( "\t\tFeature Detection is the same: " + ( psiMI == null ? "none" : psiMI ) );
                        }

                        // TODO should we check as well the Xref of the Features ?

                        // now check on the location
                        LocationTag location = psiFeature.getLocation();

                        if ( DEBUG ) {
                            System.out.println( "\t\tNow check the range vs location" );
                        }

                        boolean rangeFound = false;
                        for ( Iterator iterator3 = intactFeature.getRanges().iterator(); iterator3.hasNext() && rangeFound == false; )
                        {
                            Range range = (Range) iterator3.next();

                            // compare the content of Location Vs Range
                            if ( areRangeEquals( range, location ) ) {

                                rangeFound = true;
                            }

                        } // intact feature's ranges

                        if ( !rangeFound ) {
                            allAttributesFound = false;
                        }

                    } // psi features

                    if ( allAttributesFound == true ) {
                        featureFound = true;
                    }

                } // intact features

                if ( featureFound == false ) {

                    found = false;
                }

            } // psi feature cluster

        } else {

            if ( false == psiComponent.hasFeature() && bindingDomains.size() == 0 ) {

                // none of them has feature, hence are equals
                if ( DEBUG ) {
                    System.out.println( "\t neither the PSI nor IntAct Component has features." );
                }

                return true;

            } else {

                if ( DEBUG ) {
                    System.out.println( "\t The PSI and IntAct Component have a different feature count." );
                }

                return false;

            }
        }

        return found;
    }

    /**
     * Allows to check if the data carried by an InteractionTag are already existing in the IntAct node.
     * <pre>
     * <b>Reminder</b>:
     *  - an interaction (as a set of components) must be unique in the experiment scope.
     *  - the shortlabel of an interaction must be unique in the database scope.
     * <p/>
     * <b>Logic sketch<b>:
     *       If there is an experiment in commons between the data declare in PSI and those existing in Intact
     *           - Compare the conponent of the two interactions
     *                  - if the same: the PSI interaction has already an instance in IntAct for that experiment
     *                  - if not the same: compare with the next interaction (if there is one)
     *                          - if no more interaction: the PSI interaction doesn't have an instance in IntAct
     *                                                    for that experiment.
     * </pre>
     *
     * @param psi               the PSI data materialised as an Object.
     * @param intactInteraction an Intact Interaction.
     * @param experiments       it reflects the collection of experiments linked to the PSI interaction and we will
     *                          update it in order to leave only those for which we need to create a new interaction.
     */
    public static void alreadyExistsInIntact( final InteractionTag psi,
                                              final Collection experiments,
                                              final Interaction intactInteraction ) throws IntactException {

        // this is in theory a pretty heavy computation but in practice an interaction rarely have many experiments
        // and few interaction have a lot of components.
        if ( DEBUG ) {
            System.out.println( "Compare interactions: " + psi + "\n and " + intactInteraction );
        }
        if ( psi.getParticipants().size() == intactInteraction.getComponents().size() ) {
            Collection psiExperiments = psi.getExperiments();
            for ( Iterator iterator = psiExperiments.iterator(); iterator.hasNext(); ) {
                ExperimentDescriptionTag psiExperiment = (ExperimentDescriptionTag) iterator.next();

                Collection intactExperiments = intactInteraction.getExperiments();
                for ( Iterator iterator1 = intactExperiments.iterator(); iterator1.hasNext(); ) {
                    Experiment intactExperiment = (Experiment) iterator1.next();

                    if ( DEBUG ) {
                        System.out.println( "Check their experiment: psi(" +
                                            psiExperiment.getShortlabel() +
                                            ") and intact(" +
                                            intactExperiment.getShortLabel() + ")" );
                    }

                    // compare two experiments using their shortlabel as they should be unique.

                    if ( intactExperiment.getShortLabel().equals( psiExperiment.getShortlabel() ) ) {
                        // they are the same ... check on the conponents

                        if ( DEBUG ) {
                            System.out.println( "They are equals ! Check on their participants..." );
                        }

                        Collection psiComponents = psi.getParticipants();
                        boolean allComponentFound = true;
                        for ( Iterator iterator2 = psiComponents.iterator(); iterator2.hasNext() && allComponentFound; )
                        {
                            ProteinParticipantTag psiComponent = (ProteinParticipantTag) iterator2.next();

                            ProteinHolder holder = getProtein( psiComponent );
                            final Protein psiProtein;
                            if ( holder.isSpliceVariantExisting() ) {
                                psiProtein = holder.getSpliceVariant();
                            } else {
                                psiProtein = holder.getProtein();
                            }

                            final CvExperimentalRole psiRole = RoleChecker.getCvExperimentalRole( psiComponent.getRole() );

                            if ( DEBUG ) {
                                System.out.println( "PSI: " + psiProtein.getShortLabel() + " (" + psiRole.getShortLabel() + ")" );
                            }

                            Collection intactComponents = intactInteraction.getComponents();
                            boolean found = false;
                            for ( Iterator iterator3 = intactComponents.iterator(); iterator3.hasNext() && !found; ) {
                                Component intactComponent = (Component) iterator3.next();

                                System.out.println( "Before intactComponent.getBindingDomains().size() = " + intactComponent.getBindingDomains().size() );
                                //WARNING : if we do not reload the Component using the helper, it does not find
                                // the feature on the component (intactComponent.getBindingDomains.size() return null)
                                // even if in the dabase the Component is associated to a Feature
                                // Todo : Find why is that
                                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getComponentDao().refresh(intactComponent);

                                if ( DEBUG ) {
                                    System.out.print( "\tINTACT: " + intactComponent.getInteractor().getShortLabel() +
                                                      " (" + intactComponent.getCvExperimentalRole().getShortLabel() + "): " );
                                }

                                if ( psiRole.equals( intactComponent.getCvExperimentalRole() ) &&
                                     psiProtein.equals( intactComponent.getInteractor() ) ) {

                                    if ( DEBUG ) {
                                        System.out.println( "protein are EQUALS" );
                                    }

                                    // checking the feature's...
                                    if ( featureAreEquals( intactComponent, psiComponent ) ) {

                                        if ( DEBUG ) {
                                            System.out.println( "features are EQUALS" );
                                        }

                                        found = true;
                                    } else {

                                        if ( DEBUG ) {
                                            System.out.println( "feature are DIFFERENT" );
                                        }
                                    }

                                } else {
                                    // special case, a same protein can be bait and prey in the same interaction.
                                    // Hence, we have to browse the whole intact Component set until we find the
                                    // component or all have been checked.
                                    if ( DEBUG ) {
                                        System.out.println( "protein are DIFFERENT" );
                                    }
                                }
                            } // intact components

                            if ( !found ) {
                                // no need to carry on to check the psi component set because now, we know that
                                // at least one is not found.
                                allComponentFound = false;
                            }

                        } // psi components

                        if ( allComponentFound ) {
                            // there is already an instance of that interaction in intact
                            if ( DEBUG ) {
                                System.out.println( "All component(protein+role+feature) have been found, hence there " +
                                                    "is an instance of that interaction in intact" );
                            }

                            // create a warning message
                            StringBuffer sb = new StringBuffer( 256 );
                            sb.append( "WARNING" ).append( NEW_LINE );
                            sb.append( "An interaction having the shortlabel " ).append( intactInteraction.getShortLabel() );
                            sb.append( NEW_LINE );
                            sb.append( "and involving the following components: " );
                            for ( Iterator iterator2 = psi.getParticipants().iterator(); iterator2.hasNext(); ) {
                                ProteinParticipantTag psiComponent = (ProteinParticipantTag) iterator2.next();
                                sb.append( NEW_LINE ).append( '[' );
                                sb.append( psiComponent.getProteinInteractor().getPrimaryXref().getId() );
                                sb.append( ", " );
                                sb.append( psiComponent.getRole() );
                                if ( psiComponent.hasFeature() ) {
                                    sb.append( ", Feature[" );
                                    for ( Iterator iterator3 = psiComponent.getFeatures().iterator(); iterator3.hasNext(); )
                                    {
                                        FeatureTag feature = (FeatureTag) iterator3.next();
                                        sb.append( "type=" ).append( feature.getFeatureType().getPsiDefinition().getId() );
                                        sb.append( ',' );
                                        sb.append( "detection=" );
                                        if ( null != feature.getFeatureDetection() ) {
                                            sb.append( feature.getFeatureDetection().getPsiDefinition().getId() );
                                        } else {
                                            sb.append( "none" );
                                        }
                                        sb.append( ',' );

                                        LocationTag location = feature.getLocation();
                                        sb.append( " range from=" ).append( location.getFromIntervalEnd() );
                                        sb.append( ".." );
                                        sb.append( location.getFromIntervalStart() );
                                        sb.append( ".." );
                                        sb.append( "range to=" ).append( location.getToIntervalStart() );
                                        sb.append( ".." );
                                        sb.append( location.getToIntervalEnd() );
                                    }
                                    sb.append( ']' );
                                }

                                sb.append( ']' ).append( ' ' );
                            }
                            sb.append( NEW_LINE );
                            sb.append( "already exists in IntAct under the experiment " );
                            sb.append( intactExperiment.getShortLabel() );
                            sb.append( NEW_LINE );

                            System.out.println( sb.toString() );

                            // update the experiment collection (remove the corresponding item).
                            ExperimentWrapper experimentWrapper = null;
                            boolean found = false;
                            for ( Iterator iterator2 = experiments.iterator(); iterator2.hasNext() && !found; ) {
                                experimentWrapper = (ExperimentWrapper) iterator2.next();
                                if ( experimentWrapper.getExperiment().equals( psiExperiment ) ) {
                                    found = true;
                                }
                            }
                            if ( found ) {
                                experiments.remove( experimentWrapper );
                            }
                        }

                        // else ... just carry on searching.

                    } else {
                        if ( DEBUG ) {
                            System.out.println( "Experiment shortlabel are different ... don't check the components" );
                        }
                    }
                } // intact experiments
            } // psi experiments

            // no instance of that interaction have been found in intact.
        }
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
    private static Annotation searchIntactAnnotation( final AnnotationTag annotationTag )
            throws IntactException {

        Annotation annotation = null;

        final String text = annotationTag.getText();
        Collection annotations = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getAnnotationDao().getByTextLike(text);

        for ( Iterator iterator = annotations.iterator(); iterator.hasNext() && annotation == null; ) {
            Annotation anAnnotation = (Annotation) iterator.next();
            if ( annotationTag.getType().equals( anAnnotation.getCvTopic().getShortLabel() ) ) {
                annotation = anAnnotation;
            }
        }

        return annotation;
    }

    /**
     * introspect a Protein object and pick up it's gene name. <br> If it is not a UniProt protein, we use the
     * shortlabel instead.
     *
     * @param proteinHolder container holding either a UniProt protein of an XML definition of the protein.
     *
     * @return the Protein's gene name.
     */
    private static String getGeneName( final ProteinHolder proteinHolder ) {

        if ( ! proteinHolder.isUniprot() ) {
            // if the protein is NOT a UniProt one, then use the shortlabel as we won't get a gene name.
            return proteinHolder.getProteinInteractor().getShortlabel();
        }

        // we have a uniprot Protein attached.
        Protein protein = proteinHolder.getProtein();

        // the gene name we want to extract from the protein.
        String geneName = null;

        CvAliasType geneNameAliasType = ControlledVocabularyRepository.getGeneNameAliasType();
        if ( geneNameAliasType != null ) {
            for ( Iterator iterator = protein.getAliases().iterator(); iterator.hasNext() && geneName == null; ) {
                final Alias alias = (Alias) iterator.next();

                if ( geneNameAliasType.equals( alias.getCvAliasType() ) ) {
                    geneName = alias.getName();
                }
            }
        }

        if ( geneName == null ) {

            geneName = protein.getShortLabel();

            // remove any _organism in case it exists
            int index = geneName.indexOf( '_' );
            if ( index != -1 ) {
                geneName = geneName.substring( 0, index );
            }

            System.out.println( "NOTICE: protein " + protein.getShortLabel() +
                                " does not have a gene name, we will use it's SPTR ID: " + geneName );
        }

        return geneName;
    }

    /**
     * Get an Intact Protein out of a ProteinInteractorTag.
     *
     * @param proteinParticipant
     *
     * @return the IntAct Protein correcponding to the given ProteinParticipantTag.
     */
    private static ProteinHolder getProtein( final ProteinParticipantTag proteinParticipant ) {

        final ProteinInteractorTag proteinInteractor = proteinParticipant.getProteinInteractor();

        OrganismTag organism = proteinInteractor.getOrganism();
        BioSource bioSource = null;
        if ( organism != null ) {
            bioSource = OrganismChecker.getBioSource( proteinInteractor.getOrganism() );
        }

        String proteinId = proteinInteractor.getPrimaryXref().getId();
        String db = proteinInteractor.getPrimaryXref().getDb();

//        System.out.println( "Search interactor (" + proteinInteractor.getShortlabel() + ") from cache(" +
//                            proteinId + ", " + db + ", " + bioSource.getShortLabel() + ")" );

        return ProteinInteractorChecker.getProtein( proteinId, db, bioSource );
    }
}