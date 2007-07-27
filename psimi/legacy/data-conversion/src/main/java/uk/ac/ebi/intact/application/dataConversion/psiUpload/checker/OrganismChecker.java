/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.CellTypeTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.OrganismTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.TissueTag;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.util.protein.BioSourceFactory;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class OrganismChecker extends AbstractOrganismChecker {

    public static BioSource getBioSource( final OrganismTag organism ) {
        if ( organism == null ) {
            return null;
        }
        final String taxid = organism.getTaxId();
        final CellTypeTag cellType = organism.getCellType();
        final TissueTag tissue = organism.getTissue();

        return getBioSource( taxid, cellType, tissue );
    }

    public static void check( final OrganismTag organism,
                              final BioSourceFactory bioSourceFactory ) {

        final String taxid = organism.getTaxId();
        final CellTypeTag cellType = organism.getCellType();
        final TissueTag tissue = organism.getTissue();
        check( taxid, cellType, tissue, bioSourceFactory );
    }
}
