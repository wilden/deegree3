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
package org.deegree.protocol.wps.describeprocess;

import java.net.URL;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;

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
public class LiteralDataDescription implements DataDescription {

    private String dataType;

    private URL dataTypeRef;

    private String defaultUom;

    private String defaultUomRef;

    private Pair<String, String>[] supportedUoms;

    private String[] allowedValues;

    /**
     * Triple for (minValue, maxValue, spacing). Both minValue and maxValue are included in the interval. When the
     * interval is continuous spacing is set to: 1 (for integers), 0 (for floats)
     */
    private Triple<String, String, String>[] range;

    private boolean anyValue;

    private URL valueRef;

    private URL valueRefForm;

    public LiteralDataDescription( String dataType, URL dataTypeRef, String defaultUom, String defaultUomRef,
                                   Pair<String, String>[] supportedUoms, String[] allowedValues,
                                   Triple<String, String, String>[] range, boolean anyValue, URL reference,
                                   URL referenceForm ) {
        this.dataType = dataType;
        this.dataTypeRef = dataTypeRef;
        this.defaultUom = defaultUom;
        this.defaultUomRef = defaultUomRef;
        this.supportedUoms = supportedUoms;
        this.allowedValues = allowedValues;
        this.range = range;
        this.anyValue = anyValue;
        this.valueRef = reference;
        this.valueRefForm = referenceForm;
    }

    public boolean isAnyValue() {
        return anyValue;
    }

}
