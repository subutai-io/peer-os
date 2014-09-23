package org.safehaus.subutai.core.configuration.impl.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by bahadyr on 7/19/14.
 */
public class FileContentReader
{

    private static final Logger LOG = LoggerFactory.getLogger( FileContentReader.class.getName() );


    public String readFile( String pathToFile )
    {
        StringBuilder sb = new StringBuilder();
        File file = new File( pathToFile );
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try
        {
            fileReader = new FileReader( file );
            bufferedReader = new BufferedReader( fileReader );

            for ( String line; ( line = bufferedReader.readLine() ) != null; )
            {
                sb.append( line ).append( System.getProperty( "line.separator" ) );
            }
        }
        catch ( IOException e )
        {
            LOG.error( "FileContentReader@readFile: " + e.getMessage(), e );
        }
        finally
        {
            try
            {
                if ( bufferedReader != null )
                {
                    bufferedReader.close();
                }
            }
            catch ( IOException ignore )
            {
                LOG.error( "FileContentReader@readFile: ignore" + ignore.getMessage() );
            }
            try
            {
                if ( fileReader != null )
                {
                    fileReader.close();
                }
            }
            catch ( IOException ignore )
            {
                LOG.error( "FileContentReader@readFile: ignore" + ignore.getMessage() );
            }
        }
        return sb.toString();
    }
}
