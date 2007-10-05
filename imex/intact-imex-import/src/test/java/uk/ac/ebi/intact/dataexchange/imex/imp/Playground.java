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
package uk.ac.ebi.intact.dataexchange.imex.imp;

import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.ImexRepositoryContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.Repository;

import java.io.File;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class Playground {

    public static void main(String[] args) throws Exception{



        IntactContext.initStandaloneContext(new File(Playground.class.getResource("/d003-hibernate.cfg.xml").getFile()));

        /*
        IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        CvXrefQualifier qual = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvXrefQualifier.class).getByPsiMiRef(CvXrefQualifier.IDENTITY_MI_REF);
        CvDatabase psiMi = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvDatabase.class).getByPsiMiRef(CvDatabase.PSI_MI_MI_REF);

        Institution dip = new Institution("dip");
        InstitutionXref xref = XrefUtils.createIdentityXref(dip, "MI:0465", qual, psiMi);
        dip.addXref(xref);

        Institution mint = new Institution("mint");
        InstitutionXref mintXref = XrefUtils.createIdentityXref(dip, "MI:0471", qual, psiMi);
        mint.addXref(mintXref);
        
        IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInstitutionDao().saveOrUpdate(dip);
        IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInstitutionDao().saveOrUpdate(mint);
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        */



        File repoDir = new File(System.getProperty("java.io.tmpdir"), "myRepo-dip2/");
        Repository repo = ImexRepositoryContext.openRepository(repoDir.toString());

        ImexImporter importer = new ImexImporter(repo);
        ImportReport report = importer.importNewAndFailed();

        repo.close();
                  
        IntactContext.getCurrentInstance().close();

        System.out.println(report);
        
    }

}