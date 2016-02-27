package io.subutai.core.localpeer.impl.container;


import java.util.concurrent.TimeUnit;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultParseException;
import io.subutai.common.command.CommandResultParser;
import io.subutai.common.environment.CloneRequest;
import io.subutai.common.environment.CloneResponse;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.NullHostInterface;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.quota.ContainerResource;
import io.subutai.common.settings.Common;
import io.subutai.common.task.Command;
import io.subutai.common.task.CommandBatch;
import io.subutai.common.util.ServiceLocator;
import io.subutai.common.util.StringUtil;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.localpeer.impl.LocalPeerImpl;
import io.subutai.core.registration.api.RegistrationManager;


public class CloneTask extends AbstractTask<CloneRequest, CloneResponse> implements CommandResultParser<CloneResponse>
{
    protected static final Logger LOG = LoggerFactory.getLogger( CloneTask.class );

    private static final int CLONE_TIMEOUT = 60 * 10; // 10 min

    private final LocalPeerImpl localPeer;
    private final int vlan;
    private HostRegistry hostRegistry;
    private ContainerQuota quota;


    public CloneTask( LocalPeerImpl localPeer, HostRegistry hostRegistry, ContainerQuota quota, int vlan,
                      CloneRequest request )
    {
        super( request );
        this.localPeer = localPeer;
        this.hostRegistry = hostRegistry;
        this.quota = quota;
        this.vlan = vlan;
    }


    public static RegistrationManager getRegistrationManager() throws NamingException
    {

        return ServiceLocator.getServiceNoCache( RegistrationManager.class );
    }


    public CommandBatch getCommandBatch() throws Exception
    {
        CommandBatch result = new CommandBatch();

        Command cloneAction = new Command( "clone",
                Lists.newArrayList( request.getTemplateName(), request.getHostname(), "-i",
                        String.format( "\"%s %d\"", request.getIp(), this.vlan ), "-e", request.getEnvironmentId(),
                        "-t", getRegistrationManager().generateContainerTTLToken( ( CLONE_TIMEOUT + 10 ) * 1000L )
                                                      .getToken() ) );

        result.addCommand( cloneAction );

        for ( ContainerResource r : quota.getAllResources() )
        {

            Command quotaCommand = new Command( "quota" );
            quotaCommand.addArgument( request.getHostname() );
            quotaCommand.addArgument( r.getContainerResourceType().getKey() );
            quotaCommand.addArgument( "-s" );
            quotaCommand.addArgument( r.getWriteValue() );
            result.addCommand( quotaCommand );
        }

        return result;
    }


    @Override
    public CommandResultParser<CloneResponse> getCommandResultParser()
    {
        return this;
    }


    @Override
    public CloneResponse parse( final CommandResult commandResult ) throws CommandResultParseException
    {
        ContainerHostInfoModel r = null;
        int counter = 0;
        String ip = null;
        while ( ip == null && counter < Common.WAIT_CONTAINER_CONNECTION_SEC )
        {
            try
            {
                r = findHostInfo();
                HostInterface i = r.getHostInterfaces().findByName( Common.DEFAULT_CONTAINER_INTERFACE );
                if ( !( i instanceof NullHostInterface ) )
                {
                    ip = i.getIp();
                }
                TimeUnit.SECONDS.sleep( 1 );
            }
            catch ( HostDisconnectedException | InterruptedException e )
            {
                // ignore
            }
            counter++;
        }

        if ( r == null )
        {
            throw new CommandResultParseException( "Heartbeat not received from: " + request.getContainerName() );
        }

        if ( ip == null )
        {
            throw new CommandResultParseException( "IP not assigned: " + request.getContainerName() );
        }

        return new CloneResponse( request.getResourceHostId(), request.getHostname(), request.getContainerName(),
                r.getId(), request.getIp(), request.getTemplateName(), request.getTemplateArch() );
    }


    private ContainerHostInfoModel findHostInfo() throws HostDisconnectedException
    {
        final ContainerHostInfo info = hostRegistry.getContainerHostInfoByHostname( request.getHostname() );

        return new ContainerHostInfoModel( info );
    }


    @Override
    public int getTimeout()
    {
        return CLONE_TIMEOUT;
    }


    @Override
    public boolean isSequential()
    {
        return false;
    }
}
