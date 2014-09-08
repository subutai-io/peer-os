package org.safehaus.subutai.ui.containermanager.clone;

import org.safehaus.subutai.api.containermanager.ContainerCreateException;
import org.safehaus.subutai.api.containermanager.ContainerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by timur on 9/8/14.
 */
public class AgentExecutor {
    private String hostName;
    private String templateName;
    private List<String> cloneNames;
    private CompletionService<AgentExecutionEvent> completionService;
    private ExecutorService executor;
    private List<AgentExecutionListener> listeners = new ArrayList<>();

    public AgentExecutor(String hostName, String templateName, List<String> cloneNames) {
        this.hostName = hostName;
        this.templateName = templateName;
        this.cloneNames = cloneNames;
    }

    public void addListener(org.safehaus.subutai.ui.containermanager.clone.AgentExecutionListener listener) {
        if (listener != null)
            listeners.add(listener);
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public List<String> getCloneNames() {
        return cloneNames;
    }

    public void setCloneNames(List<String> cloneNames) {
        this.cloneNames = cloneNames;
    }

    public void execute(final ExecutorService executor, final AgentCommandFactory commandFactory) {
        this.executor = executor;
        final AgentExecutor self = this;
        completionService = new ExecutorCompletionService(executor);
        for (final String lxcHostname : cloneNames) {
            completionService.submit(new Callable() {
                public AgentExecutionEvent call() {
                    fireEvent(new AgentExecutionEvent(hostName, lxcHostname, AgentExecutionEventType.START, ""));
                    try {
                        AgentCommand command = commandFactory.newCommand(lxcHostname);
                        command.execute();
                        return (new AgentExecutionEvent(hostName, lxcHostname, AgentExecutionEventType.SUCCESS, ""));
                    } catch (AgentExecutionException ce) {
                        return (new AgentExecutionEvent(hostName, lxcHostname, AgentExecutionEventType.FAIL, ce.toString()));
                    }
                }
            });
        }

        ExecutorService waiter = Executors.newFixedThreadPool(1);
        waiter.execute(new Runnable() {
            @Override
            public void run() {
                for (String cn : cloneNames) {
                    try {
                        Future<AgentExecutionEvent> future = completionService.take();
                        AgentExecutionEvent result = future.get();
                        fireEvent(result);
                    } catch (InterruptedException | ExecutionException e) {
                        fireEvent(new AgentExecutionEvent(hostName, "", AgentExecutionEventType.FAIL, e.toString()));
                    }
                }
            }
        });
        waiter.shutdown();
    }

    private void fireEvent(AgentExecutionEvent event) {
        for (AgentExecutionListener listener : listeners) {
            if (listener != null)
                listener.onExecutionEvent(event);
        }
    }
}
