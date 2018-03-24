package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.command.Response;
import io.subutai.common.environment.Environment;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.core.hubmanager.api.RestClient;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.hub.share.dto.ansible.AnsibleDto;
import io.subutai.hub.share.dto.ansible.Group;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class ConfigureEnvironmentStateHandler extends StateHandler
{
    private static final String ENV_APPS_URL = "/rest/v1/environments/%s/apps";

    private static final String TMP_DIR = "/root/";

    private long commandTimeout = 5L;

    public final RestClient restClient;


    public ConfigureEnvironmentStateHandler( Context ctx )
    {
        super( ctx, "Configure environment" );
        restClient = ctx.restClient;
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws HubManagerException
    {

        logStart();

        AnsibleDto ansibleDto = peerDto.getAnsibleDto();

        if ( ansibleDto != null )
        {
            startConfiguration( ansibleDto, peerDto );
        }

        logEnd();

        return peerDto;
    }


    private void startConfiguration( AnsibleDto ansibleDto, EnvironmentPeerDto peerDto )
    {
        if ( ansibleDto.getCommandTimeout() != null )
        {
            commandTimeout = ansibleDto.getCommandTimeout();
        }

        prepareHostsFile( ansibleDto.getAnsibleContainerId(), ansibleDto.getGroups() );

        copyRepoUnpack( ansibleDto.getAnsibleContainerId(), ansibleDto.getRepoLink() );

        runAnsibleScript( ansibleDto, peerDto.getEnvironmentInfo().getId() );


        //invalidate desktop information cache
        try
        {
            Environment environment = ctx.envManager.loadEnvironment( peerDto.getEnvironmentInfo().getId() );
            for ( EnvironmentContainerHost host : environment.getContainerHostsByPeerId( peerDto.getPeerId() ) )
            {
                ctx.desktopManager.invalidate( host.getId() );
            }
        }
        catch ( Exception e )
        {
            log.info( e.getMessage() );
        }
    }


    private void runAnsibleScript( AnsibleDto ansibleDto, String envSubutaiId )
    {
        final String containerId = ansibleDto.getAnsibleContainerId();
        final String dirLocation = getDirLocation( ansibleDto.getRepoLink() );
        final String mainAnsibleScript = ansibleDto.getAnsibleRootFile();
        String extraVars = ansibleDto.getVars();


        if ( StringUtils.isEmpty( ansibleDto.getVars() ) )
        {
            extraVars = "{}";
        }

        String cmd =
                String.format( "cd %s; ansible-playbook  %s --extra-vars %s", TMP_DIR + dirLocation, mainAnsibleScript,
                        extraVars );
        try
        {
            runCmdAsync( containerId, cmd, envSubutaiId );
        }
        catch ( Exception e )
        {
            log.error( "Error configuring environment", e );
        }
    }


    private String getDirLocation( String repoLink )
    {
        repoLink = repoLink.replaceAll( "https://github.com/", "" );
        repoLink = repoLink.replaceAll( "/archive/", "-" );
        repoLink = repoLink.replaceAll( ".zip", "" );
        return repoLink.split( "/" )[1];
    }


    private void copyRepoUnpack( final String containerId, final String repoLink )
    {

        try
        {
            int count = 1;
            boolean reachable = isGithubReachable( containerId );

            while ( !reachable && count < 5 ) //break after 5th try
            {
                TimeUnit.SECONDS.sleep( count * 2 );
                reachable = isGithubReachable( containerId );
                count++;
                log.info( "No internet connection on container host {}", containerId );
            }

            String cmd = String.format( "cd %s; bash get_unzip.sh %s", TMP_DIR, repoLink );
            runCmd( containerId, cmd );
        }
        catch ( Exception e )
        {
            log.error( "Error configuring environment", e );
        }
    }


    private boolean isGithubReachable( String containerId ) throws Exception
    {
        Host host = ctx.localPeer.getContainerHostById( containerId );

        CommandResult result =
                host.execute( new RequestBuilder( "ping" ).withCmdArgs( "-w", "10", "-c", "3", "www.github.com" ) );

        return result.hasSucceeded();
    }


    private void prepareHostsFile( final String containerId, Set<io.subutai.hub.share.dto.ansible.Group> groups )
    {
        String groupName;
        String inventoryLine;
        try
        {
            for ( Group group : groups )
            {

                groupName = String.format( "[%s]", group.getName() );

                runCmd( containerId,
                        String.format( "grep -q -F '%s' /etc/ansible/hosts || echo '%s' >> /etc/ansible/hosts",
                                groupName, groupName ) );

                for ( io.subutai.hub.share.dto.ansible.Host host : group.getHosts() )
                {
                    inventoryLine = format( host ).trim();
                    runCmd( containerId,
                            String.format( "grep -q -F '%s' /etc/ansible/hosts || echo '%s' >> /etc/ansible/hosts",
                                    inventoryLine, inventoryLine ) );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Error configuring environment", e );
        }
    }


    private void runCmd( String containerId, String cmd ) throws HostNotFoundException, CommandException
    {
        Host host = ctx.localPeer.getContainerHostById( containerId );

        RequestBuilder rb =
                new RequestBuilder( cmd ).withTimeout( ( int ) TimeUnit.MINUTES.toSeconds( commandTimeout ) );

        CommandResult result = host.execute( rb );


        if ( !result.hasSucceeded() )
        {

            log.error( "Error configuring environment: {}", result );
        }
    }


    private static String format( io.subutai.hub.share.dto.ansible.Host host )
    {

        String inventoryLineFormat = "%s ansible_user=%s template=%s ansible_ssh_host=%s";

        //if template has python3, default is python2
        if ( host.getPythonPath() != null )
        {
            inventoryLineFormat += " ansible_python_interpreter=" + host.getPythonPath();
        }

        return String.format( inventoryLineFormat, host.getHostname(), host.getAnsibleUser(), host.getTemplateName(),
                host.getIp() );
    }


    private void runCmdAsync( String containerId, String cmd, String envSubutaiId )
            throws HostNotFoundException, CommandException
    {
        Host host = ctx.localPeer.getContainerHostById( containerId );

        RequestBuilder rb =
                new RequestBuilder( cmd ).withTimeout( ( int ) TimeUnit.MINUTES.toSeconds( commandTimeout ) );


        AnsibleCallback ansibleCallback = new AnsibleCallback( envSubutaiId );

        host.execute( rb, ansibleCallback );
    }


    private class AnsibleCallback implements CommandCallback
    {
        final String envSubutaiId;


        private List<Integer> cache = new ArrayList<>();


        AnsibleCallback( String envSubutaiId )
        {
            this.envSubutaiId = envSubutaiId;
        }


        @Override
        public void onResponse( final Response response, final CommandResult commandResult )
        {

            if ( cache.contains( response.getResponseNumber() ) )
            {
                return;
            }
            else
            {
                cache.add( response.getResponseNumber() );
            }

            AnsibleDto ansibleDto = new AnsibleDto();

            ansibleDto.setState( AnsibleDto.State.IN_PROGRESS );

            if ( commandResult.hasCompleted() )
            {
                if ( !commandResult.hasSucceeded() )
                {
                    ansibleDto.setState( AnsibleDto.State.FAILED );
                    ansibleDto.setLogs( commandResult.getStdErr() + commandResult.getStdOut() );
                }
                else
                {
                    ansibleDto.setState( AnsibleDto.State.SUCCESS );
                    ansibleDto.setLogs( commandResult.getStdOut() );
                }
            }
            else
            {
                ansibleDto.setLogs( commandResult.getStdOut() );
            }

            String path = String.format( ENV_APPS_URL, this.envSubutaiId );

            restClient.post( path, ansibleDto );
        }
    }
}