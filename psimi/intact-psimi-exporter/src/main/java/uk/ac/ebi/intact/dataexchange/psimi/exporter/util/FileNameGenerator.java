package uk.ac.ebi.intact.dataexchange.psimi.exporter.util;

import java.text.DecimalFormat;

/**
 * This class allows to transform a publication id and create a file name
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/09/11</pre>
 */

public class FileNameGenerator {

    private String newCharacter = "_";
    private String charactersToReplace = "\\/|\\.|;|,| |\\)|\\(|\\\n|\\\t";

    private String characterToTruncate = "(";

    /**
     * separator for the entry name
     */
    private String separator = "_";

    /**
     * Negative tag
     */
    private String negativeTag = "negative";

    private DecimalFormat decimalFormat;

    public FileNameGenerator(){
        decimalFormat = new DecimalFormat("00");
    }

    public FileNameGenerator(String newCharacter){
        decimalFormat = new DecimalFormat("00");
        this.newCharacter = newCharacter;
    }

    public FileNameGenerator(String newCharacter, String charactersToReplace){
        decimalFormat = new DecimalFormat("00");
        if (charactersToReplace != null){
            this.charactersToReplace = charactersToReplace;
        }

        this.newCharacter = newCharacter;
    }

    public String replaceBadCharactersFor(String name){
        String newPublication = name.trim().replaceAll(charactersToReplace, newCharacter);

        return newPublication;
    }

    public String truncateToFirstBadCharacter(String name){
        StringBuffer buffer = new StringBuffer();
        for (char c : name.toCharArray()){

        }
        String newPublication = name.trim().replaceAll(charactersToReplace, newCharacter);

        return newPublication;
    }

    public String createPublicationName(String name, Integer chunk, boolean isNegative){
         StringBuffer buffer = new StringBuffer();

        buffer.append(replaceBadCharactersFor(name));
        if (chunk != null){
            buffer.append(separator);
            buffer.append(decimalFormat.format(chunk));
        }
        if (isNegative && negativeTag != null){
            buffer.append(separator);
            buffer.append(negativeTag);
        }

        return buffer.toString();
    }

    public String createPublicationName(String name, String experimentLabel, Integer chunk, boolean isNegative){
         StringBuffer buffer = new StringBuffer();

        buffer.append(replaceBadCharactersFor(name));
        if (experimentLabel != null){
            buffer.append(separator);
            buffer.append(experimentLabel);
        }
        if (chunk != null){
            buffer.append(separator);
            buffer.append(decimalFormat.format(chunk));
        }
        if (isNegative && negativeTag != null){
            buffer.append(separator);
            buffer.append(negativeTag);
        }

        return buffer.toString();
    }

    public String createSpeciesName(String name, Integer chunk, boolean isNegative){
        StringBuffer buffer = new StringBuffer();

        String truncatedName = name;
        if (name.contains(characterToTruncate)){
            truncatedName = name.substring(0, name.indexOf(characterToTruncate));
        }

        buffer.append(replaceBadCharactersFor(truncatedName));
        if (chunk != null){
            buffer.append(separator);
            buffer.append(decimalFormat.format(chunk));
        }
        if (isNegative && negativeTag != null){
            buffer.append(separator);
            buffer.append(negativeTag);
        }

        return buffer.toString();
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getNegativeTag() {
        return negativeTag;
    }

    public void setNegativeTag(String negativeTag) {
        this.negativeTag = negativeTag;
    }

    public String getCharacterToTruncate() {
        return characterToTruncate;
    }

    public void setCharacterToTruncate(String characterToTruncate) {
        this.characterToTruncate = characterToTruncate;
    }
}
