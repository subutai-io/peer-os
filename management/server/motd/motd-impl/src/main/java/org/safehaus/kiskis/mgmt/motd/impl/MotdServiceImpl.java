package org.safehaus.kiskis.mgmt.motd.impl;

import org.safehaus.kiskis.mgmt.motd.api.service.MotdService;

/**
 * @author : yigit
 */
public class MotdServiceImpl implements MotdService {

    public String getMessageOfTheDay () {
        return "It's a good day as any!";
    }
}
