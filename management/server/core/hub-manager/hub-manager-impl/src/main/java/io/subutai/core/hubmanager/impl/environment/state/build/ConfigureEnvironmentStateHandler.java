package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.hub.share.dto.ansible.AnsibleDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class ConfigureEnvironmentStateHandler extends StateHandler
{
    private static final String TMP_DIR = "/tmp/";

    private long commandTimeout = 5L;


    public ConfigureEnvironmentStateHandler( Context ctx )
    {
        super( ctx, "Configure environment" );
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
        String containerId = ansibleDto.getAnsibleContainerId();
        String repoLink = ansibleDto.getRepoLink();
        String mainAnsibleScript = ansibleDto.getAnsibleRootFile();

        if ( ansibleDto.getCommandTimeout() != null )
        {
            commandTimeout = ansibleDto.getCommandTimeout();
        }

        prepareHostsFile( containerId, ansibleDto.getGroups() );

        copyRepoUnpack( containerId, repoLink );

        String out =
                runAnsibleScript( containerId, getDirLocation( repoLink ), mainAnsibleScript, ansibleDto.getVars() );

        peerDto.getAnsibleDto().setLogs( out );
    }


    private String runAnsibleScript( final String containerId, final String dirLocation, final String mainAnsibleScript,
                                     String extraVars )
    {
        if ( StringUtils.isEmpty( extraVars ) )
        {
            extraVars = "{}";
        }

        String cmd =
                String.format( "cd %s; ansible-playbook  %s --extra-vars %s", TMP_DIR + dirLocation, mainAnsibleScript,
                        extraVars );
        try
        {
            return runCmd( containerId, cmd );
        }
        catch ( Exception e )
        {
            log.error( "Error configuring environment", e );

            return e.getMessage();
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
            boolean reachable = isReachable( "www.github.com", containerId );

            while ( !reachable && count < 5 ) //break after 5th try
            {
                TimeUnit.SECONDS.sleep( count * 2 );
                reachable = isReachable( "www.github.com", containerId );
                count++;
                log.info( "No internet connection on container host {}", containerId );
            }

            String cmd = String.format( "cd %s; bash /root/get_unzip.sh %s", TMP_DIR, repoLink );
            runCmd( containerId, cmd );
        }
        catch ( Exception e )
        {
            log.error( "Error configuring environment", e );
        }
    }


    private void prepareHostsFile( final String containerId, Set<io.subutai.hub.share.dto.ansible.Group> groups )
    {
        try
        {
            for ( io.subutai.hub.share.dto.ansible.Group group : groups )
            {
                runCmd( containerId,
                        String.format( "echo %s >> /etc/ansible/hosts", String.format( "[%s]", group.getName() ) ) );

                for ( io.subutai.hub.share.dto.ansible.Host host : group.getHosts() )
                {
                    runCmd( containerId, String.format( "echo %s >> /etc/ansible/hosts", format( host ).trim() ) );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Error configuring environment", e );
        }
    }


    private static String format( io.subutai.hub.share.dto.ansible.Host host )
    {

        String f = "%s ansible_user=%s template=%s ansible_ssh_host=%s";

        //if template has python3, default is python2
        if ( host.getPythonPath() != null )
        {
            f += " ansible_python_interpreter=" + host.getPythonPath();
        }

        return String.format( f, host.getHostname(), host.getAnsibleUser(), host.getTemplateName(), host.getIp() );
    }


    private String runCmd( String containerId, String cmd ) throws HostNotFoundException, CommandException
    {

        Host host = ctx.localPeer.getContainerHostById( containerId );
        RequestBuilder rb =
                new RequestBuilder( cmd ).withTimeout( ( int ) TimeUnit.MINUTES.toSeconds( commandTimeout ) );

        CommandResult result = host.execute( rb );

        String msg = result.getStdOut();

        if ( !result.hasSucceeded() )
        {
            msg += " " + result.getStdErr();
            log.error( "Error configuring environment: {}", result );
        }

        return msg;
    }


    private boolean isReachable( String address, String containerId ) throws Exception
    {
        Host host = ctx.localPeer.getContainerHostById( containerId );

        CommandResult result =
                host.execute( new RequestBuilder( "ping" ).withCmdArgs( "-w", "10", "-c", "3", address ) );

        return result.hasSucceeded();
    }
}