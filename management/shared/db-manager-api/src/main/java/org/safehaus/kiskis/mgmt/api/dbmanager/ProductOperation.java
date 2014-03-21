/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.dbmanager;

import java.util.UUID;
import org.doomdark.uuid.UUIDGenerator;

/**
 *
 * @author dilshat
 */
public class ProductOperation {

    private final UUID id;
    private final String description;
    private String log;
    private ProductOperationState state;

    public ProductOperation(String description) {
        this.description = description;
        state = ProductOperationState.RUNNING;
        id = java.util.UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public ProductOperationState getState() {
        return state;
    }

    public void setState(ProductOperationState state) {
        this.state = state;
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

}
