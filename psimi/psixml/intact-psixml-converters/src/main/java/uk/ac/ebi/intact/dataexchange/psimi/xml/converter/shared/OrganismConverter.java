package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.xml.model.CellType;
import psidev.psi.mi.xml.model.Organism;
import psidev.psi.mi.xml.model.Tissue;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.annotation.PsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.*;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@PsiConverter(intactObjectType = BioSource.class, psiObjectType = Organism.class)
public class OrganismConverter extends AbstractIntactPsiConverter<BioSource, Organism> {
    private static final Log log = LogFactory.getLog(OrganismConverter.class);

    private CvObjectConverter<CvCellType,CellType> cellTypeConverter;
    private CvObjectConverter<CvTissue, Tissue> tissueConverter;
    protected AliasConverter aliasConverter;

    public OrganismConverter(Institution institution) {
        super(institution);
        cellTypeConverter = new CvObjectConverter<CvCellType,CellType>(institution, CvCellType.class, CellType.class);
        tissueConverter = new CvObjectConverter<CvTissue,Tissue>(institution, CvTissue.class, Tissue.class);
        this.aliasConverter = new AliasConverter(institution, BioSourceAlias.class);
    }

    public BioSource psiToIntact(Organism psiObject) {
        if (psiObject == null) return null;

        psiStartConversion(psiObject);

        int taxId = psiObject.getNcbiTaxId();

        BioSource bioSource = new BioSource();
        bioSource.setTaxId(String.valueOf(taxId));
        bioSource.setOwner(getInstitution());

        IntactConverterUtils.populateNames(psiObject.getNames(), bioSource, aliasConverter);

        // cell type
        final CellType cellType = psiObject.getCellType();

        if (cellType != null) {
            CvCellType intactCellType = cellTypeConverter.psiToIntact(cellType);

            bioSource.setCvCellType(intactCellType);
        }

        // tissue
        final Tissue tissue = psiObject.getTissue();

        if (tissue != null) {
            CvTissue intactTissue = tissueConverter.psiToIntact(tissue);
            bioSource.setCvTissue(intactTissue);
        }

        if (psiObject.getCompartment() != null){
            log.warn("Organism having a compartment : "+psiObject.getNcbiTaxId()+". Compartment is not converted in IntAct and is ignored.");
        }

        psiEndConversion(psiObject);
        return bioSource;
    }

    public Organism intactToPsi(BioSource intactObject) {
        intactStartConversation(intactObject);
        Organism organism = new Organism();

        // populates names
        PsiConverterUtils.populate(intactObject, organism, aliasConverter, null, null, isCheckInitializedCollections());

        // taxId
        if (intactObject.getTaxId() != null){
            organism.setNcbiTaxId(Integer.valueOf(intactObject.getTaxId()));
        }
        else {
            log.error("BioSource without taxId : " + intactObject.getShortLabel());
        }

        // cell type
        final CvCellType intactCellType = intactObject.getCvCellType();

        if (intactCellType != null) {

            CellType psiCellType = cellTypeConverter.intactToPsi(intactCellType);
            organism.setCellType(psiCellType);
        }

        // tissue
        final CvTissue intactTissue = intactObject.getCvTissue();

        if (intactTissue != null) {
            Tissue psiTissue = tissueConverter.intactToPsi(intactTissue);
            organism.setTissue(psiTissue);
        }

        intactEndConversion(intactObject);
        return organism;
    }

    @Override
    public void setInstitution(Institution institution)
    {
        super.setInstitution(institution);
        cellTypeConverter.setInstitution(institution, getInstitutionPrimaryId());
        tissueConverter.setInstitution(institution, getInstitutionPrimaryId());
        this.aliasConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    @Override
    public void setInstitution(Institution institution, String institId){
        super.setInstitution(institution, institId);
        cellTypeConverter.setInstitution(institution, getInstitutionPrimaryId());
        tissueConverter.setInstitution(institution, getInstitutionPrimaryId());
        this.aliasConverter.setInstitution(institution, getInstitutionPrimaryId());
    }

    @Override
    public void setCheckInitializedCollections(boolean check){
        super.setCheckInitializedCollections(check);
        this.cellTypeConverter.setCheckInitializedCollections(check);
        this.tissueConverter.setCheckInitializedCollections(check);
        this.aliasConverter.setCheckInitializedCollections(check);
    }
}
