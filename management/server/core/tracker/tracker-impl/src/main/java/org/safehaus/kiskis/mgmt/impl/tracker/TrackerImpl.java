/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.tracker;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationView;

/**
 *
 * @author dilshat
 */
public class TrackerImpl implements Tracker {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOG = Logger.getLogger(TrackerImpl.class.getName());
    private DbManager dbManager;

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public ProductOperationView getProductOperation(String source, UUID operationTrackId) {
        try {
            ResultSet rs = dbManager.executeQuery(
                    "select info from product_operation where source = ? and id = ?",
                    source,
                    operationTrackId);
            if (rs != null) {
                Row row = rs.one();
                if (row != null) {
                    String info = row.getString("info");
                    ProductOperationImpl po = gson.fromJson(info, ProductOperationImpl.class);
                    if (po != null) {
                        ProductOperationViewImpl productOperationViewImpl = new ProductOperationViewImpl(po);
                        return productOperationViewImpl;
                    }
                }
            }
        } catch (JsonSyntaxException ex) {
            LOG.log(Level.SEVERE, "Error in getProductOperation", ex);
        }
        return null;
    }

    boolean saveProductOperation(String source, ProductOperationImpl po) {
        return dbManager.executeUpdate(
                "insert into product_operation(source,id,info) values(?,?,?)",
                source, po.getId(), gson.toJson(po));
    }

    public ProductOperation createProductOperation(String source, String description) {
        ProductOperationImpl po = new ProductOperationImpl(source, description, this);
        if (saveProductOperation(source, po)) {
            return po;
        }
        return null;
    }

    public List<ProductOperationView> getProductOperations(String source, Date fromDate, Date toDate, int limit) {
        List<ProductOperationView> list = new ArrayList<ProductOperationView>();
        try {
            ResultSet rs = dbManager.executeQuery(
                    "select info from product_operation where source = ?"
                    + " and id >= maxTimeuuid(?)"
                    + " and id <= minTimeuuid(?)"
                    + " order by id desc limit ?",
                    source,
                    fromDate,
                    toDate,
                    limit);
            if (rs != null) {
                for (Row row : rs) {
                    String info = row.getString("info");
                    ProductOperationImpl po = gson.fromJson(info, ProductOperationImpl.class);
                    if (po != null) {
                        ProductOperationViewImpl productOperationViewImpl = new ProductOperationViewImpl(po);
                        list.add(productOperationViewImpl);
                    }
                }
            }
        } catch (JsonSyntaxException ex) {
            LOG.log(Level.SEVERE, "Error in getProductOperations", ex);
        }
        return list;
    }

    public List<String> getProductOperationSources() {
        List<String> sources = new ArrayList<String>();
        ResultSet rs = dbManager.executeQuery(
                "select distinct source from product_operation");
        if (rs != null) {
            for (Row row : rs) {
                String source = row.getString("source");
                sources.add(source);
            }
        }
        return sources;
    }

}
