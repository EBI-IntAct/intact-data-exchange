/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.DataContext;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;

import java.util.*;

/**
 * COPIED FROM THE DBUPDATE-MAVEN-DEPENDENCY to avoid group cyclic dependency data-exchange <-> plugins-miscel
 */
public class UpdateTargetSpecies {

    private static final Log log = LogFactory.getLog( UpdateTargetSpecies.class );

    //////////////////////////
    // Inner class

    //////////////////////////
    // Instance variable

    private CvDatabase newt = null;
    private CvXrefQualifier targetSpeciesQualifier = null;

    ///////////////////////////
    // Constructor

    public UpdateTargetSpecies() {
        if (getDataContext().isTransactionActive()) {
            throw new IntactException("Transaction must NOT be active when instantiating UpdateTargetSpecies");
        }

        beginTransaction();
        CvObjectDao cvObjectDao = getDataContext().getDaoFactory().getCvObjectDao();

        newt = (CvDatabase) cvObjectDao.getByPsiMiRef( CvDatabase.NEWT_MI_REF );

        if (newt == null) {
            throw new IllegalStateException("Cv is null: "+ CvDatabase.NEWT + "("+ CvDatabase.NEWT_MI_REF + ")");
        }

        targetSpeciesQualifier = (CvXrefQualifier) cvObjectDao.getByShortLabel( CvXrefQualifier.class, CvXrefQualifier.TARGET_SPECIES );

        if (targetSpeciesQualifier == null) {
            throw new IllegalStateException("Cv is null: "+ CvXrefQualifier.TARGET_SPECIES);
        }

        commitTransaction();
    }

    public void updateAllExperiments() {
        int firstResult = 0;
        int maxResults = 100;

        beginTransaction();
        int total = getDataContext().getDaoFactory().getExperimentDao().countAll();
        commitTransaction();

        Collection<Experiment> experiments;

        do {
            beginTransaction();
            experiments = getDataContext().getDaoFactory().getExperimentDao().getAll(firstResult, maxResults);
            commitTransaction();

            if (log.isInfoEnabled()) log.info("Processed "+firstResult+" out of "+total);

            for (Experiment experiment : experiments) {
                updateExperiment(experiment);
            }

            firstResult += maxResults;
        } while (!experiments.isEmpty());

        if (log.isInfoEnabled()) log.info("Finished processing "+total+" experiments");
    }

    public void updateExperiment(Experiment experiment) {
        if (getDataContext().isTransactionActive()) {
            throw new IntactException("Transaction must NOT be active");
        }

        if (log.isInfoEnabled()) log.info("Updating experiment: "+experiment.getAc()+" ("+experiment.getShortLabel()+")");

        beginTransaction();
        experiment = getDataContext().getDaoFactory().getExperimentDao().getByAc(experiment.getAc());

        Set<SimpleBioSource> existingBioSources = getExistingBioSources(experiment);
        Set<SimpleBioSource> allBioSources = getBioSourcesForExperimentInteractions(experiment);

        commitTransaction();

        Collection<SimpleBioSource> bioSourcesToRemove = CollectionUtils.subtract(existingBioSources, allBioSources);
        Collection<SimpleBioSource> bioSourcesToAdd = CollectionUtils.subtract(allBioSources, existingBioSources);
        
        if (log.isDebugEnabled()) {
            log.info("\tExisting: "+existingBioSources);
            log.debug("\tRemoving: "+bioSourcesToRemove);
            log.debug("\tAdding: "+bioSourcesToAdd);
            log.info("\tAll: "+allBioSources);
        }

        // refresh the experiment from the db
        beginTransaction();
        experiment = getDataContext().getDaoFactory().getExperimentDao().getByAc(experiment.getAc());

        // remove xrefs
        Collection<ExperimentXref> xrefs = experiment.getXrefs();
        for (Iterator<ExperimentXref> iterator = xrefs.iterator(); iterator.hasNext();) {
            ExperimentXref experimentXref = iterator.next();

            if (experimentXref.getCvXrefQualifier().equals(targetSpeciesQualifier)) {
                for (SimpleBioSource bioSourceToRemove : bioSourcesToRemove) {
                    if (experimentXref.getPrimaryId().equals(bioSourceToRemove.getTaxId())) {
                        iterator.remove();
                        getDataContext().getDaoFactory().getXrefDao(ExperimentXref.class).delete(experimentXref);
                        break;
                    }
                }
            }
        }

        // add new xrefs
        for (SimpleBioSource bioSourceToAdd : bioSourcesToAdd) {
            ExperimentXref xref = new ExperimentXref(experiment.getOwner(), newt,
                                                      bioSourceToAdd.getTaxId(), bioSourceToAdd.getLabel(), null, targetSpeciesQualifier);
            experiment.addXref(xref);
        }

        commitTransaction();

    }

    protected DataContext getDataContext() {
        return IntactContext.getCurrentInstance().getDataContext();
    }

    protected Set<SimpleBioSource> getExistingBioSources(Experiment experiment) {
        Set<SimpleBioSource> existingBioSources = new HashSet<SimpleBioSource>();

        for (ExperimentXref xref : getTargetSpeciesXrefs(experiment)) {
             existingBioSources.add(new SimpleBioSource(xref.getSecondaryId(), xref.getPrimaryId()));
        }

        return existingBioSources;
    }

    protected Set<SimpleBioSource> getBioSourcesForExperimentInteractions(Experiment experiment) {
        Set<SimpleBioSource> allBioSources = new HashSet<SimpleBioSource>();

        String experimentAc = experiment.getAc();

        int firstResult = 0;
        int maxResults = 50;
        Collection<Interaction> interactions;

        do {
            beginTransaction();
            interactions = getDataContext().getDaoFactory()
                    .getExperimentDao().getInteractionsForExperimentWithAc(experimentAc, firstResult, maxResults);

            for (Interaction interaction : interactions) {
                for (Component component : interaction.getComponents()) {
                    BioSource bioSource = component.getInteractor().getBioSource();

                    if (bioSource != null) {
                        allBioSources.add(new SimpleBioSource(bioSource));
                    } else {
                       if (log.isDebugEnabled()) log.debug("Interactor without biosource: "+component.getInteractor()); 
                    }
                }
            }
            commitTransaction();

            firstResult += maxResults;
        } while (!interactions.isEmpty());

        return allBioSources;
    }

    /**
     * Collects all Xref having a CvXrefQualifier( target-species ) linked to the given experiement.
     *
     * @param experiment
     *
     * @return a Collection of Xref. never null.
     */
    public List<ExperimentXref> getTargetSpeciesXrefs( Experiment experiment ) {

        List<ExperimentXref> targets = new ArrayList<ExperimentXref>();

        for ( ExperimentXref xref : experiment.getXrefs() ) {
            if ( targetSpeciesQualifier.equals( xref.getCvXrefQualifier() ) ) {
                targets.add( xref );
            }
        }
        return targets;
    }

    protected void beginTransaction() {
        getDataContext().beginTransaction();
    }

    protected void commitTransaction() {
        try {
            getDataContext().commitTransaction();
        } catch (IntactTransactionException e) {
            throw new IntactException(e);
        }
    }

    protected class SimpleBioSource {

        private String taxId;
        private String label;

        private SimpleBioSource(String label, String taxId) {
            this.label = label;
            this.taxId = taxId;
        }

        private SimpleBioSource(BioSource bioSource) {
            this.label = bioSource.getShortLabel();
            this.taxId = bioSource.getTaxId();
        }

        public String getLabel() {
            return label;
        }

        public String getTaxId() {
            return taxId;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SimpleBioSource that = (SimpleBioSource) o;

            if (!taxId.equals(that.taxId)) return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            return taxId.hashCode();
        }

        @Override
        public String toString() {
            return taxId+"("+label+")";
        }
    }


}