//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.console.webservices;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.URL;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import lombok.Getter;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.console.Config;
import org.deegree.console.ConfigManager;
import org.deegree.console.WorkspaceBean;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@ManagedBean
@SessionScoped
public class WebServiceConfigManager {

    private static final Logger LOG = getLogger( ConfigManager.class );

    private static final URL METADATA_EXAMPLE_URL = WebServiceConfigManager.class.getResource( "/META-INF/schemas/metadata/3.0.0/example.xml" );

    private static final URL METADATA_SCHEMA_URL = WebServiceConfigManager.class.getResource( "/META-INF/schemas/metadata/3.0.0/metadata.xsd" );

    private static final URL MAIN_EXAMPLE_URL = WebServiceConfigManager.class.getResource( "/META-INF/schemas/controller/3.0.0/example.xml" );

    private static final URL MAIN_SCHEMA_URL = WebServiceConfigManager.class.getResource( "/META-INF/schemas/controller/3.0.0/controller.xsd" );

    @Getter
    private final Config metadataConfig;

    @Getter
    private final Config mainConfig;

    public WebServiceConfigManager() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        DeegreeWorkspace ws = ( (WorkspaceBean) ctx.getApplicationMap().get( "workspace" ) ).getActiveWorkspace();
        File wsRootDir = ws.getLocation();

        File metadataLocation = new File( wsRootDir, "services/metadata.xml" );
        metadataConfig = new Config( metadataLocation, METADATA_SCHEMA_URL, METADATA_EXAMPLE_URL,
                                     "/console/webservices/webservices" );

        File mainLocation = new File( wsRootDir, "services/main.xml" );
        mainConfig = new Config( mainLocation, MAIN_SCHEMA_URL, MAIN_EXAMPLE_URL, "/console/webservices/webservices" );
    }
}