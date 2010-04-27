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

package org.deegree.tools.rendering;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;

import org.deegree.commons.utils.memory.MemoryAware;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.model.geometry.SimpleAccessGeometry;
import org.deegree.rendering.r3d.model.geometry.TexturedGeometry;
import org.deegree.rendering.r3d.opengl.display.OpenGLEventHandler;
import org.deegree.rendering.r3d.opengl.rendering.JOGLRenderable;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModel;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.tools.rendering.manager.buildings.importers.CityGMLImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>GLViewer</code> uses the jogl engine to render dataobjects.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GLViewer extends JFrame implements ActionListener, KeyListener {
    /**
     * 
     */
    private static final long serialVersionUID = 7698388852544865855L;

    private static Logger LOG = LoggerFactory.getLogger( GLViewer.class );

    private Preferences prefs;

    private final static String OPEN_KEY = "lastOpenLocation";

    private final static String LAST_EXTENSION = "lastFileExtension";

    private final static String WIN_TITLE = "Deegree 3D Object viewer: ";

    private List<CustomFileFilter> supportedOpenFilter = new ArrayList<CustomFileFilter>();

    /**
     * A panel showing some key stroke helps
     */
    JPanel helpLister;

    GLCanvas canvas = null;

    private OpenGLEventHandler openGLEventListener;

    /**
     * Creates a frame with the menus and the canvas3d set and tries to load the file from given location.
     * 
     * @param fileName
     *            to be loaded.
     */
    public GLViewer( String fileName ) {
        this( false );
        readFile( fileName );
    }

    /**
     * Creates a new frame with the menus and the canvas3d set.
     * 
     * @param testSphere
     *            true if a sphere should be displayed.
     */
    public GLViewer( boolean testSphere ) {
        super( WIN_TITLE );
        prefs = Preferences.userNodeForPackage( GLViewer.class );
        openGLEventListener = new OpenGLEventHandler( testSphere );

        setupGUI();

        // openFileChooser();

        setupOpenGL();
        ArrayList<String> extensions = new ArrayList<String>();

        extensions.add( "gml" );
        extensions.add( "xml" );
        supportedOpenFilter.add( new CustomFileFilter( extensions, "(*.gml, *.xml) GML or CityGML-Files" ) );

        extensions.clear();
        extensions.add( "shp" );
        supportedOpenFilter.add( new CustomFileFilter( extensions, "(*.shp) Esri ShapeFiles" ) );

        extensions.clear();
        extensions.add( "vrml" );
        extensions.add( "wrl" );
        supportedOpenFilter.add( new CustomFileFilter( extensions,
                                                       "(*.vrml, *.wrl) VRML97 - Virtual Reality Modelling Language" ) );

        pack();

    }

    private void addGeometries( RenderableQualityModel model, boolean remove ) {
        if ( remove ) {
            openGLEventListener.removeAllData();
        }
        Envelope env = new GeometryFactory().createEnvelope( new double[] { -3, -3, -3 }, new double[] { 3, 3, 3 },
                                                             null );
        WorldRenderableObject wro = new WorldRenderableObject( "Test", "bla", env, 1 );
        wro.setQualityLevel( 0, model );
        openGLEventListener.addDataObjectToScene( wro );

    }

    /**
     * GUI stuff
     */
    private void setupGUI() {
        // add listener for closing the frame/application
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        setVisible( true );
        setLayout( new BorderLayout() );
        setMinimumSize( new Dimension( 600, 600 ) );
        setPreferredSize( new Dimension( 600, 600 ) );

        // Adding the button panel
        JPanel totalPanel = new JPanel( new BorderLayout() );
        totalPanel.add( createButtons(), BorderLayout.NORTH );
        addKeyListener( this );
        helpLister = new JPanel( new GridBagLayout() );
        Border border = BorderFactory.createTitledBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ),
                                                          "Instant help" );
        helpLister.setBorder( border );
        GridBagConstraints gb = new GridBagConstraints();
        gb.ipadx = 10;
        gb.gridx = 0;
        gb.gridy = 0;
        JLabel tmp = new JLabel( "x: move postive X-axis" );
        helpLister.add( tmp, gb );

        gb.gridx++;
        tmp = new JLabel( "X: move negative X-axis" );
        helpLister.add( tmp, gb );

        gb.gridx = 0;
        gb.gridy++;
        tmp = new JLabel( "y: move positve Y-axis" );
        helpLister.add( tmp, gb );

        gb.gridx++;
        tmp = new JLabel( "Y: move negative Y-axis" );
        helpLister.add( tmp, gb );

        gb.gridy++;
        gb.gridx = 0;
        tmp = new JLabel( "z: move positve Z-axis" );
        helpLister.add( tmp, gb );
        gb.gridx++;

        tmp = new JLabel( "Z: move negative Z-axis" );
        helpLister.add( tmp, gb );
        helpLister.setVisible( false );

        totalPanel.add( helpLister, BorderLayout.SOUTH );
        getContentPane().add( totalPanel, BorderLayout.SOUTH );

    }

    private JFileChooser createFileChooser( List<CustomFileFilter> fileFilter ) {
        // Setting up the fileChooser.

        String lastLoc = prefs.get( OPEN_KEY, System.getProperty( "user.home" ) );

        File lastFile = new File( lastLoc );
        if ( !lastFile.exists() ) {
            lastFile = new File( System.getProperty( "user.home" ) );
        }
        JFileChooser fileChooser = new JFileChooser( lastFile );
        fileChooser.setMultiSelectionEnabled( false );
        if ( fileFilter != null && fileFilter.size() > 0 ) {
            // the *.* file fileter is off
            fileChooser.setAcceptAllFileFilterUsed( false );
            String lastExtension = prefs.get( LAST_EXTENSION, "*" );
            FileFilter selected = fileFilter.get( 0 );
            for ( CustomFileFilter filter : fileFilter ) {
                fileChooser.setFileFilter( filter );
                if ( filter.accepts( lastExtension ) ) {
                    selected = filter;
                }
            }

            fileChooser.setFileFilter( selected );
        }

        return fileChooser;
    }

    private void setupOpenGL() {
        GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered( true );
        caps.setHardwareAccelerated( true );
        // caps.setAlphaBits( 8 );
        // System.out.println( "Alpha: " + caps.getAlphaBits() );
        // System.out.println( "Alpha: " + caps.getAccumAlphaBits() );
        caps.setAlphaBits( 8 );
        caps.setAccumAlphaBits( 8 );
        // System.out.println( "Alpha: " + caps.getAlphaBits() );
        // System.out.println( "Alpha: " + caps.getAccumAlphaBits() );
        // AbstractGraphicsConfiguration config = GLDrawableFactory.getFactory().chooseGraphicsConfiguration( )
        canvas = new GLCanvas( caps );
        canvas.addGLEventListener( openGLEventListener );
        canvas.addKeyListener( this );
        canvas.addMouseListener( openGLEventListener.getTrackBall() );
        canvas.addMouseMotionListener( openGLEventListener.getTrackBall() );
        getContentPane().add( canvas, BorderLayout.CENTER );
    }

    private JPanel createButtons() {
        JPanel buttonPanel = new JPanel( new GridBagLayout() );
        GridBagConstraints gb = new GridBagConstraints();
        gb.gridx = 0;
        gb.gridy = 0;

        JRadioButton help = new JRadioButton( "Activate help" );
        help.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if ( helpLister.isVisible() ) {
                    helpLister.setVisible( false );
                    ( (JRadioButton) e.getSource() ).setText( "Activate help" );
                } else {
                    helpLister.setVisible( true );
                    ( (JRadioButton) e.getSource() ).setText( "De-Activate help" );

                }
            }
        } );
        buttonPanel.add( help, gb );

        gb.insets = new Insets( 10, 10, 10, 10 );
        gb.gridx++;
        JButton button = new JButton( "Open File" );
        button.setMnemonic( KeyEvent.VK_O );
        button.addActionListener( this );
        buttonPanel.add( button, gb );

        gb.gridx++;
        button = new JButton( "Export File" );
        button.setMnemonic( KeyEvent.VK_O );
        button.addActionListener( this );
        buttonPanel.add( button, gb );

        return buttonPanel;
    }

    private void readFile( String fileName ) {

        if ( fileName == null || "".equals( fileName.trim() ) ) {
            throw new InvalidParameterException( "the file name may not be null or empty" );
        }
        fileName = fileName.trim();

        final CityGMLImporter openFile = new CityGMLImporter( null, null, null, false );

        final JDialog dialog = new JDialog( this, "Loading", true );

        dialog.getContentPane().setLayout( new BorderLayout() );
        dialog.getContentPane().add(
                                     new JLabel( "<HTML>Loading file:<br>" + fileName + "<br>Please wait!</HTML>",
                                                 SwingConstants.CENTER ), BorderLayout.NORTH );
        final JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted( true );
        progressBar.setIndeterminate( false );
        dialog.getContentPane().add( progressBar, BorderLayout.CENTER );

        dialog.pack();
        dialog.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        dialog.setResizable( false );
        dialog.setLocationRelativeTo( this );

        final Thread openThread = new Thread() {
            /**
             * Opens the file in a separate thread.
             */
            @Override
            public void run() {
                // openFile.openFile( progressBar );
                if ( dialog.isDisplayable() ) {
                    dialog.setVisible( false );
                    dialog.dispose();
                }
            }
        };
        openThread.start();

        dialog.setVisible( true );
        WorldRenderableObject result = null;// openFile.getOpenedFile();
        //
        if ( result != null ) {
            openGLEventListener.addDataObjectToScene( result );
            File f = new File( fileName );
            setTitle( WIN_TITLE + f.getName() );
        } else {
            showExceptionDialog( "The file: " + fileName
                                 + " could not be read,\nSee error log for detailed information." );
        }

    }

    /**
     * Shows an export dialog to the user.
     */
    @SuppressWarnings("unchecked")
    private void doExport() {

        Export3DFile exportEvaluater = new Export3DFile( this );
        // find the scene graph to export
        // Enumeration<Node> en = rotationGroup.getAllChildren();
        if ( true ) {
            showExceptionDialog( "Could not get the scene to export." );
            return;
        }
        WorldRenderableObject toExport = null;

        // if ( toExport == null ) {
        // showExceptionDialog( "Could not get the scene to export." );
        // return;
        // }
        StringBuilder sb = exportEvaluater.exportBranchgroup( toExport );
        if ( sb.length() == 0 ) {
            showExceptionDialog( "Exporting failed, see error log for details." );
            return;
        }
        openGLEventListener.addDataObjectToScene( toExport );
        JFileChooser chooser = createFileChooser( null );
        int result = chooser.showSaveDialog( this );
        if ( JFileChooser.APPROVE_OPTION == result ) {
            File f = chooser.getSelectedFile();
            FileFilter ff = chooser.getFileFilter();
            if ( ff instanceof CustomFileFilter ) {
                prefs.put( LAST_EXTENSION, ( (CustomFileFilter) ff ).getExtension( f ) );
                prefs.put( OPEN_KEY, f.getParent() );
            }
            try {
                FileWriter output = new FileWriter( f );
                output.write( sb.toString() );
                output.flush();
                output.close();
            } catch ( IOException e ) {
                LOG.error( e.getMessage(), e );
                showExceptionDialog( "Exporting failed, see error log for details." );
            }

        }
    }

    /**
     * @param errorMessage
     *            to display
     */
    public void showExceptionDialog( String errorMessage ) {
        JOptionPane.showMessageDialog( this, errorMessage );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent e ) {
        Object source = e.getSource();
        if ( source instanceof JButton ) {
            JButton clicked = (JButton) source;
            if ( clicked.getText().startsWith( "Export" ) ) {
                doExport();
            } else {
                JFileChooser fileChooser = createFileChooser( supportedOpenFilter );
                int result = fileChooser.showOpenDialog( this );
                if ( JFileChooser.APPROVE_OPTION == result ) {
                    File f = fileChooser.getSelectedFile();
                    if ( f != null ) {
                        String path = f.getAbsolutePath();
                        prefs.put( LAST_EXTENSION, ( (CustomFileFilter) fileChooser.getFileFilter() ).getExtension( f ) );
                        prefs.put( OPEN_KEY, f.getParent() );
                        readFile( path );
                    }

                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed( KeyEvent arg0 ) {
        // nottin
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased( KeyEvent arg0 ) {
        // nottin
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped( KeyEvent e ) {

        boolean changed = true;
        if ( !openGLEventListener.updateView( e.getKeyChar() ) ) {
            if ( e.getKeyChar() == 'q' ) {
                System.exit( 0 );
            } else {
                changed = false;
            }
        }
        if ( changed ) {
            canvas.display();
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main( String[] args )
                            throws IOException {
        // Tesselator t = new Tesselator();
        // ArrayList<SimpleAccessGeometry> simpleAccessGeometries = new ArrayList<SimpleAccessGeometry>();
        // simpleAccessGeometries.add( GLViewer.createStar() );
        // simpleAccessGeometries.add( GLViewer.createTexturedConcav() );
        // simpleAccessGeometries.add( GLViewer.createGeometryWithRing() );
        // GeometryQualityModel gqm = new GeometryQualityModel( simpleAccessGeometries );
        // RenderableQualityModel rqm = t.createRenderableQM( gqm );

        CityGMLImporter importer = new CityGMLImporter( null, new float[] { -2568000, -5615600, 0 }, null, false );
        List<WorldRenderableObject> objects = importer.importFromFile( "/tmp/building.gml", 6, 2 );
        // List<WorldRenderableObject> objects = importer.export(
        // "/home/rutger/workspace/bonn_3doptimierung/resources/data/bonn_citygml_files/530810.GML",
        // 6, 2 );

        // String file = write( rqm );

        // RenderableQualityModel loadedModel = (RenderableQualityModel) read( file );
        // rqm.addGeometryData( createBillboard() );

        GLViewer viewer = new GLViewer( true );
        for ( WorldRenderableObject wro : objects ) {
            viewer.openGLEventListener.addDataObjectToScene( wro );
        }
        // viewer.addGeometries( rqm, true );
        // rqm = new BillBoard( "4", new float[] { -1, -2.6f, 0 }, 2, 2 );
        // file = write( rqm );
        // loadedModel = (RenderableQualityModel) read( file );
        // viewer.addGeometries( rqm, false );
        // rqm2 = new BillBoard( "3", new float[] { 1, 0, 1f }, new float[] { 3, 1 } );
        // viewer.addGeometries( rqm2, false );
        viewer.toFront();
    }

    static String write( JOGLRenderable t )
                            throws FileNotFoundException, IOException {
        File f = File.createTempFile( t.getClass().getSimpleName() + "_", ".bin" );
        ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( f ) );
        oos.writeObject( t );
        System.out.println( "file: " + f );
        System.out.println( "File size: " + f.length() );
        System.out.println( "Mem size: " + ( (MemoryAware) t ).sizeOf() );
        oos.close();
        return f.getAbsolutePath();
    }

    static JOGLRenderable read( String file )
                            throws FileNotFoundException, IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream( new FileInputStream( new File( file ) ) );
        Object o = ois.readObject();
        ois.close();
        return (JOGLRenderable) o;
    }

    private static SimpleAccessGeometry createStar() {
        float y = 0.5f;
        float[] coords = new float[] { 1, y, .75f, // up right
                                      0.25f, y, .75f, // up right middle
                                      0, y, 1, // up point
                                      -0.25f, y, .75f, // up left middle
                                      -1, y, .75f, // up left
                                      -0.75f, y, 0,// left middle
                                      -1, y, -.75f, // down left
                                      -.25f, y, -.75f,// down left middle
                                      0, y, -1, // down point
                                      0.25f, y, -.75f, // down right middle
                                      1, y, -.75f, // down right
                                      .75f, y, 0 // right middle
        };
        // float[] coords = new float[] { 0, 0, 1, -1, 0, -1, 1, 0, -1, 0, 0, -1 };

        return new SimpleAccessGeometry( coords );
    }

    private static TexturedGeometry createTexturedConcav() {
        float[] coords = new float[] { -1, -1, 1, 0.5f, -.5f, 0, 0.4f, -.4f, -.4f, -0.8f, -.2f, -1f, -.1f, .1f, -.2f,
                                      -.2f, -.2f, .2f, -1, -1, 1 };
        float[] texCoords = new float[] { 0, 1, 1, .6f, .9f, .3f, .1f, 0, .5f, .4f, .6f, .5f, 0, 1 };

        return new TexturedGeometry( coords, "1", texCoords );
    }

    private static SimpleAccessGeometry createGeometryWithRing() {
        // float[] coords = new float[] { -2, -2, -2, 2, -2, -2, 2, -2, 2, -1, -2, -1, 1, -2, -1, 1, -2, 1 };
        float[] coords = new float[] { -2, -2, 0, 2, -2, 0, 2, 2, 0, -2, 2, 0, -1, -1, 0, 1, -1, 0, 1, 1, 0, -1, 1, 0 };

        float[] texCoords = new float[] { 0, 1, 1, 1, 1, 0, 0, 0, .25f, .75f, .75f, .75f, .75f, .25f, .25f, .25f };

        int[] ring = new int[] { 4 };
        return new TexturedGeometry( coords, ring, "2", texCoords );
    }

    /**
     * 
     * The <code>CustomFileFilter</code> class adds functionality to the filefilter mechanism of the JFileChooser.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * 
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     * 
     */
    private class CustomFileFilter extends FileFilter {

        private List<String> acceptedExtensions;

        private String desc;

        /**
         * @param acceptedExtensions
         *            list of extensions this filter accepts.
         * @param description
         *            to show
         */
        CustomFileFilter( List<String> acceptedExtensions, String description ) {
            this.acceptedExtensions = new ArrayList<String>( acceptedExtensions.size() );
            StringBuilder sb = new StringBuilder();
            if ( acceptedExtensions.size() > 0 ) {

                sb.append( "(" );
                int i = 0;
                for ( String ext : acceptedExtensions ) {
                    if ( ext.startsWith( "." ) ) {
                        ext = ext.substring( 1 );
                    } else if ( ext.startsWith( "*." ) ) {
                        ext = ext.substring( 2 );
                    } else if ( ext.startsWith( "*" ) ) {
                        ext = ext.substring( 1 );
                    }

                    this.acceptedExtensions.add( ext.trim().toUpperCase() );
                    sb.append( "*." );
                    sb.append( ext );
                    if ( ++i < acceptedExtensions.size() ) {
                        sb.append( ", " );
                    }
                }
                sb.append( ")" );
            }
            sb.append( description );
            desc = sb.toString();
        }

        /**
         * @param extension
         * @return true if the extension is accepted
         */
        public boolean accepts( String extension ) {
            return extension != null && acceptedExtensions.contains( extension.toUpperCase() );
        }

        @Override
        public boolean accept( File pathname ) {
            if ( pathname.isDirectory() ) {
                return true;
            }

            String extension = getExtension( pathname );
            if ( extension != null ) {
                if ( acceptedExtensions.contains( extension.trim().toUpperCase() ) ) {
                    return true;
                }
            }
            return false;
        }

        /**
         * @param f
         * @return the file extension (e.g. gml/shp/xml etc.)
         */
        String getExtension( File f ) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf( '.' );

            if ( i > 0 && i < s.length() - 1 ) {
                ext = s.substring( i + 1 ).toLowerCase();
            }
            return ext;
        }

        @Override
        public String getDescription() {
            return desc;
        }
    }

}
