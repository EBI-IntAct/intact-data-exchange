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
        String query = "select distinct pub.pubid, pub.year, pub.dataset" +
                "  from (select" +
                "    p.shortlabel as pubid," +
                "    case when pub_year.description is null then EXTRACT (YEAR FROM p.created ) else cast(pub_year.description as numeric) end as year," +
                "    a.description as dataset" +
                "    from ia_publication p" +
                "    join ia_pub2annot pa on p.ac = pa.publication_ac" +
                "    join ia_annotation a on pa.annotation_ac = a.ac" +
                "    join ia_controlledvocab c on c.ac = a.topic_ac" +
                "    left join (" +
                "      select p2a.publication_ac, a.description" +
                "      from ia_pub2annot p2a" +
                "      join ia_annotation a on p2a.annotation_ac = a.ac" +
                "      join ia_controlledvocab t on a.topic_ac = t.ac" +
                "      where t.identifier = 'MI:0886'" +
                "      and a.description ~ '[0-9]{4}'" +
                "    ) pub_year on p.ac = pub_year.publication_ac" +
                "    where p.shortLabel != '14681455'" +
                "    and c.shortlabel='dataset'" +
                "  ) pub" +
                "  order by pub.dataset, pub.year, pub.pubid";

        setSql(query);
        setRowMapper(new PublicationDatasetRowMapper());

        super.afterPropertiesSet();
    }
}
