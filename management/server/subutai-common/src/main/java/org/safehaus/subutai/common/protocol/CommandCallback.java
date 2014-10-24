package org.safehaus.subutai.common.protocol;


/**
 * Callback that might be passed to Host.execute method
 */
public abstract class CommandCallback
{
    public abstract void onResponse( Response response, CommandResult commandResult );
}
