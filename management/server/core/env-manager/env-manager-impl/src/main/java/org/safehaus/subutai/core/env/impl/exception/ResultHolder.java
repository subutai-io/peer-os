package org.safehaus.subutai.core.env.impl.exception;


/**
 * Generic type class to hold certain {@link java.lang.Exception} parameter, handled at thread execution.
 *
 * @param <T> - object type to store as parameter
 *
 * @see org.safehaus.subutai.core.env.impl.exception.EnvironmentBuildException
 * @see org.safehaus.subutai.core.env.impl.exception.NodeGroupBuildException
 */
public class ResultHolder<T>
{
    private T result;


    public T getResult()
    {
        return result;
    }


    public void setResult( final T result )
    {
        this.result = result;
    }
}
