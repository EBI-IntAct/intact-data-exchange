/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.LabelValueBean;
import uk.ac.ebi.intact.application.dataConversion.util.DOMUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ParticipantListParser {

    private Map interactors = new HashMap();

    public ParticipantListParser( final Map interactors ) {
        this.interactors = interactors;
    }

    public ParticipantListParser() {
        this( new HashMap() );
    }


    public Map getInteractors() {
        return interactors;
    }

    /**
     * Process a &lt;interactorList&gt;
     *
     * @param entry a &lt;interactorList&gt; Element
     */
    public void process( final Element entry ) {

        final Element proteinsList = DOMUtil.getFirstElement( entry, "interactorList" );
        if ( proteinsList != null ) {
            final NodeList someProteins = proteinsList.getElementsByTagName( "proteinInteractor" );
            for ( int i = 0; i < someProteins.getLength(); i++ ) {
                final Node interactor = someProteins.item( i );
                final ProteinInteractorParser proteinInteractor = new ProteinInteractorParser( this, (Element) interactor );

                final LabelValueBean lvb = proteinInteractor.process();
                if ( lvb != null ) {
                    interactors.put( lvb.getLabel(), lvb.getValue() );
                }
            } // interactors
        }
    }
}
