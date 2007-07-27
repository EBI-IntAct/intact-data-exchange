// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.AbstractXref2Xml;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Annotation2xml;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi25.Xref2xmlPSI2;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Institution;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * TODO document this ;o)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class PsiDocumentFactory {

	public static final String URL_SCHEMA_VERSION_1 = "http://psidev.sourceforge.net/mi/xml/src/MIF.xsd";
	public static final String URL_SCHEMA_VERSION_2 = "http://psidev.sourceforge.net/mi/rel2/src/MIF2.xsd";
	public static final String URL_SCHEMA_VERSION_25 = "http://psidev.sourceforge.net/mi/rel25/src/MIF253.xsd";

	/////////////////////////
	// Constants
	private static final SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd");

	////////////////////////
	// Private methods

	/**
	 * Create today's date
	 *
	 * @return a string containing today's date.
	 */
	private static String getReleaseDate() {

		return formatter.format(new Date());
	}

	/**
	 * generate the source of a PSI document.
	 *
	 * @param document    the document to which we will ass the source.
	 * @param institution the Institution that is the source of that PSI document, can be null.
	 *
	 * @return the source.
	 */
	public static Element createSource(UserSessionDownload session,
			Document document, Institution institution) {

		Element source = document.createElement("source");
		source.setAttribute("releaseDate", getReleaseDate());

		// Extracting shortlabel and fullname...
		//        String shortlabel = null;
		//        String fullname = null;
		HashMap info = new HashMap();

		if (institution == null) {

			//            shortlabel = "IntAct";
			info.put("url", "http://www.ebi.ac.uk");
			info
					.put("postalAddress",
							"Wellcome Trust Genome Campus, Hinxton, Cambridge, CB10 1SD, United Kingdom");

			// creating shortlabel
			Element names = document.createElement("names");
			source.appendChild(names);
			Element shortlabelElement = document.createElement("shortLabel");
			Text shortlabelText = document
					.createTextNode("European Bioinformatics Institute");
			shortlabelElement.appendChild(shortlabelText);
			names.appendChild(shortlabelElement);

		} else {

			//            shortlabel = institution.getShortLabel();

			//            if ( institution.getFullName() != null && !"".equals( institution.getFullName().trim() ) ) {
			//                fullname = institution.getFullName();
			//            }

			if (institution.getUrl() != null
					&& !"".equals(institution.getUrl().trim())) {
				info.put("url", institution.getUrl());
			}

			if (institution.getPostalAddress() != null
					&& !"".equals(institution.getPostalAddress().trim())) {
				info.put("postalAddress", institution.getPostalAddress());
			}

			// creating shortlabel
			if (institution.getShortLabel() != null) {
				Element names = document.createElement("names");
				source.appendChild(names);
				Element shortlabelElement = document
						.createElement("shortLabel");
				Text shortlabelText = document.createTextNode(institution
						.getShortLabel());
				shortlabelElement.appendChild(shortlabelText);
				names.appendChild(shortlabelElement);

				if (institution.getFullName() != null) {
					Element fullNameElement = document
							.createElement("fullName");
					Text fullNameText = document.createTextNode(institution
							.getFullName());
					fullNameElement.appendChild(fullNameText);
					names.appendChild(fullNameElement);
				}
			}
		}

		//        // creating shortlabel
		//        Element names = document.createElement( "names" );
		//        source.appendChild( names );
		//        Element shortlabelElement = document.createElement( "shortLabel" );
		//        Text shortlabelText = document.createTextNode( "European Bioinformatics Institute" );
		//        shortlabelElement.appendChild( shortlabelText );
		//        names.appendChild( shortlabelElement );

		// creating fullName
		//        if ( fullname != null ) {
		//            Element fullNameElement = document.createElement( "fullName" );
		//            Text intactFullname = null;
		//            intactFullname = document.createTextNode( fullname );
		//            fullNameElement.appendChild( intactFullname );
		//            names.appendChild( fullNameElement );
		//        }

		// creating bibRef
		Element bibref = document.createElement("bibref");
		source.appendChild(bibref);
		Element bibxref = document.createElement("xref");
		bibref.appendChild(bibxref);
		Element primarybib = document
				.createElement(AbstractXref2Xml.PRIMARY_REF);
		bibxref.appendChild(primarybib);

		if (session.getPsiVersion().equals(PsiVersion.VERSION_1)
				|| session.getPsiVersion().equals(PsiVersion.VERSION_2)) {

			primarybib
					.setAttribute(AbstractXref2Xml.XREF_DB, CvDatabase.PUBMED);
			primarybib.setAttribute(AbstractXref2Xml.XREF_ID, "14681455");

		} else if (session.getPsiVersion().equals(PsiVersion.VERSION_25)) {

			primarybib.setAttribute(Xref2xmlPSI2.XREF_DB, CvDatabase.PUBMED);
			primarybib.setAttribute(Xref2xmlPSI2.XREF_DB_AC,
					CvDatabase.PUBMED_MI_REF);
			primarybib.setAttribute(Xref2xmlPSI2.XREF_ID, "14681455");
			primarybib.setAttribute(Xref2xmlPSI2.XREF_REFTYPE,
					CvXrefQualifier.PRIMARY_REFERENCE);
			primarybib.setAttribute(Xref2xmlPSI2.XREF_REFTYPE_AC,
					CvXrefQualifier.PRIMARY_REFERENCE_MI_REF);
		}

		// creating xref
		Element xref = document.createElement("xref");
		source.appendChild(xref);
		Element primaryref = document
				.createElement(AbstractXref2Xml.PRIMARY_REF);
		xref.appendChild(primaryref);

		if (session.getPsiVersion().equals(PsiVersion.VERSION_1)
				|| session.getPsiVersion().equals(PsiVersion.VERSION_2)) {

			primaryref
					.setAttribute(AbstractXref2Xml.XREF_DB, CvDatabase.PSI_MI);
			primaryref.setAttribute(AbstractXref2Xml.XREF_ID,
					CvDatabase.INTACT_MI_REF);

		} else if (session.getPsiVersion().equals(PsiVersion.VERSION_25)) {

			primaryref.setAttribute(Xref2xmlPSI2.XREF_DB, CvDatabase.PSI_MI);
			primaryref.setAttribute(Xref2xmlPSI2.XREF_DB_AC,
					CvDatabase.PSI_MI_MI_REF);
			primaryref.setAttribute(Xref2xmlPSI2.XREF_ID,
					CvDatabase.INTACT_MI_REF);
			primaryref.setAttribute(Xref2xmlPSI2.XREF_REFTYPE,
					CvXrefQualifier.PRIMARY_REFERENCE);
			primaryref.setAttribute(Xref2xmlPSI2.XREF_REFTYPE_AC,
					CvXrefQualifier.PRIMARY_REFERENCE_MI_REF);
		}

		// generate attributes if any...
		if (false == info.isEmpty()) {
			Element attributeListElement = document
					.createElement(Annotation2xml.ATTRIBUTE_LIST_NODE_NAME);
			source.appendChild(attributeListElement);

			for (Iterator iterator = info.keySet().iterator(); iterator
					.hasNext();) {
				String key = (String) iterator.next();

				Element attributeElement = document
						.createElement(Annotation2xml.ATTRIBUTE_NODE_NAME);
				attributeListElement.appendChild(attributeElement);

				// fill in the data
				attributeElement.setAttribute(Annotation2xml.NAME, key);
				Text attributeText = document.createTextNode((String) info
						.get(key));
				attributeElement.appendChild(attributeText);
			}
		}

		return source;
	}

	/**
	 * Builds an empty PSI document version 1.
	 *
	 * @param institution the source of that PSI Document.
	 *
	 * @return an empty PSI document version 1.
	 */
	private static Document buildPsiVersion1(UserSessionDownload session,
			Institution institution) {

		DOMImplementationImpl impl = new DOMImplementationImpl();
		Document document = impl.createDocument("net:sf:psidev:mi", "entrySet",
				null); //doctype only used by DTDs !

		Element psiEntrySet = document.getDocumentElement();

		psiEntrySet.setAttribute("xmlns", "net:sf:psidev:mi");
		psiEntrySet.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		psiEntrySet.setAttribute("xsi:schemaLocation", "net:sf:psidev:mi "
				+ URL_SCHEMA_VERSION_1);
		psiEntrySet.setAttribute("level", "1");
		psiEntrySet.setAttribute("version", "1");

		Element entry = document.createElement("entry");
		psiEntrySet.appendChild(entry);

		Element source = createSource(session, document, institution);
		entry.appendChild(source);

		return document;
	}

	/**
	 * Builds an empty PSI document version 2.
	 *
	 * @param institution the source of that PSI Document.
	 *
	 * @return an empty PSI document version 2.
	 */
	private static Document buildPsiVersion2(UserSessionDownload session,
			Institution institution) {

		DOMImplementationImpl impl = new DOMImplementationImpl();
		Document document = impl.createDocument("net:sf:psidev:mi", "entrySet",
				null); //doctype only used by DTDs !

		Element psiEntrySet = document.getDocumentElement();

		psiEntrySet.setAttribute("xmlns", "net:sf:psidev:mi");
		psiEntrySet.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		psiEntrySet.setAttribute("xsi:schemaLocation", "net:sf:psidev:mi "
				+ URL_SCHEMA_VERSION_2);

		psiEntrySet.setAttribute("level", "1");
		psiEntrySet.setAttribute("version", "2");

		Element entry = document.createElement("entry");
		psiEntrySet.appendChild(entry);

		Element source = createSource(session, document, institution);
		entry.appendChild(source);

		return document;
	}

	/**
	 * Builds an empty PSI document version 3.
	 *
	 * @param institution the source of that PSI Document.
	 *
	 * @return an empty PSI document version 3.
	 */
	private static Document buildPsiVersion25(UserSessionDownload session,
			Institution institution) {

		DOMImplementationImpl impl = new DOMImplementationImpl();
		Document document = impl.createDocument("net:sf:psidev:mi", "entrySet",
				null); //doctype only used by DTDs !

		Element psiEntrySet = document.getDocumentElement();

		psiEntrySet.setAttribute("xmlns", "net:sf:psidev:mi");
		psiEntrySet.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		psiEntrySet.setAttribute("xsi:schemaLocation", "net:sf:psidev:mi "
				+ URL_SCHEMA_VERSION_25);

		// values fixed by the schema
		psiEntrySet.setAttribute("level", "2");
		psiEntrySet.setAttribute("version", "5");
		psiEntrySet.setAttribute("minorVersion", "3");

		Element entry = document.createElement("entry");
		psiEntrySet.appendChild(entry);

		Element source = createSource(session, document, institution);
		entry.appendChild(source);

		return document;
	}

	/////////////////////////////
	// Public methods

	/**
	 * Builds an empty PSI document of the specified version.
	 *
	 * @param session The user session that contains the requested PSI version.
	 *
	 * @return a XML Document representing an empty PSI document.
	 */
	public static Document buildPsiDocument(UserSessionDownload session) {

		return buildPsiDocument(session, null);
	}

	/**
	 * Builds an empty PSI document of the specified version.
	 *
	 * @param session The user session that contains the requested PSI version.
	 * @param source  the institution that will represent the source of that PSI file.
	 *
	 * @return a XML Document representing an empty PSI document.
	 */
	public static Document buildPsiDocument(UserSessionDownload session,
			Institution source) {

		if (session == null) {
			throw new UnsupportedOperationException(
					"Cannot build a PSI document without a valid session (session was null).");
		}

		Document document = null;

		if (session.getPsiVersion().equals(PsiVersion.VERSION_1)) {

			document = buildPsiVersion1(session, source);

		} else if (session.getPsiVersion().equals(PsiVersion.VERSION_2)) {

			document = buildPsiVersion2(session, source);

		} else if (session.getPsiVersion().equals(PsiVersion.VERSION_25)) {

			document = buildPsiVersion25(session, source);

		} else {

			throw new UnsupportedOperationException(
					"Unsupported version of PSI (version "
							+ session.getPsiVersion() + ").");
		}

		return document;
	}
}