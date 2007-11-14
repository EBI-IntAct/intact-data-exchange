/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.parser;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.OrganismTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ProteinInteractorTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.CommandLineOptions;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.LabelValueBean;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.application.dataConversion.util.DOMUtil;

import java.util.Collection;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ProteinInteractorParser {

    private Element root;
    private ParticipantListParser interactorList;


    public ProteinInteractorParser( final ParticipantListParser interactorList, final Element root ) {

        this.interactorList = interactorList;
        this.root = root;
    }

    /**
     * Extract data from a <code>proteinInteractor</code> Element and produce an Intact <code>Protein</code>
     * <p/>
     * <pre>
     *  &lt;proteinInteractor id="hGHR"&gt;
     *      &lt;names&gt;
     *         &lt;shortLabel&gt;hGHR&lt;/shortLabel&gt;
     *         &lt;fullName&gt;Human growth hormone receptor&lt;/fullName&gt;
     *      &lt;/names&gt;
     *      &lt;xref&gt;
     *         &lt;primaryRef db="Swiss-Prot" id="P10912"/&gt;
     *      &lt;/xref&gt;
     *      &lt;organism ncbiTaxId="9606"&gt;
     *         &lt;names&gt;
     *            &lt;shortLabel&gt;Human&lt;/shortLabel&gt;
     *            &lt;fullName&gt;Homo sapiens&lt;/fullName&gt;
     *         &lt;/names&gt;
     *      &lt;/organism&gt;
     *      &lt;sequence&gt;MDLWQLLLTLALAGSSDAFSGSEATAAILSRAPWSLQSVNPGLKTNSSKEPKFTKCRSPERETFSCHWTDEVHHGTKNLGPIQLFYTRRNTQEWTQEWKECPDYVSAGENSCYFNSSFTSIWIPYCIKLTSNGGTVDEKCFSVDEIVQPDPPIALNWTLLNVSLTGIHADIQVRWEAPRNADIQKGWMVLEYELQYKEVNETKWKMMDPILTTSVPVYSLKVDKEYEVRVRSKQRNSGNYGEFSEVLYVTLPQMSQFTCEEDFYFPWLLIIIFGIFGLTVMLFVFLFSKQQRIKMLILPPVPVPKIKGIDPDLLKEGKLEEVNTILAIHDSYKPEFHSDDSWVEFIELDIDEPDEKTEESDTDRLLSSDHEKSHSNLGVKDGDSGRTSCCEPDILETDFNANDIHEGTSEVAQPQRLKGEADLLCLDQKNQNNSPYHDACPATQQPSVIQAEKNKPQPLPTEGAESTHQAAHIQLSNPSSLSNIDFYAQVSDITPAGSVVLSPGQKNKAGMSQCDMHPEMVSLCQENFLMDNAYFCEADAKKCIPVAPHIKVESHIQPSLNQEDIYITTESLTTAAGRPGTGEHVPGSEMPVPDYTSIHIVQSPQGLILNATALPLPDKEFLSSCGYVSTDQLNKIMP&lt;/sequence&gt;
     *  &lt;/proteinInteractor&gt;
     * </pre>
     *
     * @return an intact <code>Protein</code> or null if something goes wrong.
     *
     * @see uk.ac.ebi.intact.model.Protein
     */
    public LabelValueBean process() {
        ProteinInteractorTag proteinInteractor = null;

        if ( false == "proteinInteractor".equals( root.getNodeName() ) ) {
            MessageHolder.getInstance().addParserMessage( new Message( root, "We should be in proteinInteractor tag." ) );
        }

        final String id = root.getAttribute( "id" );

        // check if the proteinInteractor is existing in the global context
        if ( interactorList != null ) {
            proteinInteractor = (ProteinInteractorTag) interactorList.getInteractors().get( id );
            if ( proteinInteractor != null ) {
                final String msg = "WARNING - the protein id: " + id + " is defined several times. " +
                                   "The global definition will be used instead.";
                MessageHolder.getInstance().addParserMessage( new Message( root, msg ) );
                return new LabelValueBean( id, proteinInteractor );
            }
        }

        // CAUTION - MAY NOT BE THERE
        final Element biosourceElement = DOMUtil.getFirstElement( root, "organism" );
        OrganismTag hostOrganism = null;
        if ( biosourceElement != null ) {
            hostOrganism = OrganismParser.process( biosourceElement );
        } else {
            // the PSI file lacks a taxid, check if the user requested us to put a default value in such case.
            if ( CommandLineOptions.getInstance().hasDefaultInteractorTaxid() ) {
                final String defaultTaxid = CommandLineOptions.getInstance().getDefaultInteractorTaxid();
                hostOrganism = new OrganismTag( defaultTaxid );
            }
        }

        // TODO we could optimize by checking if the primaryRef is UniProt, if so we can shortcut the data loading

        // CAUTION - MAY NOT BE THERE
        final Element names = DOMUtil.getFirstElement( root, "names" );
        String shortlabel = null;
        String fullname = null;
        if ( names != null ) {
            shortlabel = DOMUtil.getShortLabel( names );
            // CAUTION - if names present, IT MAY NOT BE THERE
            fullname = DOMUtil.getFullName( names );
        }

        // CAUTION - MAY NOT BE THERE
        final Element xrefElement = DOMUtil.getFirstElement( root, "xref" );
        XrefTag xref = null;
        Collection secondaryXrefs = null;
        if( xrefElement != null ) {
            xref = XrefParser.processPrimaryRef( xrefElement );
            secondaryXrefs = XrefParser.processSecondaryRef( xrefElement );
        } else {

        }

        final Collection aliases = null;

        // CAUTION - MAY NOT BE THERE
        final Element sequenceElement = DOMUtil.getFirstElement( root, "sequence" );
        String sequence = null;
        if( sequenceElement != null ) {
            sequence = DOMUtil.getSimpleElementText( sequenceElement );
        }

        // Will contain the map: id ---> Protein definition
        LabelValueBean lvb = null;

        try {
            lvb = new LabelValueBean( id, new ProteinInteractorTag( shortlabel, fullname, xref, secondaryXrefs, aliases, hostOrganism, sequence ) );
        } catch ( IllegalArgumentException e ) {
            MessageHolder.getInstance().addParserMessage( new Message( root, e.getMessage() ) );
        }

        return lvb;
    }
}
