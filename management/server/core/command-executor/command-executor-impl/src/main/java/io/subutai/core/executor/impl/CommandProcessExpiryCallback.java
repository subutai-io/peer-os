package io.subutai.core.executor.impl;


import java.util.Map;

import io.subutai.common.cache.EntryExpiryCallback;
import io.subutai.common.cache.ExpiringCache;


/**
 * Callback triggered when command process expires
 */
public class CommandProcessExpiryCallback implements EntryExpiryCallback<CommandProcess>
{
    private final ExpiringCache<String, Map<String, String>> requests;
    private final String rhId;
    private final String commandId;


    public CommandProcessExpiryCallback( final ExpiringCache<String, Map<String, String>> requests, String rhId,
                                         String commandId )
    {
        this.requests = requests;
        this.rhId = rhId;
        this.commandId = commandId;
    }


    @Override
    public void onEntryExpiry( final CommandProcess process )
    {
        process.stop();

        // purge command from requests
        synchronized ( requests )
        {
            Map<String, String> rhRequests = requests.get( rhId );

            if ( rhRequests != null )
            {
                rhRequests.remove( commandId );
            }
        }
    }
}
