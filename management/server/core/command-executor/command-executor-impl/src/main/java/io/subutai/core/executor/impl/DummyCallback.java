package io.subutai.core.executor.impl;


import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.Response;


/**
 * Dummy callback
 */
public class DummyCallback implements CommandCallback
{
    @Override
    public void onResponse( final Response response, final CommandResult commandResult )
    {
        //dummy callback
    }
}
