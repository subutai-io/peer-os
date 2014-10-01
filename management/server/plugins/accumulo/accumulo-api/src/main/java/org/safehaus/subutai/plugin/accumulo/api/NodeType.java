package org.safehaus.subutai.plugin.accumulo.api;


public enum NodeType
{
    MASTER, GC, MONITOR, TRACER, LOGGER, TABLET_SERVER;


    public boolean isSlave()
    {
        return this == LOGGER || this == TABLET_SERVER;
    }
}
