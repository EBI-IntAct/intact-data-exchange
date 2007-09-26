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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import uk.ac.ebi.intact.dataexchange.imex.repository.dao.ImexServiceProvider;
import uk.ac.ebi.intact.dataexchange.imex.repository.dao.ProviderService;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.Provider;

import java.io.File;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ImexRepositoryContext {

    private static final Log log = LogFactory.getLog(ImexRepositoryContext.class);

    private static ThreadLocal<ImexRepositoryContext> instance = new ThreadLocal<ImexRepositoryContext>();

    public static ImexRepositoryContext getInstance() {
        return instance.get();
    }

    public static Repository openRepository(String repositoryDir) {
        Repository repository = RepositoryFactory.createFileSystemRepository(repositoryDir, true);

        instance.set(new ImexRepositoryContext(repository));

        // check if the main providers exist (intact, mint and dip)
        // and create them otherwise
        checkMainProviders();

        return repository;
    }

    public static void closeRepository() {
        instance.set(null);
    }

    private BeanFactory beanFactory;
    private Repository repository;

    public ImexRepositoryContext(Repository repository) {
        this.repository = repository;

        ClassPathResource resource = new ClassPathResource("/META-INF/imex-repo-app.xml");
        this.beanFactory = new XmlBeanFactory(resource);

        BasicDataSource dataSource = (BasicDataSource) beanFactory.getBean("dataSource");
        dataSource.setUrl("jdbc:h2:"+new File(repository.getConfigDir(), "imex-repo").getAbsolutePath());
    }

    private static void checkMainProviders() {
        checkProvider("intact");
        checkProvider("mint");
        checkProvider("dip");
    }

    private static void checkProvider(String name) {
        if (log.isInfoEnabled()) log.info("Checking provider: "+name);

        ImexRepositoryContext context = instance.get();
        ProviderService providerService = context.getImexServiceProvider().getProviderService();
        Provider provider = providerService.findByName(name);

        if (provider == null) {
            createProvider(name, context);
        }
    }

    private static void createProvider(String name, ImexRepositoryContext context) {
        ProviderService providerService = context.getImexServiceProvider().getProviderService();
        ImexPersistence imexPersistence = context.getImexPersistence();

        Provider provider = new Provider(name);

        imexPersistence.beginTransaction();
        providerService.saveProvider(provider);
        imexPersistence.commitTransaction();
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public ImexPersistence getImexPersistence() {
        return (ImexPersistence) beanFactory.getBean(ImexPersistence.NAME);
    }

    public Repository getRepository() {
        return repository;
    }

    public ImexServiceProvider getImexServiceProvider() {
        return new ImexServiceProvider(this);
    }
}