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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.wps.describeprocess.BBoxDataDescription;
import org.deegree.protocol.wps.describeprocess.ComplexDataDescription;
import org.deegree.protocol.wps.describeprocess.InputDescription;
import org.deegree.protocol.wps.describeprocess.LiteralDataDescription;
import org.deegree.protocol.wps.describeprocess.output.BBoxOutput;
import org.deegree.protocol.wps.describeprocess.output.ComplexOutput;
import org.deegree.protocol.wps.describeprocess.output.LiteralOutput;
import org.deegree.protocol.wps.describeprocess.output.OutputDescription;
import org.deegree.protocol.wps.execute.ExecuteResponse;
import org.deegree.protocol.wps.execute.datatypes.BoundingBoxDataType;
import org.deegree.protocol.wps.execute.datatypes.LiteralDataType;
import org.deegree.protocol.wps.execute.datatypes.XMLDataType;
import org.deegree.services.controller.ows.OWSException;
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

    private static final File BINARY_INPUT = new File( WPSClientTest.class.getResource( "image.png" ).getPath() );

    // private static final String DEMO_SERVICE_URL =
    // "http://deegree3-testing.deegree.org/deegree-wps-demo-3.0-pre6/services?service=WPS&version=1.0.0&request=GetCapabilities";

    private static final String DEMO_SERVICE_URL = "http://giv-wps.uni-muenster.de:8080/wps/WebProcessingService?Request=GetCapabilities&Service=WPS";

    @Before
    public void init() {
        if ( DEMO_SERVICE_URL == null ) {
            throw new RuntimeException( "Cannot proceed: Service URL not provided." );
        }
    }

    @Test
    public void testGetInputDescription_1()
                            throws MalformedURLException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        Process p1 = wpsClient.getProcess( "Buffer", null );
        InputDescription literalInput = p1.getInputType( "BufferDistance", null );
        Assert.assertEquals( "1", literalInput.getMinOccurs() );
        Assert.assertEquals( "1", literalInput.getMaxOccurs() );
        LiteralDataDescription literalData = (LiteralDataDescription) literalInput.getData();
        Assert.assertEquals( "double", literalData.getDataType().getValue() );
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#double",
                             literalData.getDataType().getRef().toExternalForm() );
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
    public void testGetInputDescription_2()
                            throws MalformedURLException {
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
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#boolean",
                             literalOut.getDataType().getRef().toExternalForm() );
    }

    @Test
    public void testGetInputDescription_3()
                            throws MalformedURLException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        Process p2 = wpsClient.getProcess( "ParameterDemoProcess", null );

        InputDescription firstInput = p2.getInputType( "LiteralInput", null );
        LiteralDataDescription literalInput = (LiteralDataDescription) firstInput.getData();
        Assert.assertEquals( "integer", literalInput.getDataType().getValue() );
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#integer",
                             literalInput.getDataType().getRef().toExternalForm() );
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
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#integer",
                             literalData.getDataType().getRef().toExternalForm() );
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

    @Test
    public void testGetProcess()
                            throws MalformedURLException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        Process p1 = wpsClient.getProcess( "Buffer", null );
        Assert.assertNotNull( p1 );
        Process p2 = wpsClient.getProcess( "ParameterDemoProcess", null );
        Assert.assertNotNull( p2 );
    }

    @Test
    public void testExecute1()
                            throws Exception {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );

        Process proc = wpsClient.getProcess( "Centroid", null );
        ProcessExecution execution = proc.prepareExecution();
        execution.addXMLInput( "GMLInput", null, CURVE_FILE.toURI().toURL(), "text/xml", null, null );
        execution.setRequestedOutput( "Centroid", null, null, true, null, null, null );
        ExecuteResponse response = execution.start();

        XMLDataType data = (XMLDataType) response.getOutputs()[0].getDataType();
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
    public void testExecute2()
                            throws Exception {

        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );

        Process proc = wpsClient.getProcess( "Buffer", null );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "BufferDistance", null, "0.1", "double", "unity" );
        execution.addXMLInput( "GMLInput", null, CURVE_FILE.toURI().toURL(), "text/xml", null, null );
        execution.setRequestedOutput( "BufferedGeometry", null, null, false, null, null, null );
        ExecuteResponse response = execution.start();

        Assert.assertNotNull( response );
        // TODO test response
    }

    @Test
    public void testExecute3()
                            throws OWSException, IOException, XMLStreamException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        Process proc = wpsClient.getProcess( "ParameterDemoProcess", null );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "LiteralInput", null, "0", "integer", "seconds" );
        execution.addBBoxInput( "BBOXInput", null, new double[] { 0, 0, 90, 180 }, "EPSG:4326", 2 );
        execution.addXMLInput( "XMLInput", null, CURVE_FILE.toURI().toURL(), "text/xml", null, null );
        execution.addBinaryInput( "BinaryInput", null, BINARY_INPUT.toURI().toURL(), "image/png", "base64" );
        ExecuteResponse response = execution.start();

        LiteralDataType out1 = (LiteralDataType) response.getOutputs()[0].getDataType();
        Assert.assertEquals( "0", out1.getValue() );
        Assert.assertEquals( "integer", out1.getDataType() );
        Assert.assertEquals( "seconds", out1.getUom() );

        BoundingBoxDataType out2 = (BoundingBoxDataType) response.getOutputs()[1].getDataType();
        Assert.assertTrue( Arrays.equals( new double[] { 0.0, 0.0, 90.0, 180.0 }, out2.getCoordinates() ) );
        Assert.assertEquals( "EPSG:4326", out2.getCrs() );
        Assert.assertEquals( 2, out2.getDim() );
    }
}
