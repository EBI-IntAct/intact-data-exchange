/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.business.IntactException;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09-Aug-2006</pre>
 */
public class ExperimentListItem
{

    private static final Log log = LogFactory.getLog(ExperimentListItem.class);

    /**
     * Unreadable but working pattern to parse an experimentListItem from a String.
     * Note: file path can contain / or \ indifferently
     */
    private static final Pattern PATTERN =
            Pattern.compile("((?:\\w+(?:/|\\\\))*)(\\w+(?:-\\d+)?-?+(?:_small)?)(?:_\\w+-\\d{4}-\\d+)?(?:-|_)?(\\d{1,2})?_?(negative)?\\.xml\\s(\\S+(?:,\\S)*)+(?:\\s\\[(\\d+),(\\d+)\\])?");

    private Collection<String> experimentLabels;

    /**
     * Name of the file.
     */
    private String name;
    private boolean negative;
    private Integer chunkNumber;
    private Integer largeScaleChunkSize;
    private String parentFolders;


    public ExperimentListItem(Collection<String> experimentLabels, String name, String parentFolders, boolean negative, Integer chunkNumber, Integer largeScaleSize)
    {
        this.experimentLabels = experimentLabels;
        this.name = name;
        this.negative = negative;
        this.chunkNumber = chunkNumber;
        this.largeScaleChunkSize = largeScaleSize;
        this.parentFolders = removeTrailingSlash(parentFolders);
    }

    public String getFilename()
    {
        String strNegative = "";
        if (negative)
        {
            strNegative = "_negative";
        }

        String fileNumber = "";
        if (chunkNumber != null)
        {
            fileNumber = "-"+twoDigitNumber(chunkNumber);
        }

        String strLargeScale = "";

        if (largeScaleChunkSize != null)
        {
            if (experimentLabels.size() > 1)
            {
                throw new RuntimeException("On large scale items, only one experiment label is allowed");
            }

            strLargeScale = "_"+experimentLabels.iterator().next()+"_"+twoDigitNumber(chunkNumber);
            fileNumber = "";
        }

        return parentFolders + FileHelper.SLASH + name +
               strLargeScale + fileNumber + strNegative + FileHelper.XML_FILE_EXTENSION;
    }

    public String getPattern()
    {
        StringBuffer sb = new StringBuffer();

        int i=0;
        for (String experimentLabel : experimentLabels)
        {
            if (i>0)
            {
               sb.append(",");
            }

            sb.append(experimentLabel);

            i++;
        }

        return sb.toString();
    }

    public String getInteractionRange()
    {
        if (largeScaleChunkSize == null)
        {
            return "";
        }

        int first = ((chunkNumber-1)*largeScaleChunkSize)+1;
        int last = chunkNumber*largeScaleChunkSize;

        return "["+first+","+last+"]";
    }

    public Integer getChunkNumber()
    {
        return chunkNumber;
    }

    public boolean isNegative()
    {
        return negative;
    }

    public String getName()
    {
        return name;
    }

    public Collection<String> getExperimentLabels()
    {
        return experimentLabels;
    }


    public Integer getLargeScaleChunkSize()
    {
        return largeScaleChunkSize;
    }


    public String getParentFolders()
    {
        return parentFolders;
    }

    /**
     * Instantiates a ExperimentListItem by parsing a String
     * @param strItem
     * @return
     */
    public static ExperimentListItem parseString(String strItem)
    {
        Matcher matcher = PATTERN.matcher(strItem);

        if (matcher.find())
        {
            String parentFolder = removeTrailingSlash(matcher.group(1));
            String name = matcher.group(2);
            String strChunk = matcher.group(3);
            String strNegative = matcher.group(4);
            String strExperimentLabels = matcher.group(5);
            String strFirstResult = matcher.group(6);
            String strLastResult = matcher.group(7);

            Integer chunk = null;

            if (strChunk != null)
            {
                chunk = Integer.parseInt(strChunk);
            }

            boolean negative = false;

            if (name.endsWith("_negative") || strNegative != null)
            {
                negative = true;
                name = name.replaceAll("_negative", "");
            }

            String[] experimentLabels = strExperimentLabels.split(",");

            Integer largeScaleSize = null;

            if (strFirstResult != null && strLastResult != null)
            {
                largeScaleSize = Integer.parseInt(strLastResult)-Integer.parseInt(strFirstResult)+1;
            }

            return new ExperimentListItem(Arrays.asList(experimentLabels), name, parentFolder, negative, chunk, largeScaleSize);
        }
        else
        {
            throw new IntactException("Could not parse string: " + strItem);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        ExperimentListItem eli = (ExperimentListItem) obj;

        return getFilename().equals(eli.getFilename()) && getPattern().equals(eli.getPattern());
    }

    @Override
    public String toString()
    {
        return (getFilename()+" "+getPattern()+" "+getInteractionRange()).trim();
    }

    @Override
    public int hashCode()
    {
        return 47*getFilename().hashCode()*getPattern().hashCode();
    }

    private static String twoDigitNumber(Integer number)
    {
        String strNum = "";

        if (number == null)
        {
            strNum = "0";
        }

        if (number != null && number < 10)
        {
            strNum = "0";
        }
        strNum = strNum + number;

        return strNum;
    }

    private static String removeTrailingSlash( String folder ) {
        if ( folder.endsWith( FileHelper.SLASH ) ) {
            folder = folder.substring( 0, folder.length() - 1 );
        }
        return folder;
    }
}
