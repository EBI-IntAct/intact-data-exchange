package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.Objects;

/**
 * Default implementation of SecondCCParametersVersion1
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/02/11</pre>
 */

public class SecondCCParametersVersion1Impl implements SecondCCParametersVersion1 {

    private String firstUniprotAc;
    private String secondUniprotAc;

    private String secondIntact;
    private String firstIntact;

    private String geneName;
    private String firstTaxId;
    private String secondTaxId;

    private int numberOfInteractionEvidences;

    public SecondCCParametersVersion1Impl(String firstUniprotAc, String firstIntactAc, String firstTaxId, String secondUniprotAc, String secondIntactAc, String secondTaxId, String geneName, int numberInteractions){

        this.firstUniprotAc = firstUniprotAc;
        this.secondUniprotAc = secondUniprotAc;
        this.firstIntact = firstIntactAc;
        this.secondIntact = secondIntactAc;

        this.firstTaxId = firstTaxId;
        this.secondTaxId = secondTaxId;
        this.geneName = geneName;

        this.numberOfInteractionEvidences = numberInteractions;

    }

    @Override
    public String getFirstIntactAc() {
        return this.firstIntact;
    }

    @Override
    public String getFirstUniprotAc() {
        return this.firstUniprotAc;
    }

    @Override
    public String getSecondUniprotAc() {
        return this.secondUniprotAc;
    }

    @Override
    public String getSecondIntactAc() {
        return this.secondIntact;
    }

    @Override
    public String getGeneName() {
        return this.geneName;
    }

    public void setGeneName(String name){
        this.geneName = name;
    }

    @Override
    public String getFirstTaxId() {
        return this.firstTaxId;
    }

    @Override
    public String getSecondTaxId() {
        return this.secondTaxId;
    }

    @Override
    public int getNumberOfInteractionEvidences() {
        return this.numberOfInteractionEvidences;
    }

    public void setNumberOfInteractionEvidences(int number) {
        this.numberOfInteractionEvidences = number;
    }

    public int compareTo(SecondCCParameters cc2) {

        // 1. Sort by interactant 1 (we don't need to consider case sensitivity since all our identifiers are upper cased).
        // a. canonical interactions first
        // b. isoform-specific interactions sorted alphanumerically,
        // c. PRO_xxx interactions sorted alphanumerically

        final String firstUniprotCc2 = cc2.getFirstUniprotAc();
        final String secondUniprotAcCc2 = cc2.getSecondUniprotAc();
        int compare;

        // We detect if it is a PRO_
        if (firstUniprotAc.contains(WriterUtils.CHAIN_PREFIX) && !firstUniprotCc2.contains(WriterUtils.CHAIN_PREFIX)) {
            // we put PRO_ at the end
            compare = 1;
        } else if (!firstUniprotAc.contains(WriterUtils.CHAIN_PREFIX) && firstUniprotCc2.contains(WriterUtils.CHAIN_PREFIX)) {
            compare = -1;
        } else {
            //Both are PRO or none are PRO
            // Because isoforms are longer than canonical the will be sorted properly with the string comparison
            // TODO: Note check for isoforms than are part of other entries, where should be sorted because
            // alphanumerically could end up before the canonical entry

            //if they protein comes from a transcript with different master acs we shouldn't consider the same entry
            compare = this.firstUniprotAc.compareTo(firstUniprotCc2);

            if (compare == 0) { //Same (either canonical, isoform, or PRO)
                // 2. sort by Xeno (different species)
                // for a given Molecule 'A', list first all non-Xeno interactions, then the Xeno interactions.
                final String firstTaxIdCc2 = cc2.getFirstTaxId();
                final String secondTaxIdCc2 = cc2.getSecondTaxId();

                final boolean isCc1NonXeno = firstTaxId.equals(secondTaxId);
                final boolean isCc2NonXeno = firstTaxIdCc2.equals(secondTaxIdCc2);

                if (isCc1NonXeno && !isCc2NonXeno) {
                    compare = -1;
                } else if (!isCc1NonXeno && isCc2NonXeno) {
                    compare = 1;
                } else {
                    //Both are Xeno or Non-Xeno compare gene names
                    // 3. sort by interactant 2 (Molecule 'B')
                    // - list first those with a gene name:
                    // a. sort by gene name case insensitively
                    // b. sort by ID* if gene name identical
                    // - then list those without gene name:
                    //
                    // *sort by ID: if the "ID" is "PRO_xxx [AC]", sort first by AC, then by PRO_xxx  (to group chains from the same AC)

                    final String geneNameCc2 = cc2.getGeneName();

                    if (geneName != null && geneNameCc2 != null) {

                        String lowerCaseGene1 = geneName.toLowerCase();
                        String lowerCaseGene2 = geneNameCc2.toLowerCase();

                        compare = lowerCaseGene1.compareTo(lowerCaseGene2);

                        if (compare == 0) {
                            compare = geneName.compareTo(geneNameCc2);
                        }
                    }
                    //At this point either both genes are null so compare is 0 or the comparison has happened
                    if (compare == 0) {
                        // gene names are the same or there are no gene names, then compare the uniprotID
                        String secondUniprotCc1 = this.secondUniprotAc;
                        String secondUniprotCc2 = secondUniprotAcCc2;

                        if (secondUniprotCc1 != null && secondUniprotCc2 != null) {
                            //If PRO_ we extract the Uniprot AC for sorting
                            if (secondUniprotCc1.contains(WriterUtils.CHAIN_PREFIX)) {
                                secondUniprotCc1 = secondUniprotCc1.substring(0, secondUniprotCc1.indexOf(WriterUtils.CHAIN_PREFIX));
                            }
                            if (secondUniprotCc2.contains(WriterUtils.CHAIN_PREFIX)) {
                                secondUniprotCc2 = secondUniprotCc2.substring(0, secondUniprotCc2.indexOf(WriterUtils.CHAIN_PREFIX));
                            }
                            compare = secondUniprotCc1.compareTo(secondUniprotCc2);
                            if(compare == 0) {
                                //We need this case for PRO_ comming from the same Uniprot entry
                                compare = this.secondUniprotAc.compareTo(secondUniprotAcCc2);
                            }
                        }
                    }
                }
            }
        }
        return compare;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecondCCParametersVersion1Impl that = (SecondCCParametersVersion1Impl) o;
        return firstUniprotAc.equals(that.firstUniprotAc) &&
                secondUniprotAc.equals(that.secondUniprotAc) &&
                secondIntact.equals(that.secondIntact) &&
                firstIntact.equals(that.firstIntact) &&
                Objects.equals(geneName, that.geneName) &&
                firstTaxId.equals(that.firstTaxId) &&
                secondTaxId.equals(that.secondTaxId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstUniprotAc, secondUniprotAc, secondIntact, firstIntact, geneName, firstTaxId, secondTaxId);
    }
}
