/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.oozie;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;


/**
 * @author dilshat
 */
public interface Oozie extends ApiBase<OozieConfig> {

    UUID startServer( OozieConfig config );

    UUID stopServer( OozieConfig config );

    UUID checkServerStatus( OozieConfig config );
}
