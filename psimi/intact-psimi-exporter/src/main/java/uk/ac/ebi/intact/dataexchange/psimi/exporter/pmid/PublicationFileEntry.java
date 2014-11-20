package uk.ac.ebi.intact.dataexchange.psimi.exporter.pmid;

import psidev.psi.mi.jami.model.InteractionEvidence;

import java.util.Collection;
import java.util.Date;

/**
 * The publication entry is an intactEntry with a few information about the publication.
 * A publicationEntry only contains interactions of a same publication.
 * It contains the created date (folder where to write the entry) and the name of the entry (name of the file where to write this entry)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/09/11</pre>
 */

public class PublicationFileEntry implements Comparable<PublicationFileEntry> {

    /**
     * Date of creation of the publication
     */
    private Date createdDate;
    /**
     * Name of this entry. It will be the name of the publication file
     */
    private String entryName;
    /**
     * The interactions to write
     */
    private Collection<InteractionEvidence> interactions;

    public PublicationFileEntry(Date createdDate, String pubId, Collection<InteractionEvidence> interactions){
        super();
        this.createdDate = createdDate;
        this.entryName = pubId;
        this.interactions = interactions;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getEntryName() {
        return entryName;
    }

    public Collection<InteractionEvidence> getInteractions() {
        return interactions;
    }

    @Override
    public int compareTo(PublicationFileEntry o) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if ( this == o ) return EQUAL;

        int comparison = this.createdDate.compareTo(o.getCreatedDate());
        if ( comparison == EQUAL ) {
            return this.entryName.compareTo(o.getEntryName());
        }

        return comparison;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        PublicationFileEntry entry2 = (PublicationFileEntry) o;

        if (!createdDate.equals(entry2.getCreatedDate()))
        {
            return false;
        }
        if (!entryName.equals(entry2.getEntryName()))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = createdDate.hashCode();
        result = 31 * result + entryName.hashCode();
        return result;
    }
}
