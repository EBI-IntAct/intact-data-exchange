package uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParametersVersion1;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.SortedSet;

/**
 * Default Writer of the CC line format (version 1)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/02/11</pre>
 */

public class CCLineWriterVersion1 implements CCLineWriter<CCParameters<SecondCCParametersVersion1>> {

    /**
     * The writer
     */
    private OutputStreamWriter writer;

    /**
     * Create a new CCLine writer with a fileName
     * @param outputStream : the outputStreamWriter
     * @throws IOException
     */
    public CCLineWriterVersion1(OutputStreamWriter outputStream) throws IOException {
        if (outputStream == null){
            throw new IllegalArgumentException("You must give a non null OutputStream writer");
        }
        writer = outputStream;
    }

    @Override
    public void writeCCLine(CCParameters<SecondCCParametersVersion1> parameters) throws IOException {

        // if parameter not null, write it
        if (parameters != null){

            // write the title
            writeCCLineTitle(parameters.getMasterUniprotAc());

            // write the content
            writeCCLineParameters(parameters);

            writer.flush();
        }
    }

    /**
     * Write the content of the CC line
     * @param parameters : the parameters
     */
    public void writeCCLineParameters(CCParameters<SecondCCParametersVersion1> parameters) throws IOException {

        String entryUniprotAc = parameters.getMasterUniprotAc();
        String entryTaxId = parameters.getTaxId();

        SortedSet<SecondCCParametersVersion1> secondParameters = parameters.getSecondCCParameters();

        //TODO Fix PRO-CHAINS
        for (SecondCCParametersVersion1 secondInteractor : secondParameters){
            writer.write("CC       ");

            String firstUniprotAc = secondInteractor.getFirstUniprotAc();
            String secondUniprotAc = secondInteractor.getSecondUniprotAc();


            // Examples
            // P12345-5 contains master P12345 so no need of repeating the genes
            // Self interactions
            // Interactions between isoforms of same entry
            // Interactions between chains of same entry

            // In case of self interactions of isoforms that are found in an external entry it gets cover
            // in the second part of the OR e.g Q9HDB5 contains Q9Y4C0-1, Q9Y4C0-3, Q9Y4C0-4 as isoforms

            if (secondUniprotAc.contains(entryUniprotAc) || secondUniprotAc.equals(firstUniprotAc)) {

                // We extract only the PRO part of the identifier
                if (firstUniprotAc.contains(WriterUtils.CHAIN_PREFIX)){
                    firstUniprotAc = firstUniprotAc.substring(firstUniprotAc.indexOf(WriterUtils.CHAIN_PREFIX)+1);
                }
                if (secondUniprotAc.contains(WriterUtils.CHAIN_PREFIX)){
                    // secondUniprotAc = secondUniprotAc.substring(secondUniprotAc.indexOf(WriterUtils.CHAIN_PREFIX)+1);
                    // For now Uniprot prefers to have always the master protein when there is an interaction between chains
                    // of same entry. If the change the specification, use previous line
                    String secondMasterUniprotAc = secondUniprotAc.substring(0, secondUniprotAc.indexOf(WriterUtils.CHAIN_PREFIX));
                    secondUniprotAc = secondUniprotAc.substring(secondUniprotAc.indexOf(WriterUtils.CHAIN_PREFIX)+1) + " [" + secondMasterUniprotAc + "]";
                }
                writer.write(firstUniprotAc);
                writer.write(';');
                writer.write(' ');
                writer.write(secondUniprotAc);

            // The proteins are from different entries
            } else {
                // We extract only the PRO part of the identifier
                if (firstUniprotAc.contains(WriterUtils.CHAIN_PREFIX)){
                    firstUniprotAc = firstUniprotAc.substring(firstUniprotAc.indexOf(WriterUtils.CHAIN_PREFIX)+1);
                }
                // We extract only the PRO part of the identifier and the master protein for the second interactor
                if (secondUniprotAc.contains(WriterUtils.CHAIN_PREFIX)){
                    String secondMasterUniprotAc = secondUniprotAc.substring(0, secondUniprotAc.indexOf(WriterUtils.CHAIN_PREFIX));
                    secondUniprotAc = secondUniprotAc.substring(secondUniprotAc.indexOf(WriterUtils.CHAIN_PREFIX)+1) + " [" + secondMasterUniprotAc + "]";
                }
                writer.write(firstUniprotAc);
                writer.write(';');
                writer.write(' ');
                writer.write(secondUniprotAc);
                if(secondInteractor.getGeneName()!= null && !secondInteractor.getGeneName().equals("-")) {
                    writer.write(':');
                    writer.write(' ');
                    writer.write(secondInteractor.getGeneName());
                }
            }

            // generated warning message if the two protein are from different organism
            if (secondInteractor.getSecondTaxId() != null && !entryTaxId.equals(secondInteractor.getSecondTaxId())) {
                writer.write(';');
                writer.write(' ');
                writer.write("Xeno");
            }

            writer.write(';');
            writer.write(' ');
            writer.write("NbExp=");
            writer.write(Integer.toString(secondInteractor.getNumberOfInteractionEvidences()));
            writer.write(';');
            writer.write(' ');
            writer.write("IntAct=");
            writer.write(secondInteractor.getFirstIntactAc());
            writer.write(',');
            writer.write(' ');
            writer.write(secondInteractor.getSecondIntactAc());
            writer.write(';');

            writer.write(WriterUtils.NEW_LINE);
        }
        writer.write("//");
        writer.write(WriterUtils.NEW_LINE);
        writer.flush();
    }

    @Override
    public void writeCCLines(List<CCParameters<SecondCCParametersVersion1>> CCLines) throws IOException {
        for (CCParameters<SecondCCParametersVersion1> parameters : CCLines){
             writeCCLine(parameters);
        }
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }

    /**
     * Write the CC line title
     * @param uniprot1
     */
    private void writeCCLineTitle(String uniprot1) throws IOException {
        writer.write("AC   ");
        if (uniprot1.contains("-")){
            String uniprotParent = uniprot1.substring(0, uniprot1.indexOf("-"));
            writer.write(uniprotParent);
        }
        else{
            writer.write(uniprot1);
        }
        writer.write(WriterUtils.NEW_LINE);
        writer.write("CC   -!- INTERACTION:");
        writer.write(WriterUtils.NEW_LINE);
    }
}
