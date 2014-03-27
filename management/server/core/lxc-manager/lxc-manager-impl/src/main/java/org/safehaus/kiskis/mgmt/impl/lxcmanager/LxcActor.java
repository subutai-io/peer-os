/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lxcmanager;

import java.util.concurrent.Callable;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;

/**
 *
 * @author dilshat
 */
public class LxcActor implements Callable<LxcInfo> {

    private final LxcInfo info;
    private final LxcManager lxcManager;

    public LxcActor(LxcInfo info, LxcManager lxcManager) {
        this.info = info;
        this.lxcManager = lxcManager;
    }

    public LxcInfo call() throws Exception {
        info.setResult(lxcManager.cloneNStartLxcOnHost(info.getPhysicalAgent(), info.getLxcHostname()));
        return info;
    }
}
