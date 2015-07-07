package io.subutai.common.settings;


/**
 * Class contains general Channel (tunnel) settings
 */
public class ChannelSettings
{
    public static final String OPEN_PORT = "8181";
    public static final String SPECIAL_PORT_X1 = "8542";
    public static final String SPECIAL_SECURE_PORT_X1 = "8552";
    public static final String SECURE_PORT_X1 = "8543";
    public static final String SECURE_PORT_X2 = "8544";
    public static final String SECURE_PORT_X3 = "8545";


    public static final String[] URL_ACCESS_PX1 = {
            // All  Services
            "/{*}",
            "/cxf/peer/register", "/cxf/peer/register/{$}",
            "/cxf/peer/reject",
            "/cxf/peer/approve",
            "/cxf/peer/remove",
            "/cxf/peer/trust_request",
            "/cxf/peer/trust_response",
            "/cxf/peer/approve/{$}"
    };

    public static final String[] URL_ACCESS_PX2 = {

            "/cxf/peer/",
            "/cxf/peer/getlist",
            "/cxf/peer/me",
            "/cxf/peer/id",
            "/cxf/peer/update",
            "/cxf/peer/registered_peers",
            "/cxf/peer/peer_policy",
            "/cxf/peer/ping",
            "/cxf/peer/template/get",
            "/cxf/peer/vni",
            "/cxf/peer/gateways",
            "/cxf/peer/unregister",
            "/cxf/peer/container/quota",
            "/cxf/peer/container/quota/info",
            "/cxf/peer/container/destroy",
            "/cxf/peer/container/start",
            "/cxf/peer/container/stop",
            "/cxf/peer/container/isconnected",
            "/cxf/peer/container/state",
            "/cxf/peer/container/resource/usage",
            "/cxf/peer/container/quota/ram/available",
            "/cxf/peer/container/quota/cpu/available",
            "/cxf/peer/container/quota/disk/available",
            "/cxf/peer/container/quota/ram",
            "/cxf/peer/container/quota/ram2",
            "/cxf/peer/container/quota/ram/info",
            "/cxf/peer/container/quota/cpu",
            "/cxf/peer/container/quota/cpu/info",
            "/cxf/peer/container/quota/cpuset",
            "/cxf/peer/container/quota/disk",
            "/cxf/peer/container/gateway",
            "/cxf/peer/container/info",
            "/cxf/peer/cert/import",
            "/cxf/peer/cert/export",
            "/cxf/peer/cert/remove",

            "/cxf/environments",
            "/cxf/environments/domain",
            "/cxf/environments/container/environmentId",
            "/cxf/environments/container/state",
            "/cxf/environments/container/start",
            "/cxf/environments/container/stop",

            //"/cxf/environments/{environmentId}",
            "/cxf/environments/{$}",
            "/cxf/environments/grow",
            "/cxf/environments/key",
            "/cxf/environments/container",

            "/cxf/hosts",

            //"/cxf/identity/key/{username}",
            "/cxf/identity/key/{$}",

            "/cxf/messenger/message",

            "/cxf/monitor/metrics/resource-hosts",

            //"/cxf/monitor/metrics/containers-hosts/{environmentId}",
            "/cxf/monitor/metrics/containers-hosts/{$}",

            "/cxf/monitor/alert",

            "/cxf/scripts/",

            //"/cxf/scripts/{scriptName}",
            "/cxf/scripts/{$}",

            "/cxf/registry/templates",
            "/cxf/registry/templates/import",
            "/cxf/registry/templates/tree",

            //"/cxf/registry/templates/arch/{lxcArch}",
            "/cxf/registry/templates/arch/{$}",

            "/cxf/registry/templates/plain-list",

            //"/cxf/registry/templates/{templateName}",
            "/cxf/registry/templates/{$}",

            //"/cxf/registry/templates/arch/{lxcArch}/plain-list",
            "/cxf/registry/templates/arch/{$}/plain-list",

            //"/cxf/registry/templates/{templateName}/{templateVersion}/download/{token}",
            "/cxf/registry/templates/{$}/{$}/download/{$}",

            //"/cxf/registry/templates/{templateName}/{templateVersion}",
            "/cxf/registry/templates/{$}/{$}",

            //"/cxf/registry/templates/{templateName}/{templateVersion}/remove",
            "/cxf/registry/templates/{$}/{$}/remove",

            //"/cxf/registry/templates/{templateName}/{templateVersion}/arch/{lxcArch}",
            "/cxf/registry/templates/{$}/{$}/arch/{$}",

            //"/cxf/registry/templates/{childTemplateName}/parent",
            "/cxf/registry/templates/{$}/parent",

            //"/cxf/registry/templates/{childTemplateName}/{templateVersion}/arch/{lxcArch}/parent",
            "/cxf/registry/templates/{$}/{$}/arch/{$}/parent",

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
            "/cxf/tracker/operations/{$}/{$}/{$}/{$}",

            "/cxf/tracker/operations/sources",

            //"/cxf/{plugin_name}/clusters",
            "/cxf/{$}/clusters",

            //"/cxf/{$name_of_plugin}/clusters/{$`clusterName}",
            "/cxf/{$}/clusters/{$}",

            //"/cxf/{name_of_plugin}/install",
            "/cxf/{$}/install",

            //"/cxf/{name_of_plugin}/destroy",
            "/cxf/{$}/destroy",

            //"/cxf/{$name_of_plugin}/clusters/{$clusterName}/start",
            "/cxf/{$}/clusters/{$}/start",

            //"/cxf/{$name_of_plugin}/clusters/{$clusterName}/stop",
            "/cxf/{$}/clusters/{$}/stop",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/destroy/node/{lxcHostname}/type/{nodeType}",
            "/cxf/{$}/clusters/{$}/destroy/node/{$}/type/{$}",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/add/node/{lxcHostname}/type/{nodeType}",
            "/cxf/{$}/clusters/{$}/add/node/{$}/type/{$}",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/check/node/{lxcHostname}",
            "/cxf/{$}/clusters/{$}/check/node/{$}",

            //"/cxf/{$name_of_plugin}/clusters/configure_environment/{environmentId}/clusterName/{clusterName}/nodes/{nodes}",
            "/cxf/{$}/clusters/configure_environment/{$}/clusterName/{$}/nodes/{$}",

            //"/cxf/{$name_of_plugin}/clusters/remove/{clusterName}",
            "/cxf/{$}/clusters/remove/{$}",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/check",
            "/cxf/{$}/clusters/{$}/check",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/start/node/{lxcHostname}",
            "/cxf/{$}/clusters/{$}/start/node/{$}",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/stop/node/{lxcHostname}",
            "/cxf/{$}/clusters/{$}/stop/node/{$}",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/add",
            "/cxf/{$}/clusters/{$}/add",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/remove/node/{lxcHostname}",
            "/cxf/{$}/clusters/{$}/remove/node/{$}",

            //"/cxf/{$name_of_plugin}/configure_environment",
            "/cxf/{$}/configure_environment",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/status",
            "/cxf/{$}/clusters/{$}/status",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/status/secondary",
            "/cxf/{$}/clusters/{$}/status/secondary",

            //"/cxf/{$name_of_plugin}/clusters/job/{clusterName}/start",
            "/cxf/{$}/clusters/job/{$}/start",

            //"/cxf/{$name_of_plugin}/clusters/job/{clusterName}/stop",
            "/cxf/{$}/clusters/job/{$}/stop",

            //"/cxf/{$name_of_plugin}/clusters/job/{clusterName}/status",
            "/cxf/{$}/clusters/job/{$}/status",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/nodes",
            "/cxf/{$}/clusters/{$}/nodes",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/node/{hostname}/status",
            "/cxf/{$}/clusters/{$}/node/{$}/status",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/task/{hostname}/status",
            "/cxf/{$}/clusters/{$}/task/{$}/status",

            //"/cxf/{$name_of_plugin}/clusters/destroy/{clusterName}",
            "/cxf/{$}/clusters/destroy/{$}",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/add/node/{lxcHostName}",
            "/cxf/{$}/clusters/{$}/add/node/{$}",

            //"/cxf/{$}/clusters/{clusterName}/destroy/node/{lxcHostName}",
            "/cxf/{$}/clusters/{$}/destroy/node/{$}",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/add/node/{lxcHostname}",
            "/cxf/{$}/clusters/{$}/add/node/{$}",

            //"/cxf/{$name_of_plugin}/clusters/remove/{clusterName}",
            "/cxf/{$}/clusters/remove/{$}",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/remove/node/{lxcHostName}",
            "/cxf/{$}/clusters/{$}/remove/node/{$}",

            //"/cxf/{$name_of_plugin}/clusters/destroy/{clusterName}",
            "/cxf/{$}/clusters/destroy/{$}",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/actualize_master_ip",
            "/cxf/{$}/clusters/{$}/actualize_master_ip",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/change_master/nodes/{lxcHostName}/{keepSlave}",
            "/cxf/{$}/clusters/{$}/change_master/nodes/{$}/{$}",

            //"/cxf/{$name_of_plugin}/clusters/clusters/{clusterName}/start/node/{lxcHostName}/master/{master}",
            "/cxf/{$}/clusters/clusters/{$}/start/node/{$}/master/{$}",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/stop/node/{lxcHostName}/master/{master}",
            "/cxf/{$}/clusters/{$}/stop/node/{$}/master/{$}",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/check/node/{lxcHostName}/master/{master}",
            "/cxf/{$}/clusters/{$}/check/node/{$}/master/{$}",

            //"/cxf/{$name_of_plugin}/importData",
            "/cxf/{$}/importData",

            //"/cxf/{$name_of_plugin}/exportData",
            "/cxf/{$}/exportData",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/add/node",
            "/cxf/{$}/clusters/{$}/add/node",

            //"/cxf/{$name_of_plugin}/clusters/{clusterName}/add/node{lxcHostname}"
            "/cxf/{$}/clusters/{$}/add/node/{$}"

    };

    public static final String[] URL_ACCESS_PX3 = {
            ""
    };


    public static final String[] URL_ACCESS_SPECIAL_PORT_PX1 = {
            ""
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
