package uk.ac.ebi.intact.util.uniprotExport.miscore.extension;

import org.apache.commons.collections.CollectionUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class displays comparative stacked bars of different mi scores
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/02/11</pre>
 */

public class IntactStackedBarChart {

    Map<String, ComparativeValues> interactionScores = new HashMap<String, ComparativeValues>();
    private String fileNameA;
    private String fileNameB;
    private int width = 500;
    private int height = 300;

    private static final String SCORE_SEPARATOR = ":";

    public IntactStackedBarChart(String fileNameA, String fileNameB) {
        this.fileNameA = fileNameA;
        this.fileNameB = fileNameB;
        Map<String, Integer> scoresA = extractScoresFrom(this.fileNameA);
        Map<String, Integer> scoresB = extractScoresFrom(this.fileNameB);

        initializeMapOfInteractionScores(scoresA, scoresB);
    }

    private void initializeMapOfInteractionScores(Map<String, Integer> scoresA, Map<String, Integer> scoresB){
        Set<String> existingScores = new HashSet(CollectionUtils.union(scoresA.keySet(), scoresB.keySet()));

        for (String score : existingScores){
            Integer numberA = scoresA.get(score) != null ? scoresA.get(score) : 0;
            Integer numberB = scoresB.get(score) != null ? scoresB.get(score) : 0;

            ComparativeValues comparison = new ComparativeValues(numberA, numberB);
            this.interactionScores.put(score, comparison);
        }
    }

    private Map<String, Integer> extractScoresFrom(String fileName) {
        Map<String, Integer> clusteredScores = new HashMap<String, Integer>();

        try {
            FileReader fstream = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fstream);
            String line;

            while((line = br.readLine()) != null) {

                if (line.contains(SCORE_SEPARATOR)){
                    String[] values = line.split(SCORE_SEPARATOR);

                    if (values.length == 2){
                        String score = values[1];

                        if (clusteredScores.containsKey(score)){
                            Integer number = clusteredScores.get(score);
                            number = number + 1;
                            clusteredScores.put(score, number);
                        }
                        else{
                            Integer number = 1;
                            clusteredScores.put(score, number);
                        }
                    }
                    else {
                        System.err.println("the line " + line + " cannot be loaded because is not of the form 'id-interactorA-interactorB:score'");
                    }
                }
                else {
                    System.err.println("the line " + line + " cannot be loaded because is not of the form 'id-interactorA-interactorB:score'");
                }
            }

            fstream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clusteredScores;
    }

    public Map<String, ComparativeValues> getInteractionScores() {
        return interactionScores;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void createChart(String pngFileName, double minimum, double maximum){
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // series = A or B
        // categories = score value
        // values = number of interactions having this score

        // create datasets
        int numberOfCategories = 0;
        int numberOfCharts = 0;

        for (Map.Entry<String, ComparativeValues> entry : this.interactionScores.entrySet()){
            String category = entry.getKey();
            Double score = Double.parseDouble(category);

            if (score >= minimum && score <= maximum){
                ComparativeValues values = entry.getValue();

                dataset.addValue(values.getNumberOfInteractionsA(), "Rule A", category);
                dataset.addValue(values.getNumberOfInteractionsB(), "Rule B", category);
                numberOfCategories ++;
            }

            if (numberOfCategories == 5){
                numberOfCharts ++;
                // create chart
                final JFreeChart chart = ChartFactory.createBarChart(
                        "Dual Axis Chart",        // chart title
                        "Mi score value",         // domain axis label
                        "Number of Interactions", // range axis label
                        dataset,                 // data
                        PlotOrientation.VERTICAL,
                        true,                     // include legend
                        true,                     // tooltips?
                        false                     // URL generator?  Not required...
                );

                // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
                chart.setBackgroundPaint(Color.white);
//        chart.getLegend().setAnchor(Legend.SOUTH);

                // get a reference to the plot for further customisation...
                final CategoryPlot plot = chart.getCategoryPlot();
                plot.setBackgroundPaint(new Color(0xEE, 0xEE, 0xFF));
                plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

                try {
                    ChartUtilities.saveChartAsPNG(new File(pngFileName + numberOfCharts + ".png"), chart, getWidth(), getHeight());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                dataset = new DefaultCategoryDataset();
                numberOfCategories = 0;
            }
        }

        if (numberOfCharts == 0){
            // create chart
            final JFreeChart chart = ChartFactory.createBarChart(
                    "Dual Axis Chart",        // chart title
                    "Mi score value",         // domain axis label
                    "Number of Interactions", // range axis label
                    dataset,                 // data
                    PlotOrientation.VERTICAL,
                    true,                     // include legend
                    true,                     // tooltips?
                    false                     // URL generator?  Not required...
            );

            // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
            chart.setBackgroundPaint(Color.white);
//        chart.getLegend().setAnchor(Legend.SOUTH);

            // get a reference to the plot for further customisation...
            final CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(new Color(0xEE, 0xEE, 0xFF));
            plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

            try {
                ChartUtilities.saveChartAsPNG(new File(pngFileName + numberOfCharts + ".png"), chart, getWidth(), getHeight());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]){
        IntactStackedBarChart scoreDistribution = new IntactStackedBarChart("/home/marine/Desktop/miCluster_score_results/new_results/general_results/exported/exported.txt", "/home/marine/Desktop/miCluster_score_results/new_results/general_results/exported/old_exported.txt");
        scoreDistribution.createChart("/home/marine/Desktop/miCluster_score_results/new_results/general_results/exported/comparison.png", 0.43, 1);
    }

}
