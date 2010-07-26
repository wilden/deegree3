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
package org.deegree.protocol.wps;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.protocol.wps.execute.ExecuteResponse;
import org.deegree.protocol.wps.execute.datatypes.BinaryDataType;
import org.deegree.protocol.wps.execute.datatypes.BoundingBoxDataType;
import org.deegree.protocol.wps.execute.datatypes.LiteralDataType;
import org.deegree.protocol.wps.execute.datatypes.XMLDataType;
import org.deegree.protocol.wps.execute.input.ExecuteInput;
import org.deegree.protocol.wps.execute.output.OutputDefinition;
import org.deegree.protocol.wps.execute.output.ResponseFormat;
import org.deegree.services.controller.ows.OWSException;

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
public class ProcessExecution {

    private Process process;

    private List<ExecuteInput> inputs;

    private List<OutputDefinition> outputDefs;

    private ResponseFormat responseFormat;

    private boolean rawOutput;

    ProcessExecution( Process process ) {
        this.process = process;
        inputs = new ArrayList<ExecuteInput>();
        outputDefs = new ArrayList<OutputDefinition>();
    }

    /**
     * Add literal input data under the specified id.
     * 
     * @param id
     *            input id of the parameter
     * @param codeSpace
     *            codespace of the id, may be null
     * @param value
     *            value of the literal input
     * @param type
     *            data type in which the value should be considered
     * @param uom
     *            unit of measure of the value
     */
    public void addLiteralInput( String id, String codeSpace, String value, String type, String uom ) {
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), new LiteralDataType( value, type, uom ) ) );
    }

    /**
     * Add Bounding box input data under the specified id.
     * 
     * @param id
     *            input id of the parameter
     * @param codeSpace
     *            codespace of the id, may be null
     * @param coordinates
     *            {@link double} array of coordinates: x0, y0, x1, x2, etc.
     * @param crs
     *            coordinates system of the bbox, as string, may be null
     * @param dim
     *            dimension of the bbox, e.g. 2 for plane coordinates
     */
    public void addBBoxInput( String id, String codeSpace, double[] coordinates, String crs, int dim ) {
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), new BoundingBoxDataType( coordinates, crs, dim ) ) );
    }

    /**
     * Add XML input data under the specified id as URL.
     * 
     * @param id
     *            input id of the parameter
     * @param codeSpace
     *            codespace of the id, may be null
     * @param url
     *            {@link URL} to the xml data
     * @param mimeType
     *            mime type of the xml data, may be null
     * @param encoding
     *            encoding of the xml data, may be null
     * @param schema
     *            schema of the xml fragment, may be null
     */
    public void addXMLInput( String id, String codeSpace, URL url, String mimeType, String encoding, String schema ) {
        XMLDataType xmlData = new XMLDataType( url, false, mimeType, encoding, schema );
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), xmlData ) );
    }

    /**
     * Add XML input data under the specified id as XML stream reader.
     * 
     * @param id
     *            input id of the parameter
     * @param codeSpace
     *            codespace of the id, may be null
     * @param reader
     *            {@link XMLStreamReader} to the xml data
     * @param mimeType
     *            mime type of the xml data, may be null
     * @param encoding
     *            encoding of the xml data, may be null
     * @param schema
     *            schema of the xml fragment, may be null
     */
    public void addXMLInput( String id, String codeSpace, XMLStreamReader reader, String mimeType, String encoding,
                             String schema ) {
        XMLDataType xmlDataType = new XMLDataType( reader, mimeType, encoding, schema );
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), xmlDataType ) );
    }

    /**
     * Add binary input data under the specified id as URL.
     * 
     * @param id
     *            input id of the parameter
     * @param codeSpace
     *            codespace of the id, may be null
     * @param url
     *            {@link URL} to the binary data
     * @param mimeType
     *            mime type of the binary data, may be null
     * @param encoding
     *            encoding of the binary data, may be null
     */
    public void addBinaryInput( String id, String codeSpace, URL url, String mimeType, String encoding ) {
        BinaryDataType binaryData = new BinaryDataType( url, false, mimeType, encoding );
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), binaryData ) );
    }

    /**
     * Add binary input data under the specified id as input stream.
     * 
     * @param id
     *            input id of the parameter
     * @param codeSpace
     *            codespace of the id, may be null
     * @param inputStream
     *            input stream to the binary data
     * @param mimeType
     *            mime type of the binary data, may be null
     * @param encoding
     *            encoding of the binary data, may be null
     */
    public void addBinaryInput( String id, String codeSpace, InputStream inputStream, String mimeType, String encoding ) {
        BinaryDataType binaryData = new BinaryDataType( inputStream, mimeType, encoding );
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), binaryData ) );
    }

    /**
     * Set format of the identified output as an XML response document. Use this method or
     * {@link #setRawOutput(String, String, String, String, String) } to specify the format in which the output should be
     * presented.
     * 
     * @param outputId
     *            id of the output parameter
     * @param codeSpace
     *            codespace of the id, may be null
     * @param uom
     *            unit of measure, in case it is a Literal Output, otherwise null
     * @param asRef
     *            return output as an URL, boolean
     * @param mimeType
     *            mimeType of the data, may be null
     * @param encoding
     *            encoding of data, may be null
     * @param schema
     */
    public void setRequestedOutput( String outputId, String codeSpace, String uom, boolean asRef, String mimeType,
                                    String encoding, String schema ) {
        outputDefs.add( new OutputDefinition( new CodeType( outputId ), uom, asRef, mimeType, encoding, schema ) );
    }

    /**
     * Set format of the identified output as raw data. Use this method or
     * {@link #setRequestedOutput(String, String, String, boolean, String, String, String)} to specify the format in
     * which the output should be presented.
     * 
     * @param outputId
     *            id of the output parameter
     * @param codeSpace
     *            codespace of the id, may be null
     * @param mimeType
     *            mimeType of the data, may be null
     * @param encoding
     *            encoding of data, may be null
     * @param schema
     *            schema of data, in case it is an XML document
     * @throws Exception
     */
    public void setRawOutput( String outputId, String codeSpace, String mimeType, String encoding, String schema )
                            throws Exception {
        outputDefs.add( new OutputDefinition( new CodeType( outputId ), null, false, mimeType, encoding, schema ) );
        rawOutput = true;
        if ( outputDefs.size() > 1 ) {
            throw new Exception( "A raw response can be delivered only for one output parameter." );
        }
    }

    /**
     * @param updateStatus
     */
    public void startAsync( boolean updateStatus ) {

    }

    /**
     * Perform the execute request synchronously.
     * 
     * @return {@link ExecuteResponse} instance that provides access to the output data.
     * @throws OWSException
     */
    public ExecuteResponse start()
                            throws OWSException {
        responseFormat = new ResponseFormat( rawOutput, false, false, false, outputDefs );
        ExecuteResponse response = process.execute( inputs, responseFormat );
        return response;
    }
}
