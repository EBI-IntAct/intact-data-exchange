/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter;

/**
 * Exception thrown if the case is possible by IMEx standards, but not supported by the intact curation spec.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UnsupportedConversionException extends PsiConversionException {

    public UnsupportedConversionException() {
    }

    public UnsupportedConversionException(Throwable cause) {
        super(cause);
    }

    public UnsupportedConversionException(String message) {
        super(message);
    }

    public UnsupportedConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}