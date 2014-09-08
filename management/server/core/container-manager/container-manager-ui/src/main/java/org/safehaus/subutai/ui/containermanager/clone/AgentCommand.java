package org.safehaus.subutai.ui.containermanager.clone;

/**
 * Created by timur on 9/8/14.
 */
public interface AgentCommand {
    public void setHostName(String hostName);
    public void setCloneName(String cloneName);
    public void execute() throws AgentExecutionException;
}
