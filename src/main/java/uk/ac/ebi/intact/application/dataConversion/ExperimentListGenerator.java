/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

import uk.ac.ebi.intact.application.dataConversion.dao.ExperimentListGeneratorDao;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

import java.io.PrintStream;
import java.util.*;

/**
 * <pre>
 * Generates a classified list of experiments based on :
 *  - their count of interaction,
 *  - the fact that they contain negative interaction.
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28-Jul-2005</pre>
 */
public class ExperimentListGenerator {

    /**
     * Maximum count of interaction for a small scale experiment.
     */
    public static final int SMALL_SCALE_LIMIT_DEFAULT = 500;

    /**
     * if an experiment has more than this many interactions it is considered to be large scale.
     */
    public static final int LARGE_SCALE_CHUNK_SIZE_DEFAULT = 2000;

    public static final String SMALL = "small";
    public static final String LARGE = "large";

    public static final String NEW_LINE = System.getProperty( "line.separator" );

    private static final int MAX_EXPERIMENTS_PER_CHUNK_DEFAULT = 100;

    /**
     * Classification type supported.
     */
    private enum Classification {
        SPECIES,
        PUBLICATIONS,
        DATASETS
    }

    private static final String SPECIES_FOLDER_NAME_DEFAULT = "species";
    private static final String PUBLICATIONS_FOLDER_NAME_DEFAULT = "pmid";
    private static final String DATASETS_FOLDER_NAME_DEFAULT = "datasets";

    /**
     * Pattern used to select experiment by its label
     */
    private String searchPattern;

    private PrintStream output;

    /**
     * If true, all experiment without a PubMed ID (primary-reference) will be filtered out.
     */
    private boolean onlyWithPmid = true;

    private String speciesFolderName = SPECIES_FOLDER_NAME_DEFAULT;
    private String publicationsFolderName = PUBLICATIONS_FOLDER_NAME_DEFAULT;
    private String datasetFolderName = DATASETS_FOLDER_NAME_DEFAULT;
    private String currentDataset;

    private int experimentsPerChunk = MAX_EXPERIMENTS_PER_CHUNK_DEFAULT;
    private int smallScaleLimit = SMALL_SCALE_LIMIT_DEFAULT;
    private int largeScaleChunkSize = LARGE_SCALE_CHUNK_SIZE_DEFAULT;

    private List<ExperimentListItem> speciesListItems = new ArrayList<ExperimentListItem>();
    private List<ExperimentListItem> publicationsListItems = new ArrayList<ExperimentListItem>();
    private List<ExperimentListItem> datasetsListItems = new ArrayList<ExperimentListItem>();

    private boolean experimentsClassified;
    private Set<String> filteredExperimentAcs;

    /**
     * The key being the experiment AC and the value the number of interactions for that experiment
     */
    private Map<String, Integer> interactionCount;

    /**
     * Mapping experiment to their datasets
     */
    private Map<String, Collection<SimpleDataset>> experiment2dataset;

    private Map<String, String> expAcToPmid;

    private Map<String, List<String>> expAcToTaxid;

    private Map<String, BioSource> targetSpeciesCache = new HashMap<String, BioSource>();

    /**
     * Classification of experiments by pubmedId
     */
    private Map<String, Collection<SimplifiedAnnotatedObject<Experiment>>> pubmed2experimentSet =
            new HashMap<String, Collection<SimplifiedAnnotatedObject<Experiment>>>();

    /**
     * Classification of experiments by species
     */
    private Map<SimplifiedAnnotatedObject<BioSource>, Collection<SimplifiedAnnotatedObject<Experiment>>> species2experimentSet =
            new HashMap<SimplifiedAnnotatedObject<BioSource>, Collection<SimplifiedAnnotatedObject<Experiment>>>();

    /**
     * Classification of experiments by dataset
     * (dataset/pmid -> experiment list)
     */
    private Map<SimpleDataset, Collection<SimplifiedAnnotatedObject<Experiment>>> dataset2experimentSet =
            new HashMap<SimpleDataset, Collection<SimplifiedAnnotatedObject<Experiment>>>( );

    /**
     * Holds the shortLabels of any Experiments found to contain Interactions with 'negative' information. It has to be
     * a static because the method used for writing the classifications is a static...
     */
    private Set<Experiment> negativeExperiments;

    private Map<String, String> experimentsWithErrors = new HashMap<String, String>();

    public ExperimentListGenerator() {
        this( "%", System.out );
    }

    public ExperimentListGenerator(PrintStream output) {
        this( "%", output );
    }

    public ExperimentListGenerator( String searchPattern ) {
        this(searchPattern, System.out);
    }

    public ExperimentListGenerator( String searchPattern, PrintStream output ) {
        this.searchPattern = searchPattern;
        this.output = output;
    }

    public List<ExperimentListItem> generateClassificationBySpecies() {
        if ( !experimentsClassified ) {
            classifyExperiments();
        }

        if ( speciesListItems != null && !speciesListItems.isEmpty() ) {
            return speciesListItems;
        }

        createItemClassificationBySpecies();

        return speciesListItems;
    }

    public List<ExperimentListItem> generateClassificationByPublications() {
        if ( !experimentsClassified ) {
            classifyExperiments();
        }

        if ( publicationsListItems != null && !publicationsListItems.isEmpty() ) {
            return publicationsListItems;
        }

        createItemClassificationByPubmed();

        return publicationsListItems;
    }

    public List<ExperimentListItem> generateClassificationByDatasets() {
        if ( !experimentsClassified ) {
            classifyExperiments();
        }

        if ( datasetsListItems != null && !datasetsListItems.isEmpty() ) {
            return datasetsListItems;
        }

        createItemClassificationByDataset();

        return datasetsListItems;
    }

    public List<ExperimentListItem> generateAllClassifications() {
        if ( !experimentsClassified ) {
            classifyExperiments();
        }

        List<ExperimentListItem> allItems = new ArrayList<ExperimentListItem>();
        allItems.addAll( generateClassificationBySpecies() );
        allItems.addAll( generateClassificationByPublications() );

        return allItems;
    }

    public Set<Experiment> getNegativeExperiments() {
        if ( negativeExperiments == null ) {
            classifyNegatives();
        }

        return negativeExperiments;
    }

    public Map<String, String> getExperimentWithErrors() {
        if ( !experimentsClassified ) {
            classifyExperiments();
        }

        return experimentsWithErrors;
    }


    boolean experimentsClassifiedByDataset = false;

    /**
     * Classify experiments matching searchPattern into a data structure according to species and experiment size.
     *
     * @return HashMap of HashMap of ArrayLists of Experiments: {species}{scale}[n]
     *
     * @throws uk.ac.ebi.intact.business.IntactException
     *
     */
    private void classifyExperiments() {

        // TODO separate classification - one method per type of classification !

        // Obtain data, probably experiment by experiment, build
        // PSI data for it then write it to a file....
        Collection<Experiment> searchResults;
        int firstResult = 0;

        Set<String> experimentFilter = getFilteredExperimentAcs();

        do {
            searchResults = getExperiments( firstResult, experimentsPerChunk );

            // Split the list of experiments into species- and size-specific files
            for ( Experiment experiment : searchResults ) {
                if ( experimentFilter.contains( experiment.getAc() ) ) {
                    output.println( "Skipping " + experiment.getShortLabel() );
                    continue;
                }

                int interactionCount = interactionsForExperiment( experiment.getAc() );
                // Skip empty experiments and give a warning about'em
                if ( interactionCount == 0 ) {
                    output.println( "ERROR: experiment " + experiment.getShortLabel() + " (" + experiment.getAc() + ") has no interaction." );
                    experimentsWithErrors.put( experiment.getShortLabel(), "Experiment without interactions" );
                    continue;
                }

                // 1. Get the species of one of the interactors of the experiment.
                //    The bioSource of the Experiment is irrelevant, as it may be an auxiliary experimental system.
                Collection<BioSource> sources = getTargetSpecies( experiment.getAc() );

                output.println( "Classifying " + experiment.getShortLabel() + " (" + interactionCount + " interaction" + ( interactionCount > 1 ? "s" : "" ) + ")" );

                // 2. get the pubmedId (primary-ref)
                String pubmedId = String.valueOf( getPubmedId( experiment.getAc() ) );

                output.println( "\tPubmedId: " + pubmedId + "; Sources: " + sources.size() );

                if ( sources.isEmpty() ) {
                        experimentsWithErrors.put( experiment.getShortLabel(), "Experiment without biosources" );
                        output.println( "ERROR: Experiment without target-species: " + experiment.getAc() + " (" + experiment.getShortLabel() + ")" );
                }

                // 3. create the classification by publication
                if ( pubmedId != null ) {

                    Collection<SimplifiedAnnotatedObject<Experiment>> experimentSet = null;

                    if ( !pubmed2experimentSet.containsKey( pubmedId ) ) {
                        // create an empty set
                        experimentSet = new HashSet<SimplifiedAnnotatedObject<Experiment>>();
                        pubmed2experimentSet.put( pubmedId, experimentSet );
                    } else {
                        // retreive the existing set
                        experimentSet = pubmed2experimentSet.get( pubmedId );
                    }

                    // add the experiment to the set of experiments.
                    experimentSet.add( new SimplifiedAnnotatedObject<Experiment>( experiment ) );
                } else {
                    output.println( "ERROR: Could not find a pubmed ID for experiment: " + experiment.getShortLabel() + "(" + experiment.getAc() + ")" );
                }

                // if multiple target-species have been found, that experiment will be associated redundantly
                // to each BioSource. only the publication classification is non redundant.
                for ( BioSource bioSource : sources ) {
                    SimplifiedAnnotatedObject<BioSource> source = new SimplifiedAnnotatedObject<BioSource>( bioSource );

                    if ( !species2experimentSet.containsKey( source ) ) {
                        // not yet in the structure, create an entry
                        Collection<SimplifiedAnnotatedObject<Experiment>> experiments = new HashSet<SimplifiedAnnotatedObject<Experiment>>();
                        species2experimentSet.put( source, experiments );
                    }

                    // associate experiment to the source
                    Collection<SimplifiedAnnotatedObject<Experiment>> experiments = species2experimentSet.get( source );
                    experiments.add( new SimplifiedAnnotatedObject<Experiment>( experiment ) );
                }

                // 4. Classify by datasets
                //    The classification is as follow: datasets/pmid.xml contains the list of corresponding experiments.
                Collection<SimpleDataset> datasets = getDatasets( experiment.getAc() );

                if( datasets != null ) {

                    for ( SimpleDataset dataset : datasets ) {

                        Collection<SimplifiedAnnotatedObject<Experiment>> experimentList = null;

                        if( pubmedId == null ) {

                            output.println( "ERROR: PubMed id was null when classifying experiment '"+ experiment.getShortLabel() +
                                       "' by datasets '"+ dataset.getName() +"', skipping." );

                        } else {

                            dataset.setPmid( pubmedId );

                            if( false == dataset2experimentSet.containsKey( dataset ) ) {
                                // initialize association
                                experimentList = new ArrayList<SimplifiedAnnotatedObject<Experiment>>( 4 );
                                dataset2experimentSet.put( dataset, experimentList );
                            } else {
                                experimentList = dataset2experimentSet.get( dataset );
                            }

                            experimentList.add( new SimplifiedAnnotatedObject<Experiment>( experiment ) );
                        }
                    }
                } // datasets

            } // experiments

            firstResult = firstResult + experimentsPerChunk;

        } while ( !searchResults.isEmpty() );

        experimentsClassified = true;

    }

    private Collection<Experiment> getExperiments( int firstResult, int maxResults ) {
        output.println( "Retrieving data from DB store, from " + firstResult );

        if ( searchPattern.contains( "," ) ) {
             throw new IntactException( "Lists with comma-separated experiments are not accepted anymore" );
        }

        Collection<Experiment> searchResults = getDaoFactory().getExperimentDao().getByShortLabelLike( searchPattern, true, firstResult, maxResults, true );

        int resultSize = searchResults.size();
        output.println( "done (retrieved " + resultSize + " experiment" + ( resultSize > 1 ? "s" : "" ) + ")" );

        return searchResults;
    }

    /**
     * Retreive BioSources corresponding ot the target-species assigned to the given experiment.
     *
     * @param experimentAc The experiment AC for which we want to get all target-species.
     *
     * @return A collection of BioSource, or empty if non is found.
     *
     * @throws IntactException if an error occurs.
     */
    private Collection<BioSource> getTargetSpecies( String experimentAc ) throws IntactException {
        List<String> taxIds = taxIdsForExperiment( experimentAc );

        if ( taxIds == null ) {
            experimentsWithErrors.put( experimentAc, "[INFO] No target-species found for experiment" );
            return new ArrayList<BioSource>();
        }

        List<BioSource> targetSpeciesList = new ArrayList<BioSource>();

        for ( String taxId : taxIds ) {
            if ( targetSpeciesCache.containsKey( taxId ) ) {
                targetSpeciesList.add( targetSpeciesCache.get( taxId ) );
            }

            Collection<BioSource> bioSources = getDaoFactory().getBioSourceDao().getByTaxonId( taxId );

            if ( bioSources.isEmpty() ) {
                throw new IntactException( "Experiment(" + experimentAc + ") has a target-species:" + taxId +
                                           " but we cannot find the corresponding BioSource." );
            }

            BioSource targetSpecies;

            // if choice given, get the less specific one (without tissue, cell type...)
            BioSource selectedBioSource = null;
            for ( Iterator iterator1 = bioSources.iterator(); iterator1.hasNext() && selectedBioSource == null; ) {
                BioSource bioSource = (BioSource) iterator1.next();
                if ( bioSource.getCvCellType() == null && bioSource.getCvTissue() == null ) {
                    selectedBioSource = bioSource;
                }
            }

            if ( selectedBioSource != null ) {
                targetSpecies = selectedBioSource;
            } else {
                // add the first one we find
                targetSpecies = bioSources.iterator().next();
            }

            targetSpeciesCache.put( experimentAc, targetSpecies );
            targetSpeciesList.add( targetSpecies );
        }

        return targetSpeciesList;
    }

    /**
     * Fetch publication primaryId from experiment.
     *
     * @param experimentAc the experiment AC for which we want the primary pubmed ID.
     *
     * @return a pubmed Id or null if none found.
     */
    private String getPubmedId( String experimentAc ) {
        if ( expAcToPmid == null ) {
            // map all exps to pmid
            expAcToPmid = ExperimentListGeneratorDao.getExperimentAcAndPmid( searchPattern );
        }

        String pubmedId = expAcToPmid.get( experimentAc );

        if ( pubmedId == null ) {
            experimentsWithErrors.put( experimentAc, "Null pubmed Id" );
        }

        try {
            Integer.parseInt( pubmedId );
        }
        catch ( NumberFormatException e ) {
            experimentsWithErrors.put( experimentAc, "Not a number pubmedId" );
        }

        return pubmedId;
    }

    /**
     * Fetch dataset information (if any) from experiment.
     *
     * @param experimentAc the experiment AC for which we want the primary pubmed ID.
     *
     * @return a dataset or null.
     */
    private Collection<SimpleDataset> getDatasets( String experimentAc ) {
        if ( experiment2dataset == null ) {
            // map all exps to pmid
            experiment2dataset = ExperimentListGeneratorDao.datasetForExperiments( searchPattern );
            output.println( "Loaded " + experiment2dataset.size() + " experiments having datasets information." );
        }

        Collection<SimpleDataset> datasets = experiment2dataset.get( experimentAc );

        return datasets;
    }

    /**
     * Checks for a negative interaction. NB This will have to be done using SQL otherwise we end up materializing all
     * interactions just to do the check.
     * <p/>
     * Also the new intact curation rules specify that an Experiment should ONLY contain negative Interactions if it is
     * annotated as 'negative'. Thus to decide if an Experiment is classified as 'negative', the Annotations of that
     * Experiment need to be checked for one with a 'negative' Controlled Vocab attached to it as a topic. </p>
     * <p/>
     * However at some point in the future there may be a possibility that only the Interactions will be marked as
     * 'negative' (not the Experiment), and so these should be checked also, with duplicate matches being ignored. </p>
     * This method has to be static because it is called by the static 'classifyExperiments'.
     */
    private void classifyNegatives() {
        negativeExperiments = new HashSet<Experiment>();

        negativeExperiments.addAll( ExperimentListGeneratorDao.getExpWithInteractionsContainingAnnotation( CvTopic.NEGATIVE, searchPattern ) );
        negativeExperiments.addAll( ExperimentListGeneratorDao.getContainingAnnotation( Experiment.class, CvTopic.NEGATIVE, searchPattern ) );

        output.println( negativeExperiments.size() + " negative experiment found." );
    }

    public Set<String> getFilteredExperimentAcs() {
        if ( filteredExperimentAcs != null ) {
            return filteredExperimentAcs;
        }

        if ( !onlyWithPmid ) {
            filteredExperimentAcs = Collections.EMPTY_SET;
        } else {

            filteredExperimentAcs = new HashSet<String>();

            Map<String, String> expAcAndLabels = ExperimentListGeneratorDao.getExperimentAcAndLabelWithoutPubmedId( searchPattern );

            for ( Map.Entry<String, String> expAcAndLabel : expAcAndLabels.entrySet() ) {
                String ac = expAcAndLabel.getKey();
                String shortlabel = expAcAndLabel.getValue();

                output.println( "Filter out: " + shortlabel + " (" + ac + ")" );
                filteredExperimentAcs.add( ac );
            }

            output.println( filteredExperimentAcs.size() + " experiment filtered out." );

        }

        return filteredExperimentAcs;
    }

    public void createItemClassificationBySpecies() {

        for ( SimplifiedAnnotatedObject<BioSource> bioSource : species2experimentSet.keySet() ) {

            Collection<SimplifiedAnnotatedObject<Experiment>> smallScaleExp = species2experimentSet.get( bioSource );

            // split the set into subset of size under SMALL_SCALE_LIMIT
            String filePrefixGlobal = bioSource.getShortLabel().replace( ' ', '-' );

            createExpListItems( smallScaleExp,
                                filePrefixGlobal + "_" + SMALL, // small scale
                                filePrefixGlobal,              // large scale
                                Classification.SPECIES );
        }
    }

    /**
     * Build the classification by pubmed id.
     * <br/>
     * We keep the negative experiment separated from the non negative.
     */
    private void createItemClassificationByPubmed() {

        List<String> pubmedOrderedList = new ArrayList<String>( pubmed2experimentSet.keySet() );
        Collections.sort( pubmedOrderedList );

        // Go through all clusters and split if needs be.
        for ( String pubmedid : pubmedOrderedList ) {
            // get experiments associated to that pubmed ID.
            Set<SimplifiedAnnotatedObject<Experiment>> experiments = (Set<SimplifiedAnnotatedObject<Experiment>>) pubmed2experimentSet.get( pubmedid );

            // split the set into subset of size under SMALL_SCALE_LIMIT
            createExpListItems( experiments,
                                pubmedid,   // small scale
                                pubmedid,   // large scale
                                Classification.PUBLICATIONS );

        } // pubmeds
    }

    /**
     * Build the classification by dataset.
     * <br/>
     * We keep the negative experiment separated from the non negative.
     */
    private void createItemClassificationByDataset() {

        List<SimpleDataset> datasetOrderedList = new ArrayList<SimpleDataset>( dataset2experimentSet.keySet() );
        Collections.sort( datasetOrderedList, new Comparator<SimpleDataset>() {
            public int compare( SimpleDataset o1, SimpleDataset o2 ) {

                // sort on dataset name then pmid
                SimpleDataset d1 = (SimpleDataset) o1;
                SimpleDataset d2 = (SimpleDataset) o2;

                int nameComparison = d1.getName().compareTo( d2.getName() );
                if( nameComparison != 0 ) {
                    return nameComparison;
                }

                return d1.getPmid().compareTo( d2.getPmid() );
            }
        } );

        // Go through all clusters and split if needs be.

        for ( SimpleDataset dataset : datasetOrderedList ) {
            // get experiments associated to that dataset.
            Collection<SimplifiedAnnotatedObject<Experiment>> experiments = dataset2experimentSet.get( dataset );

            // the name of the directory changes according to the dataset name.
            // note: the prefix is always datasets/
            currentDataset = dataset.getName();

            // split the set into subset of size under SMALL_SCALE_LIMIT
            createExpListItems( experiments,
                                dataset.getPmid(),         // small scale
                                dataset.getPmid(),         // large scale
                                Classification.DATASETS );
        } // datasets
    }

    private void createExpListItems( Collection<SimplifiedAnnotatedObject<Experiment>> experiments,
                                     String smallScalePrefix,
                                     String largeScalePrefix,
                                     Classification classification ) {

        final Collection<Collection<SimplifiedAnnotatedObject<Experiment>>> smallScaleChunks = new ArrayList<Collection<SimplifiedAnnotatedObject<Experiment>>>();

        Collection<SimplifiedAnnotatedObject<Experiment>> subset = null;

        int sum = 0;

        // 1. Go through the list of experiments and separate the small scale from the large scale.
        //    The filename prefix of the large scale get generated here, though the small scales' get
        //    generated later.
        for ( SimplifiedAnnotatedObject<Experiment> experiment : experiments ) {

            final int size = interactionsForExperiment( experiment.getAc() );

            if ( size >= largeScaleChunkSize ) {
                // Process large scale dataset appart from the small ones.

                // generate the large scale format: filePrefix[chunkSize]
                Collection<SimplifiedAnnotatedObject<Experiment>> largeScale = new ArrayList<SimplifiedAnnotatedObject<Experiment>>( 1 );
                largeScale.add( experiment );

                // put it in the map
                int chunk = 1;
                for ( int i = 0; i < size; i = i + largeScaleChunkSize ) {
                    createExperimentListItems( largeScalePrefix, largeScale, chunk, classification, largeScaleChunkSize );
                    chunk++;
                }

            } else {
                // that experiment is not large scale.

                if ( size > smallScaleLimit ) {

                    // that experiment by itself is a chunk.
                    // we do not alter the current subset being processed, whether there is one or not.
                    Collection<SimplifiedAnnotatedObject<Experiment>> subset2 = new ArrayList<SimplifiedAnnotatedObject<Experiment>>( 1 );
                    subset2.add( experiment );

                    smallScaleChunks.add( subset2 );


                } else if ( ( sum + size ) >= smallScaleLimit ) {

                    // that experiment would overload that chunk ... then store the subset.

                    if ( subset == null ) {

                        // that experiment will be a small chunk by itself
                        subset = new ArrayList<SimplifiedAnnotatedObject<Experiment>>();
                    }

                    // add the current experiment
                    subset.add( experiment );

                    // put it in the list
                    smallScaleChunks.add( subset );

                    // re-init
                    subset = null;
                    sum = 0;

                } else {

                    // ( sum + size ) < SMALL_SCALE_LIMIT
                    sum += size;

                    if ( subset == null ) {
                        subset = new ArrayList<SimplifiedAnnotatedObject<Experiment>>();
                    }

                    subset.add( experiment );
                }

            } // else
        } // experiments

        if ( subset != null && ( !subset.isEmpty() ) ) {

            // put it in the list
            smallScaleChunks.add( subset );
        }

        // 2. Look at the list of small scale chunks and generate their filename prefixes
        //    Note: no index if only one chunk
        boolean hasMoreThanOneChunk = ( smallScaleChunks.size() > 1 );
        Integer index = 0;

        for ( Collection<SimplifiedAnnotatedObject<Experiment>> chunk : smallScaleChunks ) {
            // generate a prefix
            if ( hasMoreThanOneChunk ) {
                index++;
            } else {
                index = null;
            }

            // add to the map
            createExperimentListItems( smallScalePrefix, chunk, index, classification, null );
        }
    }

    /**
     * Gets the parent folders for the element
     */
    private String parentFolders( Collection<SimplifiedAnnotatedObject<Experiment>> experiments,
                                  Classification classification ) {
        String parentFolders = null;

        switch ( classification ) {
            case SPECIES:
                parentFolders = speciesFolderName;
                break;
            case PUBLICATIONS:
                String year = getCreatedYear( experiments );
                parentFolders = publicationsFolderName + FileHelper.SLASH + year;
                break;
            case DATASETS:
                if( currentDataset == null ) {
                    throw new IllegalStateException( "currentDataset should have been set prior to calling that method." );
                }
                parentFolders = datasetFolderName + FileHelper.SLASH + currentDataset;
                break;
        }

        return parentFolders;
    }

    private void createExperimentListItems( String name,
                                            Collection<SimplifiedAnnotatedObject<Experiment>> exps,
                                            Integer chunkNumber,
                                            Classification classification,
                                            Integer largeScaleChunkSize ) {

        List<String> labels = new ArrayList<String>();
        List<String> labelsNegative = new ArrayList<String>();

        for ( SimplifiedAnnotatedObject exp : exps ) {
            if ( isNegative( exp.getShortLabel() ) ) {
                labelsNegative.add( exp.getShortLabel() );
            } else {
                labels.add( exp.getShortLabel() );
            }
        }

        String parentFolders = parentFolders( exps, classification );

        if ( !labels.isEmpty() ) {
            addToList( new ExperimentListItem( labels, name, parentFolders, false, chunkNumber, largeScaleChunkSize ), classification );
        }

        if ( !labelsNegative.isEmpty() ) {
            addToList( new ExperimentListItem( labelsNegative, name, parentFolders, true, chunkNumber, largeScaleChunkSize ), classification );
        }
    }


    /**
     * Given a set of Experiments, it returns the year of the date of creation of the oldest experiment.
     *
     * @param experiments experiments
     *
     * @return an int corresponding to the year.
     */
    private static String getCreatedYear( Collection<SimplifiedAnnotatedObject<Experiment>> experiments ) {

        if ( experiments.isEmpty() ) {
            throw new IllegalArgumentException( "The given Set of Experiments is empty" );
        }

        int year = Integer.MAX_VALUE;

        for ( SimplifiedAnnotatedObject<Experiment> exp : experiments ) {
            Date created = exp.getCreated();

            java.sql.Date d = new java.sql.Date( created.getTime() );
            Calendar c = new GregorianCalendar();
            c.setTime( d );

            if ( year > c.get( Calendar.YEAR ) ) {
                year = c.get( Calendar.YEAR );
            }
        }

        return String.valueOf( year );
    }

    private void addToList( ExperimentListItem eli, Classification classification ) {
        switch ( classification ) {
            case SPECIES:
                speciesListItems.add( eli );
                break;
            case PUBLICATIONS:
                publicationsListItems.add( eli );
                break;
            case DATASETS:
                datasetsListItems.add( eli );
                break;
            default:
                throw new IllegalStateException( "Unsupported Classification( "+classification+" )" );
        }
    }

    /**
     * Sort a collection of String (shorltabel). The given collection is not modified, a new one is returned.
     *
     * @param experiments collection to sort.
     *
     * @return the sorted collection.
     */
    private static List<String> getSortedShortlabel( Collection<SimplifiedAnnotatedObject<Experiment>> experiments ) {

        List<String> sorted = new ArrayList<String>( experiments.size() );

        for ( SimplifiedAnnotatedObject<Experiment> experiment : experiments ) {
            sorted.add( experiment.getShortLabel() );
        }

        Collections.sort( sorted );
        return sorted;
    }

    /**
     * Answers the following question: "Is the given shortlabel refering to a negative experiment ?".
     *
     * @param experimentLabel the experiment shortlabel.
     *
     * @return true if the label refers to a negative experiment, false otherwise.
     */
    private boolean isNegative( String experimentLabel ) {

        for ( Experiment experiment : getNegativeExperiments() ) {
            if ( experiment.getShortLabel().equals( experimentLabel ) ) {
                return true;
            }
        }
        return false;
    }

    private int interactionsForExperiment( String experimentAc ) {
        if ( interactionCount == null ) {
            interactionCount = ExperimentListGeneratorDao.countInteractionCountsForExperiments( searchPattern );
        }

        if ( experimentAc == null ) {
            throw new NullPointerException( "Experiment AC is null" );
        }

        if ( interactionCount.containsKey( experimentAc ) ) {
            return interactionCount.get( experimentAc );
        }

        return 0;
    }

    private Collection<SimpleDataset> datasetForExperiment( String experimentAc ) {
        if ( experiment2dataset == null ) {
            experiment2dataset = ExperimentListGeneratorDao.datasetForExperiments( searchPattern );
        }

        if ( experimentAc == null ) {
            throw new NullPointerException( "Experiment AC is null" );
        }

        if ( experiment2dataset.containsKey( experimentAc ) ) {
            return experiment2dataset.get( experimentAc );
        }

        return null;
    }

    private List<String> taxIdsForExperiment( String experimentAc ) {
        if ( expAcToTaxid == null ) {
            expAcToTaxid = ExperimentListGeneratorDao.getExperimentAcAndTaxids( searchPattern );
        }

        return expAcToTaxid.get( experimentAc );
    }

    public String getSearchPattern() {
        return searchPattern;
    }

    public boolean isOnlyWithPmid() {
        return onlyWithPmid;
    }

    public void setOnlyWithPmid( boolean onlyWithPmid ) {
        this.onlyWithPmid = onlyWithPmid;
    }

    public int getExperimentsPerChunk() {
        return experimentsPerChunk;
    }

    public void setExperimentsPerChunk( int experimentsPerChunk ) {
        this.experimentsPerChunk = experimentsPerChunk;
    }

    public int getLargeScaleChunkSize() {
        return largeScaleChunkSize;
    }

    public void setLargeScaleChunkSize( int largeScaleChunkSize ) {
        this.largeScaleChunkSize = largeScaleChunkSize;
    }

    public int getSmallScaleLimit() {
        return smallScaleLimit;
    }

    public void setSmallScaleLimit( int smallScaleLimit ) {
        this.smallScaleLimit = smallScaleLimit;
    }

    public String getSpeciesFolderName() {
        return speciesFolderName;
    }

    public void setSpeciesFolderName( String speciesFolderName ) {
        this.speciesFolderName = speciesFolderName;
    }

    public String getPublicationsFolderName() {
        return publicationsFolderName;
    }

    public void setPublicationsFolderName( String publicationsFolderName ) {
        this.publicationsFolderName = publicationsFolderName;
    }

    public String getDatasetsFolderName() {
        return datasetFolderName;
    }

    public void setDatasetsFolderName( String datasetFolderName ) {
        this.datasetFolderName = datasetFolderName;
    }

    private static DaoFactory getDaoFactory() {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }

    private class SimplifiedAnnotatedObject<T extends AnnotatedObject> {

        private String ac;
        private String shortLabel;
        private Date created;

        public SimplifiedAnnotatedObject( AnnotatedObject annotatedObject ) {
            this.ac = annotatedObject.getAc();
            this.shortLabel = annotatedObject.getShortLabel();
            this.created = annotatedObject.getCreated();
        }

        public String getAc() {
            return ac;
        }

        public String getShortLabel() {
            return shortLabel;
        }


        public Date getCreated() {
            return created;
        }

        @Override
        public boolean equals( Object obj ) {
            SimplifiedAnnotatedObject o = (SimplifiedAnnotatedObject) obj;
            return ac.equals( o.getAc() ) && shortLabel.equals( o.getShortLabel() );
        }

        @Override
        public String toString() {
            return getAc() + " " + getShortLabel();
        }

        @Override
        public int hashCode() {
            return 37 * ac.hashCode() * shortLabel.hashCode();
        }
    }
}