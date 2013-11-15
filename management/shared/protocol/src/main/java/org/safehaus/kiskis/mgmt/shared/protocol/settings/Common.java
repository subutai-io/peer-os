package org.safehaus.kiskis.mgmt.shared.protocol.settings;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 10/8/13 Time: 10:41 PM To
 * change this template use File | Settings | File Templates.
 */
public class Common {

    public static int MQ_PORT = 61616;
    public static int APT_REPO_EXPOSER_PORT = 8888;
    public static String APT_REPO_PATH = "/home/baturlar/repo";
    //    public static String MQ_URL = "tcp://127.0.0.1:61616";
    public static String MQ_HOST = "127.0.0.1";
    public static String MQ_SERVICE_QUEUE = "SERVICE_QUEUE";
    public static String KEYSTORE_PASS = "broker";
    public static String TRUSTSTORE_PASS = "client";
//    public static String MQ_USERNAME = "karaf";
//    public static String MQ_PASSWORD = "karaf";
    public static int cassandraPort = 9042;
    public static String cassandraHost = "localhost";
//    private final String cassandraHost = "192.168.1.106";
    public static String keyspaceName = "kiskis";
}
