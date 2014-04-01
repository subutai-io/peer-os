/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.tracker;

import java.util.Date;
import java.util.UUID;

/**
 *
 * @author dilshat
 */
public interface ProductOperationView {

    public String getDescription();

    public Date getCreateDate();

    public UUID getId();

    public String getLog();

    public ProductOperationState getState();
}
