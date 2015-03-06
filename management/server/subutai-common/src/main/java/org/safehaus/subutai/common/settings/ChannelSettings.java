package org.safehaus.subutai.common.settings;


/**
 * Created by talas on 2/27/15.
 */
public class ChannelSettings
{
    public static final String OPEN_PORT = "8181";
    public static final String SPECIAL_PORT_X1 = "8542";
    public static final String SECURE_PORT_X1 = "8543";
    public static final String SECURE_PORT_X2 = "8544";
    public static final String SECURE_PORT_X3 = "8545";


    public static String[] URL_ACCESS_PX1 = {
            "/cxf/peer/register", "/cxf/peer/reject", "/cxf/peer/approve", "/cxf/peer/remove",
            "/cxf/peer/trust_request", "/cxf/peer/trust_response",
    };

    public static String[] URL_ACCESS_PX2 = {
            "/cxf/peer/", "/cxf/peer/getlist", "/cxf/peer/me", "/cxf/peer/id", "/cxf/peer/update",
            "/cxf/peer/registered_peers", "/cxf/peer/ping", "/cxf/peer/template/get", "/cxf/peer/unregister",

            "/cxf/peer/container/quota", "/cxf/peer/container/quota/info", "/cxf/peer/container/destroy",
            "/cxf/peer/container/start", "/cxf/peer/container/stop", "/cxf/peer/container/isconnected",
            "/cxf/peer/container/state", "/cxf/peer/container/resource/usage",
            "/cxf/peer/container/quota/ram/available", "/cxf/peer/container/quota/cpu/available",
            "/cxf/peer/container/quota/disk/available", "/cxf/peer/container/quota/ram",
            "/cxf/peer/container/quota/ram/info", "/cxf/peer/container/quota/cpu", "/cxf/peer/container/quota/cpu/info",
            "/cxf/peer/container/quota/cpuset", "/cxf/peer/container/quota/disk", "/cxf/peer/container/gateway",
            "/cxf/peer/vni", "/cxf/peer/cert/import", "/cxf/peer/cert/export", "/cxf/peer/cert/remove",

            "/cxf/environments", "/cxf/environments/domain", "/cxf/environments/container/environmentId",
            "/cxf/environments/container/state", "/cxf/environments/{environmentId}", "/cxf/environments/grow",
            "/cxf/environments/key", "/cxf/environments/container",

            "/cxf/hosts",

            "/cxf/identity/key/{username}",

            "/cxf/messenger/message",

            "/cxf/monitor/metrics/resource-hosts",
            //"/cxf/monitor/metrics/containers-hosts/{environmentId}",
            "/cxf/monitor/metrics/containers-hosts/{$}", "/cxf/monitor/alert",

            "/cxf/scripts/", "/cxf/scripts/{scriptName}",

            "/cxf/registry/templates", "/cxf/registry/templates/import", "/cxf/registry/templates/tree",
            //"/cxf/registry/templates/arch/{lxcArch}",
            "/cxf/registry/templates/arch/{$}", "/cxf/registry/templates/plain-list",
            "/cxf/registry/templates/{templateName}",
            //"/cxf/registry/templates/arch/{lxcArch}/plain-list",
            //"/cxf/registry/templates/{templateName}/{templateVersion}/download/{token}",
            "/cxf/registry/templates/{$}sd/{$}/download/{$}",
            //"/cxf/registry/templates/{templateName}/{templateVersion}",
            "/cxf/registry/templates/{$}/{$}",
            //"/cxf/registry/templates/{templateName}/{templateVersion}/remove",
            "/cxf/registry/templates/{$}/{$}/remove",
            "/cxf/registry/templates/{templateName}/{templateVersion}/arch/{lxcArch}",
            "/cxf/registry/templates/{$}/{$}/arch/{$}",
            //"/cxf/registry/templates/{childTemplateName}/parent",
            "/cxf/registry/templates/{$}/parent",
            //"/cxf/registry/templates/{childTemplateName}/{templateVersion}/arch/{lxcArch}/parent",
            "/cxf/registry/templates/{$}/{$}/arch/{$}/parent", "/cxf/registry/templates/{childTemplateName}/parents",
            //"/cxf/registry/templates/{childTemplateName}/{templateVersion}/parents",
            "/cxf/registry/templates/{$}/{$}/parents",
            //"/cxf/registry/templates/{parentTemplateName}/children",
            "/cxf/registry/templates/{$}/children",
            //"/cxf/registry/templates/{parentTemplateName}/{templateVersion}/children",
            "/cxf/registry/templates/{$}/{$}/arch/{$}/children",
            //"/cxf/registry/templates/{templateName}/{templateVersion}/is-used-on-fai",
            "/cxf/registry/templates/{$}/{$}/is-used-on-fai",
            //"/cxf/registry/templates/{templateName}/{templateVersion}/fai/{faiHostname}/is-used/{isInUse}",
            "/cxf/registry/templates/{$}/{$}/fai/{$}/is-used/{$}",

            //"/cxf/tracker/operations/{source}/{uuid}",
            "/cxf/tracker/operations/{$}/{$}",
            //"/cxf/tracker/operations/{source}/{dateFrom}/{dateTo}/{limit}",
            "/cxf/tracker/operations/{$}/{$}/{$}/{$}", "/cxf/tracker/operations/sources"
    };

    public static String[] URL_ACCESS_PX3 = {
            ""
    };


    public static String[] URL_ACCESS_SPECIAL_PORT_PX1 = {
            ""
    };


    public static short checkURL( String uri, String[] URL_ACCESS )
    {
        String subURI[] = uri.split( "/" );
        int subURIsize = subURI.length;

        short status = 0;

        for ( int x = 0; x < URL_ACCESS.length; x++ )
        {
            String subURL_ACCESS[] = URL_ACCESS[x].split( "/" );

            if ( subURIsize == subURL_ACCESS.length )
            {
                int st = 0;

                for ( int i = 0; i < subURIsize; i++ )
                {
                    if ( subURL_ACCESS[i].equals( "{$}" ) )
                    {
                        st++;
                    }
                    else
                    {
                        if ( subURI[i].equals( subURL_ACCESS[i] ) )
                        {
                            st++;
                        }
                    }
                }

                if ( st == subURIsize )
                {
                    status = 1;
                    break;
                }
            }
        }

        return status;
    }
}
