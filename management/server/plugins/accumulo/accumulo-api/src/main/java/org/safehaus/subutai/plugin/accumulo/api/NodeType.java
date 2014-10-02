package org.safehaus.subutai.plugin.accumulo.api;


public enum NodeType
{
    Master, GC, Monitor, Tracer, Logger, Tablet_Server;


    public boolean isSlave()
    {
        return this == Logger || this == Tablet_Server;
    }
}
