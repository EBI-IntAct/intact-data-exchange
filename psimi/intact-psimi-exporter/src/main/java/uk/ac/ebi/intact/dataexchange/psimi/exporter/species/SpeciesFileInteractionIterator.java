package uk.ac.ebi.intact.dataexchange.psimi.exporter.species;

import psidev.psi.mi.jami.datasource.InteractionStream;
import psidev.psi.mi.jami.factory.MIDataSourceFactory;
import psidev.psi.mi.jami.factory.options.MIFileDataSourceOptions;
import psidev.psi.mi.jami.model.Interaction;
import psidev.psi.mi.jami.model.InteractionEvidence;
import psidev.psi.mi.jami.model.ParticipantEvidence;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Iterator of interaction evidences that open files and filter by taxid
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27/09/11</pre>
 */

public class SpeciesFileInteractionIterator implements Iterator<InteractionEvidence>{

    private Iterator<File> fileIterator;
    private int taxid;

    private Iterator interactionIterator;
    private InteractionStream interactionSource;
    private Map<String, Object> dataSourceOptions;

    private InteractionEvidence currentInteraction;

    public SpeciesFileInteractionIterator(List<File> files, int taxid, Map<String, Object> dataSourceOptions){
        if (files == null){
            throw new IllegalArgumentException("The list of files cannot be null");
        }
        this.fileIterator =files.iterator();
        this.taxid = taxid;
        if (dataSourceOptions == null || dataSourceOptions.isEmpty()){
            throw new IllegalArgumentException("The options for the datasource cannot be null or empty");
        }
        this.dataSourceOptions = dataSourceOptions;
        readNextInteraction();
    }

    protected void readNextInteraction() {
        if (this.interactionIterator != null && this.interactionIterator.hasNext()){
            this.currentInteraction = null;
            Interaction i = (Interaction)this.interactionIterator.next();
            while ( currentInteraction == null
                    && this.interactionIterator.hasNext()){
                if (i instanceof InteractionEvidence && doesInteractionInvolvesSpecies(this.taxid, (InteractionEvidence)i)){
                    this.currentInteraction = (InteractionEvidence)i;
                }
            }
        }
        else if (this.fileIterator.hasNext()){
            initialiseDataSource(this.fileIterator.next());

            this.interactionIterator = this.interactionSource.getInteractionsIterator();
            this.currentInteraction = null;

            if (this.interactionIterator.hasNext()){
                Interaction i = (Interaction)this.interactionIterator.next();
                while ( currentInteraction == null
                        && this.interactionIterator.hasNext()){
                    if (i instanceof InteractionEvidence && doesInteractionInvolvesSpecies(this.taxid, (InteractionEvidence)i)){
                        this.currentInteraction = (InteractionEvidence)i;
                    }
                }
            }
        }
        else {
            if (this.interactionSource != null){
                this.interactionSource.close();
            }
            this.currentInteraction = null;
        }
    }

    protected boolean doesInteractionInvolvesSpecies(int taxid, InteractionEvidence evidence) {
        for (ParticipantEvidence p : evidence.getParticipants()){
            if (p.getInteractor().getOrganism() != null && p.getInteractor().getOrganism().getTaxId() == taxid){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasNext() {
        return currentInteraction != null;
    }

    @Override
    public InteractionEvidence next() {
        InteractionEvidence interaction = this.currentInteraction;
        readNextInteraction();
        return interaction;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from this iterator");
    }

    protected void initialiseDataSource(File file) {
        // add mandatory options
        dataSourceOptions.put(MIFileDataSourceOptions.INPUT_OPTION_KEY, file);

        if (this.interactionSource == null){
            MIDataSourceFactory dataSourceFactory = MIDataSourceFactory.getInstance();
            this.interactionSource = dataSourceFactory.getInteractionSourceWith(dataSourceOptions);
        }
        else{
            this.interactionSource.close();
            this.interactionSource.initialiseContext(dataSourceOptions);
        }

        if (this.interactionSource == null){
            throw new IllegalStateException("We cannot find a valid interaction datasource with the given options.");
        }
    }
}
