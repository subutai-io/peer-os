package io.subutai.core.env.impl.exception;


/**
 * Generic type class to hold certain {@link java.lang.Exception} parameter, handled at thread execution.
 *
 * @param <T> - object type to store as parameter
 *
 * @see EnvironmentBuildException
 * @see NodeGroupBuildException
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
