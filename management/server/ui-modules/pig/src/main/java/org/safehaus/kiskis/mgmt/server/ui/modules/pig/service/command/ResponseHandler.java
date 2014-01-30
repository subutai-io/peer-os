package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.command;

import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

public interface ResponseHandler {

    public void handleResponse(String stdOut, String stdErr, ResponseType responseType);

}
