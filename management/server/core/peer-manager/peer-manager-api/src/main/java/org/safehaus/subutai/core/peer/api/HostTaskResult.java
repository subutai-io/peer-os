package org.safehaus.subutai.core.peer.api;


/**
 * Created by timur on 11/24/14.
 */
public interface HostTaskResult
{
    public boolean isOk();

    public void fail( Exception exception );

    public Exception getException();
}
