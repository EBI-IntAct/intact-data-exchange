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

        final String firstUniprotAcCc2 = cc2.getFirstUniprotAc();
        final String secondUniprotAcCc2 = cc2.getSecondUniprotAc();
        int compare = 0;

        // We detect if it is a PRO_
        if (firstUniprotAc.contains(WriterUtils.CHAIN_PREFIX) && !firstUniprotAcCc2.contains(WriterUtils.CHAIN_PREFIX)) {
            // we put PRO_ at the end
            compare = 1;
        } else if (!firstUniprotAc.contains(WriterUtils.CHAIN_PREFIX) && firstUniprotAcCc2.contains(WriterUtils.CHAIN_PREFIX)) {
            compare = -1;
        } else {
            // Both are PRO or none are PRO
            // Because isoforms are longer than canonical the will be sorted properly with the string comparison

            //if they protein comes from a transcript with different master acs we shouldn't consider the same entry
            //This sorts interactant 1
            compare = compareWithNumbers(firstUniprotAc, firstUniprotAcCc2);

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

                    String geneNameCc1 = geneName;
                    String geneNameCc2 = cc2.getGeneName();

                    if (geneNameCc1 != null) {
                        geneNameCc1 = geneName.toLowerCase();
                    }
                    if (geneNameCc2 != null) {
                        geneNameCc2 = geneNameCc2.toLowerCase();
                    }

                    if (geneNameCc1 == null && geneNameCc2 != null) {
                        compare = 1;
                    } else if (geneNameCc1 != null && geneNameCc2 == null) {
                        compare = -1;
                    } else if (geneNameCc1 != null && geneNameCc2 != null) {
                        compare = geneNameCc1.compareTo(geneNameCc2);
                    } else { //both genes are null
                        compare = 0;
                    }

                    //At this point either both genes are null so compare is 0 or the comparison has happened
                    if (compare == 0) {
                        // gene names are the same or there are no gene names, then compare the uniprotID
                        String secondUniprotCanonicalCc1 = this.secondUniprotAc;
                        String secondUniprotCanonicalCc2 = secondUniprotAcCc2;


                            if (secondUniprotCanonicalCc1 != null && secondUniprotCanonicalCc2 != null) {
                                //If PRO_ we extract the Uniprot AC for sorting
                                if (secondUniprotCanonicalCc1.contains(WriterUtils.CHAIN_PREFIX)) {
                                    secondUniprotCanonicalCc1 = secondUniprotCanonicalCc1.substring(0, secondUniprotCanonicalCc1.indexOf(WriterUtils.CHAIN_PREFIX));
                                } // If isoform we extract the Uniprot AC
                                else if (secondUniprotCanonicalCc1.contains("-")) {
                                    secondUniprotCanonicalCc1 = secondUniprotCanonicalCc1.substring(0, secondUniprotCanonicalCc1.indexOf("-"));
                                }

                                if (secondUniprotCanonicalCc2.contains(WriterUtils.CHAIN_PREFIX)) {
                                    secondUniprotCanonicalCc2 = secondUniprotCanonicalCc2.substring(0, secondUniprotCanonicalCc2.indexOf(WriterUtils.CHAIN_PREFIX));
                                }
                                else if (secondUniprotCanonicalCc2.contains("-")) {
                                    secondUniprotCanonicalCc2 = secondUniprotCanonicalCc2.substring(0, secondUniprotCanonicalCc2.indexOf("-"));
                                }

                                //Compare canonicals
                                compare = secondUniprotCanonicalCc1.compareTo(secondUniprotCanonicalCc2);

                                if (compare == 0){ //From the same entry.
                                    if (this.secondUniprotAc.contains(WriterUtils.CHAIN_PREFIX) && !secondUniprotAcCc2.contains(WriterUtils.CHAIN_PREFIX)) {
                                        // we put PRO_ at the end
                                        compare = 1;
                                    } else if (!this.secondUniprotAc.contains(WriterUtils.CHAIN_PREFIX) && secondUniprotAcCc2.contains(WriterUtils.CHAIN_PREFIX)) {
                                        compare = -1;
                                    } else {
                                        // Both are PRO or none are PRO
                                        // Because isoforms are longer than canonical the will be sorted properly with the string comparison

                                        //if they protein comes from a transcript with different master acs we shouldn't consider the same entry
                                        //This sorts interactant 1
                                        compare = compareWithNumbers(this.secondUniprotAc, secondUniprotAcCc2);
                                    }
                                    if (compare == 0) {
                                        //We need this case for PRO_ coming from the same Uniprot entry
                                        compare = this.secondUniprotAc.compareTo(secondUniprotAcCc2);
                                    }
                                }
                            } else {
                                //TODO Log this case
                            }
                    }

                }
            }
        }
        return compare;
    }

    private int compareWithNumbers(String o1, String o2) {

        String o1StringPart = o1;
        String o2StringPart = o2;

        if (!o1.contains(WriterUtils.CHAIN_PREFIX) && !o2.contains(WriterUtils.CHAIN_PREFIX)) {
            if (o1.contains("-")) {
                o1StringPart = o1.substring(0, o1.indexOf("-"));
            }

            if (o2.contains("-")) {
                o2StringPart = o2.substring(0, o2.indexOf("-"));
            }

            if (o1StringPart.equalsIgnoreCase(o2StringPart)) {
                return extractInt(o1) - extractInt(o2);
            }
        }
        return o1.compareTo(o2);
    }

    private int extractInt(String s) {

        String num = "";

        if(s.contains("-")) {
            num = s.substring(s.indexOf("-") + 1);
        }

        // return 0 if no digits found
        return num.isEmpty() ? 0 : Integer.parseInt(num);
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
