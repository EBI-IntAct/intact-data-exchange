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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ImexPersistence {

    public static final String NAME = "imexPersistence";

    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;

    public ImexPersistence() {

    }

    public EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = entityManagerFactory.createEntityManager();
        } else {
            if (!entityManager.isOpen()) {
                entityManager = entityManagerFactory.createEntityManager();
            }
        }

        return entityManager;
    }

    public void beginTransaction() {
        getEntityManager().getTransaction().begin();
    }

    public void commitTransaction() {
        getEntityManager().getTransaction().commit();
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }
}