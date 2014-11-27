package org.safehaus.subutai.core.peer.api;


public interface Task
{
    public String getId();

    public String getPeerId();

    public String getParam();

    public enum Type
    {
        CLONE, START, STOP, DESTROY
    }
}
