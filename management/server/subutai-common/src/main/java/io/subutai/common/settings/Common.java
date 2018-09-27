package io.subutai.common.settings;


public class Common
{
    public static final String E2E_PLUGIN_USER_KEY_FINGERPRINT_NAME = "su_fingerprint";
    public static final String BAZAAR_ID = "bazaar";
    public static final String SUBUTAI_ID = "subutai";
    public static final String DEFAULT_PUBLIC_URL = "https://127.0.0.1:8443";

    public static final int DEFAULT_PUBLIC_PORT = 8443;
    public static final int DEFAULT_PUBLIC_SECURE_PORT = 8444;
    public static final int DEFAULT_AGENT_PORT = 7070;
    public static final int MAX_EXECUTOR_SIZE = 10;

    public static final String P2P_INTERFACE_NAME_REGEX = "^p2p(\\d+)$";
    public static final String P2P_INTERFACE_PREFIX = "p2p";
    public static final String SUBUTAI_HTTP_HEADER = "sbt-hdr";
    public static final String KARAF_ETC = System.getProperty( "karaf.etc" );
    public static final String SUBUTAI_APP_DATA_PATH = System.getProperty( "subutaiAppDataPath" );
    public static final String BAZAAR_IP = System.getProperty( "bazaarIp" );
    public static final String P2P_PORT_RANGE_START = System.getProperty( "p2pPortRangeStart" );
    public static final String P2P_PORT_RANGE_END = System.getProperty( "p2pPortRangeEnd" );
    public static final boolean CHECK_RH_LIMITS =
            Boolean.TRUE.toString().equalsIgnoreCase( System.getProperty( "checkRhLimits" ) );
    public static final String DEFAULT_CONTAINER_INTERFACE = "eth0";
    public static final String LOCAL_HOST_IP = "127.0.0.1";
    public static final String LOCAL_HOST_NAME = "localhost";
    public static final String BAZAAR_PUB_KEY = Common.SUBUTAI_APP_DATA_PATH + "/keys/h.public.gpg";

    public static final String PEER_CERT_ALIAS = "peer_cert";

    //Command related timeouts

    public static final long RH_UPDATE_TIMEOUT_MIN = 60;
    public static final long RH_UPDATE_CHECK_TIMEOUT_MIN = 10;
    public static final long MH_UPDATE_TIMEOUT_MIN = 60;
    public static final long MH_UPDATE_CHECK_TIMEOUT_MIN = 15;

    public static final int WAIT_CONTAINER_CONNECTION_SEC = 180;
    public static final int DEFAULT_EXECUTOR_REQUEST_TIMEOUT_SEC = 60;
    public static final int MIN_COMMAND_TIMEOUT_SEC = 1;

    public static final int CLONE_TIMEOUT_SEC = 60 * 5; // 5 min

    public static final int TEMPLATE_DOWNLOAD_TIMEOUT_SEC = 60 * 60 * 5; // 5 hour
    public static final int TEMPLATE_EXPORT_TIMEOUT_SEC = 60 * 60 * 5; // 5 hour

    public static final int MAX_COMMAND_TIMEOUT_SEC = 100 * 60 * 60; // 100 hours

    public static final int DESTROY_CONTAINER_TIMEOUT_SEC = 600;

    public static final int CREATE_SSH_KEY_TIMEOUT_SEC = 60;

    public static final int CLEANUP_ENV_TIMEOUT_SEC = 3600;

    public static final int STOP_CONTAINER_TIMEOUT_SEC = 120;

    public static final int GET_QUOTA_TIMEOUT_SEC = 60;

    public static final int SET_QUOTA_TIMEOUT_SEC = 120;

    public static final int JOIN_P2P_TIMEOUT_SEC = 180;

    public static final int LEAVE_P2P_TIMEOUT_SEC = 90;

    public static final int IP_LINK_REMOVE_TIMEOUT_SEC = 90;

    // <<< timeouts

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
    public static final int MIN_VLAN_ID = 1;
    public static final int MAX_VLAN_ID = 4096;

    //constants that can be converted into settings in the future
    public static final String MANAGEMENT_HOSTNAME = "management";
    public static final int CONTAINER_SSH_TIMEOUT_SEC = 1800;
    public static final long DEFAULT_P2P_SECRET_KEY_TTL_SEC = 180 * 60L;// 3 hour
    public static final String DEFAULT_DOMAIN_NAME = "intra.lan";

    //http/rest client settings
    public static final long DEFAULT_RECEIVE_TIMEOUT = 1000 * 60 * 10L; // 10 min
    public static final long DEFAULT_CONNECTION_TIMEOUT = 1000 * 15L; // 15sec
    public static final int DEFAULT_MAX_RETRANSMITS = 3;

    public static final String ETC_HOSTS_FILE = "/etc/hosts";

    public static final String CONTAINER_SSH_FOLDER = "/root/.ssh";
    public static final String CONTAINER_SSH_FILE = String.format( "%s/authorized_keys", CONTAINER_SSH_FOLDER );
    public static final int MAX_KEYS_IN_ECHO_CMD = 100;
    public static final int MIN_PORT = 1;
    public static final int MAX_PORT = 65535;


    private Common()
    {
        throw new IllegalAccessError( "Utility class" );
    }
}
