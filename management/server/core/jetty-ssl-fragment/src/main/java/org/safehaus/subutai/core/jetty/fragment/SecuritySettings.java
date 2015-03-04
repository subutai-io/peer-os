package org.safehaus.subutai.core.jetty.fragment;


/**
 * Created by talas on 2/27/15.
 */
public class SecuritySettings
{
    //************* Certificates / KeyStore Settings *********************************************************
    public static final String KARAF_HOME = System.getProperty( "karaf.base" );
    //    public static final String CERT_EXPORT_DIR = KARAF_HOME + "/etc/export/";
    //    public static final String CERT_IMPORT_DIR = KARAF_HOME + "/etc/import/";
    //    public static final String TRUSTSTORE_PX2_FILE = KARAF_HOME + "/etc/keystores/truststore_server_px2.jks";
    //    public static final String TRUSTSTORE_PX1_FILE = KARAF_HOME + "/etc/keystores/truststore_server_px1.jks";
    //    public static final String KEYSTORE_PX2_FILE = KARAF_HOME + "/etc/keystores/keystore_server_px2.jks";
    //    public static final String KEYSTORE_PX1_FILE = KARAF_HOME + "/etc/keystores/keystore_server_px1.jks";

    public static final String CERT_EXPORT_DIR = "/var/lib/subutai/keystores/export/";
    public static final String CERT_IMPORT_DIR = "/var/lib/subutai/keystores/import/";
    public static final String TRUSTSTORE_PX2_FILE = "/var/lib/subutai/keystores/truststore_server_px2.jks";
    public static final String TRUSTSTORE_PX1_FILE = "/var/lib/subutai/keystores/truststore_server_px1.jks";
    public static final String KEYSTORE_PX2_FILE = "/var/lib/subutai/keystores/keystore_server_px2.jks";
    public static final String KEYSTORE_PX1_FILE = "/var/lib/subutai/keystores/keystore_server_px1.jks";

    public static final String KEYSTORE_PX1_PSW = "subutai";
    public static final String KEYSTORE_PX2_PSW = "subutai";
    public static final String TRUSTSTORE_PX1_PSW = "subutai";
    public static final String TRUSTSTORE_PX2_PSW = "subutai";
    public static final String KEYSTORE_PX1_ROOT_ALIAS = "root_server_px1";
    public static final String KEYSTORE_PX2_ROOT_ALIAS = "root_server_px2";
    public static final String TRUSTSTORE_PX1_ROOT_ALIAS = "";
    public static final String TRUSTSTORE_PX2_ROOT_ALIAS = "";
    //*****************************************************************************************
}
