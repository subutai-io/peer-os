package org.safehaus.subutai.common.mdc;


import java.util.concurrent.Callable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith( MockitoJUnitRunner.class )
public class SubutaiExecutorsTest
{
    private SubutaiExecutors subutaiExecutors;

    @Mock
    Runnable runnable;
    @Mock
    Callable callable;


    @Test
    public void testNewFixedThreadPool() throws Exception
    {
        SubutaiExecutors.newFixedThreadPool( 2 ).submit( runnable );
        SubutaiExecutors.newFixedThreadPool( 2 ).submit( runnable, "test" );
        SubutaiExecutors.newFixedThreadPool( 2 ).submit( callable );

    }


    @Test
    public void testNewCachedThreadPool() throws Exception
    {
        SubutaiExecutors.newCachedThreadPool().submit( runnable );
        SubutaiExecutors.newCachedThreadPool().submit( runnable, callable );
        SubutaiExecutors.newCachedThreadPool().submit( callable );
    }


    @Test
    public void testNewSingleThreadScheduledExecutor() throws Exception
    {
        SubutaiExecutors.newSingleThreadScheduledExecutor().submit( runnable );
        SubutaiExecutors.newSingleThreadScheduledExecutor().submit( runnable, callable );
        SubutaiExecutors.newSingleThreadScheduledExecutor().submit( callable );
    }


    @Test
    public void testNewSingleThreadExecutor() throws Exception
    {
        SubutaiExecutors.newSingleThreadExecutor().submit( runnable );
        SubutaiExecutors.newSingleThreadExecutor().submit( runnable, callable );
        SubutaiExecutors.newSingleThreadExecutor().submit( callable );
    }
}