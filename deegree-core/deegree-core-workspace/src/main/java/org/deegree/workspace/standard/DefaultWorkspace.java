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

package org.deegree.workspace.standard;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocator;
import org.deegree.workspace.ResourceManagerMetadata;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>DefaultWorkspace</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class DefaultWorkspace implements Workspace {

    private static final Logger LOG = LoggerFactory.getLogger( DefaultWorkspace.class );

    private File directory;

    public DefaultWorkspace( File directory ) {
        this.directory = directory;
    }

    @Override
    public void startup(){
    }
    
    @Override
    public void shutdown(){
    }
    
    @Override
    public <T extends Resource> ResourceMetadata<T> getResourceMetadata( ResourceIdentifier<T> id ) {
        return null;
    }

    @Override
    public <T extends Resource> T getResource( ResourceIdentifier<T> id ) {
        return null;
    }

    @Override
    public ClassLoader getModuleClassLoader() {
        return null;
    }

    @Override
    public <T extends Resource> List<ResourceLocator<T>> locateResources( ResourceManagerMetadata<T> md ) {
        List<ResourceLocator<T>> list = new ArrayList<ResourceLocator<T>>();
        File[] fs = new File( directory, md.getPathName() ).listFiles();
        if ( fs == null ) {
            return list;
        }
        // TODO recursive?
        for ( File f : fs ) {
            try {
                ResourceIdentifier<T> id = new ResourceIdentifier<T>( md.getProviderClass(), f.getName() );
                list.add( new DefaultResourceLocator<T>( id, f.toURI().toURL() ) );
            } catch ( MalformedURLException e ) {
                LOG.error( "Resolving file to URL failed: {}", e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            } catch ( ResourceInitException e ) {
                LOG.warn( "Could not scan file {}: {}", f, e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }
        return list;
    }
}
