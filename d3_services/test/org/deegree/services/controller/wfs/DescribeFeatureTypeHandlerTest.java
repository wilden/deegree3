//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

package org.deegree.services.controller.wfs;

import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import junit.framework.Assert;

import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureType;
import org.deegree.services.controller.ows.OWSException;
import org.junit.Before;
import org.junit.Test;

/**
 * The <code></code> class TODO add class documentation here.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class DescribeFeatureTypeHandlerTest {

    private DescribeFeatureTypeHandler handler;

    @Before
    public void setUp() {
        handler = new DescribeFeatureTypeHandler(null);
    }

    @Test
    public void testWFS100() throws OWSException {
        DescribeFeatureType request = new DescribeFeatureType (VERSION_100, null, null, null);
        Assert.assertEquals( GML_2, handler.determineRequestedGMLVersion( request ));
    }

    @Test
    public void testWFS110() throws OWSException {
        DescribeFeatureType request = new DescribeFeatureType (VERSION_110, null, null, null);
        Assert.assertEquals( GML_31, handler.determineRequestedGMLVersion( request ));
    }

    @Test
    public void testWFS200() throws OWSException {
        DescribeFeatureType request = new DescribeFeatureType (VERSION_200, null, null, null);
        Assert.assertEquals( GML_32, handler.determineRequestedGMLVersion( request ));
    }

    @Test
    public void testXMLSCHEMA() throws OWSException {
        DescribeFeatureType request = new DescribeFeatureType (VERSION_200, null, "XMLSCHEMA", null);
        Assert.assertEquals( GML_2, handler.determineRequestedGMLVersion( request ));
    }

    @Test
    public void testSubType311() throws OWSException {
        DescribeFeatureType request = new DescribeFeatureType (VERSION_100, null, "text/xml; subtype=gml/3.1.1", null);
        Assert.assertEquals( GML_31, handler.determineRequestedGMLVersion( request ));
    }

    @Test
    public void testSubType2() throws OWSException {
        DescribeFeatureType request = new DescribeFeatureType (VERSION_110, null, "text/xml; subtype=gml/2", null);
        Assert.assertEquals( GML_2, handler.determineRequestedGMLVersion( request ));
    }

    @Test
    public void testSubType3() throws OWSException {
        DescribeFeatureType request = new DescribeFeatureType (VERSION_100, null, "text/xml; subtype=gml/3", null);
        Assert.assertEquals( GML_31, handler.determineRequestedGMLVersion( request ));
    }

    @Test
    public void testMissingSubType() throws OWSException {
        DescribeFeatureType request = new DescribeFeatureType (VERSION_100, null, "text/xml; subtype=", null);
        Assert.assertEquals( GML_2, handler.determineRequestedGMLVersion( request ));
    }
}
