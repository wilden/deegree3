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

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceLoader;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocator;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceManagerMetadata;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.ResourceState;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>DefaultResourceManager</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class DefaultResourceManager<T extends Resource> implements ResourceManager<T> {

    private static final Logger LOG = LoggerFactory.getLogger( DefaultResourceManager.class );

    protected HashMap<String, ResourceProvider<T>> nsToProvider = new HashMap<String, ResourceProvider<T>>();

    protected HashMap<ResourceIdentifier<T>, ResourceMetadata<T>> metadataMap = new HashMap<ResourceIdentifier<T>, ResourceMetadata<T>>();

    protected Workspace workspace;

    protected ResourceManagerMetadata<T> metadata;

    public DefaultResourceManager( ResourceManagerMetadata<T> metadata ) {
        this.metadata = metadata;
    }

    @Override
    public void scan( Workspace workspace )
                            throws ResourceInitException {
        this.workspace = workspace;
        LOG.debug( "-- Searching providers for interface {}.", metadata.getProviderClass().getName() );
        for ( ResourceProvider<T> provider : ServiceLoader.load( metadata.getProviderClass(),
                                                                 workspace.getModuleClassLoader() ) ) {
            LOG.debug( "Found resource provider {}.", provider.getClass().getName() );
            nsToProvider.put( provider.getConfigNamespace(), provider );
        }
        LOG.info( "Scanning for {} resources...", metadata.getName() );
        scanForResources();
    }

    protected void scanForResources()
                            throws ResourceInitException {
        List<ResourceLocator<T>> list = workspace.locateResources( metadata.getPathName() );
        if ( list.isEmpty() ) {
            LOG.info( "No {} resources found.", metadata.getName() );
            return;
        }

        LOG.info( "Found {} resources of type {}.", list.size(), metadata.getName() );
        LOG.info( "Analysing dependencies." );

        for ( ResourceLocator<T> loc : list ) {
            ResourceProvider<T> provider = nsToProvider.get( loc.getNamespace() );
            if ( provider == null ) {
                LOG.warn( "No resource provider for namespace {} available.", loc.getNamespace() );
            } else {
                LOG.info( "Found resource with id {}.", loc.getIdentifier().getId() );
                metadataMap.put( loc.getIdentifier(), provider.createMetadata( loc ) );
            }
        }

        LOG.info( "" );
    }

    @Override
    public void startup( Workspace workspace )
                            throws ResourceInitException {
        // TODO Auto-generated method stub

    }

    @Override
    public void shutdown() {
        nsToProvider.clear();
    }

    @Override
    public ResourceState<?>[] getStates() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceState<?> getState( String id ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceState<?> activate( String id ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceState<?> deactivate( String id ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceState<?> createResource( String id, InputStream config )
                            throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceState<?> deleteResource( String id ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T create( String id, URL configUrl )
                            throws ResourceInitException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T get( String id ) {
        // TODO Auto-generated method stub
        return null;
    }

}
