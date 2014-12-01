package org.safehaus.subutai.core.peer.api;


/**
 * Created by timur on 11/24/14.
 */
public interface HostTaskResult<T>
{
    public T getValue();

    public boolean isOk();

    public void ok( final T value );

    public void fail( Exception exception );

    public Exception getException();
}
