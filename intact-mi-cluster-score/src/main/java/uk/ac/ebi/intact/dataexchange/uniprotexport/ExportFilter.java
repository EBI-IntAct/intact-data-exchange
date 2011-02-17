package uk.ac.ebi.intact.dataexchange.uniprotexport;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.xml.converter.ConverterException;

import java.io.*;
import java.util.*;

/**
 * This script allows to filter in a file B the elements which are in the file A (line starting)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14/02/11</pre>
 */

public class ExportFilter {

    public static void main( String[] args ) throws IOException, ConverterException {

        // Six possible arguments
        if( args.length != 3 ) {
            System.err.println( "Usage: ExportFilter <fileA> <fileB> <results>" );
            System.err.println( "Usage: <fileA> is the file containing the results we want to filter (interactor A and interactor B separated by tab)" );
            System.err.println( "Usage: <fileB> is the mitab file to filter with the results of the file A" );
            System.err.println( "Usage: <fileB> is the file where to write the results" );

            System.exit( 1 );
        }
        String fileA = args[0];
        String fileB = args[1];
        File results = new File(args[2]);

        // read the elements to filter and put them in a collection
        Map<CrossReference, List<CrossReference>> elementsToFilter = new HashMap<CrossReference, List<CrossReference>>();

        BufferedReader readerA = new BufferedReader(new FileReader(fileA));

        String lineA = readerA.readLine();

        while (lineA != null){

            if (lineA.contains("\t")){
                String [] interactors = lineA.split("\t");

                CrossReference refA = null;
                CrossReference refB = null;

                if (interactors[0].contains(":")){
                    String [] refAIdentifiers = interactors[0].split(":");
                    refA = new CrossReferenceImpl(refAIdentifiers[0], refAIdentifiers[1]);
                }
                if (interactors[1].contains(":")){
                    String [] refBIdentifiers = interactors[1].split(":");
                    refB = new CrossReferenceImpl(refBIdentifiers[0], refBIdentifiers[1]);
                }

                if (refA != null && refB != null){
                    if (elementsToFilter.containsKey(refA)){
                        List<CrossReference> refs = elementsToFilter.get(refA);
                        refs.add(refB);
                    }
                    else{
                        List<CrossReference> refs = new ArrayList<CrossReference>();
                        refs.add(refB);
                        elementsToFilter.put(refA, refs);
                    }
                }
                else {
                    System.err.print("Skip the line " + lineA + "\n");
                }
            }
            else {
                System.err.print("The line doesn't contain any tab and is ignored \n");
            }
            lineA = readerA.readLine();
        }

        readerA.close();

        // filter the mitab file
        PsimiTabReader mitabReader = new PsimiTabReader(false);
        Iterator<BinaryInteraction> iterator = mitabReader.iterate(new FileInputStream(fileB));

        PsimiTabWriter mitabWriter = new PsimiTabWriter(false);

        while(iterator.hasNext()){
            BinaryInteraction<Interactor> binaryInteraction = iterator.next();

            Interactor interactorA = binaryInteraction.getInteractorA();
            Interactor interactorB = binaryInteraction.getInteractorB();

            List<CrossReference> secondInteractors = new ArrayList<CrossReference>();

            Interactor firstInteractor = extractFirstInteractorsFor(elementsToFilter, interactorA, interactorB, secondInteractors);

            if (!secondInteractors.isEmpty() && firstInteractor != null){
                boolean hasFoundInteractorB = false;

                if (firstInteractor.equals(interactorA)){
                    for (CrossReference ref : interactorB.getIdentifiers()){
                        if (secondInteractors.contains(ref)){
                            hasFoundInteractorB = true;
                        }
                    }

                    if (!hasFoundInteractorB){
                        for (CrossReference ref : interactorB.getAlternativeIdentifiers()){
                            if (elementsToFilter.containsKey(ref)){
                                hasFoundInteractorB = true;
                            }
                        }
                    }
                }
                else{
                    for (CrossReference ref : interactorA.getIdentifiers()){
                        if (secondInteractors.contains(ref)){
                            hasFoundInteractorB = true;
                        }
                    }

                    if (!hasFoundInteractorB){
                        for (CrossReference ref : interactorA.getAlternativeIdentifiers()){
                            if (secondInteractors.contains(ref)){
                                hasFoundInteractorB = true;
                            }
                        }
                    }
                }

                if (hasFoundInteractorB){
                    mitabWriter.writeOrAppend(binaryInteraction, results, false);
                }
            }
        }
    }

    private static Interactor extractFirstInteractorsFor(Map<CrossReference, List<CrossReference>> elementsToFilter, Interactor interactorA, Interactor interactorB, List<CrossReference> secondInteractors) {
        Interactor firstInteractor = null;

        for (CrossReference ref : interactorA.getIdentifiers()){
            if (elementsToFilter.containsKey(ref)){
                secondInteractors.addAll(elementsToFilter.get(ref));
                firstInteractor = interactorA;
            }
        }

        if (firstInteractor == null){
            for (CrossReference ref : interactorA.getAlternativeIdentifiers()){
                if (elementsToFilter.containsKey(ref)){
                    secondInteractors.addAll(elementsToFilter.get(ref));
                    firstInteractor = interactorA;
                }
            }

            if (firstInteractor == null){
                for (CrossReference ref : interactorB.getIdentifiers()){
                    if (elementsToFilter.containsKey(ref)){
                        secondInteractors.addAll(elementsToFilter.get(ref));
                        firstInteractor = interactorB;
                    }
                }

                if (firstInteractor == null){
                    for (CrossReference ref : interactorB.getAlternativeIdentifiers()){
                        if (elementsToFilter.containsKey(ref)){
                            secondInteractors.addAll(elementsToFilter.get(ref));
                            firstInteractor = interactorB;
                        }
                    }
                }
            }
        }
        return firstInteractor;
    }
}
