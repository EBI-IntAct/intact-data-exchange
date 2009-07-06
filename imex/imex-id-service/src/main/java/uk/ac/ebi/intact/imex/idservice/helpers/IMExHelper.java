/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idservice.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Helper method on the IMEx.properties file.
 * 
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: IMExHelper.java 8153 2007-04-18 11:39:15Z skerrien $
 * @since <pre>
 * 16 - May - 2006
 * </pre>
 */
public class IMExHelper {

	public static final String IMEX_PROPERTIES_FILE = "/config/imex.properties";

	// //////////////////////////////
	// Properties name

	private static final String KEYSTORE_FILENAME_KEY = "keystore.filename";

	private static final String KEY_ASSIGNER_URL_KEY = "key.assigner.url";
	private static final String USERNAME_KEY = "key.assigner.username";
	private static final String PASSWORD_KEY = "key.assigner.password";

	private static final String DATABASE_NAME_KEY = "database.name";
	private static final String DATABASE_PSI_ID_KEY = "database.psimi.id";

	/**
	 * Load the IMEx.properties set by the user and made available from the
	 * classpath.
	 * 
	 * @return properties or null if not found.
	 */
	private static Properties getIMExConfig() {

		try {
			InputStream is = IMExHelper.class
					.getResourceAsStream(IMEX_PROPERTIES_FILE);
			if (is != null) {
				Properties properties = new Properties();
				properties.load(is);
				return properties;
			}
		} catch (IOException ioe) {
		}

		return null;

	}

	// /////////////////////////////
	// Access to the properties

	/**
	 * Answers the question: "Is the IMEx properties file available ?".
	 * 
	 * @return true if the properties file could be read from disk.
	 */
	public static boolean isIMExPropertiesFileAvailable() {
		return (null != getIMExConfig());
	}

	public static String getKeyAssignerUrl() {
		Properties properties = getIMExConfig();
		if (properties == null) {
			return null;
		}

		return properties.getProperty(KEY_ASSIGNER_URL_KEY);
	}


	public static String getKeyAssignerDatabasePsiName() {
		Properties properties = getIMExConfig();
		if (properties == null) {
			return null;
		}

		return properties.getProperty(DATABASE_NAME_KEY);
	}
	

	public static String getKeyAssignerdatabasePsiId() {
		Properties properties = getIMExConfig();
		if (properties == null) {
			return null;
		}

		return properties.getProperty(DATABASE_PSI_ID_KEY);
	}
	
	
	public static String getKeyAssignerUsername() {
		Properties properties = getIMExConfig();
		if (properties == null) {
			return null;
		}

		return properties.getProperty(USERNAME_KEY);
	}

	public static String getKeyAssignerPassword() {
		Properties properties = getIMExConfig();
		if (properties == null) {
			return null;
		}

		return properties.getProperty(PASSWORD_KEY);
	}

	public static String getKeyStoreFilename() {
		Properties properties = getIMExConfig();
		if (properties == null) {
			return null;
		}

		return properties.getProperty(KEYSTORE_FILENAME_KEY);
	}
}