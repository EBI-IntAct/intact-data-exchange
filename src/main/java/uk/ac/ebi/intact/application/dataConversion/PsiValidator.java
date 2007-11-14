// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;

public class PsiValidator {

    private static final Log log = LogFactory.getLog(PsiValidator.class);

    private static class PsiValidatorReportHandler extends DefaultHandler {

        private PsiValidatorReport report;

        //////////////////////
        // Constructors

        public PsiValidatorReportHandler( PsiValidatorReport report ) {
           this.report = report;
        }

        ///////////////
        // Overriding
        @Override
        public void warning( SAXParseException e ) throws SAXException {
            log.warn(e.getMessage());

            PsiValidatorMessage msg = new PsiValidatorMessage(PsiValidatorMessage.Level.WARN, e);
            report.addMessage(msg);
        }

        @Override
        public void error( SAXParseException e ) throws SAXException {
            log.error(e.getMessage());

            report.setValid(false);

            PsiValidatorMessage msg = new PsiValidatorMessage(PsiValidatorMessage.Level.ERROR, e);
            report.addMessage(msg);
        }

        @Override
        public void fatalError( SAXParseException e ) throws SAXException {
            log.error(e.getMessage());
            
            report.setValid(false);

            PsiValidatorMessage msg = new PsiValidatorMessage(PsiValidatorMessage.Level.FATAL, e);
            report.addMessage(msg);
        }


        public PsiValidatorReport getReport()
        {
            return report;
        }
    }

    /**
     * Validates a xml document
     * @param file the file with the xml to validate
     * @return if the document is valid returns true
     */
    public static PsiValidatorReport validate( File file ) throws FileNotFoundException {
        String filename = file.getAbsolutePath();

        log.debug( "Validating " + filename );

        InputSource inputSource = new InputSource( new FileReader( filename ) );

        return validate( inputSource );
    }

    /**
     * Validates a xml document
     * @param xmlString the string with the xml to validate
     * @return if the document is valid returns true
     */
    public static PsiValidatorReport validate( String xmlString ) {

        InputSource inputSource = new InputSource( new StringReader( xmlString ) );

        return validate( inputSource );
    }

    /**
     * Validates a xml document
     * @param inputSource the source for the doc to validate
     * @return if the document is valid returns true
     */
    public static PsiValidatorReport validate( InputSource inputSource ) {

        String parserClass = SAXParser.class.getName();
        String validationFeature = "http://xml.org/sax/features/validation";
        String schemaFeature = "http://apache.org/xml/features/validation/schema";

        PsiValidatorReportHandler handler = new PsiValidatorReportHandler(new PsiValidatorReport());

        try {

            XMLReader r = XMLReaderFactory.createXMLReader( parserClass );
            r.setFeature( validationFeature, true );
            r.setFeature( schemaFeature, true );

            r.setErrorHandler( handler );
            r.setEntityResolver(new XsdEntityResolver());

            r.parse( inputSource );

        } catch ( SAXException e ) {
            e.printStackTrace();

        } catch ( IOException e ) {
            e.printStackTrace();
        }

        if (log.isDebugEnabled())
        {
            log.debug( "Validation completed. Document is valid: "+handler.getReport().isValid() );
        }

        return handler.getReport();
    }
}