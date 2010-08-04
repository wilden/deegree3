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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.wps.describeprocess.input.BBoxDataDescription;
import org.deegree.protocol.wps.describeprocess.input.ComplexDataDescription;
import org.deegree.protocol.wps.describeprocess.input.InputDescription;
import org.deegree.protocol.wps.describeprocess.input.LiteralDataDescription;
import org.deegree.protocol.wps.describeprocess.output.BBoxOutput;
import org.deegree.protocol.wps.describeprocess.output.ComplexOutput;
import org.deegree.protocol.wps.describeprocess.output.LiteralOutput;
import org.deegree.protocol.wps.describeprocess.output.OutputDescription;
import org.deegree.protocol.wps.execute.datatypes.BoundingBoxDataType;
import org.deegree.protocol.wps.execute.datatypes.LiteralDataType;
import org.deegree.protocol.wps.execute.datatypes.XMLDataType;
import org.deegree.protocol.wps.execute.output.ExecuteOutput;
import org.deegree.protocol.wps.execute.output.ExecuteOutputs;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.jaxb.main.ServiceIdentificationType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPSClientTest {

    private static final File CURVE_FILE = new File( WPSClientTest.class.getResource( "curve.xml" ).getPath() );

    private static final File POLYGON_FILE = new File( WPSClientTest.class.getResource( "Polygon.gml" ).getPath() );

    private static final File POINT_FILE = new File( WPSClientTest.class.getResource( "Point_coord.gml" ).getPath() );

    private static final File BINARY_INPUT = new File( WPSClientTest.class.getResource( "image.png" ).getPath() );

    private static final File BINARY_INPUT_TIFF = new File( WPSClientTest.class.getResource( "image.tiff" ).getPath() );

    private static final String DEMO_SERVICE_URL = "http://deegree3-testing.deegree.org/deegree-wps-demo-3.0-pre6/services?service=WPS&version=1.0.0&request=GetCapabilities";

    private static final String NORTH52_SERVICE_URL = "http://giv-wps.uni-muenster.de:8080/wps/WebProcessingService?Request=GetCapabilities&Service=WPS";

    @Before
    public void init() {
        if ( DEMO_SERVICE_URL == null ) {
            throw new RuntimeException( "Cannot proceed: Service URL not provided." );
        }
    }

    @Test
    public void testMetadata()
                            throws OWSException, IOException {
        URL serviceUrl = new URL( DEMO_SERVICE_URL );
        WPSClient client = new WPSClient( serviceUrl );
        Assert.assertNotNull( client );
        ServiceIdentificationType serviceId = client.getMetadata().getServiceIdentification();
        Assert.assertNotNull( serviceId );
        Assert.assertEquals( serviceId.getTitle().size(), 1 );
        Assert.assertEquals( serviceId.getTitle().get( 0 ), "deegree 3 WPS" );
        Assert.assertEquals( serviceId.getAbstract().size(), 1 );
        Assert.assertEquals( serviceId.getAbstract().get( 0 ), "deegree 3 WPS implementation" );
    }

    @Test
    public void testProcessDescription_1()
                            throws OWSException, IOException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        Process p1 = wpsClient.getProcess( "Buffer", null );
        InputDescription literalInput = p1.getInputType( "BufferDistance", null );
        Assert.assertEquals( "1", literalInput.getMinOccurs() );
        Assert.assertEquals( "1", literalInput.getMaxOccurs() );
        LiteralDataDescription literalData = (LiteralDataDescription) literalInput.getData();
        Assert.assertEquals( "double", literalData.getDataType().getValue() );
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#double", literalData.getDataType().getRef().toString() );
        Assert.assertEquals( "unity", literalData.getDefaultUom().getValue() );
        Assert.assertEquals( "unity", literalData.getSupportedUoms()[0].getValue() );
        Assert.assertEquals( true, literalData.isAnyValue() );

        OutputDescription output = p1.getOutputType( "BufferedGeometry", null );
        ComplexOutput complexData = (ComplexOutput) output.getOutputData();
        Assert.assertEquals( "UTF-8", complexData.getDefaultFormat().getEncoding() );
        Assert.assertEquals( "text/xml", complexData.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd",
                             complexData.getDefaultFormat().getSchema() );
        Assert.assertEquals( "UTF-8", complexData.getSupportedFormats()[0].getEncoding() );
        Assert.assertEquals( "text/xml", complexData.getSupportedFormats()[0].getMimeType() );
        Assert.assertEquals( "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd",
                             complexData.getSupportedFormats()[0].getSchema() );
    }

    @Test
    public void testProcessDescription_2()
                            throws OWSException, IOException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        Process p2 = wpsClient.getProcess( "Crosses", null );
        InputDescription secondInput = p2.getInputType( "GMLInput2", null );
        Assert.assertEquals( "1", secondInput.getMinOccurs() );
        Assert.assertEquals( "1", secondInput.getMaxOccurs() );
        ComplexDataDescription complexData = (ComplexDataDescription) secondInput.getData();
        Assert.assertEquals( "text/xml", complexData.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "UTF-8", complexData.getDefaultFormat().getEncoding() );
        Assert.assertEquals( "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd",
                             complexData.getDefaultFormat().getSchema() );
        Assert.assertEquals( "text/xml", complexData.getSupportedFormats()[0].getMimeType() );
        Assert.assertEquals( "UTF-8", complexData.getSupportedFormats()[0].getEncoding() );
        Assert.assertEquals( "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd",
                             complexData.getSupportedFormats()[0].getSchema() );

        OutputDescription output = p2.getOutputType( "Crosses", null );
        LiteralOutput literalOut = (LiteralOutput) output.getOutputData();
        Assert.assertEquals( "boolean", literalOut.getDataType().getValue() );
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#boolean", literalOut.getDataType().getRef().toString() );
    }

    @Test
    public void testProcessDescription_3()
                            throws OWSException, IOException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        Process p2 = wpsClient.getProcess( "ParameterDemoProcess", null );

        InputDescription firstInput = p2.getInputType( "LiteralInput", null );
        LiteralDataDescription literalInput = (LiteralDataDescription) firstInput.getData();
        Assert.assertEquals( "integer", literalInput.getDataType().getValue() );
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#integer",
                             literalInput.getDataType().getRef().toString() );
        Assert.assertEquals( "seconds", literalInput.getDefaultUom().getValue() );
        Assert.assertEquals( "seconds", literalInput.getSupportedUoms()[0].getValue() );
        Assert.assertEquals( "minutes", literalInput.getSupportedUoms()[1].getValue() );

        InputDescription secondInput = p2.getInputType( "BBOXInput", null );
        Assert.assertEquals( "1", secondInput.getMinOccurs() );
        Assert.assertEquals( "1", secondInput.getMaxOccurs() );
        BBoxDataDescription bboxData = (BBoxDataDescription) secondInput.getData();
        Assert.assertEquals( "EPSG:4326", bboxData.getDefaultCRS() );
        Assert.assertEquals( "EPSG:4326", bboxData.getSupportedCrs()[0] );

        InputDescription thirdInput = p2.getInputType( "XMLInput", null );
        ComplexDataDescription xmlData = (ComplexDataDescription) thirdInput.getData();
        Assert.assertEquals( "text/xml", xmlData.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "text/xml", xmlData.getSupportedFormats()[0].getMimeType() );

        InputDescription fourthInput = p2.getInputType( "BinaryInput", null );
        ComplexDataDescription binaryData = (ComplexDataDescription) fourthInput.getData();
        Assert.assertEquals( "image/png", binaryData.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "base64", binaryData.getDefaultFormat().getEncoding() );
        Assert.assertEquals( "image/png", binaryData.getSupportedFormats()[0].getMimeType() );
        Assert.assertEquals( "base64", binaryData.getSupportedFormats()[0].getEncoding() );

        OutputDescription firstOutput = p2.getOutputType( "LiteralOutput", null );
        Assert.assertEquals( "A literal output parameter", firstOutput.getTitle().getString() );
        LiteralOutput literalData = (LiteralOutput) firstOutput.getOutputData();
        Assert.assertEquals( "integer", literalData.getDataType().getValue() );
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#integer", literalData.getDataType().getRef().toString() );
        Assert.assertEquals( "seconds", literalData.getDefaultUom().getValue() );
        Assert.assertEquals( "seconds", literalData.getSupportedUoms()[0].getValue() );

        OutputDescription secondOutput = p2.getOutputType( "BBOXOutput", null );
        BBoxOutput bboxOutput = (BBoxOutput) secondOutput.getOutputData();
        Assert.assertEquals( "EPSG:4326", bboxOutput.getDefaultCrs() );
        Assert.assertEquals( "EPSG:4326", bboxOutput.getSupportedCrs()[0] );

        OutputDescription thirdOutput = p2.getOutputType( "XMLOutput", null );
        ComplexOutput xmlOutput = (ComplexOutput) thirdOutput.getOutputData();
        Assert.assertEquals( "text/xml", xmlOutput.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "text/xml", xmlOutput.getSupportedFormats()[0].getMimeType() );

        OutputDescription fourthOutput = p2.getOutputType( "BinaryOutput", null );
        ComplexOutput binaryOutput = (ComplexOutput) fourthOutput.getOutputData();
        Assert.assertEquals( "text/xml", xmlOutput.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "text/xml", xmlOutput.getSupportedFormats()[0].getMimeType() );
    }

    // @Test
    // public void testProcessDescription_4()
    // throws OWSException, IOException {
    // URL processUrl = new URL( NORTH52_SERVICE_URL );
    // WPSClient wpsClient = new WPSClient( processUrl );
    // Process proc = wpsClient.getProcess( "buffer", null );
    // InputDescription inputLayer = proc.getInputType( "LAYER", null );
    // ComplexDataDescription layerData = (ComplexDataDescription) inputLayer.getData();
    // Assert.assertEquals( "http://geoserver.itc.nl:8080/wps/schemas/gml/2.1.2/gmlpacket.xsd",
    // layerData.getSupportedFormats()[1].getSchema() );
    //
    // InputDescription inputField = proc.getInputType( "FIELD", null );
    // LiteralDataDescription fieldData = (LiteralDataDescription) inputField.getData();
    // Assert.assertEquals( "xs:int", fieldData.getDataType().getRef().toString() );
    // Assert.assertEquals( "0", fieldData.getRanges()[0].getMinimumValue() );
    // Assert.assertEquals( "+Infinity", fieldData.getRanges()[0].getMaximumValue() );
    //
    // InputDescription inputMethod = proc.getInputType( "METHOD", null );
    // Assert.assertEquals( "Distance", inputMethod.getAbstract().getString() );
    // LiteralDataDescription methodData = (LiteralDataDescription) inputMethod.getData();
    // Assert.assertEquals( "Fixed distance", methodData.getAllowedValues()[0] );
    // Assert.assertEquals( "Distance from table field", methodData.getAllowedValues()[1] );
    // }

    @Test
    public void testGetProcess()
                            throws OWSException, IOException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        Process p1 = wpsClient.getProcess( "Buffer", null );
        Assert.assertNotNull( p1 );
        Process p2 = wpsClient.getProcess( "ParameterDemoProcess", null );
        Assert.assertNotNull( p2 );
    }

    @Test
    public void testExecute_1()
                            throws Exception {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );

        Process proc = wpsClient.getProcess( "Centroid", null );
        ProcessExecution execution = proc.prepareExecution();
        execution.addXMLInput( "GMLInput", null, CURVE_FILE.toURI().toURL(), "text/xml", null, null );
        execution.addOutput( "Centroid", null, null, true, null, null, null );
        ExecuteOutputs response = execution.execute();

        XMLDataType data = (XMLDataType) response.getAll()[0].getDataType();
        XMLStreamReader reader = data.getAsXMLStream();
        XMLAdapter searchableXML = new XMLAdapter( reader );
        NamespaceContext nsContext = new NamespaceContext();
        nsContext.addNamespace( "wps", WPSConstants.WPS_100_NS );
        nsContext.addNamespace( "gml", "http://www.opengis.net/gml" );
        XPath xpath = new XPath( "/gml:Point/gml:pos/text()", nsContext );
        String pos = searchableXML.getRequiredNodeAsString( searchableXML.getRootElement(), xpath );

        String[] pair = pos.split( "\\s" );
        Assert.assertEquals( -0.31043, Double.parseDouble( pair[0] ), 1E-5 );
        Assert.assertEquals( 0.56749, Double.parseDouble( pair[1] ), 1E-5 );
    }

    @Test
    public void testExecute_2()
                            throws Exception {

        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );

        Process proc = wpsClient.getProcess( "Buffer", null );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "BufferDistance", null, "0.1", "double", "unity" );
        execution.addXMLInput( "GMLInput", null, CURVE_FILE.toURI().toURL(), "text/xml", null, null );
        execution.addOutput( "BufferedGeometry", null, null, false, null, null, null );
        ExecuteOutputs response = execution.execute();
        Assert.assertNotNull( response );
        // TODO test response
    }

    @Test
    public void testExecute_3()
                            throws OWSException, IOException, XMLStreamException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        Process proc = wpsClient.getProcess( "ParameterDemoProcess", null );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "LiteralInput", null, "0", "integer", "seconds" );
        execution.addBBoxInput( "BBOXInput", null, new double[] { 0, 0 }, new double[] { 90, 180 }, "EPSG:4326" );
        execution.addXMLInput( "XMLInput", null, CURVE_FILE.toURI().toURL(), "text/xml", null, null );
        execution.addBinaryInput( "BinaryInput", null, BINARY_INPUT.toURI().toURL(), "image/png", null );
        ExecuteOutputs outputs = execution.execute();

        ExecuteOutput bufferedGeometry = outputs.get( "BufferedGeometry", null );

        LiteralDataType out1 = (LiteralDataType) outputs.getAll()[0].getDataType();
        Assert.assertEquals( "0", out1.getValue() );
        Assert.assertEquals( "integer", out1.getDataType() );
        Assert.assertEquals( "seconds", out1.getUom() );

        BoundingBoxDataType out2 = (BoundingBoxDataType) outputs.getAll()[1].getDataType();
        Assert.assertTrue( Arrays.equals( new double[] { 0.0, 0.0 }, out2.getLower() ) );
        Assert.assertTrue( Arrays.equals( new double[] { 90.0, 180.0 }, out2.getUpper() ) );
        Assert.assertEquals( "EPSG:4326", out2.getCrs() );
        Assert.assertEquals( 2, out2.getDimension() );
    }
    // @Test
    // public void testExecute_4()
    // throws OWSException, IOException, XMLStreamException {
    // URL processUrl = new URL( NORTH52_SERVICE_URL );
    // WPSClient wpsClient = new WPSClient( processUrl );
    // Process proc = wpsClient.getProcess( "sortraster", null );
    //
    // ProcessExecution execution = proc.prepareExecution();
    // execution.addBinaryInput( "INPUT", null, BINARY_INPUT_TIFF.toURI().toURL(), "image/tiff", null );
    // ExecuteResponse response = execution.start();
    //
    // BinaryDataType out1 = (BinaryDataType) response.getOutputs()[0].getDataType();
    // InputStream inStream = out1.getDataStream();
    // FileOutputStream fileStream = new FileOutputStream( File.createTempFile( "north52", ".tiff" ) );
    // byte[] ar = new byte[1024];
    // int readFlag = -1;
    // while ( ( readFlag = inStream.read( ar ) ) != -1 ) {
    // fileStream.write( ar );
    // }
    // fileStream.close();
    // inStream.close();
    // }
    //
    // @Test
    // public void testExecute_5()
    // throws OWSException, IOException, XMLStreamException {
    // URL processUrl = new URL( NORTH52_SERVICE_URL );
    // WPSClient wpsClient = new WPSClient( processUrl );
    // Process proc = wpsClient.getProcess( "ripleysk", null );
    //
    // ProcessExecution execution = proc.prepareExecution();
    // execution.addXMLInput( "POINTS", null, POINT_FILE.toURI().toURL(), "text/xml", null, null );
    // execution.setRequestedOutput( "RESULT", null, null, false, null, null, null );
    // // execution.addXMLInput( "LAYER2", null, POINT_FILE.toURI().toURL(), "text/xml", null, null );
    // ExecuteResponse response = execution.start();
    //
    // response.getOutputs()[0].getDataType();
    // }

}
