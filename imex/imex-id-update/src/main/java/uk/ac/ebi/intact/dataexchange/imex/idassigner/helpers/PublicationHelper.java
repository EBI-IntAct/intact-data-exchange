/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dataexchange.imex.idassigner.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.intact.core.IntactException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persistence.dao.XrefDao;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.IMExIdTransformer;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.ExperimentXref;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.InteractorXref;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.PublicationXref;

/**
 * Utility methods for a publication.
 * 
 * @author Arnaud Ceol
 * @version $Id
 */
public class PublicationHelper {

	public static final Log log = LogFactory.getLog(PublicationHelper.class);

	/**
	 * Simple representation of a Date.
	 * <p/>
	 * Will be used to name our IMEx files.
	 */
	private static final SimpleDateFormat SIMPLE_DATE_FORMATER = new SimpleDateFormat(
			"yyyy-MM-dd");

	public static final String IMEX_EXPORT_SEPARATOR = ":";

	private static long getLastEvidenceIdFromXrefs(Publication publication,
			String imexPrimaryId) {
		long lastId = 0;

		DaoFactory daoFactory = IntactContext.getCurrentInstance()
				.getDataContext().getDaoFactory();

		Collection<InteractorXref> xrefs = daoFactory.getXrefDao(
				InteractorXref.class).getByPrimaryIdLike(imexPrimaryId + "-%");

		for (InteractorXref xref : xrefs) {
			long evidenceIndex = IMExIdTransformer.parseIMExEvidenceId(xref
					.getPrimaryId());
			if (evidenceIndex > lastId)
				lastId = evidenceIndex;
		}

		return lastId;
	}

	public static long getLastEvidenceIMExId(Publication publication)
			throws IntactException {
		CvTopic lastEvidenceId = CvHelper.getLastEvidenceId();

		for (Annotation annotation : publication.getAnnotations()) {
			if (lastEvidenceId.equals(annotation.getCvTopic())) {
				return Long.valueOf(annotation.getAnnotationText());
			}
		}

		// get evidence Id from xrefs
		String imexPrimaryId = PublicationHelper.getIMExId(publication);
		return getLastEvidenceIdFromXrefs(publication, imexPrimaryId);

	}

	public static void setLastEvidenceId(Publication publication, long id)
			throws IntactException {
		CvTopic lastEvidenceId = CvHelper.getLastEvidenceId();

		Annotation lastEvidenceIdAnnot = null;

		for (Annotation annotation : publication.getAnnotations()) {
			if (lastEvidenceId.equals(annotation.getCvTopic())) {
				lastEvidenceIdAnnot = annotation;
				break;
			}
		}

		if (lastEvidenceIdAnnot != null
				&& id == Long.valueOf(lastEvidenceIdAnnot.getAnnotationText())) {
			return;
		}

		if (lastEvidenceIdAnnot != null) {
			publication.removeAnnotation(lastEvidenceIdAnnot);
		}

		// get annotation from database
		DaoFactory daoFactory = IntactContext.getCurrentInstance()
				.getDataContext().getDaoFactory();

		Collection<Annotation> annotations = daoFactory.getAnnotationDao()
				.getByTextLike(Long.valueOf(id).toString());

		for (Annotation annotation : annotations) {
			if (lastEvidenceId.equals(annotation.getCvTopic())) {
				lastEvidenceIdAnnot = annotation;
				break;
			}
		}

		// create it if necessary
		if (lastEvidenceIdAnnot == null) {
			lastEvidenceIdAnnot = new Annotation(IntactContext
					.getCurrentInstance().getInstitution(), lastEvidenceId,
					Long.valueOf(id).toString());
			daoFactory.getAnnotationDao().persist(lastEvidenceIdAnnot);
		}

		publication.addAnnotation(lastEvidenceIdAnnot);
		daoFactory.getPublicationDao().merge(publication);
	}

	/**
	 * Return today's date in a simple format.
	 * 
	 * @return
	 */
	public static String getTodaySimpleDate() {
		return SIMPLE_DATE_FORMATER.format(new Date()); // YYYY-MM-DD;
	}

	/**
	 * Get a list of Annotation having CvTopic( imex-exported ) and sort them
	 * chonologicaly.
	 * 
	 * @param publication
	 *            the publication of interrest.
	 * 
	 * @return a sorted collection of exported date.
	 * 
	 * @throws IntactException
	 */
	public static List<Annotation> getExportHistory(Publication publication)
			throws IntactException {
		CvTopic imexExported = CvHelper.getImexExported();

		List<Annotation> export = new ArrayList<Annotation>(publication
				.getAnnotations().size());

		for (Annotation annotation : publication.getAnnotations()) {
			if (imexExported.equals(annotation.getCvTopic())) {
				export.add(annotation);
			}
		}

		// sort by date
		Collections.sort(export, new Comparator<Annotation>() {
			public int compare(Annotation o1, Annotation o2) {
				String t1 = o1.getAnnotationText();
				String t2 = o2.getAnnotationText();

				int idx1 = t1.indexOf(IMEX_EXPORT_SEPARATOR);
				if (idx1 == -1) {
					idx1 = t1.length();
				}
				String dt1 = t1.substring(0, idx1);

				int idx2 = t2.indexOf(IMEX_EXPORT_SEPARATOR);
				if (idx2 == -1) {
					idx2 = t2.length();
				}
				String dt2 = t2.substring(0, idx2);

				Date d1 = null;
				try {
					d1 = SIMPLE_DATE_FORMATER.parse(dt1);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				Date d2 = null;
				try {
					d2 = SIMPLE_DATE_FORMATER.parse(dt2);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				if (d1 == null) {
					return -1;
				}

				if (d2 == null) {
					return 1;
				}

				return d1.compareTo(d2);
			}
		});

		return export;
	}

	public static void showExportHistory(Publication publication)
			throws IntactException {

		List<Annotation> export = getExportHistory(publication);

		// display
		for (Annotation annotation : export) {
			log.debug(annotation.getAnnotationText());
		}
	}

	// //////////////////////////
	// IMEx ID

	/**
	 * Add an IMEx ID to a publication. IMEx ID is propagated to all experiments, but not to the interactions.
	 * 
	 * @param publication
	 *            the publication
	 * @param imexId
	 *            the IMEx ID
	 * 
	 * @return true if the IMEx ID was added successfully, false otherwise (eg.
	 *         there was already one IMEx id).
	 * 
	 * @throws uk.ac.ebi.intact.business.IntactException
	 * 
	 */
	public static boolean assignIMExId(Publication publication, String imexId)
			throws IntactException {

		// check if we own the publication
		if (false == IntactContext.getCurrentInstance().getInstitution()
				.equals(publication.getOwner())) {
			throw new IntactException("This publication is owned by "
					+ publication.getOwner().getShortLabel()
					+ " and cannot be modified");
		}

		if (null == InstitutionHelper.getMyInstitutionPsiId()) {
			throw new IntactException(
					"The institution running the database should have an associated PSI-MI id");
		}

		CvDatabase imex = CvHelper.getImex();

		String id = getIMExId(publication);

		if (id != null) {
			System.out.println("Publication " + publication.getAc() + " "
					+ publication.getShortLabel() + " had already an IMEx ID: "
					+ id + ". skip update.");

			return false;
		}

		// add a new Xref
		Institution owner = IntactContext.getCurrentInstance().getInstitution();
		PublicationXref xref = new PublicationXref(owner, imex, imexId, null);
		publication.addXref(xref);

		DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
		XrefDao<PublicationXref> xdao = daoFactory.getXrefDao(PublicationXref.class);
		xdao.persist(xref);
		log.info("Added IMEx ID( " + imexId + " ) to publication " + publication.getAc() + " " + publication.getShortLabel());

		// add to all experiments
		updateExperiments(publication);		
		
		return true;

	}

	
	/**
	 * Copy the publication's IMEx ID to all experiment. Skip experiments which have already an IMEx ID.
	 * @param publication
	 * 	the publication
	 * @throws IntactException
	 * 	if the publication has no IMEx ID yet.
	 */
	public static void updateExperiments(Publication publication) throws IntactException {

		// check if we own the publication
		if (false == IntactContext.getCurrentInstance().getInstitution()
				.equals(publication.getOwner())) {
			throw new IntactException("This publication is owned by "
					+ publication.getOwner().getShortLabel()
					+ " and cannot be modified");
		}
		
		if (null == InstitutionHelper.getMyInstitutionPsiId()) {
			throw new IntactException(
					"The institution running the database should have an associated PSI-MI id");
		}

		PublicationXref imexXref = getIMExXref(publication); 

		if (imexXref == null) {
			throw new IntactException("Publication " + publication.getAc() + " "
					+ publication.getShortLabel() + " has no IMEx ID yet. Assign an IMExID to the publication before to spread it to the experiments.");
		}

		DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
		XrefDao<ExperimentXref> xdao = daoFactory.getXrefDao(ExperimentXref.class);

		for (Experiment experiment : publication.getExperiments())  {
			if (getIMExXref(experiment) != null) {
				// already assigned
				continue;
			}	
			
			// add a new Xref
			ExperimentXref xref = new ExperimentXref(imexXref.getOwner(), imexXref.getCvDatabase(), imexXref.getPrimaryId(), imexXref.getCvXrefQualifier());
			experiment.addXref(xref);

			xdao.persist(xref);
			log.info("Added IMEx ID( " +  imexXref.getPrimaryId() + " ) to experiment " + experiment.getAc() + " " + experiment.getShortLabel());
		}
				
	}
	
	
	/**
	 * Search for an IMEx id.
	 * 
	 * @param interaction
	 *            the interaction that may hold an IMEx Xref.
	 * 
	 * @return the IMEx id, or null if not found.
	 */
	public static String getIMExId(Publication publication) {

		PublicationXref imexXref = getIMExXref(publication); 

		if (imexXref != null) {
			return imexXref.getPrimaryId();
		}
		
		return null; // not found
	}

	
	private static PublicationXref getIMExXref(Publication publication)  {

		CvDatabase imex = CvHelper.getImex();

		for (PublicationXref xref : publication.getXrefs()) {
			if (imex.equals(xref.getCvDatabase())) {
				return xref;
			}
		}

		// check experiments: if the publication has no IMEx id, the experiments
		// should neither.
		for (Experiment experiment : publication.getExperiments()) {
			ExperimentXref experimentIMEx = getIMExXref(experiment);
			if (experimentIMEx != null) {
					throw new IntactException("Experiment "
							+ experiment.getAc() + " of publication "
							+ publication.getAc()
							+ " has already been assigned the IMEx id "
							+ experimentIMEx.getPrimaryId());
			}

		}

		return null; // not found
	}

	private static ExperimentXref getIMExXref(Experiment experiment)  {

		CvDatabase imex = CvHelper.getImex();

		for (ExperimentXref xref : experiment.getXrefs()) {
			if (imex.equals(xref.getCvDatabase())) {
				return xref;
			}
		}

		return null; // not found
	}
	
	
	/**
	 * Answers the question: "has the given interaction got an IMEx ID ?".
	 * 
	 * @param interaction
	 *            the interaction
	 * 
	 * @return true if the interaction has an IMEx ID.
	 */
	public static boolean hasIMExId(Publication publication) {
		return (null != getIMExId(publication));
	}
}