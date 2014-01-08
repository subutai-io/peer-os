/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import com.vaadin.data.Item;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class ManagerAction {

    private final Task task;
//    private final Table table;
//    private final Object tableRowId;
    private final Item row;
//    private final Button startButton;
//    private final Button stopButton;
//    private final Button destroyButton;
    private final ManagerActionType managerActionType;
    private final StringBuilder output = new StringBuilder();

//    public ManagerAction(Task task, Button startButton, Button stopButton, Button destroyButton, ManagerActionType managerActionType) {
    public ManagerAction(Task task, ManagerActionType managerActionType, Item row) {
        this.task = task;
//        this.table = table;
//        this.tableRowId = tableRowId;
        this.row = row;
//        this.startButton = startButton;
//        this.stopButton = stopButton;
//        this.destroyButton = destroyButton;
        this.managerActionType = managerActionType;
    }

    public Task getTask() {
        return task;
    }

//    public Table getTable() {
//        return table;
//    }
//
//    public Object getTableRowId() {
//        return tableRowId;
//    }
    public <T> T getItemPropertyValue(Object itemPropertyId) {
        return (T) row.getItemProperty(itemPropertyId).getValue();
    }

//    public Button getStartButton() {
//        return startButton;
//    }
//
//    public Button getStopButton() {
//        return stopButton;
//    }
//
//    public Button getDestroyButton() {
//        return destroyButton;
//    }
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
