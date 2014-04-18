/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.agentmanager;

import java.util.ArrayList;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

/**
 *
 * @author dilshat
 */
public class TestUtils {

    public static Response getRegistrationRequestResponse(UUID uuid, boolean isLxc, String hostname) {
        Response response = new Response();
        response.setUuid(uuid);
        response.setIsLxc(isLxc);
        response.setHostname(hostname);
        response.setIps(new ArrayList<String>());
        response.setType(ResponseType.REGISTRATION_REQUEST);
        return response;
    }
}
