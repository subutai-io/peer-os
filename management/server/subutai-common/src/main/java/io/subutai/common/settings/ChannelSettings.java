package io.subutai.common.settings;


/**
 * Class contains general Channel (tunnel) settings
 */
public class ChannelSettings
{

    private static final String[] URL_ACCESS_PX1 = {

            "/rest/v1/identity/auth", "/rest/v1/identity/authid", "/rest/v1/identity/gettoken",
            "/rest/v1/identity/signtoken", "/rest/v1/peer/id", "/rest/v1/peer/inited", "/rest/v1/peer/mhpresent",
            "/rest/v1/peer/ready", "/rest/health/ready", "/rest/v1/pks/{$}",
            "/rest/v1/security/keyman/getpublickeyring", "/rest/v1/security/keyman/getpublickey",
            "/rest/v1/security/keyman/getpublickeyfingerprint", "/rest/v1/handshake/info",
            "/rest/v1/handshake/register", "/rest/v1/handshake/approve", "/rest/v1/handshake/cancel",
            "/rest/v1/handshake/reject", "/rest/v1/handshake/unregister", "/rest/v1/handshake/status/{$}",
            "/rest/v1/registration/public-key", "/rest/v1/environments/{$}/info", "/rest/v1/metadata/token/{$}"
    };


    private ChannelSettings()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static boolean checkURLAccess( String uri )
    {

        for ( final String aUrlAccess : URL_ACCESS_PX1 )
        {
            if ( checkURL( uri, aUrlAccess ) )
            {
                return true;
            }
        }

        return false;
    }


    private static boolean checkURL( String uri, String urlAccess )
    {

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
                return true;
            }
        }

        return false;
    }
}
