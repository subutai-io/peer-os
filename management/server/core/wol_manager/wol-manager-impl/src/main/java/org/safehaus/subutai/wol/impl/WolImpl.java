package org.safehaus.subutai.wol.impl;

import org.safehaus.subutai.wol.api.WolManager;

/**
 * Created by emin on 14/11/14.
 */
public class WolImpl implements WolManager {

    @Override
    public String getWolName() {
        return "hello!";
    }


    @Override
    public String helloWol( final String name ) {
        return "Hello mydear " + name + "!";
    }
}
