package io.subutai.common.security.utils;


import java.io.Closeable;
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
    private static final String LOG_FORMAT = "Error closing {}: {}";


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
                LOGGER.warn( LOG_FORMAT, "file stream", e.getMessage() );
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
        catch ( IOException e )
        {
            LOGGER.warn( LOG_FORMAT, "inout stream", e.getMessage() );
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
        catch ( IOException e )
        {
            LOGGER.warn( LOG_FORMAT, "output stream", e.getMessage() );
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
        catch ( IOException e )
        {
            LOGGER.warn( LOG_FORMAT, "reader", e.getMessage() );
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
        catch ( IOException e )
        {
            LOGGER.warn( LOG_FORMAT, "writer", e.getMessage() );
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
        catch ( IOException e )
        {
            LOGGER.warn( LOG_FORMAT, "zip file", e.getMessage() );
        }
    }


    public static void close( Closeable closeable )
    {
        try
        {
            if ( closeable != null )
            {
                closeable.close();
            }
        }
        catch ( IOException e )
        {
            LOGGER.warn( LOG_FORMAT, "closeable stream", e.getMessage() );
        }
    }
}
