package org.safehaus.subutai.core.peer.impl;


import org.safehaus.subutai.core.peer.api.CloneParam;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.HostEvent;
import org.safehaus.subutai.core.peer.api.HostTask;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.api.ResourceHostException;


public class CloneTask extends HostTask<ResourceHost, CloneParam, CloneResult>
{


    public CloneTask( final String groupId, final ResourceHost resourceHost, final CloneParam parameter )
    {
        super( groupId, resourceHost, parameter );
        result = new CloneResult();
    }


    @Override
    public void execute()
    {
        try
        {
            host.prepareTemplates( getParameter().getTemplates() );
            host.cloneContainer( getParameter().getTemplateName(), getParameter().getHostname() );
            ContainerHost containerHost = waitContainerHost( param.getHostname(), 180 );
            if ( containerHost != null )
            {
                LOG.info( String.format( "Container %s with id %s on %s cloned successfully.", param.getHostname(),
                        containerHost.getId(), host.getHostname() ) );
                result.ok( containerHost );
            }
            else
            {
                LOG.debug( String.format( "Cloning container %s on %s failed.", param.getHostname(),
                        host.getHostname() ) );
                result.fail( new ResourceHostException(
                        String.format( "Container %s on %s is not cloned in estimated timeout.", host.getHostname(),
                                param.getHostname() ), "" ) );
                getHost().fireEvent( new HostEvent( host, HostEvent.EventType.HOST_CLONE_FAIL, containerHost ) );
            }
        }

        catch ( Exception e )
        {
            LOG.debug( String.format( "Cloning container %s on %s failed.", param.getHostname(), host.getHostname() ),
                    e );
            fail( new ResourceHostException(
                    String.format( "Error on cloning container %s on %s.", host.getHostname(), param.getHostname() ),
                    e.toString() ) );
        }
    }


    private ContainerHost waitContainerHost( final String hostname, final int timeout )
    {
        long threshold = System.currentTimeMillis() + timeout * 1000;
        LOG.debug( String.format( "Waiting for container %s on %s.", hostname, getHost().getHostname() ) );
        ContainerHost containerHost;
        do
        {
            try
            {
                Thread.sleep( 2000 );
            }
            catch ( InterruptedException ignore )
            {
            }
            containerHost = host.getContainerHostByName( hostname );
        }
        while ( threshold > System.currentTimeMillis() && containerHost == null );
        return containerHost;
    }


    @Override
    public void start()
    {
        LOG.debug( String.format( "Scheduling %s clone task on %s for container %s using template %s", getId(),
                host.getHostname(), param.getHostname(), param.getTemplateName() ) );
        getHost().queue( this );
    }


    @Override
    public CloneResult getResult()
    {
        return result;
    }
}
