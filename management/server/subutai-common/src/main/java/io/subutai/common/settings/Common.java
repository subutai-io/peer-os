package io.subutai.common.settings;


public class Common
{
    public static final String SUBUTAI_APP_DATA_PATH = System.getProperty( "subutaiAppDataPath" );
    public static final String DEFAULT_CONTAINER_INTERFACE = "eth0";
    public static final String MANAGEMENT_HOST_EXTERNAL_IP_INTERFACE = "eth1";
    public static final int WAIT_CONTAINER_CONNECTION_SEC = 300;
    public static final int REFRESH_UI_SEC = 3;
    public static final int MIN_COMMAND_TIMEOUT_SEC = 1;
    public static final int MAX_COMMAND_TIMEOUT_SEC = 100 * 60 * 60; // 100 hours
    public static final int INACTIVE_COMMAND_DROP_TIMEOUT_SEC = 24 * 60 * 60; // 24 hours
    public static final String IP_MASK = "^10\\.10\\.10\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$";
    public static final String IP_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}"
            + "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
    public static final String CIDR_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}"
            + "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])/(\\d{1,2})$";

    public static final String HOSTNAME_REGEX =
            "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,"
                    + "61}[a-zA-Z0-9]))*$";
    public static final int MAX_CONTAINER_NAME_LEN = 64;

    public static final String ENVIRONMENT_ID_HEADER_NAME = "ENV_ID";

    public static final long MIN_VNI_ID = 0;
    public static final long MAX_VNI_ID = 16777216;//2^24
    public static final int MIN_VLAN_ID = 100;
    public static final int MAX_VLAN_ID = 4096;

    //constants that can be converted into settings in the future
    public static final String MASTER_TEMPLATE_NAME = "master";
    public static final String MANAGEMENT_HOSTNAME = "management";
    public static final String GIT_REPO_URL = "git@gw.intra.lan:/opt/git/project.git";
    public static final String APT_REPO = "trusty";
    public static final String APT_REPO_PATH = "/repo/ksks/";
    public static final String APT_REPO_AMD64_PACKAGES_SUBPATH = "amd64/trusty/";
    public static final String DEFAULT_LXC_ARCH = "amd64";
    public static final String DEFAULT_NET_INTERFACE = "eth0";
    public static String DEFAULT_TEMPLATE_VERSION = "2.1.0";
    public static final String PACKAGE_PREFIX = "subutai-";
    public static final String PACKAGE_PREFIX_WITHOUT_DASH = "subutai";
    public static final String DEFAULT_DOMAIN_NAME = "intra.lan";
}
