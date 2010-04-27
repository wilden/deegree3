//$HeadURL$
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
package org.deegree.tools.coverage.rtb;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.RasterTransformer;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.container.MemoryTileContainer;
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;

/**
 * This class builds a raster tree for input files.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RasterTreeBuilder {

    private File dstDir;

    private boolean overwriteExistingFiles = true;

    private CRS srcSRS, dstSRS;

    private Envelope dstEnv;

    private int tileSize, maxTileSize;

    private InterpolationType interpolation;

    private float baseResolution = Float.NaN;

    private byte[] backgroundValue;

    private Envelope baseEnvelope = null;

    private boolean forceTileSize = false;

    private int numThreads = 4;

    private String outputFormat = "tiff";

    private OriginLocation originlocation;

    /**
     * Creates a new RasterTreeBuilder. See the additional <code>set*()</code>-methods for further options.
     * 
     * @param srcSRS
     *            the SRS of the source raster
     * @param dstSRS
     *            the target SRS of the raster tree
     * @param dstDir
     *            the output directory of the raster tree
     * @param maxTileSize
     *            the max tile size
     * @param interpolation
     *            the raster interpolation
     * @param originlocation
     *            world location of the upper left pixel.
     */
    public RasterTreeBuilder( CRS srcSRS, CRS dstSRS, File dstDir, int maxTileSize, InterpolationType interpolation,
                              OriginLocation originlocation ) {
        super();
        this.srcSRS = srcSRS;
        this.dstSRS = dstSRS;
        this.dstDir = dstDir;
        this.maxTileSize = maxTileSize;
        this.interpolation = interpolation;
        this.setOriginlocation( originlocation );
    }

    /**
     * Prints a warning that this is not a command line tool.
     * 
     * @param args
     */
    public static void main( String[] args ) {
        System.err.println( "Please use the RTBClient for a command line client!" );
        System.exit( 1 );
    }

    /**
     * Set if existing tiles should be replaced (default is true).
     * 
     * @param overwrite
     */
    public void setOverwriteExistingFiles( boolean overwrite ) {
        this.overwriteExistingFiles = overwrite;
    }

    /**
     * Set the envelope of the first level.
     * 
     * @param env
     */
    public void setBaseEnvelope( Envelope env ) {
        this.baseEnvelope = env;
    }

    /**
     * Set the target resolution of the the first level.
     * 
     * @param baseResolution
     *            resolution in units/px
     */
    public void setBaseResolution( float baseResolution ) {
        this.baseResolution = baseResolution;
    }

    /**
     * Set if the maxTileSize should be forced to be the actual size.
     * 
     * @param force
     */
    public void setForceTileSize( boolean force ) {
        this.forceTileSize = force;
    }

    /**
     * Sets the background value.
     * 
     * @param backgroundValue
     */
    public void setBackgroundValue( byte[] backgroundValue ) {
        this.backgroundValue = backgroundValue;
    }

    /**
     * Sets the format for the output raster files.
     * 
     * @param outputFormat
     */
    public void setOutputFormat( String outputFormat ) {
        this.outputFormat = outputFormat;
    }

    /**
     * @param num
     *            the number of threads to use in parallel for loading and processing
     */
    public void setNumThreads( int num ) {
        this.numThreads = num;
    }

    /**
     * Builds a raster tree for the input files-
     * 
     * @param files
     *            array with filenames
     * @param numOfLevels
     *            number of raster levels, use -1 for automatic calculation of the number
     * @throws TransformationException
     * @throws UnknownCRSException
     * @throws IllegalArgumentException
     */
    public void buildRasterTree( List<File> files, int numOfLevels )
                            throws TransformationException, IllegalArgumentException, UnknownCRSException {
        RasterDataContainerFactory.setDefaultLoadingPolicy( RasterDataContainerFactory.LoadingPolicy.CACHED );
        AbstractRaster srcRaster = loadBaseLevel( files );

        if ( baseEnvelope != null ) {
            dstEnv = baseEnvelope;
        } else {
            GeometryTransformer dstTransformer = new GeometryTransformer( dstSRS.getWrappedCRS() );
            dstEnv = (Envelope) dstTransformer.transform( srcRaster.getEnvelope(),
                                                          srcRaster.getCoordinateSystem().getWrappedCRS() );
        }

        if ( forceTileSize ) {
            tileSize = maxTileSize;
        } else {
            tileSize = TileGrid.calculateOptimalTileSize( srcRaster.getColumns(), srcRaster.getRows(), dstEnv,
                                                          maxTileSize );
        }
        if ( Float.isNaN( baseResolution ) ) {
            baseResolution = (float) TileGrid.calculateBaseResolution( srcRaster.getColumns(), srcRaster.getRows(),
                                                                       dstEnv, tileSize );
        }

        System.out.println( "using " + tileSize + " as tile size." );
        System.out.println( "using " + baseResolution + " as base resolution." );

        if ( numOfLevels > 0 ) { // calculate n levels
            createRasterLevels( srcRaster, numOfLevels );
        } else {
            createRasterLevels( srcRaster );
        }
    }

    /**
     * Load the base level of the raster tree.
     */
    private AbstractRaster loadBaseLevel( List<File> files ) {
        final MemoryTileContainer tileContainer = new MemoryTileContainer();

        ExecutorService executor = Executors.newFixedThreadPool( numThreads );

        for ( final File file : files ) {
            Runnable command = new Runnable() {
                public void run() {
                    System.out.println( "loading... " + file );
                    try {
                        RasterIOOptions options = RasterIOOptions.forFile( file );
                        options.add( RasterIOOptions.GEO_ORIGIN_LOCATION, getOriginlocation().name() );
                        tileContainer.addTile( RasterFactory.loadRasterFromFile( file, options ) );
                    } catch ( IOException ex ) {
                        System.err.println( "ignoring " + file + ": " + ex.getMessage() );
                    }
                }
            };
            executor.execute( command );
        }
        shutdownExecutorAndWaitForFinish( executor );
        AbstractRaster result = new TiledRaster( tileContainer );
        result.setCoordinateSystem( srcSRS );
        return result;
    }

    /**
     * Create <code>n</code> raster levels.
     * 
     * @throws UnknownCRSException
     * @throws IllegalArgumentException
     */
    private void createRasterLevels( AbstractRaster srcRaster, int numOfLevels )
                            throws IllegalArgumentException, UnknownCRSException {
        TiledRaster dstRaster;
        AbstractRaster tmpRaster = srcRaster;
        for ( int i = 1; i <= numOfLevels; i++ ) {
            dstRaster = createRasterLevel( tmpRaster, i );
            tmpRaster = dstRaster;
        }
    }

    /**
     * Create raster levels until the last level consists of only one tile.
     * 
     * @throws UnknownCRSException
     * @throws IllegalArgumentException
     */
    private void createRasterLevels( AbstractRaster srcRaster )
                            throws IllegalArgumentException, UnknownCRSException {
        TiledRaster dstRaster;
        AbstractRaster tmpRaster = srcRaster;
        int i = 1;
        do { // calculate until the last level consist of one tile
            dstRaster = createRasterLevel( tmpRaster, i );
            tmpRaster = dstRaster;
            i++;
        } while ( tmpRaster.getColumns() > tileSize || tmpRaster.getRows() > tileSize );
    }

    /**
     * Create a single raster level.
     * 
     * @throws UnknownCRSException
     * @throws IllegalArgumentException
     */
    private TiledRaster createRasterLevel( AbstractRaster srcRaster, int level )
                            throws IllegalArgumentException, UnknownCRSException {
        System.out.println( "Generating level " + level );

        TileGrid grid = TileGrid.createTileGrid( dstEnv, tileSize, getResolutionForLevel( level ) );

        List<TileGrid.Tile> tiles = grid.createTileEnvelopes();

        return createRasterTiles( srcRaster, tiles, outputDir( level ), numThreads );
    }

    private float getResolutionForLevel( int level ) {
        // level 0: baseRes, level 1: baseRes * 2, level 2: baseRes * 4, ...
        return (float) ( baseResolution * Math.pow( 2, level - 1 ) );
    }

    private TiledRaster createRasterTiles( final AbstractRaster srcRaster, List<TileGrid.Tile> tiles, File outputDir,
                                           int numThreads )
                            throws IllegalArgumentException, UnknownCRSException {
        final MemoryTileContainer tileContainer = new MemoryTileContainer();
        final RasterTransformer transf = new RasterTransformer( dstSRS.getWrappedCRS() );
        transf.setBackgroundValue( backgroundValue );

        ExecutorService executor = Executors.newFixedThreadPool( numThreads );

        for ( final TileGrid.Tile tile : tiles ) {
            final String tileName = tile.x + "-" + tile.y;
            // System.out.println( "*** " + tileName + " " + tile.envelope );

            String relTileFilename = tileName + "." + outputFormat;
            final File absTileFilename = new File( outputDir, relTileFilename );

            if ( !overwriteExistingFiles && absTileFilename.exists() ) {
                System.out.println( "**skipped**" );
            } else {
                Runnable command = new Runnable() {
                    public void run() {
                        try {

                            String thread = Thread.currentThread().getName();
                            System.out.println( thread + " transforming... " + tileName );
                            AbstractRaster crop = transf.transform( srcRaster, tile.envelope, getTileSize(),
                                                                    getTileSize(), getInterpolation() );
                            System.out.println( thread + " saving... " + tileName );
                            RasterFactory.saveRasterToFile( crop, absTileFilename );
                            System.out.println( thread + " done... " + tileName );

                            crop = RasterFactory.loadRasterFromFile( absTileFilename );
                            tileContainer.addTile( crop );

                        } catch ( TransformationException e ) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch ( Exception e ) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                };
                executor.execute( command );
            }
        }
        shutdownExecutorAndWaitForFinish( executor );

        TiledRaster result = new TiledRaster( tileContainer );
        result.setCoordinateSystem( dstSRS );

        return result;
    }

    private void shutdownExecutorAndWaitForFinish( ExecutorService executor ) {
        executor.shutdown();
        try {
            executor.awaitTermination( 42, TimeUnit.DAYS );
        } catch ( InterruptedException e1 ) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private File outputDir( int level ) {
        File levelDir = new File( dstDir, String.valueOf( level ) );
        if ( !levelDir.isDirectory() && !levelDir.mkdirs() ) {
            System.err.println( "Could not create directory for output" );
            System.exit( 1 );
        }
        return levelDir;
    }

    /**
     * @return the tileSize
     */
    public int getTileSize() {
        return tileSize;
    }

    /**
     * @return the interpolation
     */
    public InterpolationType getInterpolation() {
        return interpolation;
    }

    /**
     * @param originlocation
     *            the originlocation to set
     */
    public void setOriginlocation( OriginLocation originlocation ) {
        this.originlocation = originlocation;
    }

    /**
     * @return the originlocation
     */
    public OriginLocation getOriginlocation() {
        return originlocation;
    }

}
