package io.subutai.common.cache;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.cache.CacheEntryWithExpiryCallback;
import io.subutai.common.cache.EntryExpiryCallback;


@RunWith( MockitoJUnitRunner.class )
public class CacheEntryWithExpiryCallbackTest
{
    private CacheEntryWithExpiryCallback cacheEntryWithExpiryCallback;

    @Mock
    Object object;
    @Mock
    EntryExpiryCallback entryExpiryCallback;

    @Before
    public void setUp() throws Exception
    {
        cacheEntryWithExpiryCallback = new CacheEntryWithExpiryCallback( object, 500, entryExpiryCallback );
    }


    @Test
    public void testCallExpiryCallback() throws Exception
    {
        cacheEntryWithExpiryCallback.callExpiryCallback();
    }
}