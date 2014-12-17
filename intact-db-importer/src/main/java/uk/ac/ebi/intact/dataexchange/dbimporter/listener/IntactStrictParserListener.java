package uk.ac.ebi.intact.dataexchange.dbimporter.listener;

import psidev.psi.mi.jami.datasource.FileSourceContext;
import psidev.psi.mi.jami.datasource.FileSourceLocator;
import psidev.psi.mi.jami.listener.MIFileParserListener;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.tab.extension.*;
import psidev.psi.mi.jami.tab.listener.MitabParserListener;
import psidev.psi.mi.jami.tab.listener.MitabParserLogger;
import psidev.psi.mi.jami.xml.listener.PsiXmlParserListener;
import psidev.psi.mi.jami.xml.listener.PsiXmlParserLogger;
import psidev.psi.mi.jami.xml.model.reference.XmlIdReference;

import java.util.Collection;

/**
 * Intact parser listener that throws exceptions when it fails to read an object
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/12/14</pre>
 */

public class IntactStrictParserListener implements MIFileParserListener,MitabParserListener,PsiXmlParserListener{

    private MitabParserListener mitabParserListener;
    private PsiXmlParserListener psiXmlParserListener;

    public IntactStrictParserListener(){
        this.mitabParserListener = new MitabParserLogger();
        this.psiXmlParserListener = new PsiXmlParserLogger();
    }

    @Override
    /**
     * We just ignore the text found on identifier
     */
    public void onTextFoundInIdentifier(MitabXref xref) {
        this.mitabParserListener.onTextFoundInIdentifier(xref);
    }

    @Override
    /**
     * We just ignore text found in confidence
     */
    public void onTextFoundInConfidence(MitabConfidence conf) {
        this.mitabParserListener.onTextFoundInConfidence(conf);
    }

    @Override
    /**
     * We don't accept invalid expanded interactions
     */
    public void onMissingExpansionId(MitabCvTerm expansion) {
        throw new IntactImporterException("The mitab expansion method is invalid because does not have a valid MI identifier "+expansion.getShortName());
    }

    @Override
    /**
     * We bjust ignore this message and consider several identifiers
     */
    public void onSeveralUniqueIdentifiers(Collection<MitabXref> ids) {
        this.mitabParserListener.onSeveralUniqueIdentifiers(ids);
    }

    @Override
    public void onEmptyUniqueIdentifiers(int line, int column, int mitabColumn) {
        throw new IntactImporterException("The interactor line "+line+", column "+column+" does not have unique identifiers and it is required.");
    }

    @Override
    public void onMissingInteractorIdentifierColumns(int line, int column, int mitabColumn) {
        throw new IntactImporterException("The interactor line "+line+", column "+column+" does not have identifiers and it is required.");
    }

    @Override
    public void onSeveralOrganismFound(Collection<MitabOrganism> organisms) {
        throw new IntactImporterException(organisms.size() + " organisms have been found and only one is allowed: "+organisms.iterator().next());
    }

    @Override
    public void onSeveralStoichiometryFound(Collection<MitabStoichiometry> stoichiometry) {
        throw new IntactImporterException(stoichiometry.size() + " stoichiometry elements have been found and only one is allowed: "+stoichiometry.iterator().next());
    }

    @Override
    public void onSeveralFirstAuthorFound(Collection<MitabAuthor> authors) {
        throw new IntactImporterException(authors.size() + " first authors have been found and only one is allowed: "+authors.iterator().next());
    }

    @Override
    public void onSeveralSourceFound(Collection<MitabSource> sources) {
        throw new IntactImporterException(sources.size() + " institutions/sources have been found and only one is allowed: "+sources.iterator().next());
    }

    @Override
    public void onSeveralCreatedDateFound(Collection<MitabDate> dates) {
        this.mitabParserListener.onSeveralCreatedDateFound(dates);
    }

    @Override
    public void onSeveralUpdatedDateFound(Collection<MitabDate> dates) {
        this.mitabParserListener.onSeveralUpdatedDateFound(dates);
    }

    @Override
    public void onAliasWithoutDbSource(MitabAlias alias) {
        this.mitabParserListener.onAliasWithoutDbSource(alias);
    }

    @Override
    public void onSeveralCvTermsFound(Collection<MitabCvTerm> terms, FileSourceContext context, String message) {
        throw new IntactImporterException(message+". Only one Cv is allowed "+context);
    }

    @Override
    public void onSeveralHostOrganismFound(Collection<MitabOrganism> organisms, FileSourceContext context) {
        throw new IntactImporterException(organisms.size() + " Host organisms have been found and only one is allowed: "+organisms.iterator().next());
    }

    @Override
    public void onUnresolvedReference(XmlIdReference ref, String message) {
        throw new IntactImporterException("Unresolved XML Reference "+ref+". Could not find any existing elements with the provided id.");
    }

    @Override
    public void onSeveralHostOrganismFound(Collection<Organism> organisms, FileSourceLocator locator) {
        throw new IntactImporterException(organisms.size() + " Host organisms have been found and only one is allowed: "+organisms.iterator().next());
    }

    @Override
    public void onSeveralExpressedInOrganismFound(Collection<Organism> organisms, FileSourceLocator locator) {
        throw new IntactImporterException(organisms.size() + " expressed in organisms have been found and only one is allowed: "+organisms.iterator().next());
    }

    @Override
    public void onSeveralExperimentalRolesFound(Collection<CvTerm> roles, FileSourceLocator locator) {
        throw new IntactImporterException(roles.size() + " experimental roles have been found and only one is allowed: "+locator);
    }

    @Override
    public void onSeveralExperimentsFound(Collection<Experiment> experiments, FileSourceLocator locator) {
        throw new IntactImporterException(experiments.size() + " experiments have been found and only one is allowed: "+experiments.iterator().next());
    }

    @Override
    public void onInvalidSyntax(FileSourceContext context, Exception e) {
        throw new IntactImporterException("The file syntax is invalid: "+context, e);
    }

    @Override
    public void onSyntaxWarning(FileSourceContext context, String message) {
        throw new IntactImporterException("The file syntax has warnings: "+message+", "+context);
    }

    @Override
    public void onMissingCvTermName(CvTerm term, FileSourceContext context, String message) {
        throw new IntactImporterException(message+". Names are required for cv terms. "+context);
    }

    @Override
    public void onMissingInteractorName(Interactor interactor, FileSourceContext context) {
        throw new IntactImporterException("Interactor without any names. Names are required for interactors. "+context);
    }

    @Override
    public void onParticipantWithoutInteractor(Participant participant, FileSourceContext context) {
        throw new IntactImporterException("Participant without interactor. Interactor details such as identifiers/names are required for interactors. "+context);
    }

    @Override
    public void onInteractionWithoutParticipants(Interaction interaction, FileSourceContext context) {
        throw new IntactImporterException("Interaction without any participants. Participants are required for interactions. "+context);
    }

    @Override
    public void onInvalidOrganismTaxid(String taxid, FileSourceContext context) {
        throw new IntactImporterException("Invalid organism taxid "+taxid+", "+context);
    }

    @Override
    public void onMissingParameterValue(FileSourceContext context) {
        throw new IntactImporterException("Missing parameter value , "+context);
    }

    @Override
    public void onMissingParameterType(FileSourceContext context) {
        throw new IntactImporterException("Missing parameter type , "+context);
    }

    @Override
    public void onMissingConfidenceValue(FileSourceContext context) {
        throw new IntactImporterException("Missing confidence value , "+context);
    }

    @Override
    public void onMissingConfidenceType(FileSourceContext context) {
        throw new IntactImporterException("Missing confidence type , "+context);
    }

    @Override
    public void onMissingChecksumValue(FileSourceContext context) {
        throw new IntactImporterException("Missing checksum value , "+context);
    }

    @Override
    public void onMissingChecksumMethod(FileSourceContext context) {
        throw new IntactImporterException("Missing checksum method , "+context);
    }

    @Override
    public void onInvalidPosition(String message, FileSourceContext context) {
        throw new IntactImporterException("Invalid range position, "+message+" , "+context);
    }

    @Override
    public void onInvalidRange(String message, FileSourceContext context) {
        throw new IntactImporterException("Invalid range, "+message+" , "+context);
    }

    @Override
    public void onInvalidStoichiometry(String message, FileSourceContext context) {
        throw new IntactImporterException("Invalid stoichiometry, "+message+" , "+context);
    }

    @Override
    public void onXrefWithoutDatabase(FileSourceContext context) {
        throw new IntactImporterException("Database xref without a database, "+context);
    }

    @Override
    public void onXrefWithoutId(FileSourceContext context) {
        throw new IntactImporterException("Database xref without a primary identifier, "+context);
    }

    @Override
    public void onAnnotationWithoutTopic(FileSourceContext context) {
        throw new IntactImporterException("Annotation without a topic, "+context);
    }

    @Override
    public void onAliasWithoutName(FileSourceContext context) {
        throw new IntactImporterException("Alias without a name, "+context);
    }
}
