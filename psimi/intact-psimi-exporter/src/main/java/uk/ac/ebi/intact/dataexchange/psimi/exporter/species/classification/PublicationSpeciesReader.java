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
package uk.ac.ebi.intact.dataexchange.psimi.exporter.species.classification;

import org.springframework.batch.item.database.JdbcCursorItemReader;

/**
 * reader of the publications, ordered by the species of the interactors, the taxid of the species,
 * the shortlabel of the publication and the total number of interactions
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 */
public class PublicationSpeciesReader extends JdbcCursorItemReader<PublicationSpeciesUnit> {

    public PublicationSpeciesReader() {
        super();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String query = "select distinct pub.pubid, pub.year, pub.species, pub.taxid, pub.number_interactions" +
                "  from (select" +
                "    p.shortlabel as pubid," +
                "    case when pub_year.description is null then EXTRACT (YEAR FROM p.created ) else cast(pub_year.description as numeric) end as year," +
                "    b.shortlabel as species," +
                "    b.taxid as taxid," +
                "    v.count_interaction as number_interactions" +
                "    from ia_publication p"+
                "    join ia_experiment e on p.ac = e.publication_ac"+
                "    join ia_int2exp ie on ie.experiment_ac = e.ac"+
                "    join ia_component c on ie.interaction_ac = c.interaction_ac"+
                "    join ia_interactor i on i.ac = c.interactor_ac"+
                "    join ia_biosource b on b.ac = i.biosource_ac"+
                "    join (" +
                "      select count(ie2.interaction_ac) as count_interaction, p2.ac as publication_ac"+
                "      from ia_int2exp ie2, ia_experiment e2, ia_publication p2"+
                "      where e2.ac = ie2.experiment_ac and e2.publication_ac = p2.ac"+
                "      group by p2.ac" +
                "    ) v on v.publication_ac = p.ac" +
                "    left join (" +
                "      select p2a.publication_ac, a.description" +
                "      from ia_pub2annot p2a" +
                "      join ia_annotation a on p2a.annotation_ac = a.ac" +
                "      join ia_controlledvocab t on a.topic_ac = t.ac" +
                "      where t.identifier = 'MI:0886'" +
                "      and a.description ~ '[0-9]{4}'" +
                "    ) pub_year on p.ac = pub_year.publication_ac"+
                "    where p.shortLabel != '14681455'" +
                "  ) pub"+
                "  group by b.shortlabel, p.shortlabel, b.taxid, p.created, v.count_interaction"+
                "  order by b.shortlabel, p.shortlabel, v.count_interaction DESC";

        setSql(query);
        setRowMapper(new PublicationSpeciesRowMapper());

        super.afterPropertiesSet();
    }
}
