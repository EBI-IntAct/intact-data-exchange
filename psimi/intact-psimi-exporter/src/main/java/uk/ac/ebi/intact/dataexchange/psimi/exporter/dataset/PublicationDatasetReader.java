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
package uk.ac.ebi.intact.dataexchange.psimi.exporter.dataset;

import org.springframework.batch.item.database.JdbcCursorItemReader;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.species.classification.PublicationSpeciesUnit;

/**
 * reader of the publications, ordered by the species of the interactions, the taxid of the species,
 * the shortlabel of the publication and the total number of interactions
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 */
public class PublicationDatasetReader extends JdbcCursorItemReader<PublicationSpeciesUnit> {

    public PublicationDatasetReader() {
        super();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String query = "select distinct p.shortlabel as pubid, EXTRACT ( YEAR FROM p.created ) as year, a.description as dataset" +
                "        from ia_publication p" +
                "        join ia_pub2annot pa on p.ac = pa.publication_ac" +
                "        join ia_annotation a on pa.annotation_ac = a.ac" +
                "        join ia_controlledvocab c on c.ac = a.topic_ac" +
                "        where p.shortLabel != '14681455'" +
                "        and c.shortlabel='dataset'" +
                "        order by a.description, EXTRACT ( YEAR FROM p.created ), p.shortLabel";

        setSql(query);
        setRowMapper(new PublicationDatasetRowMapper());

        super.afterPropertiesSet();
    }
}
