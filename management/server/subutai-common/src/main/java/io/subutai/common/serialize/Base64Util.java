package io.subutai.common.serialize;


import org.apache.commons.codec.binary.Base64;


public class Base64Util
{

    private Base64Util()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static String toBase64( String input )
    {
        return new Base64().encodeToString( input.getBytes() );
    }


    public static String fromBase64( String input )
    {
        return new String( new Base64().decode( input ) );
    }
}
