package io.subutai.common.settings;


/**
 * Class contains general Channel (tunnel) settings
 */
public class ChannelSettings
{
    public static int OPEN_PORT = 8080;

    public static int SECURE_PORT_X1 = 8443;
    public static int SECURE_PORT_X2 = 8444;
    public static int SECURE_PORT_X3 = 8445;
    public static int SPECIAL_PORT_X1 = 8551;
    public static final String SPECIAL_REST_BUS = "cxfBusAptManager";

    public static final String[] URL_ACCESS_PX1 = {

            "/rest/v1/identity/gettoken", "/rest/v1/peer/id", "/rest/v1/pks/{$}",
            "/rest/v1/security/keyman/getpublickeyring", "/rest/v1/handshake/info", "/rest/v1/handshake/register",
            "/rest/v1/handshake/approve", "/rest/v1/handshake/cancel", "/rest/v1/handshake/reject",
            "/rest/v1/handshake/unregister"
    };


    public static short checkURLAccess( String uri, String[] urlAccessArray )
    {
        short status = 0;

        for ( final String aUrlAccess : urlAccessArray )
        {
            if ( checkURL( uri, aUrlAccess ) == 1 )
            {
                status = 1;
                break;
            }
        }

        return status;
    }


    private static short checkURL( String uri, String urlAccess )
    {
        short status = 0;

        String subURI[] = uri.split( "/" );
        int subURISize = subURI.length;
        String subURLAccess[] = urlAccess.split( "/" );

        if ( subURISize == subURLAccess.length )
        {
            int st = 0;

            for ( int i = 0; i < subURISize; i++ )
            {
                if ( "{$}".equals( subURLAccess[i] ) )
                {
                    st++;
                }
                else
                {
                    if ( subURI[i].equals( subURLAccess[i] ) )
                    {
                        st++;
                    }
                }
            }

            if ( st == subURISize )
            {
                status = 1;
            }
        }

        return status;
    }


    public static void setOpenPort( int openPort )
    {
        OPEN_PORT = openPort;
    }


    public static void setSecurePortX1( int securePortX1 )
    {
        SECURE_PORT_X1 = securePortX1;
    }


    public static void setSecurePortX2( int securePortX2 )
    {
        SECURE_PORT_X2 = securePortX2;
    }


    public static void setSecurePortX3( int securePortX3 )
    {
        SECURE_PORT_X3 = securePortX3;
    }


    public static void setSpecialPortX1( int specialPortX1 )
    {
        SPECIAL_PORT_X1 = specialPortX1;
    }
}
