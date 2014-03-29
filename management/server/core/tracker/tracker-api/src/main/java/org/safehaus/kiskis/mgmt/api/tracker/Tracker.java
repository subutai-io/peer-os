/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.tracker;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author dilshat
 */
public interface Tracker {

    public ProductOperationView getProductOperation(String source, UUID operationTrackId);

    public ProductOperation createProductOperation(String source, String description);

    public List<ProductOperationView> getProductOperations(String source, Date fromDate, Date toDate, int limit);

    public List<String> getProductOperationSources();

}
