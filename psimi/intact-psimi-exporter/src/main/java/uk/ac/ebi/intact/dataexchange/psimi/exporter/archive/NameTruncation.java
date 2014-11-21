package uk.ac.ebi.intact.dataexchange.psimi.exporter.archive;

/**
 * A NameTruncation is a class which truncates a name using a name separator.
 * The nameSeparator is a property which can be customized
 *
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>06/10/11</pre>
 */

public class NameTruncation {

    private String nameSeparator;

    public NameTruncation(){

    }

    public NameTruncation(String separator){
        nameSeparator = separator;
    }

    public String truncate(String line){
        if (line == null){
            return null;
        }

        // the NameTruncation always truncate the first nameSeparator!!!
        if (line.contains(nameSeparator)){
            int indexOfSeparator = line.indexOf(nameSeparator);

            return line.substring(0, indexOfSeparator);
        }
        else {
            return line;
        }
    }

    public String getNameSeparator() {
        return nameSeparator;
    }

    public void setNameSeparator(String nameSeparator) {
        this.nameSeparator = nameSeparator;
    }
}
