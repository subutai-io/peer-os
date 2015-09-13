package io.subutai.core.hintegration.impl.settings;


import io.subutai.common.settings.Common;


/**
 * Created by ermek on 9/6/15.
 */
public class HSettings
{
    public static final String HUB_PUB_KEY = Common.SUBUTAI_APP_DATA_PATH + "/hkeystore/hub.public.gpg";
    public static final String PEER_OWNER_PUB_KEY = Common.SUBUTAI_APP_DATA_PATH + "/hkeystore/owner.public.gpg";
    public static final String PEER_OWNER_SECRET_KEY = Common.SUBUTAI_APP_DATA_PATH + "/hkeystore/owner.secret.gpg";
    public static final String PEER_PUB_KEY = Common.SUBUTAI_APP_DATA_PATH + "/hkeystore/peer.public.gpg";
    public static final String PEER_SECRET_KEY = Common.SUBUTAI_APP_DATA_PATH + "/hkeystore/peer.secret.gpg";
    public static final String PEER_KEYSTORE = Common.SUBUTAI_APP_DATA_PATH + "/hkeystore/peer.jks";

    public static final String SECURE_PORT_X1 = "4000";
    public static final String IP = "hub-server:";

}
