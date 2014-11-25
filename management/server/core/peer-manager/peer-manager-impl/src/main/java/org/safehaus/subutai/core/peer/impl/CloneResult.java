package org.safehaus.subutai.core.peer.impl;


import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.HostTaskResult;


/**
 * Created by timur on 11/24/14.
 */
public class CloneResult implements HostTaskResult
{
    private ContainerHost containerHost;
    private boolean success = false;
    private Exception exception;


    public ContainerHost getContainerHost()
    {
        return containerHost;
    }


    public void setContainerHost( final ContainerHost containerHost )
    {
        this.containerHost = containerHost;
        this.success = true;
    }


    @Override
    public boolean isOk()
    {
        return success;
    }


    @Override
    public void fail( final Exception exception )
    {
        this.exception = exception;
        this.success = false;
    }


    @Override
    public Exception getException()
    {
        return exception;
    }
}
