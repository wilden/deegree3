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
     * @param id
     * @param codeSpace
     * @param value
     * @param type
     * @param uom
     */
    public void addLiteralInput( String id, String codeSpace, String value, String type, String uom ) {
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), new LiteralDataType( value, type, uom ) ) );
    }

    public void addBBoxInput( String id, String codeSpace, double[] coordinates, String crs, int dim ) {
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), new BoundingBoxDataType( coordinates, crs, dim ) ) );
    }

    public void addXMLInput( String id, String codeSpace, URL url, String mimeType, String encoding, String schema ) {
        XMLDataType xmlData = new XMLDataType( url, false, mimeType, encoding, schema );
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), xmlData ) );
    }

    public void addXMLInput( String id, String codeSpace, XMLStreamReader reader, String mimeType, String encoding,
                             String schema ) {
        XMLDataType xmlDataType = new XMLDataType( reader, mimeType, encoding, schema );
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), xmlDataType ) );
    }

    public void addBinaryInput( String id, String codeSpace, URL url, String mimeType, String encoding ) {
        BinaryDataType binaryData = new BinaryDataType( url, false, mimeType, encoding );
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), binaryData ) );
    }

    public void addBinaryInput( String id, String codeSpace, InputStream inputStream, String mimeType, String encoding ) {
        BinaryDataType binaryData = new BinaryDataType( inputStream, mimeType, encoding );
        inputs.add( new ExecuteInput( new CodeType( id, codeSpace ), binaryData ) );
    }

    public void setRequestedOutput( String outputId, String codeSpace, String uom, boolean asRef, String mimeType,
                                    String encoding, String schema ) {
        outputDefs.add( new OutputDefinition( new CodeType( outputId ), uom, asRef, mimeType, encoding, schema ) );
    }

    /**
     * @param outputId
     * @param codeSpace
     * @param mimeType
     * @param encoding
     * @param schema
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

    public ExecuteResponse start()
                            throws OWSException {
        ResponseFormat responseFormat = new ResponseFormat( rawOutput, false, false, false, outputDefs );
        ExecuteResponse response = process.execute( inputs, responseFormat );
        return response;
    }
}
