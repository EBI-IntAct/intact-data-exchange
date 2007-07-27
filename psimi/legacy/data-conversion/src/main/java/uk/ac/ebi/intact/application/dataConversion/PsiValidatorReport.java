/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the result of the validation
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>18-Aug-2006</pre>
 */
public class PsiValidatorReport
{

    private static final Log log = LogFactory.getLog(PsiValidatorReport.class);

    protected static final String NEW_LINE = System.getProperty("line.separator");

    private boolean valid = true;
    private List<PsiValidatorMessage> messages;

    public PsiValidatorReport()
    {
        this.messages = new ArrayList<PsiValidatorMessage>();
    }

    public boolean isValid()
    {
        return valid;
    }


    public void setValid(boolean valid)
    {
        this.valid = valid;
    }

    public List<PsiValidatorMessage> getMessages()
    {
        return messages;
    }

    public void addMessage(PsiValidatorMessage msg)
    {
        messages.add(msg);
    }


    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (PsiValidatorMessage msg : messages)
        {
            sb.append("\t").append(msg).append(NEW_LINE);
        }

        return sb.toString();
    }
}
