package uk.ac.ebi.intact.task.mitab.pmid;

import java.util.Date;

/**
 * The publication entry is an mitab inputstream with a few information about the publication. A publicationEntry only contains interactions of a same publication.
 * It contains the created date (folder where to write the entry) and the name of the entry (name of the file where to write this entry)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/08/12</pre>
 */

public class PublicationFileEntry implements Comparable<PublicationFileEntry>{

    /**
     * Date of creation of the publication
     */
    private Date createdDate;
    /**
     * Name of this entry. It will be the name of the publication file
     */
    private String entryName;
    /**
     * The mitab inputStream to write
     */
    private StringBuffer binaryInteractions;

    private boolean isNegative;

    public PublicationFileEntry(Date createdDate, String pubId, StringBuffer mitab, boolean isNegative){
        super();
        this.createdDate = createdDate;
        this.entryName = pubId;

        this.binaryInteractions = new StringBuffer(1064);

        if (mitab != null){
            this.binaryInteractions.append(mitab);
        }
        this.isNegative = isNegative;
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

    public StringBuffer getBinaryInteractions() {
        return binaryInteractions;
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

    public boolean isNegative() {
        return isNegative;
    }
}
