package io.subutai.core.executor.impl;


import io.subutai.common.cache.EntryExpiryCallback;


/**
 * Callback triggered when command process expires
 */
class CommandProcessExpiryCallback implements EntryExpiryCallback<CommandProcess>
{
    @Override
    public void onEntryExpiry( final CommandProcess process )
    {
        process.stop();
    }
}
