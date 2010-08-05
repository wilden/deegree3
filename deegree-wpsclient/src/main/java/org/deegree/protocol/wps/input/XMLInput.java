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
package org.deegree.protocol.wps.input;

import java.io.IOException;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.protocol.wps.describeprocess.ComplexAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class XMLInput extends ExecutionInput {

    private static Logger LOG = LoggerFactory.getLogger( XMLInput.class );

    private final ComplexAttributes complexAttribs;

    private URL url;

    private XMLStreamReader reader;

    private boolean isWebAccessible;

    public XMLInput( CodeType id, URL url, boolean isWebAcessible, String mimeType, String encoding, String schema ) {
        super( id );
        this.url = url;
        this.isWebAccessible = isWebAcessible;
        this.complexAttribs = new ComplexAttributes( mimeType, null, schema );
    }

    /**
     * 
     * @param reader
     *            cursor must point at a <code>START_ELEMENT</code> event, position afterwards is undefined
     * @param mimeType
     * @param encoding
     * @param schema
     */
    public XMLInput( CodeType id, XMLStreamReader reader, String mimeType, String encoding, String schema ) {
        super( id );
        if ( reader.getEventType() != XMLStreamConstants.START_ELEMENT ) {
            String msg = "The given XML stream does not point to a START_ELEMENT event.";
            throw new IllegalArgumentException( msg );
        }
        this.reader = reader;
        this.isWebAccessible = false;
        this.complexAttribs = new ComplexAttributes( mimeType, encoding, schema );
    }

    /**
     * Gets the xml data as {@link XMLStreamReader}. In case the xml stream begins with the START_DOCUMENT event, the
     * returning stream will have skipped it.
     * 
     * @return an {@link XMLStreamReader} instance, positioned after the START_DOCUMENT element
     */
    public XMLStreamReader getAsXMLStream() {
        try {
            if ( reader == null ) {
                XMLInputFactory inFactory = XMLInputFactory.newInstance();
                reader = inFactory.createXMLStreamReader( url.openStream() );
            }
            if ( reader.getEventType() == XMLStreamConstants.START_DOCUMENT ) {
                StAXParsingHelper.nextElement( reader );
            }
        } catch ( XMLStreamException e ) {
            // TODO
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO
            e.printStackTrace();
        }

        return reader;
    }

    /**
     * 
     * @return complex attributes (encoding, mime type, schema) associated with the xml data type
     */
    public ComplexAttributes getComplexAttributes() {
        return complexAttribs;
    }

    public URL getWebAccessibleURL() {
        return isWebAccessible ? url : null;
    }
}
