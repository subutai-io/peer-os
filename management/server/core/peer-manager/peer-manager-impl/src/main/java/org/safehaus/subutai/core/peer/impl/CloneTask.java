package org.safehaus.subutai.core.peer.impl;


import org.safehaus.subutai.core.peer.api.CloneParam;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.HostEvent;
import org.safehaus.subutai.core.peer.api.HostTask;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.api.ResourceHostException;


public class CloneTask extends HostTask<ResourceHost, CloneParam, CloneResult>
{


    public CloneTask( final ResourceHost resourceHost, final CloneParam parameter )
    {
        super( resourceHost, parameter );
        result = new CloneResult();
    }


    @Override
    public void execute()
    {
        try
        {
            LOG.debug( String.format( "Preparing templates on %s...", host.getHostname() ) );
            host.prepareTemplates( getParameter().getTemplates() );
            LOG.debug( String.format( "Cloning container %s on %s...", param.getHostname(), host.getHostname() ) );
            host.cloneContainer( getParameter().getTemplateName(), getParameter().getHostname() );
            ContainerHost containerHost = host.getContainerHostByName( param.getHostname() );
            result.setContainerHost( containerHost );
            if ( containerHost != null )
            {
                LOG.info( String.format( "Container %s with id %s on %s cloned successfully.", param.getHostname(),
                        containerHost.getId(), host.getHostname() ) );
                done();
            }
            else
            {
                LOG.debug( String.format( "Cloning container %s on %s failed.", param.getHostname(),
                        host.getHostname() ) );
                fail( new ResourceHostException(
                        String.format( "Container %s on % is not cloned in estimated time.", host.getHostname(),
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


    @Override
    public void start()
    {
        getHost().queue( this );
    }


    @Override
    public CloneResult getResult()
    {
        return result;
    }
}
