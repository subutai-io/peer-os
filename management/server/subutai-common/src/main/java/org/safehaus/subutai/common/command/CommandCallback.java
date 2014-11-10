package org.safehaus.subutai.common.command;


/**
 * Callback that might be passed to Host.execute method
 */
public abstract class CommandCallback
{
    public abstract void onResponse( org.safehaus.subutai.common.protocol.Response response, CommandResult commandResult );
}
