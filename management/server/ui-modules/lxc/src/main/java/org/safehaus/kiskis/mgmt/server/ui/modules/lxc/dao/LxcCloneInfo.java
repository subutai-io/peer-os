/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.dao;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author dilshat
 */
public class LxcCloneInfo {

    public static final String TABLE_NAME = "lxc_info",
            TASK_UUID_NAME = "task_uuid",
            DATE_IN_NAME = "date_in",
            PHYSICAL_HOSTS_NAME = "physical_hosts",
            STATUS_NAME = "status";
    private final UUID taskUUID;
    private final List<String> physicalHosts;
    private Date dateIn;
    private LxcCloneStatus cloneStatus;

    public LxcCloneInfo(UUID taskUUID, List<String> physicalHosts) {
        this.taskUUID = taskUUID;
        this.physicalHosts = physicalHosts;
    }

    public LxcCloneInfo(UUID taskUUID, List<String> physicalHosts, Date dateIn, LxcCloneStatus cloneStatus) {
        this.taskUUID = taskUUID;
        this.physicalHosts = physicalHosts;
        this.dateIn = dateIn;
        this.cloneStatus = cloneStatus;
    }

    public UUID getTaskUUID() {
        return taskUUID;
    }

    public List<String> getPhysicalHosts() {
        return physicalHosts;
    }

    public Date getDateIn() {
        return dateIn;
    }

    public LxcCloneStatus getCloneStatus() {
        return cloneStatus;
    }

    public void setCloneStatus(LxcCloneStatus cloneStatus) {
        this.cloneStatus = cloneStatus;
    }

}
