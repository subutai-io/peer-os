package io.subutai.common.settings;


import java.util.regex.Pattern;


public class Common
{
    public static final String HUB_ID = "hub";
    public static final String SUBUTAI_ID = "subutai";
    public static final String DEFAULT_PUBLIC_URL = "https://127.0.0.1:8443";
    public static final int DEFAULT_PUBLIC_PORT = 8443;
    public static final int DEFAULT_PUBLIC_SECURE_PORT = 8444;
    public static final int DEFAULT_AGENT_PORT = 7070;
    public static final int MAX_EXECUTOR_SIZE = 10;

    public static final String GATEWAY_INTERFACE_NAME_REGEX = "^gw-(\\d+)$";
    public static final Pattern GATEWAY_INTERFACE_NAME_PATTERN = Pattern.compile( GATEWAY_INTERFACE_NAME_REGEX );
    public static final String P2P_INTERFACE_NAME_REGEX = "^p2p(\\d+)$";
    public static final Pattern P2P_INTERFACE_NAME_PATTERN = Pattern.compile( P2P_INTERFACE_NAME_REGEX );
    public static final String SUBUTAI_HTTP_HEADER = "sbt-hdr";
    public static final String KARAF_ETC = System.getProperty( "karaf.etc" );
    public static final String SUBUTAI_APP_DATA_PATH = System.getProperty( "subutaiAppDataPath" );
    public static final String DEFAULT_CONTAINER_INTERFACE = "eth0";
    public static final String MNG_NET_INTERFACE = "mng-net";
    public static final String WAN_INTERFACE = "wan";
    public static final String LOCAL_HOST_IP = "127.0.0.1";
    public static final String LOCAL_HOST_NAME = "localhost";
    public static final String H_PUB_KEY = Common.SUBUTAI_APP_DATA_PATH + "/keys/h.public.gpg";

    public static final String PEER_CERT_ALIAS = "peer_cert";

    public static final int WAIT_CONTAINER_CONNECTION_SEC = 50;
    public static final int DEFAULT_EXECUTOR_REQUEST_TIMEOUT_SEC = 30;
    public static final int MIN_COMMAND_TIMEOUT_SEC = 1;

    public static final int CLONE_TIMEOUT_SEC = 60 * 5; // 5 min

    public static final int TEMPLATE_DOWNLOAD_TIMEOUT_SEC = 60 * 60 * 5; // 5 hour

    public static final int MAX_COMMAND_TIMEOUT_SEC = 100 * 60 * 60; // 100 hours
    public static final String IP_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}"
            + "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
    public static final String CIDR_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}"
            + "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])/(\\d{1,2})$";

    public static final String HOSTNAME_REGEX =
            "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,"
                    + "61}[a-zA-Z0-9]))*$";
    public static final String HOSTNAME_REGEX_WITH_PORT =
            "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,"
                    + "61}[a-zA-Z0-9]))*(:\\d+)?$";

    public static final long MIN_VNI_ID = 0;
    public static final long MAX_VNI_ID = 16777216;//2^24
    public static final int MIN_VLAN_ID = 100;
    public static final int MAX_VLAN_ID = 4096;

    //constants that can be converted into settings in the future
    public static final String MANAGEMENT_HOSTNAME = "management";
    public static final int CONTAINER_SSH_TIMEOUT_SEC = 1800;
    public static final long DEFAULT_P2P_SECRET_KEY_TTL_SEC = 180 * 60L;// 3 hour
    public static final String PACKAGE_PREFIX = "subutai-";
    public static final String PACKAGE_PREFIX_WITHOUT_DASH = "subutai";
    public static final String DEFAULT_DOMAIN_NAME = "intra.lan";

    //http/rest client settings
    public static final long DEFAULT_RECEIVE_TIMEOUT = 1000 * 60 * 10L;
    public static final long DEFAULT_CONNECTION_TIMEOUT = 1000 * 15L;
    public static final int DEFAULT_MAX_RETRANSMITS = 3;

    public static final String ETC_HOSTS_FILE = "/etc/hosts";

    public static final String CONTAINER_SSH_FOLDER = "/root/.ssh";
    public static final String CONTAINER_SSH_FILE = String.format( "%s/authorized_keys", CONTAINER_SSH_FOLDER );
    public static final int MAX_KEYS_IN_ECHO_CMD = 100;
    public static final int MIN_PORT = 0;
    public static final int MAX_PORT = 65535;


    private Common()
    {
        throw new IllegalAccessError( "Utility class" );
    }
}
