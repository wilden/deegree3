//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.protocol.wps.input.type;

import java.net.URI;
import java.net.URL;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.protocol.wps.describeprocess.ValueWithRef;
import org.deegree.services.jaxb.wps.Range;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class LiteralInputType extends InputType {

    private ValueWithRef<String> dataType;

    private ValueWithRef<String> defaultUom;

    private ValueWithRef<String>[] supportedUoms;

    private String[] allowedValues;

    /**
     * Triple for (minValue, maxValue, spacing). Both minValue and maxValue are included in the interval. When the
     * interval is continuous spacing is set to: 1 (for integers), 0 (for floats)
     */
    private Range[] range;

    private boolean anyValue;

    private ValueWithRef<URI> reference;

    public LiteralInputType( CodeType id, LanguageString inputTitle, LanguageString inputAbstract, String minOccurs,
                             String maxOccurs, ValueWithRef<String> dataType, ValueWithRef<String> defaultUom,
                             ValueWithRef[] supportedUoms, String[] allowedValues, Range[] range, boolean anyValue,
                             ValueWithRef<URI> reference ) {
        super (id, inputTitle, inputAbstract, minOccurs, maxOccurs);
        this.dataType = dataType;
        this.defaultUom = defaultUom;
        this.supportedUoms = supportedUoms;
        this.allowedValues = allowedValues;
        this.range = range;
        this.anyValue = anyValue;
        this.reference = reference;
    }

    /**
     * 
     * @return a string array with the concrete values the input can take
     */
    public String[] getAllowedValues() {
        return allowedValues;
    }

    /**
     * 
     * @return an array of {@link Range} instances, each describing the interval in which the input values can be.
     */
    public Range[] getRanges() {
        return range;
    }

    /**
     * Returns a {@link ValueWithRef} instance (that encapsulates a String and an {@link URL}), as data type for the
     * literal input.
     * 
     * @return the data type of the literal input
     */
    public ValueWithRef<String> getDataType() {
        return dataType;
    }

    /**
     * Returns a {@link ValueWithRef} instance (that encapsulates a String and an {@link URL}), as default
     * Unit-of-measure for the literal input.
     * 
     * @return default Unit-of-measure for the literal input
     */
    public ValueWithRef<String> getDefaultUom() {
        return defaultUom;
    }

    /**
     * Returns an array of {@link ValueWithRef} instances (that encapsulates a String and an {@link URL}), as default
     * Unit-of-measure for the literal input.
     * 
     * @return an array of supported Unit-of-measure instance for the literal input
     */
    public ValueWithRef<String>[] getSupportedUoms() {
        return supportedUoms;
    }

    /**
     * Returns whether any value is accepted as input or not.
     * 
     * @return true, if any value is accepted as input. False otherwise.
     */
    public boolean isAnyValue() {
        return anyValue;
    }

}
