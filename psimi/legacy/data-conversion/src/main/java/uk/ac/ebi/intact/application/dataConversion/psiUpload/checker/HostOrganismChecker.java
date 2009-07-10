/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.CellTypeTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.HostOrganismTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.TissueTag;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.util.protein.BioSourceFactory;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class HostOrganismChecker extends AbstractOrganismChecker {

    public static BioSource getBioSource( final HostOrganismTag hostOrganism ) {
        final String taxid = hostOrganism.getTaxId();
        final CellTypeTag cellType = hostOrganism.getCellType();
        final TissueTag tissue = hostOrganism.getTissue();

        return getBioSource( taxid, cellType, tissue );
    }


    public static void check( final HostOrganismTag hostOrganism,
                              final BioSourceFactory bioSourceFactory ) {

        final String taxid = hostOrganism.getTaxId();
        final CellTypeTag cellType = hostOrganism.getCellType();
        final TissueTag tissue = hostOrganism.getTissue();
        check( taxid, cellType, tissue, bioSourceFactory );
    }
}
