//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.commons.jdbc.param;

import java.net.URL;

import org.deegree.workspace.ResourceLocator;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * {@link JDBCParamsProvider} for {@link DefaultJDBCParams}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultJDBCParamsProvider implements JDBCParamsProvider {

    private static final String CONFIG_NAMESPACE = "http://www.deegree.org/jdbc";

    static final URL CONFIG_SCHEMA_URL = DefaultJDBCParamsProvider.class.getResource( "/META-INF/schemas/jdbc/3.0.0/jdbc.xsd" );

    static final String CONFIG_JAXB_PACKAGE = "org.deegree.commons.jdbc.jaxb";

    private Workspace workspace;

    @Override
    public void init( Workspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public String getConfigNamespace() {
        return CONFIG_NAMESPACE;
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA_URL;
    }

    @Override
    public ResourceMetadata<JDBCParams> createMetadata( ResourceLocator<JDBCParams> locator )
                            throws org.deegree.workspace.ResourceInitException {
        return new DefaultJDBCParamsMetadata( locator, workspace );
    }
}
