package io.subutai.common.settings;


/**
 * Class contains general Security settings (SSL and Keystore settings).
 */
public class SecuritySettings
{
    //************* Certificates / KeyStore Settings *********************************************************
    public static final String KARAF_HOME = System.getProperty( "karaf.base" );

    public static final String CERT_EXPORT_DIR = String.format( "%s/keystores/export/", Common.SUBUTAI_APP_DATA_PATH );
    public static final String CERT_IMPORT_DIR = String.format( "%s/keystores/import/", Common.SUBUTAI_APP_DATA_PATH );
    public static final String TRUSTSTORE_PX2_FILE =
            String.format( "%s/keystores/truststore_server_px2.jks", Common.SUBUTAI_APP_DATA_PATH );
    public static final String TRUSTSTORE_PX1_FILE =
            String.format( "%s/keystores/truststore_server_px1.jks", Common.SUBUTAI_APP_DATA_PATH );
    public static final String TRUSTSTORE_SPECIAL_PX1_FILE =
            String.format( "%s/keystores/truststore_special_px1.jks", Common.SUBUTAI_APP_DATA_PATH );
    public static final String KEYSTORE_PX2_FILE =
            String.format( "%s/keystores/keystore_server_px2.jks", Common.SUBUTAI_APP_DATA_PATH );
    public static final String KEYSTORE_PX1_FILE =
            String.format( "%s/keystores/keystore_server_px1.jks", Common.SUBUTAI_APP_DATA_PATH );
    public static final String KEYSTORE_SPECIAL_PX1_FILE =
            String.format( "%s/keystores/keystore_special_px1.jks", Common.SUBUTAI_APP_DATA_PATH );

    public static final String KEYSTORE_PX1_PSW = "subutai";
    public static final String KEYSTORE_PX2_PSW = "subutai";
    public static final String KEYSTORE_SPECIAL_PX1_PSW = "subutai";
    public static final String TRUSTSTORE_PX1_PSW = "subutai";
    public static final String TRUSTSTORE_PX2_PSW = "subutai";
    public static final String TRUSTSTORE_SPECIAL_PX1_PSW = "subutai";
    public static final String KEYSTORE_PX1_ROOT_ALIAS = "root_server_px1";
    public static final String KEYSTORE_PX2_ROOT_ALIAS = "root_server_px2";
    public static final String KEYSTORE_SPECIAl_PX1_ROOT_ALIAS = "root_special_px1";
    public static final String TRUSTSTORE_PX1_ROOT_ALIAS = "";
    public static final String TRUSTSTORE_PX2_ROOT_ALIAS = "";
    public static final String TRUSTSTORE_SPECIAL_PX1_ROOT_ALIAS = "";
    //*****************************************************************************************
}
