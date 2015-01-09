package org.safehaus.subutai.core.peer.ui.container.executor;


import java.util.concurrent.ExecutorService;


/**
 * Created by timur on 9/9/14.
 */
public interface AgentExecutor
{
    void addListener( AgentExecutionListener listener );

    void execute( ExecutorService executor, AgentCommandFactory commandFactory );
}
