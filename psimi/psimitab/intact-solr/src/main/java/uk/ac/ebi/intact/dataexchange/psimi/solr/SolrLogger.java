/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.solr;

import org.apache.log4j.Logger;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.Enumeration;

/**
 * Workaround while slf4j uses jul to log and the rest is using commons-logging. Once everything is setup
 * using slf4j this can be removed.
 * 
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public final class SolrLogger {

    private SolrLogger() {}

    public static void readFromLog4j() {
        Logger logger = org.apache.log4j.LogManager.getLogger("org.apache.solr");

        if (logger != null && logger.getLevel() != null) {
            String log4jLevelStr = logger.getLevel().toString();
            Level level;

            if (org.apache.log4j.Level.DEBUG.equals(logger.getLevel())) {
                level = Level.FINE;
            } else if (org.apache.log4j.Level.WARN.equals(logger.getLevel())) {
                level = Level.WARNING;
            } else if (org.apache.log4j.Level.ERROR.equals(logger.getLevel())) {
                level = Level.SEVERE;
            } else {
                level = Level.parse(log4jLevelStr);
            }
            
            setLevel(level);
        } else {
            setLevel(Level.WARNING);
        }
    }

    public static void setLevel(Level level) {
        Enumeration<String> loggerNames = LogManager.getLogManager().getLoggerNames();
        while (loggerNames.hasMoreElements()) {
            LogManager.getLogManager().getLogger(loggerNames.nextElement()).setLevel(level);
        }
    }
}
