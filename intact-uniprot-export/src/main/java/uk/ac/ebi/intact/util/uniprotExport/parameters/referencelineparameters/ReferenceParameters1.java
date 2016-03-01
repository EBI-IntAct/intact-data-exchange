package uk.ac.ebi.intact.util.uniprotExport.parameters.referencelineparameters;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ReferenceParameters1 implements ReferenceParameters {
    
    private String uniProtAc;
    private String institution;
    private String pmid;
    private String publicationAc;

    public ReferenceParameters1(String uniProtAc, String institution, String pmid, String publicationAc) {
        this.uniProtAc = uniProtAc;
        this.institution = institution;
        this.pmid = pmid;
        this.publicationAc = publicationAc;
    }

    @Override
    public String getUniProtAc() {
        return uniProtAc;
    }

    @Override
    public String getInstitution() {
        return institution;
    }

    @Override
    public String getPMID() {
        return pmid;
    }

    @Override
    public String getPublicationAc() {
        return publicationAc;
    }
}
