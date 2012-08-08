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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.commons.jdbc.param;

import static org.deegree.commons.jdbc.param.DefaultJDBCParamsProvider.CONFIG_JAXB_PACKAGE;
import static org.deegree.commons.jdbc.param.DefaultJDBCParamsProvider.CONFIG_SCHEMA_URL;

import java.util.Collections;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.deegree.commons.jdbc.jaxb.JDBCConnection;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocator;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * <code>DefaultJDBCParamsMetadata</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class DefaultJDBCParamsMetadata implements ResourceMetadata<JDBCParams> {

    private ResourceLocator<JDBCParams> locator;

    private Workspace workspace;

    public DefaultJDBCParamsMetadata( ResourceLocator<JDBCParams> locator, Workspace workspace ) {
        this.locator = locator;
        this.workspace = workspace;
    }

    @Override
    public JDBCParams create()
                            throws ResourceInitException {
        try {
            JDBCConnection cfg = (JDBCConnection) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA_URL,
                                                                        locator.getConfiguration(), workspace );
            return new DefaultJDBCParams( cfg.getUrl(), cfg.getUser(), cfg.getPassword(),
                                          cfg.isReadOnly() == null ? false : cfg.isReadOnly() );
        } catch ( JAXBException e ) {
            throw new ResourceInitException( "Error parsing JDBC configuration '" + locator + "': "
                                             + e.getLocalizedMessage(), e );
        }
    }

    @Override
    public Set<ResourceIdentifier<? extends Resource>> getDependencies() {
        return Collections.emptySet();
    }

}
