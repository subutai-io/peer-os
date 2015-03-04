package org.safehaus.subutai.core.identity.ssl.utils.io;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;


/**
 * Class of utility methods to copy data between I/O streams.
 */
public class CopyUtil
{
    private CopyUtil()
    {
    }


    /**
     * Copy data from one stream to another and do not close I/O.
     *
     * @param in Input stream
     * @param out Output stream
     *
     * @throws java.io.IOException If an I/O problem occurred
     */
    public static void copy( InputStream in, OutputStream out ) throws IOException
    {
        byte[] buffer = new byte[2048];
        int i;
        while ( ( i = in.read( buffer ) ) > 0 )
        {
            out.write( buffer, 0, i );
        }
    }


    /**
     * Copy data from one stream to another and close I/O.
     *
     * @param in Input stream
     * @param out Output stream
     *
     * @throws java.io.IOException If an I/O problem occurred
     */
    public static void copyClose( InputStream in, OutputStream out ) throws IOException
    {
        try
        {
            copy( in, out );
        }
        finally
        {
            SafeCloseUtil.close( in );
            SafeCloseUtil.close( out );
        }
    }


    /**
     * Copy data from a reader to a writer and do not close I/O.
     *
     * @param reader Reader
     * @param writer Writer
     *
     * @throws java.io.IOException If an I/O problem occurred
     */
    public static void copy( Reader reader, Writer writer ) throws IOException
    {
        char[] buffer = new char[2048];
        int i;
        while ( ( i = reader.read( buffer ) ) > 0 )
        {
            writer.write( buffer, 0, i );
        }
    }


    /**
     * Copy data from a reader to a writer and close I/O.
     *
     * @param reader Reader
     * @param writer Writer
     *
     * @throws java.io.IOException If an I/O problem occurred
     */
    public static void copyClose( Reader reader, Writer writer ) throws IOException
    {
        try
        {
            copy( reader, writer );
        }
        finally
        {
            SafeCloseUtil.close( reader );
            SafeCloseUtil.close( writer );
        }
    }
}
