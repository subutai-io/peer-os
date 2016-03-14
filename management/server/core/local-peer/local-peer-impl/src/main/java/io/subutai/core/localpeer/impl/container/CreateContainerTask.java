package io.subutai.core.localpeer.impl.container;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.NullHostInterface;
import io.subutai.common.peer.ContainerCreationException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.quota.ContainerResource;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.NumUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.registration.api.RegistrationManager;


public class CreateContainerTask implements Callable<ContainerHostInfo>
{
    protected static final Logger LOG = LoggerFactory.getLogger( CreateContainerTask.class );
    private final ResourceHost resourceHost;
    private final String hostname;
    private final TemplateKurjun template;

    private final String ip;
    private final int vlan;
    private final int timeoutSec;
    private final String environmentId;
    private final ContainerQuota quota;
    protected CommandUtil commandUtil = new CommandUtil();
    private HostRegistry hostRegistry;


    public CreateContainerTask( HostRegistry hostRegistry, final ResourceHost resourceHost,
                                final TemplateKurjun template, final String hostname, final ContainerQuota quota,
                                final String ip, final int vlan, final int timeoutSec, final String environmentId )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkNotNull( template );
        Preconditions.checkArgument( timeoutSec > 0 );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ) && ip.matches( Common.CIDR_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );

        this.hostRegistry = hostRegistry;
        this.resourceHost = resourceHost;
        this.template = template;
        this.hostname = hostname;
        this.ip = ip;
        this.vlan = vlan;
        this.timeoutSec = timeoutSec;
        this.environmentId = environmentId;
        this.quota = quota;
    }


    public static RegistrationManager getRegistrationManager() throws NamingException
    {

        return ServiceLocator.getServiceNoCache( RegistrationManager.class );
    }


    @Override
    public ContainerHostInfo call() throws Exception
    {

        List<BatchAction> actions = new ArrayList<>();

        BatchAction cloneAction = new BatchAction( "clone",
                Lists.newArrayList( template.getName(), hostname, "-i", String.format( "%s %s", ip, vlan ), "-t",
                        getRegistrationManager().generateContainerTTLToken( ( timeoutSec + 10 ) * 1000L )
                                                .getToken() ) );

        actions.add( cloneAction );
        for ( ContainerResource r : quota.getAllResources() )
        {

            BatchAction quotaAction = new BatchAction( "quota" );
            quotaAction.addArgument( hostname );
            quotaAction.addArgument( r.getContainerResourceType().getKey() );
            quotaAction.addArgument( "-s" );
            quotaAction.addArgument( r.getWriteValue() );
            actions.add( quotaAction );
        }

        commandUtil.execute(
                new RequestBuilder( String.format( "subutai batch -json '%s'", JsonUtil.toJson( actions ) ) )
                        .withTimeout( 1 ).daemon(), resourceHost );

        long start = System.currentTimeMillis();

        ContainerHostInfo hostInfo = null;
        String ip = null;
        long timePass = System.currentTimeMillis() - start;
        final int limit = timeoutSec * 1000;
        int counter = 0;
        while ( timePass < limit && ( Strings.isNullOrEmpty( ip ) ) )
        {
            TimeUnit.SECONDS.sleep( 1 );
            counter++;
            if ( counter % 30 == 0 )
            {
                LOG.debug( String.format( "Still waiting for %s. Time: %d/%d. %d sec", hostname, timePass, limit,
                        counter ) );
            }
            try
            {
                hostInfo = hostRegistry.getContainerHostInfoByHostname( hostname );

                HostInterface intf = hostInfo.getHostInterfaces().findByName( Common.DEFAULT_CONTAINER_INTERFACE );
                if ( !( intf instanceof NullHostInterface ) )
                {
                    ip = intf.getIp();
                }
            }
            catch ( HostDisconnectedException e )
            {
                //ignore
            }
            timePass = System.currentTimeMillis() - start;
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

        LOG.info( String.format( "Container '%s' successfully created.", hostname ) );
        LOG.debug( hostInfo.toString() );
        return hostInfo;
    }


    protected class BatchAction
    {
        private String action;
        private List<String> args = new ArrayList<>();


        public BatchAction( final String action, final List<String> args )
        {
            this.action = action;
            this.args = args;
        }


        public BatchAction( final String action )
        {
            this.action = action;
        }


        public void addArgument( final String arg )
        {
            this.args.add( arg );
        }
    }
}
