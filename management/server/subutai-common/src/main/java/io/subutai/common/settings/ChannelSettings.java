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
                "/rest/peer/register",
                "/rest/peer/register/{$}",
                "/rest/peer/reject",
                "/rest/peer/approve",
                "/rest/peer/remove",
                "/rest/peer/trust_request",
                "/rest/peer/trust_response",
                "/rest/peer/approve/{$}",

                "/rest/security/keyman/getpublickeyring",

                "/rest/pks/{$}"

        };

        public static final String[] REST_URL = {

            // All  Services

                "/{*}",
                "/rest/peer/register",
                "/rest/peer/register/{$}",
                "/rest/peer/reject",
                "/rest/peer/approve",
                "/rest/peer/remove",
                "/rest/peer/trust_request", "/rest/peer/trust_response", "/rest/peer/approve/{$}",
                "/rest/peer/",
                "/rest/peer/getlist",
                "/rest/peer/me",
                "/rest/peer/id",
                "/rest/peer/update",
                "/rest/peer/registered_peers",
                "/rest/peer/peer_policy",
                "/rest/peer/ping",
                "/rest/peer/template/get",
                "/rest/peer/vni",
                "/rest/peer/gateways",
                "/rest/peer/unregister",
                "/rest/peer/container/quota",
                "/rest/peer/container/quota/info",
                "/rest/peer/container/destroy",
                "/rest/peer/container/start",
                "/rest/peer/container/stop",
                "/rest/peer/container/isconnected",
                "/rest/peer/container/state",
                "/rest/peer/container/resource/usage",
                "/rest/peer/container/quota/ram/available",
                "/rest/peer/container/quota/cpu/available",
                "/rest/peer/container/quota/disk/available",
                "/rest/peer/container/quota/ram",
                "/rest/peer/container/quota/ram2",
                "/rest/peer/container/quota/ram/info",
                "/rest/peer/container/quota/cpu",
                "/rest/peer/container/quota/cpu/info",
                "/rest/peer/container/quota/cpuset",
                "/rest/peer/container/quota/disk",
                "/rest/peer/container/gateway",
                "/rest/peer/container/info",
                "/rest/peer/cert/import",
                "/rest/peer/cert/export",
                "/rest/peer/cert/remove",

                "/rest/environments",
                "/rest/environments/domain",
                "/rest/environments/container/environmentId",
                "/rest/environments/container/state",
                "/rest/environments/container/start",
                "/rest/environments/container/stop",

                //"/rest/environments/{environmentId}",
                "/rest/environments/{$}",
                "/rest/environments/grow",
                "/rest/environments/key",
                "/rest/environments/container",

                "/rest/hosts",

                //"/rest/identity/key/{username}",
                "/rest/identity/key/{$}",

                "/rest/messenger/message",

                "/rest/monitor/metrics/resource-hosts",

                //"/rest/monitor/metrics/containers-hosts/{environmentId}",
                "/rest/monitor/metrics/containers-hosts/{$}",

                "/rest/monitor/alert",

                "/rest/scripts/",

                //"/rest/scripts/{scriptName}",
                "/rest/scripts/{$}",

                "/rest/registry/templates",
                "/rest/registry/templates/import",
                "/rest/registry/templates/tree",

                //"/rest/registry/templates/arch/{lxcArch}",
                "/rest/registry/templates/arch/{$}",

                "/rest/registry/templates/plain-list",

                //"/rest/registry/templates/{templateName}",
                "/rest/registry/templates/{$}",

                //"/rest/registry/templates/arch/{lxcArch}/plain-list",
                "/rest/registry/templates/arch/{$}/plain-list",

                //"/rest/registry/templates/{templateName}/{templateVersion}/download/{token}",
                "/rest/registry/templates/{$}/{$}/download/{$}",

                //"/rest/registry/templates/{templateName}/{templateVersion}",
                "/rest/registry/templates/{$}/{$}",

                //"/rest/registry/templates/{templateName}/{templateVersion}/remove",
                "/rest/registry/templates/{$}/{$}/remove",

                //"/rest/registry/templates/{templateName}/{templateVersion}/arch/{lxcArch}",
                "/rest/registry/templates/{$}/{$}/arch/{$}",

                //"/rest/registry/templates/{childTemplateName}/parent",
                "/rest/registry/templates/{$}/parent",

                //"/rest/registry/templates/{childTemplateName}/{templateVersion}/arch/{lxcArch}/parent",
                "/rest/registry/templates/{$}/{$}/arch/{$}/parent",

                //"/rest/registry/templates/{childTemplateName}/{templateVersion}/parents",
                "/rest/registry/templates/{$}/{$}/parents",

                //"/rest/registry/templates/{parentTemplateName}/children",
                "/rest/registry/templates/{$}/children",

                //"/rest/registry/templates/{parentTemplateName}/{templateVersion}/children",
                "/rest/registry/templates/{$}/{$}/arch/{$}/children",

                //"/rest/registry/templates/{templateName}/{templateVersion}/is-used-on-fai",
                "/rest/registry/templates/{$}/{$}/is-used-on-fai",

                //"/rest/registry/templates/{templateName}/{templateVersion}/fai/{faiHostname}/is-used/{isInUse}",
                "/rest/registry/templates/{$}/{$}/fai/{$}/is-used/{$}",

                //"/rest/tracker/operations/{source}/{uuid}",
                "/rest/tracker/operations/{$}/{$}",

                //"/rest/tracker/operations/{source}/{dateFrom}/{dateTo}/{limit}",
                "/rest/tracker/operations/{$}/{$}/{$}/{$}",

                "/rest/tracker/operations/sources",

                //"/rest/{plugin_name}/clusters",
                "/rest/{$}/clusters",

                //"/rest/{$name_of_plugin}/clusters/{$`clusterName}",
                "/rest/{$}/clusters/{$}",

                //"/rest/{name_of_plugin}/install",
                "/rest/{$}/install",

                //"/rest/{name_of_plugin}/destroy",
                "/rest/{$}/destroy",

                //"/rest/{$name_of_plugin}/clusters/{$clusterName}/start",
                "/rest/{$}/clusters/{$}/start",

                //"/rest/{$name_of_plugin}/clusters/{$clusterName}/stop",
                "/rest/{$}/clusters/{$}/stop",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/destroy/node/{lxcHostname}/type/{nodeType}",
                "/rest/{$}/clusters/{$}/destroy/node/{$}/type/{$}",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/add/node/{lxcHostname}/type/{nodeType}",
                "/rest/{$}/clusters/{$}/add/node/{$}/type/{$}",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/check/node/{lxcHostname}",
                "/rest/{$}/clusters/{$}/check/node/{$}",

                //"/rest/{$name_of_plugin}/clusters/configure_environment/{environmentId}/clusterName/{clusterName}/nodes/{nodes}",
                "/rest/{$}/clusters/configure_environment/{$}/clusterName/{$}/nodes/{$}",

                //"/rest/{$name_of_plugin}/clusters/remove/{clusterName}",
                "/rest/{$}/clusters/remove/{$}",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/check",
                "/rest/{$}/clusters/{$}/check",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/start/node/{lxcHostname}",
                "/rest/{$}/clusters/{$}/start/node/{$}",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/stop/node/{lxcHostname}",
                "/rest/{$}/clusters/{$}/stop/node/{$}",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/add",
                "/rest/{$}/clusters/{$}/add",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/remove/node/{lxcHostname}",
                "/rest/{$}/clusters/{$}/remove/node/{$}",

                //"/rest/{$name_of_plugin}/configure_environment",
                "/rest/{$}/configure_environment",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/status",
                "/rest/{$}/clusters/{$}/status",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/status/secondary",
                "/rest/{$}/clusters/{$}/status/secondary",

                //"/rest/{$name_of_plugin}/clusters/job/{clusterName}/start",
                "/rest/{$}/clusters/job/{$}/start",

                //"/rest/{$name_of_plugin}/clusters/job/{clusterName}/stop",
                "/rest/{$}/clusters/job/{$}/stop",

                //"/rest/{$name_of_plugin}/clusters/job/{clusterName}/status",
                "/rest/{$}/clusters/job/{$}/status",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/nodes",
                "/rest/{$}/clusters/{$}/nodes",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/node/{hostname}/status",
                "/rest/{$}/clusters/{$}/node/{$}/status",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/task/{hostname}/status",
                "/rest/{$}/clusters/{$}/task/{$}/status",

                //"/rest/{$name_of_plugin}/clusters/destroy/{clusterName}",
                "/rest/{$}/clusters/destroy/{$}",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/add/node/{lxcHostName}",
                "/rest/{$}/clusters/{$}/add/node/{$}",

                //"/rest/{$}/clusters/{clusterName}/destroy/node/{lxcHostName}",
                "/rest/{$}/clusters/{$}/destroy/node/{$}",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/add/node/{lxcHostname}",
                "/rest/{$}/clusters/{$}/add/node/{$}",

                //"/rest/{$name_of_plugin}/clusters/remove/{clusterName}",
                "/rest/{$}/clusters/remove/{$}",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/remove/node/{lxcHostName}",
                "/rest/{$}/clusters/{$}/remove/node/{$}",

                //"/rest/{$name_of_plugin}/clusters/destroy/{clusterName}",
                "/rest/{$}/clusters/destroy/{$}",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/actualize_master_ip",
                "/rest/{$}/clusters/{$}/actualize_master_ip",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/change_master/nodes/{lxcHostName}/{keepSlave}",
                "/rest/{$}/clusters/{$}/change_master/nodes/{$}/{$}",

                //"/rest/{$name_of_plugin}/clusters/clusters/{clusterName}/start/node/{lxcHostName}/master/{master}",
                "/rest/{$}/clusters/clusters/{$}/start/node/{$}/master/{$}",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/stop/node/{lxcHostName}/master/{master}",
                "/rest/{$}/clusters/{$}/stop/node/{$}/master/{$}",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/check/node/{lxcHostName}/master/{master}",
                "/rest/{$}/clusters/{$}/check/node/{$}/master/{$}",

                //"/rest/{$name_of_plugin}/importData",
                "/rest/{$}/importData",

                //"/rest/{$name_of_plugin}/exportData",
                "/rest/{$}/exportData",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/add/node",
                "/rest/{$}/clusters/{$}/add/node",

                //"/rest/{$name_of_plugin}/clusters/{clusterName}/add/node{lxcHostname}"
                "/rest/{$}/clusters/{$}/add/node/{$}"

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
