package io.subutai.core.executor.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class CommandProcessExpiryCallbackTest
{

    CommandProcessExpiryCallback callback;

    @Before
    public void setUp() throws Exception
    {
        callback = new CommandProcessExpiryCallback();
    }

    @Test
    public void testOnEntryExpiry() throws Exception
    {
        CommandProcess commandProcess = mock( CommandProcess.class );
        callback.onEntryExpiry( commandProcess );

        verify( commandProcess ).stop();
    }
}
