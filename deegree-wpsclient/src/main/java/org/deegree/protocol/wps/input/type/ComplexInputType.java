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

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.protocol.wps.param.ComplexAttributes;

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
public class ComplexInputType extends InputType {

    // private int maximumFileSize;

    private ComplexAttributes defaultFormat;

    private ComplexAttributes[] supportedFormats;

    public ComplexInputType( CodeType id, LanguageString inputTitle, LanguageString inputAbstract, String minOccurs,
                             String maxOccurs, ComplexAttributes defaultFormat, ComplexAttributes[] supportedFormats ) {
        super( id, inputTitle, inputAbstract, minOccurs, maxOccurs );
        this.defaultFormat = defaultFormat;
        this.supportedFormats = supportedFormats;
    }

    /**
     * Returns a {@link ComplexAttributes} instance (that encapsulates encoding, mime type and schema) as default format
     * for this input.
     * 
     * @return the default format used for this input.
     */
    public ComplexAttributes getDefaultFormat() {
        return defaultFormat;
    }

    /**
     * Returns an array of {@link ComplexAttributes} instances (that encapsulates encoding, mime type and schema) as
     * supported formats for this input.
     * 
     * @return the supported format used for this input.
     */
    public ComplexAttributes[] getSupportedFormats() {
        return supportedFormats;
    }

}
