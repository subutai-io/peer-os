/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.oozie;

import org.safehaus.kiskis.mgmt.shared.protocol.ApiBase;

import java.util.UUID;

/**
 * @author dilshat
 */
public interface Oozie extends ApiBase<Config> {

    UUID startServer(Config config);

    UUID stopServer(Config config);

    UUID checkServerStatus(Config config);
}
