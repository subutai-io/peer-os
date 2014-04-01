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
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationView;

/**
 *
 * @author dilshat
 */
public class ProductOperationViewImpl implements ProductOperationView {

    private final UUID id;
    private final String description;
    private final String log;
    private final ProductOperationState state;
    private final Date createDate;

    public ProductOperationViewImpl(ProductOperation po) {
        id = po.getId();
        description = po.getDescription();
        log = po.getLog();
        state = po.getState();
        createDate = po.createDate();
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getLog() {
        return log;
    }

    public ProductOperationState getState() {
        return state;
    }

    @Override
    public int hashCode() {
        int hash = 3;
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
        final ProductOperationViewImpl other = (ProductOperationViewImpl) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    public Date getCreateDate() {
        return createDate;
    }

}
