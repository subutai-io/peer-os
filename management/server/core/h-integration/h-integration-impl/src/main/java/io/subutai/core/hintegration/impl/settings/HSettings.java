package io.subutai.core.hintegration.impl.settings;


import io.subutai.common.settings.Common;


/**
 * Created by ermek on 9/6/15.
 */
public class HSettings
{
    public static final String H_PUB_KEY = Common.SUBUTAI_APP_DATA_PATH + "/keystores/h.public.gpg";
    public static final String PEER_KEYSTORE = Common.SUBUTAI_APP_DATA_PATH + "/keystores/peer.jks";

    public static final String SECURE_PORT_X1 = "4000";
    public static final String IP = "hub-server:";

}
