package org.safehaus.subutai.common.command;


/**
 * Callback that might be passed to Host.execute method
 */
public abstract class CommandCallback
{
    private volatile boolean stopped;


    public final void stop()
    {
        stopped = true;
    }


    public final boolean isStopped()
    {
        return stopped;
    }


    public abstract void onResponse( org.safehaus.subutai.common.protocol.Response response,
                                     CommandResult commandResult );
}
