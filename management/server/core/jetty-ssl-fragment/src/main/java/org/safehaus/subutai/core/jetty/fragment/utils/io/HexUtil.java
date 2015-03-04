package org.safehaus.subutai.core.jetty.fragment.utils.io;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;


/**
 * Class of utility methods to output data in hex.
 */
public class HexUtil
{
    private static final String NEWLINE = "\n";


    private HexUtil()
    {
    }


    /**
     * ***********************************************************************
     */
    public static byte[] hexStringToByteArray( String s )
    {
        byte[] b = new byte[s.length() / 2];

        for ( int i = 0; i < b.length; i++ )
        {
            int index = i * 2;
            int v = Integer.parseInt( s.substring( index, index + 2 ), 16 );
            b[i] = ( byte ) v;
        }
        return b;
    }


    /**
     * ***********************************************************************
     */
    public static String byteArrayToHexString( byte[] b )
    {
        StringBuffer sb = new StringBuffer( b.length * 2 );

        for ( int i = 0; i < b.length; i++ )
        {
            int v = b[i] & 0xff;
            if ( v < 16 )
            {
                sb.append( '0' );
            }

            sb.append( Integer.toHexString( v ) );
        }
        return sb.toString().toUpperCase();
    }


    /**
     * ************************************************************************* Get hex string for the supplied big
     * integer: "0x<hex string>" where hex string is outputted in groups of exactly four characters sub-divided by
     * spaces.
     *
     * @param bigInt Big integer
     *
     * @return Hex string
     */
    public static String getHexString( BigInteger bigInt )
    {
        // Convert number to hex string
        String hex = bigInt.toString( 16 ).toUpperCase();

        // Get number padding bytes
        int padding = ( 4 - ( hex.length() % 4 ) );

        // Insert any required padding to get groups of exactly 4 characters
        if ( ( padding > 0 ) && ( padding < 4 ) )
        {
            StringBuffer sb = new StringBuffer( hex );

            for ( int i = 0; i < padding; i++ )
            {
                sb.insert( 0, '0' );
            }

            hex = sb.toString();
        }

        // Output with leading "0x" and spaces to form groups
        StringBuffer strBuff = new StringBuffer();

        strBuff.append( "0x" );

        for ( int i = 0; i < hex.length(); i++ )
        {
            strBuff.append( hex.charAt( i ) );

            if ( ( ( ( i + 1 ) % 4 ) == 0 ) && ( ( i + 1 ) != hex.length() ) )
            {
                strBuff.append( ' ' );
            }
        }

        return strBuff.toString();
    }


    /**
     * Get hex string for the supplied byte array: "0x<hex string>" where hex string is outputted in groups of exactly
     * four characters sub-divided by spaces.
     *
     * @param bytes Byte array
     *
     * @return Hex string
     */
    public static String getHexString( byte[] bytes )
    {
        return getHexString( new BigInteger( 1, bytes ) );
    }


    /**
     * Get hex and clear text dump of byte array.
     *
     * @param bytes Array of bytes
     *
     * @return Hex/clear dump
     *
     * @throws java.io.IOException If an I/O problem occurs
     */
    public static String getHexClearDump( byte[] bytes ) throws IOException
    {
        ByteArrayInputStream bais = null;

        try
        {
            // Divide dump into 8 byte lines
            StringBuffer strBuff = new StringBuffer();

            bais = new ByteArrayInputStream( bytes );
            byte[] line = new byte[8];
            int read = -1;
            boolean firstLine = true;

            while ( ( read = bais.read( line ) ) != -1 )
            {
                if ( firstLine )
                {
                    firstLine = false;
                }
                else
                {
                    strBuff.append( NEWLINE );
                }

                strBuff.append( getHexClearLineDump( line, read ) );
            }

            return strBuff.toString();
        }
        finally
        {
            SafeCloseUtil.close( bais );
        }
    }


    private static String getHexClearLineDump( byte[] bytes, int len )
    {
        StringBuffer sbHex = new StringBuffer();
        StringBuffer sbClr = new StringBuffer();

        for ( int cnt = 0; cnt < len; cnt++ )
        {
            // Convert byte to int
            byte b = bytes[cnt];
            int i = b & 0xFF;

            // First part of byte will be one hex char
            int i1 = ( int ) Math.floor( i / 16 );

            // Second part of byte will be one hex char
            int i2 = i % 16;

            // Get hex characters
            sbHex.append( Character.toUpperCase( Character.forDigit( i1, 16 ) ) );
            sbHex.append( Character.toUpperCase( Character.forDigit( i2, 16 ) ) );

            if ( ( cnt + 1 ) < len )
            {
                // Divider between hex characters
                sbHex.append( ' ' );
            }

            // Get clear character

            // Character to display if character not defined in Unicode or is a
            // control charcter
            char c = '.';

            // Not a control character and defined in Unicode
            if ( ( !Character.isISOControl( ( char ) i ) ) && ( Character.isDefined( ( char ) i ) ) )
            {
                Character clr = new Character( ( char ) i );
                c = clr.charValue();
            }

            sbClr.append( c );
        }

		/*
         * Put both dumps together in one string (hex, clear) with appropriate
		 * padding between them (pad to array length)
		 */
        StringBuffer strBuff = new StringBuffer();

        strBuff.append( sbHex.toString() );
        sbHex = new StringBuffer();

        int i = bytes.length - len;
        for ( int cnt = 0; cnt < i; cnt++ )
        {
            strBuff.append( "   " ); // Each missing byte takes up three spaces
        }

        strBuff.append( "   " ); // The gap between hex and clear output is
        // three
        // spaces
        strBuff.append( sbClr.toString() );
        sbClr = new StringBuffer();

        return strBuff.toString();
    }
}
