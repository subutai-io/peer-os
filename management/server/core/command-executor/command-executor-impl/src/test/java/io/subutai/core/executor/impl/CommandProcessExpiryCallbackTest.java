package io.subutai.core.executor.impl;


import org.junit.Test;

import io.subutai.core.executor.impl.CommandProcess;
import io.subutai.core.executor.impl.CommandProcessExpiryCallback;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class CommandProcessExpiryCallbackTest
{

    CommandProcessExpiryCallback callback = new CommandProcessExpiryCallback();


    @Test
    public void testOnEntryExpiry() throws Exception
    {
        CommandProcess commandProcess = mock( CommandProcess.class );
        callback.onEntryExpiry( commandProcess );

        verify( commandProcess ).stop();
    }
}
