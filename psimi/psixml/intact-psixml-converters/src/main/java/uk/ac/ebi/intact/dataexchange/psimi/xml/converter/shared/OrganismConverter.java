package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import psidev.psi.mi.xml.model.CellType;
import psidev.psi.mi.xml.model.Organism;
import psidev.psi.mi.xml.model.Tissue;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.annotation.PsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IntactConverterUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiConverterUtils;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.CvCellType;
import uk.ac.ebi.intact.model.CvTissue;
import uk.ac.ebi.intact.model.Institution;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@PsiConverter(intactObjectType = BioSource.class, psiObjectType = Organism.class)
public class OrganismConverter extends AbstractIntactPsiConverter<BioSource, Organism> {

    private CvObjectConverter<CvCellType,CellType> cellTypeConverter;
    private CvObjectConverter<CvTissue, Tissue> tissueConverter;

    public OrganismConverter(Institution institution) {
        super(institution);
        cellTypeConverter = new CvObjectConverter<CvCellType,CellType>(institution, CvCellType.class, CellType.class);
        tissueConverter = new CvObjectConverter<CvTissue,Tissue>(institution, CvTissue.class, Tissue.class);
    }

    public BioSource psiToIntact(Organism psiObject) {
        if (psiObject == null) return null;

        String shortLabel = IntactConverterUtils.getShortLabelFromNames(psiObject.getNames());
        int taxId = psiObject.getNcbiTaxId();

        BioSource bioSource = new BioSource(getInstitution(), shortLabel, String.valueOf(taxId));
        psiStartConversion(psiObject);

        IntactConverterUtils.populateNames(psiObject.getNames(), bioSource);

        // cell type
        final CellType cellType = psiObject.getCellType();

        if (cellType != null) {
            cellTypeConverter.setInstitution(getInstitution());
            CvCellType intactCellType = cellTypeConverter.psiToIntact(cellType);

            bioSource.setCvCellType(intactCellType);
        }

        // tissue
        final Tissue tissue = psiObject.getTissue();

        if (tissue != null) {
            tissueConverter.setInstitution(getInstitution());
            CvTissue intactTissue = tissueConverter.psiToIntact(tissue);
            bioSource.setCvTissue(intactTissue);
        }

        psiEndConversion(psiObject);
        return bioSource;
    }

    public Organism intactToPsi(BioSource intactObject) {
        Organism organism = new Organism();
        intactStartConversation(intactObject);
        PsiConverterUtils.populate(intactObject, organism, this);

        organism.setNcbiTaxId(Integer.valueOf(intactObject.getTaxId()));

        // cell type
        final CvCellType intactCellType = intactObject.getCvCellType();

        if (intactCellType != null) {
             cellTypeConverter.setInstitution(getInstitution());

            CellType psiCellType = cellTypeConverter.intactToPsi(intactCellType);
            organism.setCellType(psiCellType);
        }

        // tissue
        final CvTissue intactTissue = intactObject.getCvTissue();

        if (intactTissue != null) {
             tissueConverter.setInstitution(getInstitution());
            Tissue psiTissue = tissueConverter.intactToPsi(intactTissue);
            organism.setTissue(psiTissue);
        }

        intactEndConversion(intactObject);
        return organism;
    }
}
