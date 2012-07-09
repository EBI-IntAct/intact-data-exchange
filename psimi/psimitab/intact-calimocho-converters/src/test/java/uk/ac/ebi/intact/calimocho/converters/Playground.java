package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.io.IllegalFieldException;
import org.hupo.psi.calimocho.io.IllegalRowException;
import org.hupo.psi.calimocho.model.Row;
import org.hupo.psi.calimocho.tab.io.DefaultRowWriter;
import org.hupo.psi.calimocho.tab.io.IllegalColumnException;
import org.hupo.psi.calimocho.tab.io.RowWriter;
import org.hupo.psi.calimocho.tab.util.MitabDocumentDefinitionFactory;
import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.psimitab.converters.expansion.NotExpandableInteractionException;
import uk.ac.ebi.intact.psimitab.converters.expansion.SpokeWithoutBaitExpansion;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/05/12</pre>
 */

public class Playground {

    public static void main( String[] args ) throws IOException {
        String database = "enzrls";
        IntactContext.initContext(new String[]{"/META-INF/" + database + ".spring.xml"});

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        final DaoFactory daoFactory = dataContext.getDaoFactory();

        TransactionStatus status = dataContext.beginTransaction();

        String pubId = "22493164";
        String fileName = "/home/marine/Desktop/sampleFile1.txt";

        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName));

        RowWriter writer = new DefaultRowWriter(MitabDocumentDefinitionFactory.mitab27());
        InteractionConverter interactionConverter = new InteractionConverter(new SpokeWithoutBaitExpansion(), "spoke expansion", "MI:1060");

        Publication pub = daoFactory.getPublicationDao().getByPubmedId(pubId);

        if (pub != null){
            for (Experiment exp : pub.getExperiments()){
                for (Interaction inter : exp.getInteractions()){
                    try {
                        List<Row> rows = interactionConverter.intactToCalimocho(inter);

                        for (Row row : rows){
                            try {
                                fileWriter.write(writer.writeLine(row));
                            } catch (IllegalRowException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (IllegalColumnException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (IllegalFieldException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    } catch (NotExpandableInteractionException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }

        fileWriter.close();

        dataContext.commitTransaction(status);
    }
}
