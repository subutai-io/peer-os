package org.safehaus.subutai.ui.containermanager.executor;

/**
 * Created by timur on 9/8/14.
 */
public interface AgentCommandFactory {
    public AgentCommand newCommand(String cloneName);
}
