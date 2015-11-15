package io.subutai.common.settings;


/**
 * Class contains general Channel (tunnel) settings
 */
public class ChannelSettings
{
    public static final String OPEN_PORT      = "8181";

    public static final String SECURE_PORT_X1 = "8443";
    public static final String SECURE_PORT_X2 = "8444";
    public static final String SECURE_PORT_X3 = "8445";

    public static final String SPECIAL_PORT_X1 = "8551";
    public static final String SPECIAL_SECURE_PORT_X1 = "8552";

        public static final String[] URL_ACCESS_PX1 = {

                "/rest/peer/id",
                "/rest/pks/{$}",
                "/rest/security/keyman/getpublickeyring",
                "/rest/v1/registration/register",
                "/rest/v1/registration/approve",
                "/rest/v1/registration/cancel",
                "/rest/v1/registration/reject"

        };

        public static short checkURLArray( String uri, String[] urlAccessArray )
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


        public static short checkURL( String uri, String urlAccess )
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
}
