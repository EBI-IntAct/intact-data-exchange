/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.psimitab.model;

import java.io.Serializable;
import java.util.Formatter;

/**
 * MITAB parameter
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */
public class Parameter implements Serializable {

    private String type;
    private double factor;
    private int base;
    private int exponent;
    private String value;
    private String unit;

    public Parameter(String type, String value, String unit) {
        this.type = type;
        this.unit = unit;
        this.base = 10;
        this.exponent = 0;

        this.value = value;

        // value using scientific notation
        try {
            this.factor = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            this.factor = Float.NaN;
        }
    }

    public Parameter(String type, double factor, int base, int exponent, String unit) {
        this.type = type;
        this.factor = factor;
        this.base = base;
        this.exponent = exponent;
        this.unit = unit;

        this.value = String.valueOf(factor);
        if (exponent > 0) {
            value = " x " + base + "^" + exponent;
        }
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    public double getFactor() {
        return factor;
    }

    public int getBase() {
        return base;
    }

    public int getExponent() {
        return exponent;
    }
}
