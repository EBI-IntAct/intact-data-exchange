/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.uniprotExport.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30-Aug-2006</pre>
 */
public class StatisticsCcLineEventListener implements CcLineEventListener
{

    private static final Log log = LogFactory.getLog(StatisticsCcLineEventListener.class);

    private final int totalDrLinesCount;

    private int nonBinaryInteractionsCount = 0;
    private int drProcessedCount = 0;
    private int ccLinesCreatedCount = 0;

    public StatisticsCcLineEventListener(int totalDrLinesCount)
    {
        this.totalDrLinesCount = totalDrLinesCount;
    }

    public void processNonBinaryInteraction(NonBinaryInteractionFoundEvent evt)
    {
        nonBinaryInteractionsCount++;
    }

    public void drLineProcessed(DrLineProcessedEvent evt)
    {
        drProcessedCount++;
    }

    public void ccLineCreated(CcLineCreatedEvent evt)
    {
        ccLinesCreatedCount++;
    }

    public int getTotalDrLinesCount()
    {
        return totalDrLinesCount;
    }

    public int getNonBinaryInteractionsCount()
    {
        return nonBinaryInteractionsCount;
    }

    public int getDrProcessedCount()
    {
        return drProcessedCount;
    }

    public int getCcLinesCreatedCount()
    {
        return ccLinesCreatedCount;
    }

    public boolean isFinished()
    {
        return drProcessedCount == totalDrLinesCount;
    }

    public int getDrLinesRemaining()
    {
        return totalDrLinesCount-drProcessedCount;
    }

    @Override
    public String toString()
    {
        return "StatisticsCcLineEventListener{" +
                "totalDrLinesCount=" + totalDrLinesCount +
                ", nonBinaryInteractionsCount=" + nonBinaryInteractionsCount +
                ", drProcessedCount=" + drProcessedCount +
                ", ccLinesCreatedCount=" + ccLinesCreatedCount +
                '}';
    }
}
