package uk.ac.ebi.intact.dataexchange.imex.idassigner;

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

import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.ExperimentUtils;

import javax.persistence.Query;
import java.util.List;

/**
 * Add a publication to experiment whenever missing and updates the "unassigned" publications adding the new PMID
 * when it has changed in the experiment.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: CreateMissingPublications.java 823 2010-05-07 11:08:17Z skerrien $
 */
public class CreateMissingPublications {

    private static final String UNASSIGNED = "unassigned";

    public static void main(String[] args) throws Exception {

        if ( args.length < 1 ) {
            System.err.println( "usage: CreateMissingPublications <db.profile>" );
            System.exit( 1 );
        }

        final String database = args[0];
        IntactContext.initContext(new String[] {"/META-INF/"+database+".spring.xml"});

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        final DaoFactory daoFactory = dataContext.getDaoFactory();

        final Query countQuery = daoFactory.getEntityManager().createQuery("select count(e) " +
                                                                           "from Experiment e left join e.publication as p " +
                                                                           "where    p is null " +
                                                                          "       or p.shortLabel like '"+UNASSIGNED+"%' " );
        final Long totalCount = (Long) countQuery.getResultList().iterator().next();
        System.out.println("Experiments unassigned or without publications: "+ totalCount);


        final int maxResults = 500;
        int currentIndex = 0;

        int updatedExpCount = 0;
        int expWithoutPubmedCount = 0;
        int expNotUnassignedAnymoreCount = 0;
        int newPublicationCreated = 0;

        List<Experiment> experiments = null;

        // TODO deal with DOI (e.g. floss-1997-1, EBI-931157)

        do {
            System.out.println("Processing ["+currentIndex+"-"+ (currentIndex + maxResults) +"]");

            final TransactionStatus transactionStatus = dataContext.beginTransaction();

            final Query query = daoFactory.getEntityManager().createQuery("select e " +
                                                                          "from Experiment e left join e.publication as p " +
                                                                          "where    p is null " +
                                                                          "      or p.shortLabel like '"+UNASSIGNED+"%' " +
                                                                          "order by e.ac");
            query.setFirstResult(currentIndex);
            query.setMaxResults(maxResults);

            experiments = query.getResultList();
            System.out.println( "Batch ["+currentIndex+","+ (maxResults) +"] contained " + experiments.size() + " experiment(s)." );

            for (Experiment exp : experiments) {
                ExperimentXref primaryRef = null;

                ExperimentXref firstPubmed = null;
                ExperimentXref lastDOI = null;
                ExperimentXref other = null;
                for (ExperimentXref xref : exp.getXrefs()) {
                    String qualMi = null;

                    if (xref.getCvXrefQualifier() != null) {
                        qualMi = xref.getCvXrefQualifier().getIdentifier();
                    }

                    if (CvXrefQualifier.PRIMARY_REFERENCE_MI_REF.equals(qualMi)) {
                        if (CvDatabase.PUBMED_MI_REF.equalsIgnoreCase(xref.getCvDatabase().getIdentifier())){
                            firstPubmed = xref;
                            break;
                        }
                        else if (CvDatabase.DOI_MI_REF.equalsIgnoreCase(xref.getCvDatabase().getIdentifier())){
                            lastDOI = xref;
                        }
                        else{
                            other = xref;
                        }
                    }
                }

                if (firstPubmed != null){
                    primaryRef = firstPubmed;
                }
                else if (lastDOI != null){
                    primaryRef = lastDOI;
                }
                else{
                    primaryRef = other;
                }

                if (primaryRef != null) {
                    String pubmed = primaryRef.getPrimaryId();

                    Publication publication = daoFactory.getPublicationDao().getByPubmedId(pubmed);

                    String oldLabel = null;

                    if (publication == null) {
                        System.out.println( "Creating new publication: " + pubmed + " for experiment " + exp.getShortLabel() );
                        publication = new Publication(exp.getOwner(), String.valueOf(pubmed));
                        newPublicationCreated++;
                    } else if (publication.getShortLabel().startsWith( UNASSIGNED )) {
                        oldLabel = publication.getShortLabel();

                        if (publication.getShortLabel().startsWith( UNASSIGNED )) {
                            for ( Xref xref : exp.getXrefs()) {
                                if ( CvDatabase.PUBMED_MI_REF.equals(xref.getCvDatabase().getIdentifier()) &&
                                        CvXrefQualifier.PRIMARY_REFERENCE_MI_REF.equals(xref.getCvXrefQualifier().getIdentifier())) {
                                    if (!xref.getPrimaryId().startsWith( UNASSIGNED )) {
                                        pubmed = xref.getPrimaryId();
                                    }
                                }
                            }
                        }

                        publication.setShortLabel(pubmed);
                        expNotUnassignedAnymoreCount++;
                    }

                    exp.setPublication(publication);
                    publication.addExperiment(exp);

                    System.out.println("Saving publication: "+publication.getShortLabel()+" ("+oldLabel+")");
                    daoFactory.getPublicationDao().saveOrUpdate(publication);
                    daoFactory.getExperimentDao().update(exp);

                    updatedExpCount++;
                } else {
                    System.out.println("Experiment without pubmed: "+exp.getShortLabel()+" AC="+exp.getAc()+" Fullname='"+exp.getFullName() + "'");
                    expWithoutPubmedCount++;
                }

                currentIndex++;
            }

            dataContext.commitTransaction(transactionStatus);

        } while (currentIndex < totalCount);

        System.out.println("Updated experiments: " + updatedExpCount);
        System.out.println("New publication object created: " + newPublicationCreated );
        System.out.println("Experiments without pubmed: " + expWithoutPubmedCount);
        System.out.println("Experiments that were unassigned, but not anymore: " + expNotUnassignedAnymoreCount);
    }
}
