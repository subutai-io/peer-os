package org.safehaus.subutai.common.command;


/**
 * Callback that might be passed to Host.execute method
 */
public interface CommandCallback
{


    public void onResponse( Response response, CommandResult commandResult );
}
