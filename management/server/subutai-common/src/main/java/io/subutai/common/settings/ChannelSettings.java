package io.subutai.common.settings;


/**
 * Class contains general Channel (tunnel) settings
 */
public class ChannelSettings
{
    public static final String OPEN_PORT      = "8080";

    public static final String SECURE_PORT_X1 = "8443";
    public static final String SECURE_PORT_X2 = "8444";
    public static final String SECURE_PORT_X3 = "8445";

    public static final String SPECIAL_PORT_X1 = "8551";
    public static final String SPECIAL_SECURE_PORT_X1 = "8552";

    public static final String SPECIAL_REST_BUS = "cxfBusAptManager";

    public static final String[] URL_ACCESS_PX1 = {

            "/rest/v1/identity/gettoken",
            "/rest/v1/peer/id",
            "/rest/v1/pks/{$}",
            "/rest/v1/security/keyman/getpublickeyring",
            "/rest/v1/handshake/register",
            "/rest/v1/handshake/approve",
            "/rest/v1/handshake/cancel",
            "/rest/v1/handshake/reject",
            "/rest/v1/handshake/unregister",


            "/rest/registry/templates",
            "/rest/registry/templates/import",
            "/rest/registry/templates/arch/{$}",
            "/rest/registry/templates/plain-list",
            "/rest/registry/templates/arch/{$}/plain-list",
            "/rest/registry/templates/{$}",
            "/rest/registry/templates/{$}/{$}",
            "/rest/registry/templates/{$}/{$}/remove",
            "/rest/registry/templates/{$}/{$}/arch/{$}",
            "/rest/registry/templates/{$}/parent",
            "/rest/registry/templates/{$}/{$}/parent",
            "/rest/registry/templates/{$}/{$}/arch/{$}/parent",
            "/rest/registry/templates/{$}/parents",
            "/rest/registry/templates/{$}/{$}/parents",
            "/rest/registry/templates/{$}/{$}/arch/{$}/parents",
            "/rest/registry/templates/{$}/children",
            "/rest/registry/templates/{$}/{$}/children",
            "/rest/registry/templates/{$}/{$}/arch/{$}/children",
            "/rest/registry/templates/{$}/{$}/is-used-on-fai",
            "/rest/registry/templates/{$}/{$}/fai/{$}/is-used/{$}"


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
