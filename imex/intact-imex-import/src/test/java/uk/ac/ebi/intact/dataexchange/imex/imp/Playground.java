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

import uk.ac.ebi.intact.config.impl.SmallCvPrimer;
import uk.ac.ebi.intact.context.DataContext;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.ImexRepositoryContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.Repository;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.InstitutionXref;
import uk.ac.ebi.intact.model.meta.ImexImport;
import uk.ac.ebi.intact.model.meta.ImexImportPublication;
import uk.ac.ebi.intact.model.util.XrefUtils;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.util.SchemaUtils;

import java.io.File;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class Playground {

    public static void main(String[] args) throws Exception{

        IntactContext.initStandaloneContext(new File(Playground.class.getResource("/postgres-hibernate.cfg.xml").getFile()));
        
        getDataContext().beginTransaction();

        SmallCvPrimer primer = new SmallCvPrimer(getDataContext().getDaoFactory());
        primer.createCVs();

        getDataContext().commitTransaction();

        CvXrefQualifier qual = getDataContext().getDaoFactory().getCvObjectDao(CvXrefQualifier.class).getByPsiMiRef(CvXrefQualifier.IDENTITY_MI_REF);
        CvDatabase psiMi = getDataContext().getDaoFactory().getCvObjectDao(CvDatabase.class).getByPsiMiRef(CvDatabase.PSI_MI_MI_REF);

        Institution intact = new Institution("INTACT");
        InstitutionXref intactXref = XrefUtils.createIdentityXref(intact, CvDatabase.INTACT_MI_REF, qual, psiMi);
        intact.addXref(intactXref);

        Institution dip = new Institution("DIP");
        InstitutionXref dipXref = XrefUtils.createIdentityXref(dip, CvDatabase.DIP_MI_REF, qual, psiMi);
        dip.addXref(dipXref);

        Institution mint = new Institution("MINT");
        InstitutionXref mintXref = XrefUtils.createIdentityXref(dip, CvDatabase.MINT_MI_REF, qual, psiMi);
        mint.addXref(mintXref);

        PersisterHelper.saveOrUpdate(intact, dip, mint);

        System.out.println("Institutions: "+ getDataContext().getDaoFactory()
                .getInstitutionDao().getShortLabelsLike("%"));


        File repoDir = new File(System.getProperty("java.io.tmpdir"), "myRepo-all/");
        Repository repo = ImexRepositoryContext.openRepository(repoDir.toString());

        ImexImporter importer = new ImexImporter(repo);
        ImexImport imexImport = importer.importNew();

        repo.close();



        printStats();
         /*
        SanityReport report = SanityChecker.executeSanityCheck();
        System.out.println("Sanity check: "+report.getSanityResult().size() + " issues");

        StringWriter w = new StringWriter();
        ReportWriter writer = new SimpleReportWriter(w);
        writer.write(report);

        System.out.println(w.toString());      */
         
    }

    protected static void printStats() throws Exception {
        System.out.println("\nDatabase counts:\n");
        System.out.println("\tPublications: "+getDataContext().getDaoFactory().getPublicationDao().countAll());
        System.out.println("\tExperiments: "+getDataContext().getDaoFactory().getExperimentDao().countAll());
        System.out.println("\tInteractions: "+getDataContext().getDaoFactory().getInteractionDao().countAll());
        System.out.println("\tComponents: "+getDataContext().getDaoFactory().getComponentDao().countAll());
        System.out.println("\tProteins: "+getDataContext().getDaoFactory().getProteinDao().countAll());
        System.out.println("\tFeatures: "+getDataContext().getDaoFactory().getFeatureDao().countAll());

        System.out.println("\nIMEx publications:");
        System.out.println("-------------------");

        for (ImexImport ii : getDataContext().getDaoFactory().getImexImportDao().getAll()) {
            System.out.println("Import: "+ii.getCreated()+" ("+ii.getActivationType()+") - Total="+ii.getCountTotal()+", Failed="+ii.getCountFailed()+", Not found="+ii.getCountNotFound());
        }
        System.out.println("");

        for (ImexImportPublication iip : getDataContext().getDaoFactory().getImexImportPublicationDao().getAll()) {
            System.out.println("\t"+iip.getPmid()+"\t"+iip.getProvider().getShortLabel()+"\t"+iip.getReleaseDate()+"\t"+iip.getOriginalFilename()+"\t"+iip.getMessage());
        }
    }

    protected static DataContext getDataContext() {
        return IntactContext.getCurrentInstance().getDataContext();
    }

}