package io.subutai.core.hubmanager.impl.util;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AsyncUtil
{
    public static <T> T execute( Callable<T> callable ) throws InterruptedException, ExecutionException
    {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        ExecutorCompletionService<T> completionService = new ExecutorCompletionService<>( executorService );

        completionService.submit( callable );

        executorService.shutdown();

        return completionService.take().get();
    }
}
