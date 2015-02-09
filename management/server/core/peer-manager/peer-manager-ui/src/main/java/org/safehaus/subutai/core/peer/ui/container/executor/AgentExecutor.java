package org.safehaus.subutai.core.peer.ui.container.executor;


import java.util.concurrent.ExecutorService;

public interface AgentExecutor
{
    void addListener( AgentExecutionListener listener );

    void execute( ExecutorService executor, AgentCommandFactory commandFactory );
}
