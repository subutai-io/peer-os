package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandStatus;
import io.subutai.common.command.CommandUtil;
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

    private static final String ANSIBLE_LINK = "/rest/v1/environments/%s/ansible";
    private static final String TMP_DIR = "/tmp/";

    private final CommandUtil commandUtil = new CommandUtil();
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
        if ( extraVars == null || StringUtils.isEmpty( extraVars ) )
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
        catch ( HostNotFoundException | CommandException e )
        {
            e.printStackTrace();
            return e.getMessage();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return "";
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
        String cmd = String.format( "cd %s; bash /root/get_unzip.sh %s", TMP_DIR, repoLink );

        try
        {
            runCmd( containerId, cmd );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    private void prepareHostsFile( final String containerId, Set<io.subutai.hub.share.dto.ansible.Group> groups )
    {
        for ( io.subutai.hub.share.dto.ansible.Group group : groups )
        {
            try
            {
                runCmd( containerId,
                        String.format( "echo %s >> /etc/ansible/hosts", String.format( "[%s]", group.getName() ) ) );

                for ( io.subutai.hub.share.dto.ansible.Host host : group.getHosts() )
                {
                    runCmd( containerId, String.format( "echo %s >> /etc/ansible/hosts", format( host ).trim() ) );
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
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
        RequestBuilder rb = new RequestBuilder( cmd );
        rb.withTimeout( ( int ) TimeUnit.MINUTES.toSeconds( commandTimeout ) );

        CommandResult result = host.execute( rb );

        String msg = result.getStdOut();
        if ( msg == null || msg.isEmpty() )
        {
            msg = result.getStdErr();
        }

        return msg;
    }
}