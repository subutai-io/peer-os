package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class ConfigureEnvironmentStateHandler extends StateHandler
{
    private final CommandUtil commandUtil = new CommandUtil();


    public ConfigureEnvironmentStateHandler( Context ctx )
    {
        super( ctx, "Configure environment" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws HubManagerException
    {
        try
        {
            logStart();

            Object result = configure( peerDto );

            logEnd();

            return result;
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
    }


    private Object configure( final EnvironmentPeerDto peerDto )
    {

        if ( StringUtils.isNotEmpty( peerDto.getAnsible() ) && StringUtils.isNotEmpty( peerDto.getPlaybook() ) )
        {
            try
            {
                runPlaybook( peerDto.getAnsible(), peerDto.getPlaybook() );
            }
            catch ( HostNotFoundException ignore )
            {
                //ignore: skipping due to ansible container absence
            }
            catch ( CommandException ce )
            {
                peerDto.setError( ce.getMessage() );
            }
        }
        else
        {
            peerDto.setMessage( "Skipping. Ansible settings is empty." );
        }

        return peerDto;
    }


    private String runPlaybook( String containerId, String playbook ) throws HostNotFoundException, CommandException
    {
        CommandResult result;

        Host host = ctx.localPeer.getContainerHostById( containerId );
        final String command = String.format( "cd /root/playbooks/%s && ansible-playbook site.yaml", playbook );

        RequestBuilder rb = new RequestBuilder( command );
        rb.withTimeout( ( int ) TimeUnit.MINUTES.toSeconds( 5 ) );

        result = commandUtil.execute( rb, host );


        return result.getStdOut();
    }
}