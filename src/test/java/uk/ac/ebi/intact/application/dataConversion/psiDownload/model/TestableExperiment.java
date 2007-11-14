// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.model;

import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Institution;

/**
 * Allow to creatre an Experiment to which we can set an AC.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class TestableExperiment extends Experiment {

//    public TestableExperiment( Institution owner, String shortLabel, BioSource source ) {
//        super( owner, shortLabel, source );
//    }

    public TestableExperiment() {
        
    }

    public TestableExperiment( String ac, Institution owner, String shortLabel, BioSource source ) {
        super( owner, shortLabel, source );
        this.ac = ac;
    }
}