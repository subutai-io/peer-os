package org.safehaus.subutai.core.identity.ssl.utils.io;


/**
 * Class for manipulating and checking file names.
 */
public class FileNameUtil
{

    /**
     * Make a string safely usable as a file name by removing all illegal characters (and a few more).
     *
     * @param s A string that is supposed to be used as a file name and therefore cleaned from illegal characters.
     *
     * @return Sanitized string
     */
    public static String cleanFileName( String s )
    {
        return s.replaceAll( "[^a-zA-Z0-9\\._]+", "_" );
    }


    /**
     * Remove file extension
     *
     * @param fileName file name (with or without path)
     *
     * @return File name without extension
     */
    public static String removeExtension( String fileName )
    {

        if ( fileName == null )
        {
            return null;
        }

        // find position in string where extension begins (and handle paths like "C:\my.dir\fileName")
        int extensionPos = fileName.lastIndexOf( "." );
        int lastSeparator = Math.max( fileName.lastIndexOf( '/' ), fileName.lastIndexOf( '\\' ) );
        int index = ( lastSeparator > extensionPos ) ? -1 : extensionPos;

        // no extension found
        if ( index == -1 )
        {
            return fileName;
        }

        return fileName.substring( 0, index );
    }
}
