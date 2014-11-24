package uk.ac.ebi.intact.dataexchange.psimi.exporter.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A release unit contains a list of entities and a name for this unit
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>18/10/11</pre>
 */

public class ReleaseUnit<T extends Object> {

    private String unitName;
    private List<T> entities = new ArrayList<T>();

    public ReleaseUnit(String name){
        this.unitName = name;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setEntities(List<T> entities) {
        this.entities = entities;
    }

    public List<T> getEntities() {
        return entities;
    }

    @Override
    public String toString() {
        return unitName;
    }
}
