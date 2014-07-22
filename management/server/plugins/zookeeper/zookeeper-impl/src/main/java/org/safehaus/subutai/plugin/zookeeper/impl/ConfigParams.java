package org.safehaus.subutai.plugin.zookeeper.impl;


/**
 * Created by dilshat on 7/23/14.
 */
public enum ConfigParams {
    DATA_DIR( "dataDir", "/var/zookeeper" ), SERVERS( "servers", null );

    private final String placeHolder;
    private final String paramValue;


    ConfigParams( final String placeHolder, final String paramValue ) {
        this.placeHolder = placeHolder;
        this.paramValue = paramValue;
    }


    public String getPlaceHolder() {
        return placeHolder;
    }


    public String getParamValue() {
        return paramValue;
    }
}

