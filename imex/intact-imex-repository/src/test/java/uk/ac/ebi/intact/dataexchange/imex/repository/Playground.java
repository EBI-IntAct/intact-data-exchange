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
package uk.ac.ebi.intact.dataexchange.imex.repository;

import uk.ac.ebi.intact.dataexchange.imex.repository.dao.ProviderService;
import uk.ac.ebi.intact.dataexchange.imex.repository.dao.ProviderDao;
import uk.ac.ebi.intact.dataexchange.imex.repository.dao.impl.JpaProviderService;
import uk.ac.ebi.intact.dataexchange.imex.repository.dao.impl.JpaProviderDao;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.Provider;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class Playground {

    public static void main(String[] args) {
        ImexRepositoryContext.openRepository("/tmp/myRepo");

        ImexPersistence imexPersistence = ImexRepositoryContext.getInstance().getImexPersistence();
        ProviderService providerService = ImexRepositoryContext.getInstance().getImexServiceProvider().getProviderService();

        //providerService.saveProvider(new Provider());
        //ProviderDao providerDao = (ProviderDao) ImexRepositoryContext.getInstance().getBeanFactory().getBean("providerDao");

        Provider provider = new Provider();
        provider.setName("intact");

        imexPersistence.beginTransaction();
        providerService.saveProvider(provider);
        imexPersistence.commitTransaction();

        System.out.println("Providers: "+providerService.queryAllProviders());
        
    }
}