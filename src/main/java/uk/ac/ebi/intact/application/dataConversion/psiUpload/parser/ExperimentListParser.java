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
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.application.dataConversion.util.DOMUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * That class deals with: http://psidev.sourceforge.net/mi/xml/doc/MIF.html#element_experimentList_Link02BD0B20
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class ExperimentListParser {

    private Map experiments = new HashMap();

    ////////////////////////
    // Constructor
    ////////////////////////
    public ExperimentListParser() {
    }


    ///////////////////////
    // Getter
    ///////////////////////
    public Map getExperiments() {
        return experiments;
    }


    /**
     * Take &lt;entry&gt; in parameter.
     *
     * @param element
     */
    public void process( final Element element ) {
        final String name = element.getNodeName();
        if ( false == "entry".equals( name )
             &&
             false == "interaction".equals( name ) ) {
            MessageHolder.getInstance().addParserMessage( new Message( element, "ERROR - We should be in either " +
                                                                                "entry or interaction tag." ) );
            // TODO should we carry on here ? If the tag is not right ... the parsing can only fail !
        }

        final Element experimentList = DOMUtil.getFirstElement( element, "experimentList" );

        if ( experimentList != null ) {
            final NodeList someExperiments = experimentList.getElementsByTagName( "experimentDescription" );
            final int count = someExperiments.getLength();

            for ( int i = 0; i < count; i++ ) {
                final Node experimentNode = someExperiments.item( i );

                final ExperimentDescriptionParser expDesc = new ExperimentDescriptionParser( this, (Element) experimentNode );
                final LabelValueBean lvb = expDesc.process();

                if ( lvb != null ) {
                    experiments.put( lvb.getLabel(), lvb.getValue() );
                }
            } // experiments
        }
    }
}
