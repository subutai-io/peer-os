package io.subutai.core.executor.impl;


import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.Response;


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
