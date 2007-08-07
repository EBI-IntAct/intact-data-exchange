package uk.ac.ebi.intact.dataexchange.imex.repository.split.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.Interaction;
import psidev.psi.mi.xml.model.Participant;
import uk.ac.ebi.intact.dataexchange.imex.repository.ImexRepositoryContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.Repository;
import uk.ac.ebi.intact.dataexchange.imex.repository.RepositoryException;
import uk.ac.ebi.intact.dataexchange.imex.repository.RepositoryHelper;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntrySet;
import uk.ac.ebi.intact.dataexchange.imex.repository.split.EntrySetSplitter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DefaultEntrySetSplitter implements EntrySetSplitter
{
    private static final Log log = LogFactory.getLog(DefaultEntrySetSplitter.class);

    public List<RepoEntry> splitRepoEntrySet(RepoEntrySet repoEntrySet) throws IOException
    {
        if (log.isDebugEnabled()) log.debug("Splitting RepoEntrySet: "+repoEntrySet.getName());

        List<RepoEntry> repoEntries = new ArrayList<RepoEntry>();

        Repository repository = ImexRepositoryContext.getInstance().getRepository();
        RepositoryHelper helper = new RepositoryHelper(repository);

        File fileToSplit = helper.getEntrySetFile(repoEntrySet);

        PsimiXmlReader reader = new PsimiXmlReader();
        PsimiXmlWriter writer = new PsimiXmlWriter();

        EntrySet entrySet = null;
        try
        {
            entrySet = reader.read(fileToSplit);
        }
        catch (Exception e)
        {
            throw new RepositoryException("Problem splitting file, while reading XML", e);
        }

        for (Entry entry : entrySet.getEntries())
        {
            RepoEntry repoEntry = entryToRepoEntry(entry);
            repoEntry.setRepoEntrySet(repoEntrySet);

            EntrySet entrySetToWrite = new EntrySet(Arrays.asList(entry), entrySet.getLevel(), entrySet.getVersion(), entrySet.getMinorVersion());
            try
            {
                File splittedFile = helper.getEntryFile(repoEntry);

                if (log.isDebugEnabled()) log.debug("\tCreated splitted: "+splittedFile);

                writer.write(entrySetToWrite, splittedFile);
 
                ImexRepositoryContext.getInstance().getImexServiceProvider()
                        .getRepoEntryService().saveRepoEntry(repoEntry);
                repoEntries.add(repoEntry);

            }
            catch (Exception e)
            {
                throw new RepositoryException("Problem splitting file, while writing XML", e);
            }
        }

        return repoEntries;
    }

    private void normalizeEntry(Entry entry) {
        if (entry.getExperiments().isEmpty()) {
            for (Interaction interaction : entry.getInteractions()) {
                entry.getExperiments().addAll(interaction.getExperiments());
            }
        }

        if (entry.getInteractors().isEmpty()) {
            for (Interaction interaction : entry.getInteractions()) {
                for(Participant participant : interaction.getParticipants()) {
                    entry.getInteractors().add(participant.getInteractor());
                }
            }
        }
    }

    protected RepoEntry entryToRepoEntry(Entry entry)
    {
        RepoEntry repoEntry = new RepoEntry();

        normalizeEntry(entry);

        String name = entry.getExperiments().iterator().next()
                .getBibref().getXref().getPrimaryRef().getId();
        repoEntry.setName(name);

        return repoEntry;
    }
}
