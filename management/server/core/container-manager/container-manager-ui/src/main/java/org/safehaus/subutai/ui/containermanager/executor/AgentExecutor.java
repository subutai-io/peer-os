package org.safehaus.subutai.ui.containermanager.executor;

import java.util.concurrent.ExecutorService;

/**
 * Created by timur on 9/9/14.
 */
public interface AgentExecutor {
    void addListener(AgentExecutionListener listener);

    void execute(ExecutorService executor, AgentCommandFactory commandFactory);
}
