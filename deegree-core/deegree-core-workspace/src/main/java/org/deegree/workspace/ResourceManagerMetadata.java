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

/**
 * Describes a {@link ResourceManager} in the workspace. It holds the name (human readable), pathname (a kind of
 * abstract path name in the workspace, like 'datasource/feature') and the {@link ResourceProvider} class.
 * 
 * With this information, most of the {@link ResourceManager} implementations should become obsolete, and can be
 * replaced with the default implementation.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class ResourceManagerMetadata<T extends Resource> {

    private Class<? extends ResourceProvider<T>> providerClass;

    private String pathname;

    private String name;

    public ResourceManagerMetadata( Class<? extends ResourceProvider<T>> providerClass, String pathname, String name ) {
        this.providerClass = providerClass;
        this.pathname = pathname;
        this.name = name;
    }

    public Class<? extends ResourceProvider<T>> getProviderClass() {
        return providerClass;
    }

    public String getPathName() {
        return pathname;
    }

    public String getName() {
        return name;
    }

}
