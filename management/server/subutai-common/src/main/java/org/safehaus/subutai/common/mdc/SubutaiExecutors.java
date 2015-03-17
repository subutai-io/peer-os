package org.safehaus.subutai.common.mdc;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.MDC;


/**
 * MDC Context Aware Custom Subutai Executors
 */
public class SubutaiExecutors
{

    public static ExecutorService newFixedThreadPool( int nThreads )
    {
        return new ThreadPoolExecutor( nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>() )
        {
            private Map<String, String> parentContext;
            private Map<String, String> currentContext;


            @Override
            public Future<?> submit( final Runnable task )
            {
                parentContext = MDC.getCopyOfContextMap();
                return super.submit( task );
            }


            @Override
            public <T> Future<T> submit( final Runnable task, final T result )
            {
                parentContext = MDC.getCopyOfContextMap();
                return super.submit( task, result );
            }


            @Override
            public <T> Future<T> submit( final Callable<T> task )
            {
                parentContext = MDC.getCopyOfContextMap();
                return super.submit( task );
            }


            @Override
            public void execute( final Runnable command )
            {
                parentContext = MDC.getCopyOfContextMap();
                super.execute( command );
            }


            @Override
            protected void beforeExecute( final Thread t, final Runnable r )
            {
                if ( parentContext != null )
                {
                    currentContext = MDC.getCopyOfContextMap();
                    MDC.setContextMap( parentContext );
                }
                super.beforeExecute( t, r );
            }


            @Override
            protected void afterExecute( final Runnable r, final Throwable t )
            {
                if ( parentContext != null )
                {
                    MDC.setContextMap( currentContext );
                }
                super.afterExecute( r, t );
            }
        };
    }


    public static ExecutorService newCachedThreadPool()
    {
        return new ThreadPoolExecutor( 0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>() )
        {
            private Map<String, String> parentContext;
            private Map<String, String> currentContext;


            @Override
            public Future<?> submit( final Runnable task )
            {
                parentContext = MDC.getCopyOfContextMap();
                return super.submit( task );
            }


            @Override
            public <T> Future<T> submit( final Runnable task, final T result )
            {
                parentContext = MDC.getCopyOfContextMap();
                return super.submit( task, result );
            }


            @Override
            public <T> Future<T> submit( final Callable<T> task )
            {
                parentContext = MDC.getCopyOfContextMap();
                return super.submit( task );
            }


            @Override
            public void execute( final Runnable command )
            {
                parentContext = MDC.getCopyOfContextMap();
                super.execute( command );
            }


            @Override
            protected void beforeExecute( final Thread t, final Runnable r )
            {
                if ( parentContext != null )
                {
                    currentContext = MDC.getCopyOfContextMap();
                    MDC.setContextMap( parentContext );
                }
                super.beforeExecute( t, r );
            }


            @Override
            protected void afterExecute( final Runnable r, final Throwable t )
            {
                if ( parentContext != null )
                {
                    MDC.setContextMap( currentContext );
                }
                super.afterExecute( r, t );
            }
        };
    }


    public static ScheduledExecutorService newSingleThreadScheduledExecutor()
    {
        return new DelegatedScheduledExecutorService( new ScheduledThreadPoolExecutor( 1 )
        {
            private Map<String, String> parentContext;
            private Map<String, String> currentContext;


            @Override
            public Future<?> submit( final Runnable task )
            {
                parentContext = MDC.getCopyOfContextMap();
                return super.submit( task );
            }


            @Override
            public <T> Future<T> submit( final Runnable task, final T result )
            {
                parentContext = MDC.getCopyOfContextMap();
                return super.submit( task, result );
            }


            @Override
            public <T> Future<T> submit( final Callable<T> task )
            {
                parentContext = MDC.getCopyOfContextMap();
                return super.submit( task );
            }


            @Override
            public void execute( final Runnable command )
            {
                parentContext = MDC.getCopyOfContextMap();
                super.execute( command );
            }


            @Override
            protected void beforeExecute( final Thread t, final Runnable r )
            {
                if ( parentContext != null )
                {
                    currentContext = MDC.getCopyOfContextMap();
                    MDC.setContextMap( parentContext );
                }
                super.beforeExecute( t, r );
            }


            @Override
            protected void afterExecute( final Runnable r, final Throwable t )
            {
                if ( parentContext != null )
                {
                    MDC.setContextMap( currentContext );
                }
                super.afterExecute( r, t );
            }
        } );
    }


    public static ExecutorService newSingleThreadExecutor()
    {
        return new FinalizableDelegatedExecutorService(
                new ThreadPoolExecutor( 1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>() )
                {
                    private Map<String, String> parentContext;
                    private Map<String, String> currentContext;


                    @Override
                    public Future<?> submit( final Runnable task )
                    {
                        parentContext = MDC.getCopyOfContextMap();
                        return super.submit( task );
                    }


                    @Override
                    public <T> Future<T> submit( final Runnable task, final T result )
                    {
                        parentContext = MDC.getCopyOfContextMap();
                        return super.submit( task, result );
                    }


                    @Override
                    public <T> Future<T> submit( final Callable<T> task )
                    {
                        parentContext = MDC.getCopyOfContextMap();
                        return super.submit( task );
                    }


                    @Override
                    public void execute( final Runnable command )
                    {
                        parentContext = MDC.getCopyOfContextMap();
                        super.execute( command );
                    }


                    @Override
                    protected void beforeExecute( final Thread t, final Runnable r )
                    {
                        if ( parentContext != null )
                        {
                            currentContext = MDC.getCopyOfContextMap();
                            MDC.setContextMap( parentContext );
                        }
                        super.beforeExecute( t, r );
                    }


                    @Override
                    protected void afterExecute( final Runnable r, final Throwable t )
                    {
                        if ( parentContext != null )
                        {
                            MDC.setContextMap( currentContext );
                        }
                        super.afterExecute( r, t );
                    }
                } );
    }


    //helpers


    static class FinalizableDelegatedExecutorService extends DelegatedExecutorService
    {
        FinalizableDelegatedExecutorService( ExecutorService executor )
        {
            super( executor );
        }


        protected void finalize()
        {
            super.shutdown();
        }
    }


    static class DelegatedScheduledExecutorService extends DelegatedExecutorService implements ScheduledExecutorService
    {
        private final ScheduledExecutorService e;


        DelegatedScheduledExecutorService( ScheduledExecutorService executor )
        {
            super( executor );
            e = executor;
        }


        public ScheduledFuture<?> schedule( Runnable command, long delay, TimeUnit unit )
        {
            return e.schedule( command, delay, unit );
        }


        public <V> ScheduledFuture<V> schedule( Callable<V> callable, long delay, TimeUnit unit )
        {
            return e.schedule( callable, delay, unit );
        }


        public ScheduledFuture<?> scheduleAtFixedRate( Runnable command, long initialDelay, long period, TimeUnit unit )
        {
            return e.scheduleAtFixedRate( command, initialDelay, period, unit );
        }


        public ScheduledFuture<?> scheduleWithFixedDelay( Runnable command, long initialDelay, long delay,
                                                          TimeUnit unit )
        {
            return e.scheduleWithFixedDelay( command, initialDelay, delay, unit );
        }
    }


    static class DelegatedExecutorService extends AbstractExecutorService
    {
        private final ExecutorService e;


        DelegatedExecutorService( ExecutorService executor ) { e = executor; }


        public void execute( Runnable command ) { e.execute( command ); }


        public void shutdown() { e.shutdown(); }


        public List<Runnable> shutdownNow() { return e.shutdownNow(); }


        public boolean isShutdown() { return e.isShutdown(); }


        public boolean isTerminated() { return e.isTerminated(); }


        public boolean awaitTermination( long timeout, TimeUnit unit ) throws InterruptedException
        {
            return e.awaitTermination( timeout, unit );
        }


        public Future<?> submit( Runnable task )
        {
            return e.submit( task );
        }


        public <T> Future<T> submit( Callable<T> task )
        {
            return e.submit( task );
        }


        public <T> Future<T> submit( Runnable task, T result )
        {
            return e.submit( task, result );
        }


        public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks ) throws InterruptedException
        {
            return e.invokeAll( tasks );
        }


        public <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
                throws InterruptedException
        {
            return e.invokeAll( tasks, timeout, unit );
        }


        public <T> T invokeAny( Collection<? extends Callable<T>> tasks )
                throws InterruptedException, ExecutionException
        {
            return e.invokeAny( tasks );
        }


        public <T> T invokeAny( Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit )
                throws InterruptedException, ExecutionException, TimeoutException
        {
            return e.invokeAny( tasks, timeout, unit );
        }
    }
}
