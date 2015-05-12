package org.safehaus.subutai.common.security.utils.io;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class of utility methods to safely close streams, readers, writers and archives. By safe it ignores null parameters
 * and any exceptions raised by closing.
 */
public class SafeCloseUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SafeCloseUtil.class );
    private static final String LOG_FORMAT = "Error closing %s";


    private SafeCloseUtil()
    {
    }


    /**
     * Safely close an input stream.
     *
     * @param finStream Input stream
     */
    public static void close( FileInputStream finStream )
    {
        if ( null != finStream )
        {
            try
            {
                finStream.close();
            }
            catch ( IOException e )
            {
                LOGGER.warn( String.format( LOG_FORMAT, "file stream" ) );
            }
        }
    }


    /**
     * Safely close an input stream.
     *
     * @param inputStream Input stream
     */
    public static void close( InputStream inputStream )
    {
        try
        {
            if ( inputStream != null )
            {
                inputStream.close();
            }
        }
        catch ( IOException ex )
        {
            LOGGER.warn( String.format( LOG_FORMAT, " stream" ) );
        }
    }


    /**
     * Safely close an output stream.
     *
     * @param outputStream Output stream
     */
    public static void close( OutputStream outputStream )
    {
        try
        {
            if ( outputStream != null )
            {
                outputStream.close();
            }
        }
        catch ( IOException ex )
        {
            LOGGER.warn( String.format( LOG_FORMAT, "output stream" ) );
        }
    }


    /**
     * Safely close a reader.
     *
     * @param reader Reader
     */
    public static void close( Reader reader )
    {
        try
        {
            if ( reader != null )
            {
                reader.close();
            }
        }
        catch ( IOException ex )
        {
            LOGGER.warn( String.format( LOG_FORMAT, "reader" ) );
        }
    }


    /**
     * Safely close a writer.
     *
     * @param writer Writer
     */
    public static void close( Writer writer )
    {
        try
        {
            if ( writer != null )
            {
                writer.close();
            }
        }
        catch ( IOException ex )
        {
            LOGGER.warn( String.format( LOG_FORMAT, "writer" ) );
        }
    }


    /**
     * Safely close a zip file.
     *
     * @param zipFile Zip file
     */
    public static void close( ZipFile zipFile )
    {
        try
        {
            if ( zipFile != null )
            {
                zipFile.close();
            }
        }
        catch ( IOException ex )
        {
            LOGGER.warn( String.format( LOG_FORMAT, "zipFile" ) );
        }
    }
}
