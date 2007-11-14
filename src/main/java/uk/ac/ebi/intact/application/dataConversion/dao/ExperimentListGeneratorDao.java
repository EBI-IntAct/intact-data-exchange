/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import uk.ac.ebi.intact.application.dataConversion.SimpleDataset;
import uk.ac.ebi.intact.config.impl.AbstractHibernateDataConfig;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.AnnotatedObjectImpl;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Experiment;

import java.util.*;

/**
 * TODO: comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/08/2006</pre>
 */
@SuppressWarnings("unchecked")
public class ExperimentListGeneratorDao {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( ExperimentListGeneratorDao.class );

    /**
     * Query to get at the Experiment ACs containing negative interaction annotations
     */
    public static List<Experiment> getExpWithInteractionsContainingAnnotation( String cvshortLabel, String shortLabelLike ) {

        return getSession().createCriteria( Experiment.class )
                .add( Restrictions.like( "shortLabel", shortLabelLike ) )
                .createCriteria( "interactions" )
                .createCriteria( "annotations" )
                .createCriteria( "cvTopic" )
                .add( Restrictions.eq( "shortLabel", cvshortLabel ) ).list();
    }

    /**
     * Query to obtain annotated objects by searching for an Annotation with the cvTopic label provided
     */
    public static <T extends AnnotatedObjectImpl> List<T> getContainingAnnotation( Class<T> annObject, String cvshortLabel,
                                                                                   String shortLabelLike ) {
        return getSession().createCriteria( annObject.getClass() )
                .add( Restrictions.like( "shortLabel", shortLabelLike ) )
                .createCriteria( "annotations" )
                .createCriteria( "cvTopic" )
                .add( Restrictions.eq( "shortLabel", cvshortLabel ) ).list();
    }

    public static Map<String, String> getExperimentAcAndLabelWithoutPubmedId( String shortLabelLike ) {
        List<Object[]> allExps = getSession().createCriteria( Experiment.class )
                .add( Restrictions.like( "shortLabel", shortLabelLike ) )
                .setProjection( Projections.projectionList()
                        .add( Projections.distinct( Projections.property( "ac" ) ) )
                        .add( Projections.property( "shortLabel" ) ) ).list();

        Map<String, String> filteredExpsMap = new HashMap<String, String>();

        for ( Object[] exp : allExps ) {
            filteredExpsMap.put( (String) exp[ 0 ], (String) exp[ 1 ] );
        }

        Map<String, String> expsAndPmid = getExperimentAcAndPmid( shortLabelLike );

        for ( String expWithPmid : expsAndPmid.keySet() ) {
            filteredExpsMap.remove( expWithPmid );
        }

        return filteredExpsMap;
    }

    public static Map<String, String> getExperimentAcAndPmid( String shortLabelLike ) {
        List<Object[]> expsAndPmidResults = getSession().createCriteria( Experiment.class )
                .add( Restrictions.like( "shortLabel", shortLabelLike ) )
                .createAlias( "xrefs", "xref" )
                .createAlias( "xref.cvDatabase", "cvDb" )
                .createAlias( "xref.cvXrefQualifier", "cvXrefQual" )
                .add( Restrictions.eq( "cvDb.shortLabel", CvDatabase.PUBMED ) )
                .add( Restrictions.eq( "cvXrefQual.shortLabel", CvXrefQualifier.PRIMARY_REFERENCE ) )
                .setProjection( Projections.projectionList()
                        .add( Projections.distinct( Projections.property( "ac" ) ) )
                        .add( Projections.property( "xref.primaryId" ) ) ).list();

        Map<String, String> expAndPmids = new HashMap<String, String>();

        for ( Object[] expAndPmid : expsAndPmidResults ) {
            String pmid = (String) expAndPmid[ 1 ];

            if ( pmid != null ) {
                expAndPmids.put( (String) expAndPmid[ 0 ], pmid );
            }
        }

        return expAndPmids;

    }

    public static Map<String, List<String>> getExperimentAcAndTaxids( String shortLabelLike ) {
        List<Object[]> expsAndTaxidResults = getSession().createCriteria( Experiment.class )
                .add( Restrictions.like( "shortLabel", shortLabelLike ) )
                .createAlias( "xrefs", "xref" )
                .createAlias( "xref.cvXrefQualifier", "cvXrefQual" )
                .add( Restrictions.eq( "cvXrefQual.shortLabel", CvXrefQualifier.TARGET_SPECIES ) )
                .setProjection( Projections.projectionList()
                        .add( Projections.distinct( Projections.property( "ac" ) ) )
                        .add( Projections.property( "xref.primaryId" ) ) ).list();

        Map<String, List<String>> expAndTaxid = new HashMap<String, List<String>>();

        for ( Object[] expAndTaxidResult : expsAndTaxidResults ) {
            String expAc = (String) expAndTaxidResult[ 0 ];
            String taxId = (String) expAndTaxidResult[ 1 ];

            if ( expAndTaxid.containsKey( expAc ) ) {
                expAndTaxid.get( expAc ).add( taxId );
            } else {
                List<String> taxIds = new ArrayList<String>();
                taxIds.add( taxId );
                expAndTaxid.put( expAc, taxIds );
            }
        }

        return expAndTaxid;
    }

    public static Map<String, Integer> countInteractionCountsForExperiments( String shortLabelLike ) {
        List<Object[]> expWithInteractionsCount = getSession().createCriteria( Experiment.class )
                .add( Restrictions.like( "shortLabel", shortLabelLike ) )
                .createAlias( "interactions", "int" )
                .setProjection( Projections.projectionList()
                        .add( Projections.distinct( Projections.property( "ac" ) ) )
                        .add( Projections.count( "int.ac" ) )
                        .add( Projections.groupProperty( "ac" ) ) ).list();

        Map<String, Integer> interactionCountByExpAc = new HashMap<String, Integer>();

        for ( Object[] expAndIntCount : expWithInteractionsCount ) {
            interactionCountByExpAc.put( (String) expAndIntCount[ 0 ], (Integer) expAndIntCount[ 1 ] );
        }

        return interactionCountByExpAc;

    }

    /**
     * Retreives experiment ahving assiciated dataset annotation.
     *
     * @param shortLabelLike experiments to look for.
     *
     * @return a map of association between an experiment and a dataset description.
     */
    public static Map<String, Collection<SimpleDataset>> datasetForExperiments( String shortLabelLike ) {

        List<Object[]> expWithDataset = getSession().createCriteria( Experiment.class )
                .add( Restrictions.like( "shortLabel", shortLabelLike ) )
                .createAlias( "annotations", "annot" )
                .createAlias( "annot.cvTopic", "topic" )
                .add( Restrictions.eq( "topic.shortLabel", "dataset" ) )
                .setProjection( Projections.projectionList()
                        .add( Projections.distinct( Projections.property( "ac" ) ) )
                        .add( Projections.property( "annot.annotationText" ) ) ).list();

        Map<String, Collection<SimpleDataset>> experiment2dataset = new HashMap<String, Collection<SimpleDataset>>();

        for ( Object[] exp2dataset : expWithDataset ) {
            String exp = (String) exp2dataset[ 0 ];
            String dataset = (String) exp2dataset[ 1 ];

            // parse dataset into name and description
            String name = null;
            if ( dataset != null ) {
                int idx = dataset.indexOf( "-" );
                if ( idx != -1 ) {
                    name = dataset.substring( 0, idx ).trim();
                } else {
                    name = dataset.trim();
                }
            }

            if ( name != null ) {

                // dont allow spaces in the name, replace them by _
                name = name.replaceAll( " ", "_" );

                Collection<SimpleDataset> datasets;
                if( experiment2dataset.containsKey( exp ) ) {
                    // retreive existing one
                    datasets = experiment2dataset.get( exp );
                } else {
                    // create a new entry
                    datasets = new ArrayList<SimpleDataset>( 2 );
                    experiment2dataset.put( exp, datasets );
                }

                // add dataset
                datasets.add( new SimpleDataset( name ) );
            } else {
                log.error( "Could not extract a dataset name out ot '" + dataset + "' for experiment '"+ shortLabelLike +"'." );
            }
        }

        return experiment2dataset;
    }

    private static Session getSession() {
        AbstractHibernateDataConfig dataConfig = (AbstractHibernateDataConfig) IntactContext.getCurrentInstance().getConfig().getDefaultDataConfig();
        return dataConfig.getSessionFactory().getCurrentSession();
    }
}