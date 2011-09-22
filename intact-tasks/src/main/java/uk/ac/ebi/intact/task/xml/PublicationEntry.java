package uk.ac.ebi.intact.task.xml;

import psidev.psi.mi.xml.model.EntrySet;

import java.util.Date;

/**
 * The publication entry is an intactEntry with a few information about the publication. A publicationEntry only contains interactions of a same publication.
 * It contains the created date (folder where to write the entry) and the name of the entry (name of the file where to write this entry)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/09/11</pre>
 */

public class PublicationEntry implements Comparable<PublicationEntry>{

    /**
     * Date of creation of the publication
     */
    private Date createdDate;
    /**
     * Name of this entry. It will be the name of the publication file
     */
    private String entryName;
    /**
     * The xml entry to write
     */
    private EntrySet xmlEntry;

    public PublicationEntry(Date createdDate, String pubId, EntrySet xmlEntry){
        super();
        this.createdDate = createdDate;
        this.entryName = pubId;
        this.xmlEntry = xmlEntry;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public EntrySet getXmlEntry() {
        return xmlEntry;
    }

    public void setXmlEntry(EntrySet xmlEntry) {
        this.xmlEntry = xmlEntry;
    }

    @Override
    public int compareTo(PublicationEntry o) {
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

        PublicationEntry entry2 = (PublicationEntry) o;

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
