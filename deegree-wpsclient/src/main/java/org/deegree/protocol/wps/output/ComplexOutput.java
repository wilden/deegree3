//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/branches/aionita/deegree-wpsclient/src/main/java/org/deegree/protocol/wps/execute/output/ExecuteOutput.java $
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
package org.deegree.protocol.wps.output;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.io.StreamBufferStore;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.protocol.wps.param.ComplexAttributes;

/**
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 25694 $, $Date: 2010-08-04 21:45:33 +0200 (Mi, 04. Aug 2010) $
 */
public class ComplexOutput extends ExecutionOutput {

    private final ComplexAttributes complexAttribs;

    private final URL url;

    private final StreamBufferStore store;

    public ComplexOutput( CodeType id, URL url, String mimeType, String encoding, String schema ) {
        super( id );
        this.url = url;
        this.store = null;
        this.complexAttribs = new ComplexAttributes( mimeType, null, schema );
    }

    public ComplexOutput( CodeType id, StreamBufferStore store, String mimeType, String encoding, String schema ) {
        super( id );
        this.url = null;
        this.store = store;
        this.complexAttribs = new ComplexAttributes( mimeType, encoding, schema );
    }

    /**
     * Returns the web-accessible URL for the complex data (as provided by the process).
     * <p>
     * This method is only applicable if the parameter has been requested as reference.
     * </p>
     * 
     * @return the web-accessible URL, or <code>null</code> if the parameter has been returned in the response document
     *         or raw
     */
    public URL getWebAccessibleURL() {
        return url;
    }

    /**
     * Gets the xml data as {@link XMLStreamReader}. In case the xml stream begins with the START_DOCUMENT event, the
     * returning stream will have skipped it.
     * 
     * @return an {@link XMLStreamReader} instance, positioned after the START_DOCUMENT element
     * 
     * @throws IOException
     * @throws XMLStreamException
     */
    public XMLStreamReader getAsXMLStream()
                            throws XMLStreamException, IOException {
        XMLStreamReader xmlReader = null;
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        if ( url != null ) {
            xmlReader = inFactory.createXMLStreamReader( url.openStream() );
        } else {
            xmlReader = inFactory.createXMLStreamReader( store.getInputStream() );
        }
        StAXParsingHelper.skipStartDocument( xmlReader );
        return xmlReader;
    }

    public InputStream getAsBinaryStream()
                            throws IOException {
        InputStream is = null;
        if ( url != null ) {
            is = url.openStream();
        } else {
            is = store.getInputStream();
        }
        return is;
    }

    /**
     * 
     * @return complex attributes (encoding, mime type, schema) associated with the xml data type
     */
    public ComplexAttributes getComplexAttributes() {
        return complexAttribs;
    }
}