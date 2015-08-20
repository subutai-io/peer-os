package io.subutai.core.keyserver.rest;


/**
 * HTTP Input parameters class for Key operations
 */
public class KeyServerParams
{

    public static final String PGP_KEYS_CONTENT_TYPE = "application/pgp-keys";

    public static final String MOD_VARIABLE_SEARCH = "search";
    public static final String MOD_VARIABLE_OPERATION = "op";
    // modifier variables
    public static final String MOD_VARIABLE_OPTIONS = "options";
    public static final String MOD_VARIABLE_FINGERPRINT = "fingerprint";
    public static final String MOD_VARIABLE_EXACT = "exact";

    public static final String HKP_OPERATION_GET = "get";
    public static final String HKP_OPERATION_INDEX = "index";
    public static final String HKP_OPERATION_VINDEX = "vindex";

    public static final String OTHER_PREFIX = "x-";
    public static final String ON = "on";
    public static final String OFF = "off";
    public static final String MR_OUTPUT_FORMAT_VERSION = "1";

}
