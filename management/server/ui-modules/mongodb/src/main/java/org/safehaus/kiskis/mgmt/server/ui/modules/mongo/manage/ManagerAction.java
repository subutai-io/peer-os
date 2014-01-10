/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import com.vaadin.data.Item;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Constants;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
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
    private final Agent agent;
    private final NodeType nodeType;
    private final StringBuilder output = new StringBuilder();
    private int responseCount = 0;

    public ManagerAction(Task task, ManagerActionType managerActionType, Item row, Agent agent, NodeType nodeType) {
        this.task = task;
        this.row = row;
        this.agent = agent;
        this.nodeType = nodeType;
        this.managerActionType = managerActionType;
        showProgress();
    }

    public Task getTask() {
        return task;
    }

    public void incrementResponseCount() {
        responseCount++;
    }

    public int getResponseCount() {
        return responseCount;
    }

    private <T> T getItemPropertyValue(Object itemPropertyId) {
        return (T) row.getItemProperty(itemPropertyId).getValue();
    }

    public void disableButtons() {
        Button startBtn = getItemPropertyValue(Constants.TABLE_START_PROPERTY);
        Button stopBtn = getItemPropertyValue(Constants.TABLE_STOP_PROPERTY);
        Button destroyBtn = getItemPropertyValue(Constants.TABLE_DESTROY_PROPERTY);
        startBtn.setEnabled(false);
        stopBtn.setEnabled(false);
        destroyBtn.setEnabled(false);
    }

    public void enableStartButton() {
        Button startBtn = getItemPropertyValue(Constants.TABLE_START_PROPERTY);
        Button stopBtn = getItemPropertyValue(Constants.TABLE_STOP_PROPERTY);
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
    }

    public void enableDestroyButton() {
        Button destroyBtn = getItemPropertyValue(Constants.TABLE_DESTROY_PROPERTY);
        destroyBtn.setEnabled(true);
    }

    public void enableStopButton() {
        Button startBtn = getItemPropertyValue(Constants.TABLE_START_PROPERTY);
        Button stopBtn = getItemPropertyValue(Constants.TABLE_STOP_PROPERTY);
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
    }

    public void hideProgress() {
        row.getItemProperty(Constants.TABLE_STATUS_PROPERTY).setValue(null);
    }

    public void showProgress() {
        if (row.getItemProperty(Constants.TABLE_STATUS_PROPERTY).getValue() == null) {
            row.getItemProperty(Constants.TABLE_STATUS_PROPERTY).setValue(progressIcon);
        }
    }

    public Item getRow() {
        return row;
    }

    public Agent getAgent() {
        return agent;
    }

    public NodeType getNodeType() {
        return nodeType;
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
