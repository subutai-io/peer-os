package org.safehaus.subutai.core.peer.api;


public interface HostTaskResult<T>
{
    public T getValue();

    public boolean isOk();

    public void ok( final T value );

    public void fail( Exception exception );

    public Exception getException();
}
