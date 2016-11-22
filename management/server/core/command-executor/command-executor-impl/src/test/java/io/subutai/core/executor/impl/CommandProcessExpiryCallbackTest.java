package io.subutai.core.executor.impl;


import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.cache.ExpiringCache;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class CommandProcessExpiryCallbackTest
{

    @Mock
    ExpiringCache<String, Map<String, String>> requests;

    CommandProcessExpiryCallback callback;


    @Before
    public void setUp() throws Exception
    {
        callback = new CommandProcessExpiryCallback( requests, "RH_ID", "CMD_ID" );
    }


    @Test
    public void testOnEntryExpiry() throws Exception
    {
        CommandProcess commandProcess = mock( CommandProcess.class );
        callback.onEntryExpiry( commandProcess );

        verify( commandProcess ).stop();
    }
}
