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
package org.deegree.protocol.wps.describeprocess.output;

import org.deegree.protocol.wps.describeprocess.ValueWithRef;

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
public class LiteralOutputType implements OutputType {

    private ValueWithRef<String> dataType;

    private ValueWithRef<String> defaultUom;

    private ValueWithRef<String>[] supportedUoms;

    public LiteralOutputType( ValueWithRef<String> dataType, ValueWithRef<String> defaultUom,
                          ValueWithRef<String>[] supportedUoms ) {
        this.dataType = dataType;
        this.defaultUom = defaultUom;
        this.supportedUoms = supportedUoms;
    }

    /**
     * 
     * @return the data type as {@link ValueWithRef} for this literal
     */
    public ValueWithRef<String> getDataType() {
        return dataType;
    }

    /**
     * 
     * @return the default unit-of-measure used, as {@link ValueWithRef}
     */
    public ValueWithRef<String> getDefaultUom() {
        return defaultUom;
    }

    /**
     * 
     * @return the supported units-of-measure used, as an array of {@link ValueWithRef}
     */
    public ValueWithRef<String>[] getSupportedUoms() {
        return supportedUoms;
    }

}
