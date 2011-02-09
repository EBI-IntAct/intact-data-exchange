package uk.ac.ebi.intact.util.uniprotExport.miscore.extension;

import uk.ac.ebi.enfin.mi.score.distribution.MiscoreDistribution;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is for displaying in a diagram the score distribution of interactions from a file
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24-Sep-2010</pre>
 */

public class IntActFileMiScoreDistribution implements MiscoreDistribution {
    private double[] scores = null;
    private List scoreList = new ArrayList<String>();
    private String fileName;

    private static final String SCORE_SEPARATOR = ":";

    public IntActFileMiScoreDistribution(String fileName) {
        this.fileName = fileName;
        try {
            FileReader fstream = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fstream);
            String line;
            while((line = br.readLine()) != null) {

                if (line.contains(SCORE_SEPARATOR)){
                    String[] values = line.split(SCORE_SEPARATOR);

                    if (values.length == 2){
                        scoreList.add(values[1]);
                    }
                    else {
                        System.err.println("the line " + line + " cannot be loaded because is not of the form 'id-interactorA-interactorB:score'");
                    }
                }
                else {
                    System.err.println("the line " + line + " cannot be loaded because is not of the form 'id-interactorA-interactorB:score'");
                }
            }
            scores = new double[scoreList.size()];
            int i = 0;
            for(Object score:scoreList){
                scores[i] = Double.parseDouble(score.toString());
                i++;
            }
            fstream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double[] getScores() {
        return scores;
    }

    public void createChart() {
        IntActMiScoreHistogram miH = new IntActMiScoreHistogram();
        miH.setTitle("Score distribution for " + scores.length + " clustered interactions from " + fileName);
        miH.setNumberOfBars(100);
        miH.setValues(getScores());
        miH.setMaximumScore(1);
        miH.setMinimumScore(0);
        miH.createChart();
    }

    /**
     * Create a chart with the results of mi cluster score
     * @param diagrammName
     */
    public void createChart(String diagrammName) {
        IntActMiScoreHistogram miH = new IntActMiScoreHistogram();
        miH.setTitle("Score distribution for " + scores.length + " clustered interactions from " + fileName);
        miH.setNumberOfBars(100);
        miH.setMaximumScore(1);
        miH.setMinimumScore(0);
        miH.setValues(getScores());
        miH.createChart(diagrammName);
    }

    /**
     * Create a chart with the results of mi cluster score
     * @param diagrammName
     */
    public void createChart(String diagrammName, String title, int numberOfBars, double min, double max) {
        IntActMiScoreHistogram miH = new IntActMiScoreHistogram();
        miH.setTitle(title);
        miH.setNumberOfBars(numberOfBars);
        miH.setMaxScore(max);
        miH.setMinScore(min);
        miH.setValues(getScores());
        miH.createChart(diagrammName);
    }

    public static void main(String args[]){
        IntActFileMiScoreDistribution scoreDistribution = new IntActFileMiScoreDistribution("/home/marine/Desktop/miCluster_score_results/new_results/general_results/exported/old_excluded.txt");
        scoreDistribution.createChart("/home/marine/Desktop/miCluster_score_results/new_results/general_results/exported/old_excluded.png", "Distribution of the mi score of excluded interactions using rules on detection method", 100, 0, 1);
    }

}
