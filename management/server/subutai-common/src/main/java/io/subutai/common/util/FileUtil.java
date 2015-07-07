package io.subutai.common.util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileUtil
{

    private static final Logger LOG = LoggerFactory.getLogger( FileUtil.class.getName() );


    public static File getFile( String fileName, Object object )
    {
        String currentPath = System.getProperty( "user.dir" ) + "/res/" + fileName;
        File file = new File( currentPath );
        if ( !file.exists() )
        {
            checkFolder( System.getProperty( "user.dir" ) + "/res/" );
            writeFile( fileName, object );
            file = new File( currentPath );
        }
        return file;
    }


    private static void checkFolder( String path )
    {
        File file = new File( path );
        if ( !file.exists() )
        {
            file.mkdir();
        }
    }


    private static void writeFile( String fileName, Object object )
    {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try
        {
            String currentPath = System.getProperty( "user.dir" ) + "/res/" + fileName;
            inputStream = getClassLoader( object.getClass() ).getResourceAsStream( "img/" + fileName );
            outputStream = new FileOutputStream( new File( currentPath ) );
            int read;
            byte[] bytes = new byte[1024];

            while ( ( read = inputStream.read( bytes ) ) != -1 )
            {
                outputStream.write( bytes, 0, read );
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error while writing to file: " + ex );
        }
        finally
        {
            if ( inputStream != null )
            {
                try
                {
                    inputStream.close();
                }
                catch ( IOException ignore )
                {
                }
            }
            if ( outputStream != null )
            {
                try
                {
                    outputStream.close();
                }
                catch ( IOException ignore )
                {
                }
            }
        }
    }


    private static URLClassLoader getClassLoader( Class clazz )
    {
        // Needed an instance to get URL, i.e. the static way doesn't work: FileUtil.class.getClass().
        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();

        return new URLClassLoader( new URL[] { url }, Thread.currentThread().getContextClassLoader() );
    }


    public static String getContent( String filePath, Object object )
    {

        if ( object != null )
        {
            return getContent( filePath, object.getClass() );
        }
        return null;
    }


    public static String getContent( String filePath, Class clazz )
    {
        String content = "";

        try
        {
            content = readFile( filePath, clazz );
        }
        catch ( Exception e )
        {
            LOG.error( "Error while reading file: " + e );
        }

        return content;
    }


    private static String readFile( String filePath, Class clazz ) throws IOException
    {

        InputStream is = getClassLoader( clazz ).getResourceAsStream( filePath );
        String s = streamToString( is );
        is.close();

        return s;
    }


    private static String streamToString( InputStream is )
    {
        Scanner scanner = new Scanner( is ).useDelimiter( "\\A" );
        return scanner.hasNext() ? scanner.next() : "";
    }


    public static String readFile( String path, Charset encoding ) throws IOException
    {
        byte[] encoded = Files.readAllBytes( Paths.get( path ) );
        return new String( encoded, encoding );
    }
}
