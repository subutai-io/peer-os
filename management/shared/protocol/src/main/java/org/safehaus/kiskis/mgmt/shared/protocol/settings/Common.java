package org.safehaus.kiskis.mgmt.shared.protocol.settings;

public class Common {

    public static final String UNKNOWN_LXC_PARENT_NAME = "UNKNOWN";
    public static final String PARENT_CHILD_LXC_SEPARATOR = "-lxc-";
    public static final int MAX_PENDING_MESSAGE_QUEUE_LENGTH = 10000;
    public static final int WEB_SERVER_PORT = 8888;
    public static final String WEB_SERVER_RES_FOLDER = "res";
    public static final int REFRESH_UI_SEC = 3;
    public static final int AGENT_FRESHNESS_MIN = 5;
    public static final int MAX_COLLECTED_RESPONSE_LENGTH = 10000;
    public static final String IP_MASK = "^10\\.10\\.10\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$";
    public static final String HOSTNAME_REGEX = "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*$";
}
