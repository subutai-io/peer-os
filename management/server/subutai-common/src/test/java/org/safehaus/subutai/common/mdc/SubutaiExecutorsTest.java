package org.safehaus.subutai.common.mdc;


import java.util.concurrent.Callable;

import org.junit.Before;
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

    @Before
    public void setUp() throws Exception
    {
        subutaiExecutors = new SubutaiExecutors();
    }


    @Test
    public void testNewFixedThreadPool() throws Exception
    {
        subutaiExecutors.newFixedThreadPool( 2 ).submit( runnable );
        subutaiExecutors.newFixedThreadPool( 2 ).submit( runnable, "test" );
        subutaiExecutors.newFixedThreadPool( 2 ).submit( callable );

    }


    @Test
    public void testNewCachedThreadPool() throws Exception
    {
        subutaiExecutors.newCachedThreadPool().submit( runnable );
        subutaiExecutors.newCachedThreadPool().submit( runnable, callable );
        subutaiExecutors.newCachedThreadPool().submit( callable );
    }


    @Test
    public void testNewSingleThreadScheduledExecutor() throws Exception
    {
        subutaiExecutors.newSingleThreadScheduledExecutor().submit( runnable );
        subutaiExecutors.newSingleThreadScheduledExecutor().submit( runnable, callable );
        subutaiExecutors.newSingleThreadScheduledExecutor().submit( callable );
    }


    @Test
    public void testNewSingleThreadExecutor() throws Exception
    {
        subutaiExecutors.newSingleThreadExecutor().submit( runnable );
        subutaiExecutors.newSingleThreadExecutor().submit( runnable, callable );
        subutaiExecutors.newSingleThreadExecutor().submit( callable );
    }
}