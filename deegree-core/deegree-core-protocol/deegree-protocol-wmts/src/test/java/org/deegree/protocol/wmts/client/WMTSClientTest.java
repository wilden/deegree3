//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.protocol.wmts.client;

import static junit.framework.Assert.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;

import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.ows.http.OwsHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@company.com">Your Name</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WMTSClientTest {

//    private WMTSClient scenario1;
//
//    @Before
//    public void setup()
//                            throws OWSExceptionReport, XMLStreamException, IOException {
//        scenario1 = createScenario1();
//    }
//
//    private WMTSClient createScenario1()
//                            throws OWSExceptionReport, XMLStreamException, IOException {
//        URL capaUrl = WMTSClientTest.class.getResource( "scenario1_capabilities.xml" );
//        OwsHttpClient mockedClient = Mockito.mock( OwsHttpClient.class );
//        Mockito.when(  )
//        return new WMTSClient( capaUrl, mockedClient );
//    }
//
//    /**
//     * Test method for
//     * {@link org.deegree.protocol.wmts.client.WMTSClient#getTile(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, int)}
//     * .
//     */
//    @Test
//    public void testGetTile()
//                            throws IOException, OWSExceptionReport, XMLStreamException {
//        GetTileResponse response = scenario1.getTile( "medford:hydro", "_null", "image/png", "EPSG:900913",
//                                                      "EPSG:900913:24", 6203400, 2660870 );
//        BufferedImage img = response.getAsImage();
//        assertNotNull( img );
//    }

    // /**
    // * Test method for
    // * {@link org.deegree.protocol.wmts.client.WMTSClient#getCapabilitiesAdapter(org.apache.axiom.om.OMElement,
    // java.lang.String)}
    // * .
    // */
    // public void testGetCapabilitiesAdapterOMElementString() {
    // fail( "Not yet implemented" );
    // }
}
