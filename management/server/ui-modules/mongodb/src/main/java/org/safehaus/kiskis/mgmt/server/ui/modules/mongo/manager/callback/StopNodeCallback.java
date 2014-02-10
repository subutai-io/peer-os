/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.callback;

import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ChainedTaskCallback;

/**
 *
 * @author dilshat
 */
public class StopNodeCallback implements ChainedTaskCallback {

    private final Button checkButton;

    public StopNodeCallback(Embedded progressIcon, Button checkButton, Button startButton, Button stopButton, Button destroyButton) {
        progressIcon.setVisible(true);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        destroyButton.setEnabled(false);
        this.checkButton = checkButton;
    }

    @Override
    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
        if (Util.isFinalResponse(response)) {
            checkButton.click();
        }

        return null;
    }

}
