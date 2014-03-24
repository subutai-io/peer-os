/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.dbmanager;

import java.util.Date;
import java.util.UUID;

/**
 *
 * @author dilshat
 */
public interface ProductOperation {

    public String getDescription();

    public UUID getId();

    public String getLog();

    public Date createDate();

    public ProductOperationState getState();

    public void addLog(String logString);

    public void addLogDone(String logString);

    public void addLogFailed(String logString);
}
