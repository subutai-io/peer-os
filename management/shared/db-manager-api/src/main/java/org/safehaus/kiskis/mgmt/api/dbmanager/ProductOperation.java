/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.dbmanager;

import java.util.UUID;
import org.doomdark.uuid.UUIDGenerator;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class ProductOperation {

    private final UUID id;
    private final String description;
    private final DbManager dbManager;
    private final StringBuilder log;
    private ProductOperationState state;

    public ProductOperation(String description, DbManager dbManager) {
        this.description = description;
        this.dbManager = dbManager;
        log = new StringBuilder();
        state = ProductOperationState.RUNNING;
        id = java.util.UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
        dbManager.saveProductOperation(this);
    }

    public String getLog() {
        return log.toString();
    }

    public void addLog(String logString) {
        if (!Util.isStringEmpty(logString)) {

            if (log.length() > 0) {
                log.append("\n");
            }
            log.append(logString);
            dbManager.saveProductOperation(this);
        }
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
