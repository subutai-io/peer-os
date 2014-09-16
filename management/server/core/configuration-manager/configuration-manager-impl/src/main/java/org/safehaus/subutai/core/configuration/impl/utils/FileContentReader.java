package org.safehaus.subutai.core.configuration.impl.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * Created by bahadyr on 7/19/14.
 */
public class FileContentReader {

    private final Logger LOG = Logger.getLogger( FileContentReader.class.getName() );


    public String readFile( String pathToFile ) {
        StringBuilder sb = new StringBuilder();
        File file = new File( pathToFile );
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader( file );
            bufferedReader = new BufferedReader( fileReader );

            for ( String line; ( line = bufferedReader.readLine() ) != null; ) {
                sb.append( line ).append( System.getProperty( "line.separator" ) );
            }
        }
        catch ( IOException e ) {
            LOG.info( e.getMessage() );
        }
        finally {
            try {
                if ( bufferedReader != null ) {
                    bufferedReader.close();
                }
            }
            catch ( IOException ignore ) {
            }
            try {
                if ( fileReader != null ) {
                    fileReader.close();
                }
            }
            catch ( IOException ignore ) {
            }
        }

        return sb.toString();
    }
}
