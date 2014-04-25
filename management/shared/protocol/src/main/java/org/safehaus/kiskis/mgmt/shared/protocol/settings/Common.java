package org.safehaus.kiskis.mgmt.shared.protocol.settings;

public class Common {

    public static final String UNKNOWN_LXC_PARENT_NAME = "UNKNOWN";
    public static final String BASE_CONTAINER_NAME = "base-container";
    public static final int REFRESH_UI_SEC = 3;
    public static final int AGENT_FRESHNESS_MIN = 5;
    public static final int MAX_COMMAND_TIMEOUT_SEC = 100 * 60 * 60; // 100 hours
    public static final String IP_MASK = "^10\\.10\\.10\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$";
    public static final String HOSTNAME_REGEX = "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*$";
}
