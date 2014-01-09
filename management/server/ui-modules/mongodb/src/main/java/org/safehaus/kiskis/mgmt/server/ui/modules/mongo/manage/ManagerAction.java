/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import com.vaadin.data.Item;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Embedded;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Constants;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public final class ManagerAction {

    private final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
    private final Task task;
    private final Item row;
    private final ManagerActionType managerActionType;
    private final StringBuilder output = new StringBuilder();

    public ManagerAction(Task task, ManagerActionType managerActionType, Item row) {
        this.task = task;
        this.row = row;
        this.managerActionType = managerActionType;
        showProgress();
    }

    public Task getTask() {
        return task;
    }

    public <T> T getItemPropertyValue(Object itemPropertyId) {
        return (T) row.getItemProperty(itemPropertyId).getValue();
    }

    public void hideProgress() {
        row.getItemProperty(Constants.TABLE_STATUS_PROPERTY).setValue(null);
    }

    public void showProgress() {
        row.getItemProperty(Constants.TABLE_STATUS_PROPERTY).setValue(progressIcon);
    }

    public ManagerActionType getManagerActionType() {
        return managerActionType;
    }

    public void addOutput(String out) {
        if (!Util.isStringEmpty(out)) {
            output.append(out);
        }
    }

    public String getOutput() {
        return output.toString();
    }

}
