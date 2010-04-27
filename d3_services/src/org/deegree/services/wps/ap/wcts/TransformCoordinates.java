//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.services.wps.ap.wcts;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.deegree.commons.utils.FileUtils;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;

/**
 * The <code>TransformCoordinates</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */


public class TransformCoordinates implements Processlet {

    final static String IP_INPUT_DATA_ID = "InputData";

    final static String IP_TEST_TRANSFORM_ID = "TestTransformation";

    final static String IP_TRANSFORM_ID = "Transformation";

    final static String IP_SCRS_ID = "SourceCRS";

    final static String IP_TCRS_ID = "TargetCRS";

    final static String OP_DATA_ID = "TransformedData";
    
    private final static String SOURCE_CRS_LOCATION;

    private final static String SOURCE_CRS_SCHEMA_LOCATION;

    private final static String TARGET_CRS_LOCATION;

    private final static String TARGET_CRS_SCHEMA_LOCATION;

    private final static String PROP_SCRS_REF = "SOURCE_CRS_REFERENCE_LOCATION";

    private final static String PROP_SCRS_REF_SCHEMA = "SOURCE_CRS_REF_SCHEMA_LOCATION";

    private final static String PROP_TCRS_REF = "TARGET_CRS_REFERENCE_LOCATION";

    private final static String PROP_TCRS_REF_SCHEMA = "TARGET_CRS_REF_SCHEMA_LOCATION";
 

    static {
        URL config = FileUtils.loadDeegreeConfiguration( TransformCoordinates.class,
                                                                           "wcts-configuration.properties" );
        String scrsRef = null;
        String scrsRefSchema = null;
        String tcrsRef = null;
        String tcrsRefSchema = null;
        if ( config != null ) {
            Properties props = new Properties();
            try {
                props.load( config.openStream() );
            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            scrsRef = props.getProperty( PROP_SCRS_REF );
            scrsRefSchema = props.getProperty( PROP_SCRS_REF_SCHEMA );
            tcrsRef = props.getProperty( PROP_TCRS_REF );
            tcrsRefSchema = props.getProperty( PROP_TCRS_REF_SCHEMA );
        }

        if ( scrsRef == null ) {
            // use deegree internal format as result set.
            SOURCE_CRS_LOCATION = null;
            SOURCE_CRS_SCHEMA_LOCATION = null;
            TARGET_CRS_LOCATION = null;
            TARGET_CRS_SCHEMA_LOCATION = null;
        } else {
            SOURCE_CRS_LOCATION = scrsRef;
            SOURCE_CRS_SCHEMA_LOCATION = scrsRefSchema;
            TARGET_CRS_LOCATION = tcrsRef;
            TARGET_CRS_SCHEMA_LOCATION = tcrsRefSchema;
        }
    }
    
    @Override
    public void destroy() {
        // destroy...
    }

    @Override
    public void init() {
        // load configuration of some sort
    }

    @Override
    public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info )
                            throws ProcessletException {
//        ProcessletInput parameter = in.getParameter( IP_INPUT_DATA );
    }

}
