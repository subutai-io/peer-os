package org.safehaus.subutai.core.env.impl.exception;


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
