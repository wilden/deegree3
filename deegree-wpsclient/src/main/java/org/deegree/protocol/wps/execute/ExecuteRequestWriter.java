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
package org.deegree.protocol.wps.execute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.wps.execute.datatypes.BinaryDataType;
import org.deegree.protocol.wps.execute.datatypes.BoundingBoxDataType;
import org.deegree.protocol.wps.execute.datatypes.ComplexAttributes;
import org.deegree.protocol.wps.execute.datatypes.DataType;
import org.deegree.protocol.wps.execute.datatypes.LiteralDataType;
import org.deegree.protocol.wps.execute.datatypes.XMLDataType;
import org.deegree.protocol.wps.execute.input.ExecuteInput;
import org.deegree.protocol.wps.execute.input.InputReference;
import org.deegree.protocol.wps.execute.output.DocumentOutputDefinition;
import org.deegree.protocol.wps.execute.output.OutputDefinition;
import org.deegree.protocol.wps.execute.output.ResponseDocumentFormat;
import org.deegree.protocol.wps.execute.output.ResponseFormat;
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
public class ExecuteRequestWriter {

    private static Logger LOG = LoggerFactory.getLogger( ExecuteRequestWriter.class );

    private static final String wpsPrefix = "wps";

    private static final String xsiPrefix = "xsi";

    private static final String owsPrefix = "ows";

    private static final String wpsNS = "http://www.opengis.net/wps/1.0.0";

    private static final String owsNS = "http://www.opengis.net/ows/1.1";

    private static final String xsiNS = "http://www.w3.org/1999/xlink";

    private static XMLStreamWriter writer;

    public ExecuteRequestWriter( XMLStreamWriter writer ) {
        this.writer = writer;
    }

    public void write100( ExecuteRequest request ) {
        try {
            writer.writeStartDocument();
            writer.writeStartElement( wpsPrefix, "Execute", wpsNS );

            writer.writeNamespace( wpsPrefix, wpsNS );
            writer.writeNamespace( owsPrefix, owsNS );
            writer.writeNamespace( xsiPrefix, xsiNS );

            writeHeader( request.getId() );
            writeInputs( request.getInputList() );
            writeOutputs( request.getOutputFormat() );

            writer.writeEndElement();
            writer.writeEndDocument();
        } catch ( XMLStreamException e ) {
            LOG.error( "Error while writing the Execute request. " + e.getMessage() );
            e.printStackTrace();
        }
    }

    /**
     * @param outputFormat
     * @throws XMLStreamException
     */
    private void writeOutputs( ResponseFormat outputFormat )
                            throws XMLStreamException {
        if ( outputFormat != null ) {
            writer.writeStartElement( wpsPrefix, "ResponseForm", wpsNS );

            if ( outputFormat instanceof ResponseDocumentFormat ) {
                writer.writeStartElement( wpsPrefix, "ResponseDocument", wpsNS );

                ResponseDocumentFormat docOutput = (ResponseDocumentFormat) outputFormat;
                if ( docOutput.getStoreExecuteResponse() != null ) {
                    writer.writeAttribute( "storeExecuteResponse", String.valueOf( docOutput.getStoreExecuteResponse() ) );
                }
                if ( docOutput.getLineage() != null ) {
                    writer.writeAttribute( "lineage", String.valueOf( docOutput.getLineage() ) );
                }
                if ( docOutput.getLineage() != null ) {
                    writer.writeAttribute( "status", String.valueOf( docOutput.getStatus() ) );
                }

                DocumentOutputDefinition[] output = docOutput.getDocOutputDefs();
                for ( int i = 0; i < output.length; i++ ) {
                    writer.writeStartElement( wpsPrefix, "Output", wpsNS );
                    if ( output[i].isReference() != null ) {
                        writer.writeAttribute( "asReference", String.valueOf( output[i].isReference() ) );
                    }
                    writeIdentifier( output[i].getId() );

                    if ( output[i].getUom() != null ) {
                        writer.writeAttribute( "uom", output[i].getUom() );
                    }
                    writeComplexAttributes( output[i].getComplexAttributes() );
                    writer.writeEndElement();
                }
                writer.writeEndElement();

            } else if ( outputFormat instanceof OutputDefinition ) {
                writer.writeStartElement( wpsPrefix, "RawDataOutput", wpsNS );

                OutputDefinition rawOutput = (OutputDefinition) outputFormat;
                writeIdentifier( rawOutput.getId() );

                if ( rawOutput.getUom() != null ) {
                    writer.writeAttribute( "uom", rawOutput.getUom() );
                }
                writeComplexAttributes( rawOutput.getComplexAttributes() );
                writer.writeEndElement();
            }
        }
    }

    /**
     * @param complexAttributes
     * @throws XMLStreamException
     */
    private void writeComplexAttributes( ComplexAttributes complexAttributes )
                            throws XMLStreamException {
        if ( complexAttributes.getEncoding() != null ) {
            writer.writeAttribute( "encoding", complexAttributes.getEncoding() );
        }
        if ( complexAttributes.getSchema() != null ) {
            writer.writeAttribute( "schema", complexAttributes.getSchema() );
        }
        if ( complexAttributes.getMimeType() != null ) {
            writer.writeAttribute( "mimeType", complexAttributes.getMimeType() );
        }
    }

    private void writeIdentifier( CodeType id )
                            throws XMLStreamException {
        writer.writeStartElement( wpsPrefix, "Identifier", wpsNS );
        writer.writeCharacters( id.getCodeSpace() + ":" + id.getCode() );
        writer.writeEndElement();
    }

    private void writeHeader( CodeType id )
                            throws XMLStreamException {
        writeIdentifier( id );
    }

    private void writeInputs( List<ExecuteInput> inputList )
                            throws XMLStreamException {
        if ( inputList != null && inputList.size() > 0 ) {
            writer.writeStartElement( wpsPrefix, "DataInputs", wpsNS );

            for ( int i = 0; i < inputList.size(); i++ ) {
                ExecuteInput dataInput = inputList.get( i );

                writer.writeStartElement( wpsPrefix, "Input", wpsNS );
                writer.writeStartElement( owsPrefix, "Identifier", owsNS );
                writer.writeCharacters( dataInput.getId().getCodeSpace() + ":" + dataInput.getId().getCode() );
                writer.writeEndElement();

                if ( dataInput.getInputReference() != null ) {
                    InputReference referenceInput = dataInput.getInputReference();
                    writer.writeStartElement( wpsPrefix, "Reference", wpsNS );
                    writer.writeAttribute( "href", referenceInput.getXlink() );
                    writer.writeEndElement();

                } else {

                    DataType dataType = dataInput.getDataType();
                    if ( dataType instanceof XMLDataType ) {
                        XMLDataType complexInput = (XMLDataType) dataType;
                        writer.writeStartElement( wpsPrefix, "ComplexData", wpsNS );

                        writeComplexAttributes( complexInput.getComplexAttributes() );

                        XMLAdapter.writeElement( writer, ( (XMLDataType) dataType ).getAsXMLStream() );

                        writer.writeEndElement();

                    } else if ( dataType instanceof BinaryDataType ) {
                        BinaryDataType binaryInput = (BinaryDataType) dataType;

                        try {
                            writer.writeStartElement( wpsPrefix, "ComplexData", wpsNS );
                            BufferedReader buff = new BufferedReader( new InputStreamReader( binaryInput.getData() ) );
                            char[] cbuf = new char[1024];
                            buff.read( cbuf );
                            writer.writeCharacters( cbuf, 0, 1023 );
                            writer.writeEndElement();
                        } catch ( IOException e ) {
                            LOG.error( e.getMessage() );
                        }

                    } else if ( dataType instanceof LiteralDataType ) {
                        LiteralDataType literalDataType = (LiteralDataType) dataType;
                        writer.writeStartElement( wpsPrefix, "LiteralData", wpsNS );

                        if ( literalDataType.getDataType() != null ) {
                            writer.writeAttribute( "dataType", literalDataType.getDataType() );
                        }
                        if ( literalDataType.getUom() != null ) {
                            writer.writeAttribute( "uom", literalDataType.getUom() );
                        }
                        writer.writeCharacters( literalDataType.getValue() );
                        writer.writeEndElement();

                    } else if ( dataType instanceof BoundingBoxDataType ) {
                        BoundingBoxDataType bboxInput = (BoundingBoxDataType) dataType;
                        writer.writeStartElement( wpsPrefix, "BoundingBoxData", wpsNS );

                        double[] coords = bboxInput.getCoordiantes();
                        int dim = 2;
                        if ( bboxInput.getDim() > 0 ) {
                            dim = bboxInput.getDim();
                            writer.writeAttribute( "dimensions", String.valueOf( dim ) );
                        }
                        if ( bboxInput.getCrs() != null ) {
                            writer.writeAttribute( "crs", bboxInput.getCrs() );
                        }
                        writer.writeStartElement( wpsPrefix, "LowerCorner", wpsNS );
                        for ( int j = 0; j < dim; j++ ) {
                            writer.writeCharacters( String.valueOf( coords[j] ) );
                        }
                        writer.writeStartElement( wpsPrefix, "UpperCorner", wpsNS );
                        for ( int j = dim; j < 2 * dim; j++ ) {
                            writer.writeCharacters( String.valueOf( coords[j] ) );
                        }
                        writer.writeEndElement();
                    }
                }
                writer.writeEndElement(); // Input
            }
            writer.writeEndElement(); // DataInputs
        }
    }
}
