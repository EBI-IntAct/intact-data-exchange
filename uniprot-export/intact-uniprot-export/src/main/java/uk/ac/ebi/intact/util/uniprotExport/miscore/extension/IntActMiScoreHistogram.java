package uk.ac.ebi.intact.util.uniprotExport.miscore.extension;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import uk.ac.ebi.enfin.mi.score.distribution.MiscoreHistogram;

import java.io.File;
import java.io.IOException;

/**
 * The class to display the score distribution
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24-Sep-2010</pre>
 */

public class IntActMiScoreHistogram extends MiscoreHistogram{

    public void createChart(String pngFileName){
        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.FREQUENCY);
        setNumberOfBars(100);
        setMinimumScore(0);
        setMaximumScore(1);
        
        dataset.addSeries(getTitle(),getValues(),getNumberOfBars(),getMinimumScore(),getMaximumScore());
        String plotTitle = getTitle();
        String xaxis = "Score";
        String yaxis = "Number of interactions";
        PlotOrientation orientation = PlotOrientation.VERTICAL;
        boolean show = false;
        boolean toolTips = false;
        boolean urls = false;
        JFreeChart chart = ChartFactory.createHistogram( plotTitle, xaxis, yaxis,
                dataset, orientation, show, toolTips, urls);

        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.getRangeAxis().setAutoRange(true);

        try {
            ChartUtilities.saveChartAsPNG(new File(pngFileName), chart, getWidth(), getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
