package io.subutai.core.bazaarmanager.impl.util;


import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;


public class ReschedulableTimer extends Timer
{
    private final Runnable task;
    private TimerTask timerTask;


    public ReschedulableTimer( final Runnable task )
    {
        Preconditions.checkNotNull( task );

        this.task = task;
    }


    public synchronized void schedule( long delayInSec )
    {
        Preconditions.checkArgument( delayInSec >= 0 );

        if ( timerTask != null )
        {
            timerTask.cancel();
        }

        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                task.run();
            }
        };

        this.schedule( timerTask, TimeUnit.SECONDS.toMillis( delayInSec ) );
    }
}
