/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXParseException;

/**
 * Represents a validation message
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>18-Aug-2006</pre>
 */
public class PsiValidatorMessage
{

    private static final Log log = LogFactory.getLog(PsiValidatorMessage.class);

    public enum Level {
        WARN("Warn"), ERROR("Error"), FATAL("Fatal");

        private String levelAsString;

        private Level(String levelAsString)
        {
            this.levelAsString = levelAsString;
        }

        @Override
        public String toString()
        {
            return levelAsString;
        }
    }

    private String messageText;
    private Level level;
    private String systemId;
    private int lineNumber;
    private int columnNumber;

     public PsiValidatorMessage(Level level, SAXParseException e)
    {
        this.messageText = e.getMessage();
        this.level = level;
        this.systemId = e.getSystemId();
        this.lineNumber = e.getLineNumber();
        this.columnNumber = e.getColumnNumber();
    }

    public PsiValidatorMessage(String messageText, Level level, String systemId, int lineNumber, int columnNumber)
    {
        this.messageText = messageText;
        this.level = level;
        this.systemId = systemId;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
    
    public String getMessageText()
    {
        return messageText;
    }

    public Level getLevel()
    {
        return level;
    }

    public String getSystemId()
    {
        return systemId;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    public int getColumnNumber()
    {
        return columnNumber;
    }

    @Override
    public String toString()
    {
        return level+":("+lineNumber+","+columnNumber+") "+messageText;
    }
}
