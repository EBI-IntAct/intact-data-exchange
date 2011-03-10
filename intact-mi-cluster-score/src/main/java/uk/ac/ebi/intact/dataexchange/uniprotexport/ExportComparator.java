package uk.ac.ebi.intact.dataexchange.uniprotexport;

import org.apache.commons.collections.CollectionUtils;
import psidev.psi.mi.xml.converter.ConverterException;

import java.io.*;
import java.util.*;

/**
 * Compares two mitab files based on the interactor identifiers. (only compare the binary interactions present using interactor A identifier and interactor B identifier, independently of the order)
 *
 * The possible arguments are :
 * - fileA : first mitab file
 * - fileB : second mitab file
 * - file1 : the mitab lines which correspond to the binary interactions present in the file A but not in the file B
 * - file2 : the mitab lines which correspond to the binary interactions present in both files A and B
 * - file3 : the mitab lines which correspond to the binary interactions present in the file B but not in the file A
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/02/11</pre>
 */

public class ExportComparator {

    public static void main( String[] args ) throws IOException, ConverterException {

        // Six possible arguments
        if( args.length != 5 ) {
            System.err.println( "Usage: ExportFilter <fileA> <fileB> <file1> <file2> <file3>" );
            System.err.println( "Usage: <fileA> is the file containing the results of a first export (list of interactor A and interactor B separated by tab)" );
            System.err.println( "Usage: <fileB> is the file containing the results of a second export (list of interactor A and interactor B separated by tab)" );
            System.err.println( "Usage: <file1> is the file where to write the binary interactions exported in the file A but not in the file B" );
            System.err.println( "Usage: <file2> is the file where to write the binary interactions exported in the file A and in the file B" );
            System.err.println( "Usage: <file3> is the file where to write the binary interactions exported in the file B but not in the file A" );

            System.exit( 1 );
        }
        String fileA = args[0];
        String fileB = args[1];
        File fileANotB = new File(args[2]);
        File fileAAndB = new File(args[3]);
        File fileBNotA = new File(args[4]);

        // read the elements in the fileA
        Set<BinaryInteraction> binaryinteractionA = new HashSet<BinaryInteraction>();

        extractBinaryInteractionsFromFile(fileA, binaryinteractionA);

        // read the elements in the fileB
        Set<BinaryInteraction> binaryinteractionB = new HashSet<BinaryInteraction>();

        extractBinaryInteractionsFromFile(fileB, binaryinteractionB);

        // compare what is in file A but not in the file B
        Set<BinaryInteraction> binaryInteractionsANotB = new HashSet<BinaryInteraction>(CollectionUtils.subtract(binaryinteractionA, binaryinteractionB));
        System.out.print(binaryInteractionsANotB.size() + " binary interactions are in the file " + fileA + " but not in the file " + fileB + "\n");

        Set<BinaryInteraction> binaryInteractionsAAndB = new HashSet<BinaryInteraction>(CollectionUtils.intersection(binaryinteractionA, binaryinteractionB));
        System.out.print(binaryInteractionsAAndB.size() + " binary interactions are in the file " + fileA + " and in the file " + fileB + "\n");

        Set<BinaryInteraction> binaryInteractionsBNotA = new HashSet<BinaryInteraction>(CollectionUtils.subtract(binaryinteractionB, binaryinteractionA));
        System.out.print(binaryInteractionsBNotA.size() + " binary interactions are in the file " + fileB + " but not in the file " + fileA + "\n");

        // write the results
        writeComparisonResultsFor(fileANotB, binaryInteractionsANotB);
        writeComparisonResultsFor(fileAAndB, binaryInteractionsAAndB);
        writeComparisonResultsFor(fileBNotA, binaryInteractionsBNotA);
    }

    private static void writeComparisonResultsFor(File fileANotB, Set<BinaryInteraction> binaryInteractionsANotB) throws IOException {
        FileWriter writer1 = new FileWriter(fileANotB);

        for (BinaryInteraction interaction : binaryInteractionsANotB){
            writer1.write(interaction.toString());
            writer1.write("\n");
        }

        writer1.close();
    }

    private static void extractBinaryInteractionsFromFile(String fileA, Set<BinaryInteraction> binaryinteractionA) throws IOException {
        BufferedReader readerA = new BufferedReader(new FileReader(fileA));

        String lineA = readerA.readLine();

        while (lineA != null){

            if (lineA.contains("\t")){
                String [] interactors = lineA.split("\t");

                BinaryInteraction binary = new BinaryInteraction(interactors[0], interactors[1]);
                binaryinteractionA.add(binary);
            }
            else {
                System.err.print("The line "+lineA+ "doesn't contain any tab and is ignored \n");
            }
            lineA = readerA.readLine();
        }

        readerA.close();

        System.out.print(binaryinteractionA.size() + " loaded binary interactions from the file " + fileA + "\n");
    }
}
