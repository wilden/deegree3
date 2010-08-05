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

import static org.deegree.services.controller.wps.ProcessExecution.ExecutionState.FAILED;
import static org.deegree.services.controller.wps.ProcessExecution.ExecutionState.SUCCEEDED;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.wps.describeprocess.input.BBoxInputType;
import org.deegree.protocol.wps.describeprocess.input.ComplexInputType;
import org.deegree.protocol.wps.describeprocess.input.InputType;
import org.deegree.protocol.wps.describeprocess.input.LiteralInputType;
import org.deegree.protocol.wps.describeprocess.output.BBoxOutputType;
import org.deegree.protocol.wps.describeprocess.output.ComplexOutputType;
import org.deegree.protocol.wps.describeprocess.output.LiteralOutputType;
import org.deegree.protocol.wps.describeprocess.output.OutputDescription;
import org.deegree.protocol.wps.execute.ExecutionOutputs;
import org.deegree.protocol.wps.execute.output.BBoxOutput;
import org.deegree.protocol.wps.execute.output.ComplexOutput;
import org.deegree.protocol.wps.execute.output.LiteralOutput;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.wps.ProcessExecution.ExecutionState;
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
        LiteralInputType literalInput = (LiteralInputType) p1.getInputType( "BufferDistance", null );
        Assert.assertEquals( "1", literalInput.getMinOccurs() );
        Assert.assertEquals( "1", literalInput.getMaxOccurs() );
        Assert.assertEquals( "double", literalInput.getDataType().getValue() );
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#double", literalInput.getDataType().getRef().toString() );
        Assert.assertEquals( "unity", literalInput.getDefaultUom().getValue() );
        Assert.assertEquals( "unity", literalInput.getSupportedUoms()[0].getValue() );
        Assert.assertEquals( true, literalInput.isAnyValue() );

        OutputDescription output = p1.getOutputType( "BufferedGeometry", null );
        ComplexOutputType complexData = (ComplexOutputType) output.getOutputData();
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
        InputType secondInput = p2.getInputType( "GMLInput2", null );
        Assert.assertEquals( "1", secondInput.getMinOccurs() );
        Assert.assertEquals( "1", secondInput.getMaxOccurs() );
        ComplexInputType complexData = (ComplexInputType) secondInput;
        Assert.assertEquals( "text/xml", complexData.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "UTF-8", complexData.getDefaultFormat().getEncoding() );
        Assert.assertEquals( "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd",
                             complexData.getDefaultFormat().getSchema() );
        Assert.assertEquals( "text/xml", complexData.getSupportedFormats()[0].getMimeType() );
        Assert.assertEquals( "UTF-8", complexData.getSupportedFormats()[0].getEncoding() );
        Assert.assertEquals( "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd",
                             complexData.getSupportedFormats()[0].getSchema() );

        OutputDescription output = p2.getOutputType( "Crosses", null );
        LiteralOutputType literalOut = (LiteralOutputType) output.getOutputData();
        Assert.assertEquals( "boolean", literalOut.getDataType().getValue() );
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#boolean", literalOut.getDataType().getRef().toString() );
    }

    @Test
    public void testProcessDescription_3()
                            throws OWSException, IOException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        Process p2 = wpsClient.getProcess( "ParameterDemoProcess", null );

        InputType firstInput = p2.getInputType( "LiteralInput", null );
        LiteralInputType literalInput = (LiteralInputType) firstInput;
        Assert.assertEquals( "integer", literalInput.getDataType().getValue() );
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#integer",
                             literalInput.getDataType().getRef().toString() );
        Assert.assertEquals( "seconds", literalInput.getDefaultUom().getValue() );
        Assert.assertEquals( "seconds", literalInput.getSupportedUoms()[0].getValue() );
        Assert.assertEquals( "minutes", literalInput.getSupportedUoms()[1].getValue() );

        InputType secondInput = p2.getInputType( "BBOXInput", null );
        Assert.assertEquals( "1", secondInput.getMinOccurs() );
        Assert.assertEquals( "1", secondInput.getMaxOccurs() );
        BBoxInputType bboxData = (BBoxInputType) secondInput;
        Assert.assertEquals( "EPSG:4326", bboxData.getDefaultCRS() );
        Assert.assertEquals( "EPSG:4326", bboxData.getSupportedCrs()[0] );

        InputType thirdInput = p2.getInputType( "XMLInput", null );
        ComplexInputType xmlData = (ComplexInputType) thirdInput;
        Assert.assertEquals( "text/xml", xmlData.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "text/xml", xmlData.getSupportedFormats()[0].getMimeType() );

        InputType fourthInput = p2.getInputType( "BinaryInput", null );
        ComplexInputType binaryData = (ComplexInputType) fourthInput;
        Assert.assertEquals( "image/png", binaryData.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "base64", binaryData.getDefaultFormat().getEncoding() );
        Assert.assertEquals( "image/png", binaryData.getSupportedFormats()[0].getMimeType() );
        Assert.assertEquals( "base64", binaryData.getSupportedFormats()[0].getEncoding() );

        OutputDescription firstOutput = p2.getOutputType( "LiteralOutput", null );
        Assert.assertEquals( "A literal output parameter", firstOutput.getTitle().getString() );
        LiteralOutputType literalData = (LiteralOutputType) firstOutput.getOutputData();
        Assert.assertEquals( "integer", literalData.getDataType().getValue() );
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#integer", literalData.getDataType().getRef().toString() );
        Assert.assertEquals( "seconds", literalData.getDefaultUom().getValue() );
        Assert.assertEquals( "seconds", literalData.getSupportedUoms()[0].getValue() );

        OutputDescription secondOutput = p2.getOutputType( "BBOXOutput", null );
        BBoxOutputType bboxOutput = (BBoxOutputType) secondOutput.getOutputData();
        Assert.assertEquals( "EPSG:4326", bboxOutput.getDefaultCrs() );
        Assert.assertEquals( "EPSG:4326", bboxOutput.getSupportedCrs()[0] );

        OutputDescription thirdOutput = p2.getOutputType( "XMLOutput", null );
        ComplexOutputType xmlOutput = (ComplexOutputType) thirdOutput.getOutputData();
        Assert.assertEquals( "text/xml", xmlOutput.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "text/xml", xmlOutput.getSupportedFormats()[0].getMimeType() );

        OutputDescription fourthOutput = p2.getOutputType( "BinaryOutput", null );
        ComplexOutputType binaryOutput = (ComplexOutputType) fourthOutput.getOutputData();
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
        ExecutionOutputs response = execution.execute();
        
        assertEquals( SUCCEEDED, execution.getState() );

        ComplexOutput output = (ComplexOutput) response.get( 0 );
        XMLStreamReader reader = output.getAsXMLStream();
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
        ExecutionOutputs response = execution.execute();
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
        ExecutionOutputs outputs = execution.execute();

        LiteralOutput out1 = (LiteralOutput) outputs.get( 0 );
        Assert.assertEquals( "0", out1.getValue() );
        Assert.assertEquals( "integer", out1.getDataType() );
        Assert.assertEquals( "seconds", out1.getUom() );

        BBoxOutput out2 = (BBoxOutput) outputs.get( 1 );
        Assert.assertTrue( Arrays.equals( new double[] { 0.0, 0.0 }, out2.getLower() ) );
        Assert.assertTrue( Arrays.equals( new double[] { 90.0, 180.0 }, out2.getUpper() ) );
        Assert.assertEquals( "EPSG:4326", out2.getCrs() );
        Assert.assertEquals( 2, out2.getDimension() );
    }

    @Test
    public void testExecuteAsync()
                            throws OWSException, IOException, XMLStreamException, InterruptedException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        Process proc = wpsClient.getProcess( "ParameterDemoProcess", null );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "LiteralInput", null, "5", "integer", "seconds" );
        execution.addBBoxInput( "BBOXInput", null, new double[] { 0, 0 }, new double[] { 90, 180 }, "EPSG:4326" );
        execution.addXMLInput( "XMLInput", null, CURVE_FILE.toURI().toURL(), "text/xml", null, null );
        execution.addBinaryInput( "BinaryInput", null, BINARY_INPUT.toURI().toURL(), "image/png", null );

        execution.executeAsync();
        Assert.assertNotSame( SUCCEEDED, execution.getState() );
        Assert.assertNotSame( FAILED, execution.getState() );

        ExecutionState state = null;
        while ( ( state = execution.getState() ) != SUCCEEDED ) {            
            System.out.println (execution.getPercentCompleted());
            Thread.sleep( 500 );
        }
        
        ExecutionOutputs outputs = execution.getOutputs();
        LiteralOutput out1 = (LiteralOutput) outputs.get( "LiteralOutput", null );
        Assert.assertEquals( "5", out1.getValue() );
        Assert.assertEquals( "integer", out1.getDataType() );
//        Assert.assertEquals( "seconds", out1.getUom() );

        BBoxOutput out2 = (BBoxOutput) outputs.get( "BBOXOutput", null );
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
