package io.subutai.core.env.impl.tasks;


/**
 * Runnable that exposes wait method for clients
 */
public interface Awaitable extends Runnable
{
    public void waitCompletion() throws InterruptedException;
}
