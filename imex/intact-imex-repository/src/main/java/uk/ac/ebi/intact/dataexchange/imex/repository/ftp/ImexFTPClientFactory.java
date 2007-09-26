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
package uk.ac.ebi.intact.dataexchange.imex.repository.ftp;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public final class ImexFTPClientFactory {

    private ImexFTPClientFactory() {}

    private static final String DIP_HOST = "dip.doe-mbi.ucla.edu";
    private static final String DIP_FOLDER = "/imex/";

    private static final String MINT_HOST = "mint.bio.uniroma2.it";
    private static final String MINT_FOLDER = "/pub/IMEx/";

    private static final String INTACT_HOST = "ftp.ebi.ac.uk";
    private static final String INTACT_FOLDER = "/pub/databases/intact/imex/";

    public static ImexFTPClient createDipClient() {
        return new ImexFTPClient(DIP_HOST, DIP_FOLDER);
    }

    public static ImexFTPClient createMintClient() {
        return new ImexFTPClient(MINT_HOST, MINT_FOLDER);
    }

    public static ImexFTPClient createIntactClient() {
        return new ImexFTPClient(INTACT_HOST, INTACT_FOLDER);
    }

    public static ImexFTPClient createUndefinedClient(String host, String folder) {
        return new ImexFTPClient(host, folder);
    }

}