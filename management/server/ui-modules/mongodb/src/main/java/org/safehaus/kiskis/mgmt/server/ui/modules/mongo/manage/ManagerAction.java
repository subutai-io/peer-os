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
    private final Item row;
//    private final Object rowId;
    private final ManagerActionType managerActionType;
    private final StringBuilder output = new StringBuilder();

    public ManagerAction(Task task, ManagerActionType managerActionType, Item row) {
        this.task = task;
        this.row = row;
//        this.rowId = rowId;
        this.managerActionType = managerActionType;
    }

    public Task getTask() {
        return task;
    }

//    public Object getRowId() {
//        return rowId;
//    }

    public <T> T getItemPropertyValue(Object itemPropertyId) {
        return (T) row.getItemProperty(itemPropertyId).getValue();
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
