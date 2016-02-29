/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.uniprotExport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.util.ElapsedTime;
import uk.ac.ebi.intact.util.uniprotExport.event.StatisticsCcLineEventListener;

import java.io.PrintStream;
import java.util.Calendar;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30-Aug-2006</pre>
 */
public class CcLineExportProgressThread extends Thread
{

    private static final Log log = LogFactory.getLog(CcLineExportProgressThread.class);

    private static final String NEW_LINE = System.getProperty( "line.separator");

    private final StatisticsCcLineEventListener listener;
    private PrintStream printStream;

    private final static int DEFAULT_SECONDS_WITHIN_CHECKS = 5;

    private int secondsWithinChecks = DEFAULT_SECONDS_WITHIN_CHECKS;

    public CcLineExportProgressThread(CCLineExport ccLineExport, int totalDrLinesCount)
    {
        this.listener = new StatisticsCcLineEventListener(totalDrLinesCount);
        ccLineExport.addCcLineExportListener(listener);
    }

    public CcLineExportProgressThread(CCLineExport ccLineExport, int totalDrLinesCount, PrintStream printStream)
    {
        this(ccLineExport, totalDrLinesCount);
        this.printStream = printStream;
    }

    @Override
    public void run()
    {
        log.info("Starting ccLineExport progress listener thread");

        double seconds = 0;

        while (!listener.isFinished()) {

            try
            {
                Thread.sleep(secondsWithinChecks*1000);

                seconds = seconds + secondsWithinChecks;
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            int drProcessed = listener.getDrProcessedCount();

            double speed = (double)drProcessed / seconds;

            int drRemaining = listener.getDrLinesRemaining();
            int secsRemaining = Double.valueOf((double)drRemaining / speed).intValue();

            ElapsedTime elapsedTime = new ElapsedTime(Double.valueOf(seconds).intValue());
            ElapsedTime remainingTime = new ElapsedTime(secsRemaining);

            Calendar finishDate = Calendar.getInstance();
            finishDate.set(Calendar.SECOND, secsRemaining);

            log.debug(listener.toString() );
            log.info("ETA: "+remainingTime.toString()+" ; Speed (DR Line / sec): "+speed);

            if (printStream != null)
            {
                printStream.append("------------------------------------------"+NEW_LINE);
                printStream.append("Elapsed time: "+elapsedTime.toString()+" ; ETA: "+remainingTime.toString()+" - "+finishDate.getTime()+NEW_LINE);
                printStream.append("Speed (DR Line / sec): "+speed+NEW_LINE);
                printStream.append(listener.toString()+NEW_LINE);
                printStream.flush();
            }

        }
    }


    public int getSecondsWithinChecks()
    {
        return secondsWithinChecks;
    }

    public void setSecondsWithinChecks(int secondsWithinChecks)
    {
        this.secondsWithinChecks = secondsWithinChecks;
    }
}
