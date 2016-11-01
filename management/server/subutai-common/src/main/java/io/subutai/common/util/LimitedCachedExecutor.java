package io.subutai.common.util;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class LimitedCachedExecutor extends ThreadPoolExecutor
{
    public LimitedCachedExecutor( int maxThreadsCount, int threadExpiryInSec )
    {
        super( 1, Math.max( 1, maxThreadsCount ), Math.max( 1, threadExpiryInSec ), TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>() );

        allowCoreThreadTimeOut( true );
    }
}
