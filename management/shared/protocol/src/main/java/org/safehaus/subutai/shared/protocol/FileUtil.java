package org.safehaus.subutai.shared.protocol;


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
import java.util.logging.Level;
import java.util.logging.Logger;


public class FileUtil {

    private static final Logger log = Logger.getLogger( FileUtil.class.getName() );


    public static File getFile( String fileName, Object object ) {
        String currentPath = System.getProperty( "user.dir" ) + "/res/" + fileName;
        File file = new File( currentPath );
        if ( !file.exists() ) {
            checkFolder( System.getProperty( "user.dir" ) + "/res/" );
            writeFile( fileName, object );
            file = new File( currentPath );
        }
        return file;
    }


    private static void writeFile( String fileName, Object object ) {

        try {
            String currentPath = System.getProperty( "user.dir" ) + "/res/" + fileName;
            InputStream inputStream = getClassLoader( object.getClass() ).getResourceAsStream( "img/" + fileName );
            OutputStream outputStream = new FileOutputStream( new File( currentPath ) );
            int read = 0;
            byte[] bytes = new byte[1024];

            while ( ( read = inputStream.read( bytes ) ) != -1 ) {
                outputStream.write( bytes, 0, read );
            }

            inputStream.close();
            outputStream.close();
        }
        catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }


    private static void checkFolder( String path ) {
        File file = new File( path );
        if ( !file.exists() ) {
            file.mkdir();
        }
    }


    private static URLClassLoader getClassLoader( Class clazz ) {
        // Needed an instance to get URL, i.e. the static way doesn't work: FileUtil.class.getClass().
        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        URLClassLoader classLoader =
                new URLClassLoader( new URL[] { url }, Thread.currentThread().getContextClassLoader() );

        return classLoader;
    }


    private static String streamToString( InputStream is ) {
        Scanner scanner = new Scanner( is ).useDelimiter( "\\A" );
        return scanner.hasNext() ? scanner.next() : "";
    }


    private static String readFile( String filePath, Class clazz ) throws IOException {

        InputStream is = getClassLoader( clazz ).getResourceAsStream( filePath );
        String s = streamToString( is );
        is.close();

        return s;
    }


    public static String getContent( String filePath, Object object ) {

        if ( object != null ) {
            return getContent( filePath, object.getClass() );
        }
        return null;
    }


    public static String getContent( String filePath, Class clazz ) {
        String content = "";

        try {
            content = readFile( filePath, clazz );
        }
        catch ( Exception e ) {
            log.log( Level.SEVERE, "Error while reading file: " + e );
        }

        return content;
    }


    public static String readFile( String path, Charset encoding ) throws IOException {
        byte[] encoded = Files.readAllBytes( Paths.get( path ) );
        return new String( encoded, encoding );
    }
}
