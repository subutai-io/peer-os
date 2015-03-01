package org.safehaus.subutai.common.settings;


/**
 * Created by talas on 2/27/15.
 */
public class ChannelSettings
{
    public static final String SECURE_PORT_X1 = "8543";
    public static final String SECURE_PORT_X2 = "8544";
    public static final String SECURE_PORT_X3 = "8545";


    public static String[] URL_ACCESS_PX1 =
    {
            "/cxf/subutai/peer/register",
            "/cxf/subutai/peer/reject",
            "/cxf/subutai/peer/approve",
            "/cxf/subutai/peer/remove",
            "/cxf/subutai/peer/trust_request",
            "/cxf/subutai/peer/trust_response",
    };

    public static String[] URL_ACCESS_PX2 =
    {
            "/cxf/subutai/peer/",
            "/cxf/subutai/peer/getlist",
            "/cxf/subutai/peer/me",
            "/cxf/subutai/peer/id",
            "/cxf/subutai/peer/update",
            "/cxf/subutai/peer/registered_peers",
            "/cxf/subutai/peer/ping",
            "/cxf/subutai/peer/template/get",
            "/cxf/subutai/peer/unregister",
            "/cxf/subutai/peer/container/quota",
            "/cxf/subutai/peer/container/quota/info",
            "/cxf/subutai/peer/container/destroy",
            "/cxf/subutai/peer/container/start",
            "/cxf/subutai/peer/container/stop",
            "/cxf/subutai/peer/container/isconnected",
            "/cxf/subutai/peer/container/state",
            "/cxf/subutai/peer/container/resource/usage",
            "/cxf/subutai/peer/container/quota/ram/available",
            "/cxf/subutai/peer/container/quota/cpu/available",
            "/cxf/subutai/peer/container/quota/disk/available",
            "/cxf/subutai/peer/container/quota/ram",
            "/cxf/subutai/peer/container/quota/ram/info",
            "/cxf/subutai/peer/container/quota/cpu",
            "/cxf/subutai/peer/container/quota/cpu/info",
            "/cxf/subutai/peer/container/quota/cpuset",
            "/cxf/subutai/peer/container/quota/disk",
            "/cxf/subutai/peer/container/gateway",
            "/cxf/subutai/peer/vni",

            "/cxf/subutai/environments",
            "/cxf/subutai/environments/domain",
            "/cxf/subutai/environments/container/environmentId",
            "/cxf/subutai/environments/container/state",
            "/cxf/subutai/environments/{environmentId}",
            "/cxf/subutai/environments/grow",
            "/cxf/subutai/environments/key",
            "/cxf/subutai/environments/container",

            "/cxf/subutai/hosts",

            "/cxf/subutai/identity/key/{username}",

            "/cxf/subutai/messenger/message",

            "/cxf/subutai/monitor/metrics/resource-hosts",
            //"/cxf/subutai/monitor/metrics/containers-hosts/{environmentId}",
            "/cxf/subutai/monitor/metrics/containers-hosts/{$}",
            "/cxf/subutai/monitor/alert",

            "/cxf/subutai/scripts/",
            "/cxf/subutai/scripts/{scriptName}",

            "/cxf/subutai/registry/templates",
            "/cxf/subutai/registry/templates/import",
            "/cxf/subutai/registry/templates/tree",
            //"/cxf/subutai/registry/templates/arch/{lxcArch}",
            "/cxf/subutai/registry/templates/arch/{$}",
            "/cxf/subutai/registry/templates/plain-list",
            "/cxf/subutai/registry/templates/{templateName}",
            //"/cxf/subutai/registry/templates/arch/{lxcArch}/plain-list",
            //"/cxf/subutai/registry/templates/{templateName}/{templateVersion}/download/{token}",
            "/cxf/subutai/registry/templates/{$}sd/{$}/download/{$}",
            //"/cxf/subutai/registry/templates/{templateName}/{templateVersion}",
            "/cxf/subutai/registry/templates/{$}/{$}",
            //"/cxf/subutai/registry/templates/{templateName}/{templateVersion}/remove",
            "/cxf/subutai/registry/templates/{$}/{$}/remove",
            "/cxf/subutai/registry/templates/{templateName}/{templateVersion}/arch/{lxcArch}",
            "/cxf/subutai/registry/templates/{$}/{$}/arch/{$}",
            //"/cxf/subutai/registry/templates/{childTemplateName}/parent",
            "/cxf/subutai/registry/templates/{$}/parent",
            //"/cxf/subutai/registry/templates/{childTemplateName}/{templateVersion}/arch/{lxcArch}/parent",
            "/cxf/subutai/registry/templates/{$}/{$}/arch/{$}/parent",
            "/cxf/subutai/registry/templates/{childTemplateName}/parents",
            //"/cxf/subutai/registry/templates/{childTemplateName}/{templateVersion}/parents",
            "/cxf/subutai/registry/templates/{$}/{$}/parents",
            //"/cxf/subutai/registry/templates/{parentTemplateName}/children",
            "/cxf/subutai/registry/templates/{$}/children",
            //"/cxf/subutai/registry/templates/{parentTemplateName}/{templateVersion}/children",
            "/cxf/subutai/registry/templates/{$}/{$}/arch/{$}/children",
            //"/cxf/subutai/registry/templates/{templateName}/{templateVersion}/is-used-on-fai",
            "/cxf/subutai/registry/templates/{$}/{$}/is-used-on-fai",
            //"/cxf/subutai/registry/templates/{templateName}/{templateVersion}/fai/{faiHostname}/is-used/{isInUse}",
            "/cxf/subutai/registry/templates/{$}/{$}/fai/{$}/is-used/{$}",

            //"/cxf/subutai/tracker/operations/{source}/{uuid}",
            "/cxf/subutai/tracker/operations/{$}/{$}",
            //"/cxf/subutai/tracker/operations/{source}/{dateFrom}/{dateTo}/{limit}",
            "/cxf/subutai/tracker/operations/{$}/{$}/{$}/{$}",
            "/cxf/subutai/tracker/operations/sources"

    };

    public static String[] URL_ACCESS_PX3 =
    {
            ""
    };

    public static short checkURL(String uri, String[] URL_ACCESS)
    {
        String subURI[]  = uri.split( "/" );
        int subURIsize   = subURI.length;

        short status = 0;

        for(int x = 0; x<URL_ACCESS.length;x++)
        {
            String subURL_ACCESS[] = URL_ACCESS[x].split( "/" );

            if(subURIsize == subURL_ACCESS.length)
            {
                int st = 0;

                for(int i=0;i<subURIsize;i++)
                {
                    if(subURL_ACCESS[i].equals( "{$}" ))
                    {
                        st++;
                    }
                    else
                    {
                        if(subURI[i].equals( subURL_ACCESS[i] ))
                        {
                            st++;
                        }
                    }
                }

                if(st == subURIsize)
                {
                    status = 1;
                    break;
                }
            }
        }

        return status;
    }

}
