package org.safehaus.subutai.core.peer.api.message;


/**
 * Created by dilshat on 9/17/14.
 */
public class Common
{

    private Common()
    {
    }


    public static final String PING_URL = "http://%s:8181/cxf/peer/ping";
    public static final String GET_AGENTS_URL = "http://%s:8181/cxf/peer/agents";
    public static final String ENV_ID_PARAM_NAME = "envId";
    public static final String PEER_BASE_PATH = "http://%s:8181/cxf/peer/";
    public static final String MESSAGE_REQUEST_SUB_PATH = "message";
    public static final String MESSAGE_REQUEST_URL = String.format( "%s%s", PEER_BASE_PATH, MESSAGE_REQUEST_SUB_PATH );
    public static final String MESSAGE_PARAM_NAME = "message";
    public static final String PEER_ID_PARAM_NAME = "peerId";
    public static final String RECIPIENT_PARAM_NAME = "recipient";
}
