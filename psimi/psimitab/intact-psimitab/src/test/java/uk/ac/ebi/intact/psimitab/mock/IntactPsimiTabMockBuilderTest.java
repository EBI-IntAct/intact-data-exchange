/*
 * Copyright (c) 2008 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
 
package uk.ac.ebi.intact.psimitab.mock;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import junit.framework.Assert;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;

/**
 * IntactPsimiTabMockBuilder Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class IntactPsimiTabMockBuilderTest {

    private IntactPsimiTabMockBuilder mockBuilder = new IntactPsimiTabMockBuilder();

    @Before
    public void setup() {
        mockBuilder = new IntactPsimiTabMockBuilder();
    }

    @After
    public void cleanup() {
        mockBuilder = null;
    }

    @Test
    public void buildInteraction() throws Exception {
        final BinaryInteraction i = mockBuilder.createInteractionRandom();
        Assert.assertNotNull( i );
        Assert.assertTrue( i instanceof IntactBinaryInteraction );
        Assert.assertTrue( i.getInteractorA() instanceof ExtendedInteractor );
        Assert.assertTrue( i.getInteractorB() instanceof ExtendedInteractor );
    }

    @Test
    public void buildInteractor() throws Exception {
        final Interactor i = mockBuilder.createInteractor( 9606, "uniprot", "P12345" );
        Assert.assertNotNull( i );
        Assert.assertTrue( i instanceof ExtendedInteractor );
    }
}
