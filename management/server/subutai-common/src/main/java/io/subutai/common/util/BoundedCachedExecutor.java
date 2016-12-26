package io.subutai.common.util;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class BoundedCachedExecutor extends ThreadPoolExecutor
{
    public BoundedCachedExecutor( int minThreadsCount, int maxThreadsCount, int threadExpiryInSec )
    {
        super( Math.max( 1, minThreadsCount ), Math.max( 2, maxThreadsCount ), Math.max( 5, threadExpiryInSec ),
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>() );

        allowCoreThreadTimeOut( true );
    }
}
