/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.tracker;

import java.util.Date;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationState;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class ProductOperationImpl implements ProductOperation {

    private final UUID id;
    private final String description;
    private final transient TrackerImpl dbManager;
    private final StringBuilder log;
    private final Date createDate;
    private ProductOperationState state;
    private final String source;

    public ProductOperationImpl(String source, String description, TrackerImpl dbManager) {
        this.description = description;
        this.source = source;
        this.dbManager = dbManager;
        log = new StringBuilder();
        state = ProductOperationState.RUNNING;
        id = UUID.fromString(new com.eaio.uuid.UUID().toString());
//        id = java.util.UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
        createDate = new Date();
    }

    public String getLog() {
        return log.toString();
    }

    public void addLog(String logString) {
        addLog(logString, state);
    }

    public void addLogDone(String logString) {
        addLog(logString, ProductOperationState.SUCCEEDED);
    }

    public void addLogFailed(String logString) {
        addLog(logString, ProductOperationState.FAILED);
    }

    private void addLog(String logString, ProductOperationState state) {
        if (!Util.isStringEmpty(logString)) {

            if (log.length() > 0) {
                log.append("\n");
            }
            log.append(logString);
        }
        this.state = state;
        dbManager.saveProductOperation(source, this);
    }

    public ProductOperationState getState() {
        return state;
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProductOperationImpl other = (ProductOperationImpl) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    public Date createDate() {
        return createDate;
    }

}
