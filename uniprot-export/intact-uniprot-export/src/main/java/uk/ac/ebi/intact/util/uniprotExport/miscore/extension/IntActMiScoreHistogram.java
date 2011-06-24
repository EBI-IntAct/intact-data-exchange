package uk.ac.ebi.intact.util.uniprotExport.miscore.extension;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import uk.ac.ebi.enfin.mi.score.distribution.MiscoreHistogram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The class to display the score distribution
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24-Sep-2010</pre>
 */

public class IntActMiScoreHistogram extends MiscoreHistogram{
    double minScore = 0;
    double maxScore = 1;

    public double[] getValuesBetween(double min, double max){
        List<Double> subset = new ArrayList<Double>();

        for (double v : getValues()){
            if (v <= max && v >= min){
                subset.add(v);
            }
        }

        double [] subsetValues = new double [subset.size()];

        for (int i = 0; i < subset.size(); i++){
            subsetValues[i] = subset.get(i);
        }

        return subsetValues;
    }

    public static double[] extractValuesBetween(double [] values, double min, double max){
        List<Double> subset = new ArrayList<Double>();

        for (double v : values){
            if (v <= max && v >= min){
                subset.add(v);
            }
        }

        double [] subsetValues = new double [subset.size()];

        for (int i = 0; i < subset.size(); i++){
            subsetValues[i] = subset.get(i);
        }

        return subsetValues;
    }

    public void createChart(String pngFileName){
        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.FREQUENCY);

        double [] values = getValuesBetween(getMinScore(), getMaxScore());
        dataset.addSeries(getTitle(), values,getNumberOfBars(),getMinScore(),getMaxScore());
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

    public void createRelativeChart(String pngFileName){
        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.RELATIVE_FREQUENCY);

        double [] values = getValuesBetween(getMinScore(), getMaxScore());
        dataset.addSeries(getTitle(), values,getNumberOfBars(),getMinScore(),getMaxScore());
        String plotTitle = getTitle();
        String xaxis = "Score";
        String yaxis = "% of interactions";
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

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }
}
