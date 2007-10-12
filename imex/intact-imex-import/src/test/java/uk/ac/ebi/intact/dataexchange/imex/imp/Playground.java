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

import uk.ac.ebi.intact.context.DataContext;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.ImexRepositoryContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.Repository;
import uk.ac.ebi.intact.sanity.check.SanityChecker;
import uk.ac.ebi.intact.sanity.commons.SanityReport;
import uk.ac.ebi.intact.sanity.commons.rules.report.ReportWriter;
import uk.ac.ebi.intact.sanity.commons.rules.report.SimpleReportWriter;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class Playground {

    public static void main(String[] args) throws Exception{

        IntactContext.initStandaloneContext(new File(Playground.class.getResource("/temph2-hibernate.cfg.xml").getFile()));

           /*
        getDataContext().beginTransaction();

        SmallCvPrimer primer = new SmallCvPrimer(getDataContext().getDaoFactory());
        primer.createCVs();

        CvXrefQualifier qual = getDataContext().getDaoFactory().getCvObjectDao(CvXrefQualifier.class).getByPsiMiRef(CvXrefQualifier.IDENTITY_MI_REF);
        CvDatabase psiMi = getDataContext().getDaoFactory().getCvObjectDao(CvDatabase.class).getByPsiMiRef(CvDatabase.PSI_MI_MI_REF);

        Institution dip = new Institution("DIP");
        InstitutionXref xref = XrefUtils.createIdentityXref(dip, "MI:0465", qual, psiMi);
        dip.addXref(xref);

        Institution mint = new Institution("MINT");
        InstitutionXref mintXref = XrefUtils.createIdentityXref(dip, "MI:0471", qual, psiMi);
        mint.addXref(mintXref);
        
        getDataContext().getDaoFactory().getInstitutionDao().saveOrUpdate(dip);
        getDataContext().getDaoFactory().getInstitutionDao().saveOrUpdate(mint);
        getDataContext().commitTransaction();
         */


        getDataContext().beginTransaction();
        System.out.println("Institutions: "+ getDataContext().getDaoFactory()
                .getInstitutionDao().getShortLabelsLike("%"));
         getDataContext().commitTransaction();


        File repoDir = new File(System.getProperty("java.io.tmpdir"), "myRepo-all2/");
        Repository repo = ImexRepositoryContext.openRepository(repoDir.toString());

        ImexImporter importer = new ImexImporter(repo);
        ImportReport imexReport = importer.importNewAndFailed();

        repo.close();
                  
        IntactContext.getCurrentInstance().close();

        System.out.println(imexReport);
        

        //printStats();

        SanityReport report = SanityChecker.executeSanityCheck();
        System.out.println("Sanity check: "+report.getSanityResult().size() + " issues");

        StringWriter w = new StringWriter();
        ReportWriter writer = new SimpleReportWriter(w);
        writer.write(report);

        System.out.println(w.toString());
         
    }

    protected static void printStats() throws Exception {
        getDataContext().beginTransaction();

        System.out.println("\nDatabase counts:\n");
        System.out.println("\tPublications: "+getDataContext().getDaoFactory().getPublicationDao().countAll());
        System.out.println("\tExperiments: "+getDataContext().getDaoFactory().getExperimentDao().countAll());
        System.out.println("\tInteractions: "+getDataContext().getDaoFactory().getInteractionDao().countAll());
        System.out.println("\tComponents: "+getDataContext().getDaoFactory().getComponentDao().countAll());
        System.out.println("\tProteins: "+getDataContext().getDaoFactory().getProteinDao().countAll());
        System.out.println("\tFeatures: "+getDataContext().getDaoFactory().getFeatureDao().countAll());
        /*
        List<Object[]> l = getDataContext().getDaoFactory().getBaseDao().getSession().createQuery("select i.owner, count(i.owner) from InteractionImpl i group by i.owner").list();

        System.out.println("\nInteractions by Institution:\n");

        for (Object[] o : l) {
            System.out.println(o[0]+": "+o[1]);
        }
         */

        getDataContext().commitTransaction();
    }

    protected static DataContext getDataContext() {
        return IntactContext.getCurrentInstance().getDataContext();
    }

}