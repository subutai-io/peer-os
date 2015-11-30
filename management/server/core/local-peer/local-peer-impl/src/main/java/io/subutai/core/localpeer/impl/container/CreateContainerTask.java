package io.subutai.core.localpeer.impl.container;


import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.peer.ContainerCreationException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.Template;
import io.subutai.common.settings.Common;
import io.subutai.common.util.NumUtil;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;


public class CreateContainerTask implements Callable<ContainerHostInfo>
{
    protected static final Logger LOG = LoggerFactory.getLogger( CreateContainerTask.class );
    private static final int TEMPLATE_IMPORT_TIMEOUT_SEC = 10 * 60 * 60;
    private final ResourceHost resourceHost;
    private final String hostname;
    private final Template template;
    private final String ip;
    private final int vlan;
    private final int timeoutSec;
//    private final String environmentId;
    protected CommandUtil commandUtil = new CommandUtil();
    private HostRegistry hostRegistry;


    public CreateContainerTask( HostRegistry hostRegistry, final ResourceHost resourceHost, final Template template,
                                final String hostname, final String ip, final int vlan, final int timeoutSec/*,
                                final String environmentId*/ )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkNotNull( template );
        Preconditions.checkArgument( timeoutSec > 0 );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ) && ip.matches( Common.CIDR_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );
//        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );

        this.hostRegistry = hostRegistry;
        this.resourceHost = resourceHost;
        this.template = template;
        this.hostname = hostname;
        this.ip = ip;
        this.vlan = vlan;
        this.timeoutSec = timeoutSec;
//        this.environmentId = environmentId;
    }


    @Override
    public ContainerHostInfo call() throws Exception
    {

        prepareTemplate( template );

        commandUtil.execute( new RequestBuilder( "subutai clone" ).withCmdArgs(
                Lists.newArrayList( template.getTemplateName(), hostname, "-i",
                        String.format( "\"%s %s\"", ip, vlan ) ) ).withTimeout( 1 ).daemon(), resourceHost );

        long start = System.currentTimeMillis();

        ContainerHostInfo hostInfo = null;
        String ip = null;
        while ( System.currentTimeMillis() - start < timeoutSec * 1000 && ( hostInfo == null || Strings
                .isNullOrEmpty( ip ) ) )
        {
            Thread.sleep( 100 );
            try
            {
                hostInfo = hostRegistry.getContainerHostInfoByHostname( hostname );
                //TODO: use findByName() method
                for ( HostInterface intf : hostInfo.getHostInterfaces().getAll() )
                {
                    if ( Common.DEFAULT_CONTAINER_INTERFACE.equals( intf.getName() ) )
                    {
                        ip = intf.getIp();
                        break;
                    }
                }
            }
            catch ( HostDisconnectedException e )
            {
                //ignore
            }
        }

        if ( hostInfo == null )
        {
            throw new ContainerCreationException(
                    String.format( "Container %s did not connect within timeout with proper IP", hostname ) );
        }
        else
        {
            //TODO sign CH key with PEK identified by LocalPeerId+environmentId
            //at this point the CH key is already in the KeyStore and might be just updated.
        }

        return hostInfo;
    }


    protected void prepareTemplate( final Template template ) throws ResourceHostException
    {
        Preconditions.checkNotNull( template, "Invalid template" );

        if ( !templateExists( template ) )
        {
            importTemplate( template );

            if ( !templateExists( template ) )
            {
                LOG.debug( String.format( "Could not prepare template %s on %s.", template.getTemplateName(),
                        resourceHost.getHostname() ) );
                throw new ResourceHostException(
                        String.format( "Could not prepare template %s on %s", template.getTemplateName(),
                                resourceHost.getHostname() ) );
            }
        }
    }


    protected boolean templateExists( final Template template ) throws ResourceHostException
    {
        Preconditions.checkNotNull( template, "Invalid template" );

        try
        {
            CommandResult commandresult = resourceHost.execute( new RequestBuilder( "subutai list -t" )
                    .withCmdArgs( Lists.newArrayList( template.getTemplateName() ) ) );
            if ( commandresult.hasSucceeded() )
            {
                String[] lines = commandresult.getStdOut().split( "\n" );
                if ( lines.length == 3 && lines[2].startsWith( template.getTemplateName() ) )
                {
                    LOG.debug( String.format( "Template %s exists on %s.", template.getTemplateName(),
                            resourceHost.getHostname() ) );
                    return true;
                }
            }
            LOG.warn( String.format( "Template %s does not exists on %s.", template.getTemplateName(),
                    resourceHost.getHostname() ) );
            return false;
        }
        catch ( CommandException ce )
        {
            LOG.error( "Command exception.", ce );
            throw new ResourceHostException( "General command exception on checking container existence.", ce );
        }
    }


    protected void importTemplate( final Template template ) throws ResourceHostException
    {
        Preconditions.checkNotNull( template, "Invalid template" );

        try
        {
            commandUtil.execute( new RequestBuilder( "subutai import" ).withTimeout( TEMPLATE_IMPORT_TIMEOUT_SEC )
                                                                       .withCmdArgs( Lists.newArrayList(
                                                                               template.getTemplateName() ) ),
                    resourceHost );
        }
        catch ( CommandException ce )
        {
            LOG.error( "Template import failed", ce );
            throw new ResourceHostException( "Template import failed", ce );
        }
    }
}
