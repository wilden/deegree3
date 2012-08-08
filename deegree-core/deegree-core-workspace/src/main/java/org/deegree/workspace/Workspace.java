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

package org.deegree.workspace;

import java.util.List;

/**
 * The deegree workspace workflow:
 * 
 * <ul>
 * <li>scan for {@link ResourceManager}s and {@link ResourceProvider}s on the classpath (also scan the modules directory
 * of the current workspace if available)</li>
 * <li>initial scan of the workspace resources</li>
 * <li>resource managers now have a list of {@link ResourceMetadata}s, with a {@link ResourceIdentifier} each, and a
 * list of {@link ResourceIdentifier}s identifying their dependencies</li>
 * <li>compute an order of initialization, compute which resources are not available (could not be found even in the
 * initial scan)</li>
 * <li>initialize all resources with satisfied dependencies in proper order</li>
 * </ul>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public interface Workspace {

    void startup();
    
    void shutdown();
    
    <T extends Resource> ResourceMetadata<T> getResourceMetadata( ResourceIdentifier<T> id );

    <T extends Resource> T getResource( ResourceIdentifier<T> id );

    ClassLoader getModuleClassLoader();

    <T extends Resource> List<ResourceLocator<T>> locateResources( ResourceManagerMetadata<T> metadata );

}
