package org.safehaus.subutai.core.peer.impl;


import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.HostTaskResult;


/**
 * Created by timur on 11/24/14.
 */
public class CloneResult implements HostTaskResult<ContainerHost>
{
    private ContainerHost value;
    private boolean success = false;
    private Exception exception;


    @Override
    public ContainerHost getValue()
    {
        return value;
    }


    @Override
    public boolean isOk()
    {
        return success;
    }


    @Override
    public void ok( final ContainerHost containerHost )
    {
        this.value = containerHost;
        this.success = true;
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
