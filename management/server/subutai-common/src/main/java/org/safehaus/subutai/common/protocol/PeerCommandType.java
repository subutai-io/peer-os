package org.safehaus.subutai.common.protocol;


/**
 * Peer command type.
 */
public enum PeerCommandType
{
    UNKNOWN, CLONE, DESTROY, START, STOP, IS_CONNECTED, EXECUTE, GET_PEER_ID, GET_CONNECTED_CONTAINERS,
    PREPARE_TEMPLATES, GET_TEMPLATE, REGISTER_TEMPLATE;

}
