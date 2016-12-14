package io.subutai.core.hubmanager.rest;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SystemSettings;
import io.subutai.common.util.RestUtil;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;


//TODO after finish ENVIRONMENT MANAGEMENT should delete this class
//TODO close webclient with RestUtil.close
public class VEHServiceUtil
{
    private static final Logger LOG = LoggerFactory.getLogger( VEHServiceUtil.class.getName() );


    private VEHServiceUtil()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static Response upSite( PeerManager peerManager, IdentityManager identityManager )
    {
        String projectName = "";
        String ownerName = "";
        String userName = "";
        String password = null;
        String domain = null;

        SystemSettings systemSettings = new SystemSettings();

        String url = "https://%s/vehs/rest/%s";

        String json =
                executeRequest( String.format( url, systemSettings.getHubIp(), peerManager.getLocalPeer().getId() ) );

        LOG.error( json );
        try
        {
            JSONObject jsonObject = new JSONObject( json );
            projectName = jsonObject.getString( "projectName" );
            ownerName = jsonObject.getString( "ownerName" );
            userName = jsonObject.getString( "userName" );
            password = jsonObject.getString( "password" );
            domain = jsonObject.getString( "domain" );

            LOG.error( jsonObject.toString() );
        }
        catch ( JSONException e )
        {
            LOG.warn( e.getMessage() );
        }
        setupSite( peerManager, identityManager, projectName, ownerName, userName, password, domain );

        executeRequestPost( String.format( url, systemSettings.getHubIp(), peerManager.getLocalPeer().getId() ), "" );
        return Response.status( Response.Status.OK ).build();
    }


    public static Response downSite( PeerManager peerManager, IdentityManager identityManager )
    {
        ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHosts().iterator().next();
        Set<ContainerHost> containerHosts = resourceHost.getContainerHosts();
        ContainerHost containerHost;

        for ( final ContainerHost containerHost1 : containerHosts )
        {
            containerHost = containerHost1;
            if ( "Container_12".equals( containerHost.getContainerName() ) )
            {
                String sptoken = identityManager.getSystemUserToken();
                String evnUrl = "%s/rest/ui/environments/%s?sptoken=%s";

                executeRequestDelete(
                        String.format( evnUrl, Common.DEFAULT_PUBLIC_URL, containerHost.getEnvironmentId().toString(),
                                sptoken ) );
            }
        }


        return Response.status( Response.Status.OK ).build();
    }


    private static void setupSite( PeerManager peerManager, IdentityManager identityManager, String projectName,
                                   String ownerName, String userName, String password, String domain )
    {
        ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHosts().iterator().next();

        String sptoken = identityManager.getSystemUserToken();

        String evnUrl = "%s/rest/v1/environments?sptoken=%s";


        String peerId = peerManager.getLocalPeer().getId();

        String body = "{\n" + "    \"id\": \"%s\",\n" + "    \"name\": \"VH: %s\",\n" + "    \"placement\": {\n"
                + "        \"%s\": [\n" + "{\n" + "\"name\": \"Container_12\",\n"
                + "                \"templateName\": \"staticWebsite\",\n" + "                \"type\": \"HUGE\",\n"
                + "                \"sshGroupId\": 0,\n" + "                \"hostsGroupId\": 0,\n"
                + "                \"peerId\": \"%s\",\n" + "                \"hostId\": \"%s\",\n"
                + "                \"hostname\": \"413717dc-aa0c-4201-a336-0270dd1582b5\"\n" + "   \n" + "         }\n"
                + "        ]\n" + "    },\n" + "    \"subnet\": null,\n" + "    \"sshKey\": null\n" + "}";


        boolean hasSite = hasStaticSite( peerManager );

        if ( !hasSite )
        {
            executeRequestPost( String.format( evnUrl, Common.DEFAULT_PUBLIC_URL, sptoken ),
                    String.format( body, UUID.randomUUID(), projectName, peerId, peerId, resourceHost.getId() ) );
        }

        TaskUtil.sleep( 20 * 1000L );

        String ip = deployStaticSite( peerManager, projectName, ownerName, userName, password );


        String conf =
                "echo " + "'# put this into /var/lib/apps/subutai/current/nginx-includes with name like 'blabla.conf'\n"
                        + "upstream %s-upstream {\n" + "#Add new host here\n" + "server %s;\n" + "\n" + "}\n" + "\n"
                        + "server{\n" + "listen 80;\n" + "server_name %s;\n" + "\n" + "location / {\n"
                        + "proxy_pass http://%s-upstream/;\n" + "proxy_set_header X-Real-IP $remote_addr;\n"
                        + "proxy_set_header Host $http_host;\n"
                        + "proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n" + "}\n" + "}\n'"
                        + " > /var/lib/apps/subutai/current/nginx-includes/101.conf";

        try
        {
            resourceHost.execute( new RequestBuilder( "mkdir -p /var/lib/apps/subutai/current/nginx-includes/" ) );

            resourceHost.execute( new RequestBuilder( String.format( conf, domain, ip, domain, domain ) ) );

            TaskUtil.sleep( 5 * 1000L );

            resourceHost.execute( new RequestBuilder( "systemctl restart *nginx*" ) );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
    }


    private static String deployStaticSite( PeerManager peerManager, final String projectName, final String ownerName,
                                            final String userName, final String password )
    {

        String url = "bash pullMySite.sh %s %s %s \"%s\"";
        ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHosts().iterator().next();
        Set<ContainerHost> containerHosts = resourceHost.getContainerHosts();
        ContainerHost containerHost;
        Iterator<ContainerHost> iterator = containerHosts.iterator();

        while ( iterator.hasNext() )
        {
            containerHost = iterator.next();

            if ( "Container_12".equals( containerHost.getContainerName() ) )
            {
                try
                {
                    containerHost.execute(
                            new RequestBuilder( String.format( url, projectName, ownerName, userName, password ) ) );

                    CommandResult commandResult = containerHost.execute( new RequestBuilder(
                            "/sbin/ifconfig $1 | grep \"inet addr\" |grep 192 | awk -F: '{print "
                                    + "$2}' | awk '{print $1}'" ) );

                    return commandResult.getStdOut();
                }
                catch ( Exception e )
                {
                    LOG.error( e.getMessage() );
                }
            }
        }

        return "";
    }


    private static boolean hasStaticSite( PeerManager peerManager )
    {
        ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHosts().iterator().next();
        Set<ContainerHost> containerHosts = resourceHost.getContainerHosts();
        ContainerHost containerHost;
        Iterator<ContainerHost> iterator = containerHosts.iterator();

        while ( iterator.hasNext() )
        {
            containerHost = iterator.next();

            if ( "Container_12".equals( containerHost.getContainerName() ) )
            {
                return true;
            }
        }
        return false;
    }


    public static String executeRequest( String request )
    {
        WebClient client = RestUtil.createTrustedWebClient( request );
        Response response = client.get();

        InputStream inputStream = response.readEntity( InputStream.class );

        LOG.error( "Request : " + request );
        LOG.error( "Resonse status: " + response.getStatus() );
        return getStringFromInputStream( inputStream );
    }


    public static String executeRequestPost( String request, String body )
    {
        WebClient client = RestUtil.createTrustedWebClient( request );
        client.accept( MediaType.APPLICATION_JSON );
        client.type( "application/json" );
        Response response = client.post( body );

        InputStream inputStream = response.readEntity( InputStream.class );

        LOG.error( "Request : " + request );
        LOG.error( "Resonse status: " + response.getStatus() );
        return getStringFromInputStream( inputStream );
    }


    public static String executeRequestDelete( String request )
    {
        WebClient client = RestUtil.createTrustedWebClient( request );
        client.accept( MediaType.APPLICATION_JSON );
        client.type( "application/json" );
        Response response = client.delete();

        InputStream inputStream = response.readEntity( InputStream.class );

        LOG.error( "Request : " + request );
        LOG.error( "Resonse status: " + response.getStatus() );
        return getStringFromInputStream( inputStream );
    }


    private static String getStringFromInputStream( InputStream is )
    {

        StringBuilder sb = new StringBuilder();

        try ( BufferedReader br = new BufferedReader( new InputStreamReader( is ) ) )
        {
            String line;
            while ( ( line = br.readLine() ) != null )
            {
                sb.append( line );
            }
        }
        catch ( IOException e )
        {
            LOG.error( e.getMessage() );
        }

        return sb.toString();
    }


    public static Response getChecksum( final PeerManager peerManager )
    {
        ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHosts().iterator().next();
        Set<ContainerHost> containerHosts = resourceHost.getContainerHosts();
        ContainerHost containerHost;
        Iterator<ContainerHost> iterator = containerHosts.iterator();

        while ( iterator.hasNext() )
        {
            containerHost = iterator.next();

            if ( "Container_12".equals( containerHost.getContainerName() ) )
            {
                CommandResult commandResult = null;
                try
                {
                    commandResult = containerHost.execute( new RequestBuilder( "bash checksum.sh /var/www" ) );

                    return Response.ok().entity( commandResult.getStdOut() ).build();
                }
                catch ( Exception e )
                {
                    LOG.error( e.getMessage() );
                }
                LOG.error( commandResult != null ? commandResult.getStdOut() : null );
            }
        }

        return null;
    }
}


