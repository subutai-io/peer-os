package org.safehaus.subutai.core.lxc.quota.impl;


import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;
import org.safehaus.subutai.core.lxc.quota.api.QuotaException;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public class QuotaManagerImpl implements QuotaManager
{

    private PeerManager peerManager;
    private static Logger LOGGER = LoggerFactory.getLogger( QuotaManagerImpl.class );


    public QuotaManagerImpl( PeerManager peerManager )
    {
        Preconditions.checkNotNull( peerManager );
        this.peerManager = peerManager;
    }


    @Override
    public void setQuota( final String containerName, final QuotaEnum parameter, final String newValue )
            throws QuotaException
    {
        Preconditions.checkNotNull( containerName, "Target containerName is null." );
        Preconditions.checkNotNull( parameter, "Parameter is null." );
        Preconditions.checkNotNull( newValue, "New value to set is null." );

        String precomputedString =
                String.format( "lxc-cgroup -n %s %s %s", containerName, parameter.getKey(), newValue );

        runCommand( new RequestBuilder( precomputedString ), false, containerName, parameter );
    }


    @Override
    public String getQuota( final String containerName, final QuotaEnum parameter ) throws QuotaException
    {
        Preconditions.checkNotNull( containerName, "Target containerName is null." );
        Preconditions.checkNotNull( parameter, "Parameter is null." );

        String precomputedString = String.format( "lxc-cgroup -n %s %s", containerName, parameter.getKey() );

        return runCommand( new RequestBuilder( precomputedString ), true, containerName, parameter );
    }


    private String runCommand( RequestBuilder requestBuilder, boolean givesOutput, String hostname,
                               QuotaEnum parameter ) throws QuotaException
    {
        try
        {
            ResourceHost host = getResourceHost( hostname );
            CommandResult result = host.execute( requestBuilder );
            if ( !result.hasSucceeded() )
            {
                if ( result.hasCompleted() )
                {
                    throw new QuotaException( String.format(
                            "Error while performing [lxc-cgroup -n %1$s %2$s]: %3$s%n%4$s, exit code %5$s",
                            host.getHostname(), parameter.getKey(), result.getStdOut(), result.getStdErr(),
                            result.getExitCode() ) );
                }
                else
                {
                    throw new QuotaException(
                            String.format( "Error while performing [lxc-cgroup -n %1$s %2$s]: Command timed out",
                                    host.getHostname(), parameter.getKey() ) );
                }
            }
            else if ( givesOutput )
            {
                LOGGER.info( result.getStdOut() );
                return result.getStdOut();
            }
        }
        catch ( CommandException e )
        {
            LOGGER.error( "Error executing lxc-cgroup command.", e.getMessage() );
            throw new QuotaException( e );
        }
        return "";
    }


    private ResourceHost getResourceHost( String containerName ) throws QuotaException
    {
        try
        {
            ContainerHost containerHost = peerManager.getLocalPeer().getContainerHostByName( containerName );
            return peerManager.getLocalPeer().getResourceHostByName( containerHost.getParentHostname() );
        }
        catch ( PeerException e )
        {
            throw new QuotaException( e );
        }
    }
}
