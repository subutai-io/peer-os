package org.safehaus.subutai.plugin.common.api;


public enum NodeType
{
    MASTER_NODE,
    SLAVE_NODE,
    NAMENODE,
    SECONDARY_NAMENODE,
    JOBTRACKER,
    TASKTRACKER,
    DATANODE,
    SERVER,
    CLIENT,
    STORM_NIMBUS,
    STORM_SUPERVISOR,
    STORM_UI,

    /** accumulo node types   */
    ACCUMULO_MASTER,
    ACCUMULO_GC,
    ACCUMULO_TRACER,
    ACCUMULO_MONITOR,
    ACCUMULO_TABLET_SERVER,
    ACCUMULO_LOGGER
}
