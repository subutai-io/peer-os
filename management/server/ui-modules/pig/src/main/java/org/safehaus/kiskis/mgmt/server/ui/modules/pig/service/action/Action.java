package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.action;

import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

import java.util.logging.Logger;

public class Action {

    private final Logger LOG = Logger.getLogger(getClass().getName());

    public void handleResponse(String stdOut, String stdErr, ResponseType responseType) {
        LOG.info(stdOut);
    }
}
