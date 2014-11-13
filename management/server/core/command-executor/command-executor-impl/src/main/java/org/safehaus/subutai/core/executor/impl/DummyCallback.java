package org.safehaus.subutai.core.executor.impl;


import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.Response;


/**
 * Dummy callback
 */
public class DummyCallback extends CommandCallback
{
    @Override
    public void onResponse( final Response response, final CommandResult commandResult )
    {
        //dummy callback
    }
}
