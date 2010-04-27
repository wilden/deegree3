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
package org.deegree.tools.coverage;

import static org.deegree.tools.CommandUtils.getFloatOption;
import static org.deegree.tools.CommandUtils.getIntOption;
import static org.deegree.tools.coverage.RasterCommandUtils.getInterpolationType;
import static org.deegree.tools.coverage.RasterCommandUtils.parseBBOX;

import java.io.File;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.tools.CommandUtils;
import org.deegree.tools.annotations.Tool;
import org.deegree.tools.coverage.rtb.RasterTreeBuilder;

/**
 * This is a commandline interface for the raster tree builder.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
@Tool("Builds a raster tree from a given set of rasters.")
public class RTBClient {

    private static final String OPT_BBOX = "bbox";

    private static final String OPT_INPUT_FILE_EXT = "input_file_ext";

    private static final String OPT_NUM_THREADS = "num_threads";

    private static final String OPT_RECURSIVE = "recursive";

    private static final String OPT_OUT_FORMAT = "out_format";

    private static final String OPT_BACKGROUND = "background";

    private static final String OPT_INTERPOLATION = "interpolation";

    private static final String OPT_ORIGIN_LOCATION = "originlocation";

    private static final String OPT_RES = "res";

    private static final String OPT_NUM_LEVELS = "num_levels";

    private static final String OPT_FORCE_SIZE = "force_size";

    private static final String OPT_TILE_SIZE = "tile_size";

    private static final String OPT_OUT_DIR = "out_dir";

    private static final String OPT_S_SRS = "s_srs";

    private static final String OPT_T_SRS = "t_srs";

    private static final String DEFAULT_OUTPUT_FORMAT = "tiff";

    private static final int DEFAULT_TILE_SIZE = 800;

    /**
     * @param args
     * @throws UnknownCRSException
     * @throws TransformationException
     * @throws IllegalArgumentException
     */
    public static void main( String[] args )
                            throws UnknownCRSException, IllegalArgumentException, TransformationException {
        CommandLineParser parser = new PosixParser();

        Options options = initOptions();

        // for the moment, using the CLI API there is no way to respond to a help argument; see
        // https://issues.apache.org/jira/browse/CLI-179
        if ( args.length == 0 || ( args.length > 0 && ( args[0].contains( "help" ) || args[0].contains( "?" ) ) ) ) {
            printHelp( options );
        }

        try {
            CommandLine line = parser.parse( options, args );

            RasterTreeBuilder rtb = initRTB( line );
            setAdditionalOptions( rtb, line );
            startRTB( rtb, line );

        } catch ( ParseException exp ) {
            System.err.println( "ERROR: Invalid command line: " + exp.getMessage() );
        }
        System.exit( 0 );
    }

    private static RasterTreeBuilder initRTB( CommandLine line )
                            throws ParseException {
        String srcSRSValue = line.getOptionValue( OPT_S_SRS );
        String dstSRSValue = line.getOptionValue( OPT_T_SRS, srcSRSValue );
        CRS srcSRS = new CRS( srcSRSValue );
        CRS dstSRS = new CRS( dstSRSValue );

        File outDir = new File( line.getOptionValue( OPT_OUT_DIR ) );

        InterpolationType interpolationType = getInterpolationType( line.getOptionValue( OPT_INTERPOLATION ) );

        OriginLocation location = getLocation( line.getOptionValue( line.getOptionValue( OPT_ORIGIN_LOCATION ) ) );

        int maxTileSize = getIntOption( line, OPT_TILE_SIZE, DEFAULT_TILE_SIZE );

        return new RasterTreeBuilder( srcSRS, dstSRS, outDir, maxTileSize, interpolationType, location );
    }

    /**
     * @param optionValue
     * @return
     */
    private static OriginLocation getLocation( String optionValue ) {
        OriginLocation result = OriginLocation.CENTER;
        if ( "outer".equalsIgnoreCase( optionValue ) ) {
            result = OriginLocation.OUTER;
        }

        return result;
    }

    private static void setAdditionalOptions( RasterTreeBuilder rtb, CommandLine line )
                            throws ParseException {
        float baseResolution = getFloatOption( line, OPT_RES, Float.NaN );
        rtb.setBaseResolution( baseResolution );

        String outputFormat = line.getOptionValue( OPT_OUT_FORMAT, DEFAULT_OUTPUT_FORMAT );
        rtb.setOutputFormat( outputFormat );

        if ( line.hasOption( OPT_FORCE_SIZE ) ) {
            rtb.setForceTileSize( true );
        }

        if ( line.hasOption( OPT_BBOX ) ) {
            Envelope envelope = parseBBOX( line.getOptionValue( OPT_BBOX ) );
            rtb.setBaseEnvelope( envelope );
        }

        if ( line.hasOption( OPT_BACKGROUND ) ) {
            rtb.setBackgroundValue( RasterCommandUtils.parseBackgroundValue( OPT_BACKGROUND ) );
        }

        rtb.setNumThreads( getIntOption( line, OPT_NUM_THREADS, 4 ) );
    }

    private static void startRTB( RasterTreeBuilder rtb, CommandLine line )
                            throws ParseException, TransformationException, IllegalArgumentException,
                            UnknownCRSException {
        if ( line.getArgs().length == 0 ) {
            throw new ParseException( "no input file(s) given" );
        }
        String fileExt = line.getOptionValue( OPT_INPUT_FILE_EXT, "" );
        List<File> files = RasterCommandUtils.getAllFiles( line.getArgs(), line.hasOption( OPT_RECURSIVE ), fileExt );
        int numOfLevels = getIntOption( line, OPT_NUM_LEVELS, -1 );
        rtb.buildRasterTree( files, numOfLevels );
    }

    private static Options initOptions() {
        Options options = new Options();
        Option option = new Option( OPT_T_SRS, "the srs of the target raster (defaults to the source srs)" );
        option.setArgs( 1 );
        option.setArgName( "epsg code" );
        options.addOption( option );

        option = new Option( OPT_S_SRS, "the srs of the source raster" );
        option.setRequired( true );
        option.setArgs( 1 );
        option.setArgName( "epsg code" );
        options.addOption( option );

        option = new Option( OPT_OUT_DIR, "the output directory for the raster tree" );
        option.setRequired( true );
        option.setArgs( 1 );
        option.setArgName( "dir" );
        options.addOption( option );

        option = new Option( OPT_TILE_SIZE, "the max tile size in pixel (defaults to " + DEFAULT_TILE_SIZE
                                            + "). the actual tile size is calculated to reduce 'black' borders"
                                            + " (see force_size)" );
        option.setArgs( 1 );
        option.setArgName( "size" );
        options.addOption( option );

        option = new Option( OPT_FORCE_SIZE, "use the given tile_size as it is, do not calculate the optimal tile size" );
        options.addOption( option );

        option = new Option( OPT_BBOX, "the target bbox" );
        option.setArgs( 1 );
        option.setArgName( "x0,y0,x1,y1" );
        options.addOption( option );

        option = new Option( OPT_NUM_LEVELS,
                             "the number of raster levels. when omitted, generate levels until a level contains one tile." );
        option.setArgs( 1 );
        option.setArgName( "levels" );
        options.addOption( option );

        option = new Option( OPT_RES, "the target resolution for the first level in units/px." );
        option.setArgs( 1 );
        option.setLongOpt( "base_resolution" );
        option.setArgName( "units/px" );
        options.addOption( option );

        option = new Option( OPT_INTERPOLATION, "the raster interpolation (nn: nearest neighbour, bl: bilinear" );
        option.setArgs( 1 );
        option.setArgName( "nn|bl" );
        options.addOption( option );

        option = new Option( OPT_BACKGROUND, "background value. hex value for color raster (eg. 0xca42fe or 0xc4f)"
                                             + ", float value for others." );
        option.setArgs( 1 );
        option.setArgName( "hex|float" );
        options.addOption( option );

        option = new Option( OPT_OUT_FORMAT, "the output format (defaults to " + DEFAULT_OUTPUT_FORMAT + ")" );
        option.setArgs( 1 );
        option.setArgName( "tiff|jpg|png|..." );
        options.addOption( option );

        option = new Option( OPT_INPUT_FILE_EXT, "the extension of the input files " );
        option.setArgs( 1 );
        option.setArgName( "tiff|jpg|png|..." );
        options.addOption( option );

        option = new Option( OPT_RECURSIVE, "recurse into all directories" );
        options.addOption( option );

        option = new Option( OPT_NUM_THREADS, "the number of threads used." );
        option.setArgs( 1 );
        option.setArgName( "threads" );
        options.addOption( option );

        option = new Option( "orig", OPT_ORIGIN_LOCATION, true,
                             "the location of the origin on the upper left pixel (default = center)" );
        option.setArgs( 1 );
        option.setArgName( "center|outer" );
        options.addOption( option );

        options.addOption( "?", "help", false, "print (this) usage information" );

        return options;
    }

    private static void printHelp( Options options ) {
        CommandUtils.printHelp( options, "RTBClient", null, "file/dir [file/dir(s)]" );
    }
}
