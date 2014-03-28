/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.dbmanager;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.ProductOperation;
import org.safehaus.kiskis.mgmt.shared.protocol.ProductOperationView;

/**
 *
 * @author dilshat
 */
public class DbManagerImpl implements DbManager {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOG = Logger.getLogger(DbManagerImpl.class.getName());
    private final Map<String, PreparedStatement> statements = new ConcurrentHashMap<String, PreparedStatement>();
//    private final DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
    private Cluster cluster;
    private Session session;
    private String cassandraHost;
    private String cassandraKeyspace;
    private int cassandraPort;

    public void setCassandraKeyspace(String cassandraKeyspace) {
        this.cassandraKeyspace = cassandraKeyspace;
    }

    public void setCassandraHost(String cassandraHost) {
        this.cassandraHost = cassandraHost;
    }

    public void setCassandraPort(int cassandraPort) {
        this.cassandraPort = cassandraPort;
    }

    public void init() {
        try {
            cluster = Cluster.builder().withPort(cassandraPort).addContactPoint(cassandraHost).build();
            session = cluster.connect(cassandraKeyspace);
            LOG.log(Level.INFO, "DbManager started");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in init", ex);
        }
    }

    public void destroy() {
        try {
            session.close();
        } catch (Exception e) {
        }
        try {
            cluster.close();
        } catch (Exception e) {
        }
        LOG.log(Level.INFO, "DbManager stopped");
    }

    public ResultSet executeQuery(String cql, Object... values) {
        try {
            PreparedStatement stmt = statements.get(cql);
            if (stmt == null) {
                stmt = session.prepare(cql);
                statements.put(cql, stmt);
            }
            BoundStatement boundStatement = new BoundStatement(stmt);
            if (values != null && values.length > 0) {
                boundStatement.bind(values);
            }
            return session.execute(boundStatement);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in executeQuery", ex);
        }
        return null;
    }

    public boolean executeUpdate(String cql, Object... values) {
        try {
            PreparedStatement stmt = statements.get(cql);
            if (stmt == null) {
                stmt = session.prepare(cql);
                statements.put(cql, stmt);
            }
            BoundStatement boundStatement = new BoundStatement(stmt);
            if (values != null && values.length > 0) {
                boundStatement.bind(values);
            }
            session.execute(boundStatement);
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in executeUpdate", ex);
        }
        return false;
    }

    public boolean saveInfo(String source, String key, Object info) {
        return executeUpdate("insert into product_info(source,key,info) values (?,?,?)",
                source,
                key,
                gson.toJson(info));
    }

    public <T> T getInfo(String source, String key, Class<T> clazz) {
        try {

            ResultSet rs = executeQuery("select info from product_info where source = ? and key = ?", source, key);
            Row row = rs.one();
            if (row != null) {

                String info = row.getString("info");
                return gson.fromJson(info, clazz);
            }
        } catch (JsonSyntaxException ex) {
            LOG.log(Level.SEVERE, "Error in T getInfo", ex);
        }
        return null;
    }

    public <T> List<T> getInfo(String source, Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        try {
            ResultSet rs = executeQuery("select info from product_info where source = ?", source);
            for (Row row : rs) {
                String info = row.getString("info");
                list.add(gson.fromJson(info, clazz));
            }
        } catch (JsonSyntaxException ex) {
            LOG.log(Level.SEVERE, "Error in List<T> getInfo", ex);
        }
        return list;
    }

    public boolean deleteInfo(String source, String key) {
        return executeUpdate("delete from product_info where source = ? and key = ?", source, key);
    }

    public ProductOperationView getProductOperation(String source, UUID operationTrackId) {
        try {
            ResultSet rs = executeQuery(
                    "select info from product_operation where source = ? and id = ?",
                    source,
                    operationTrackId);
            Row row = rs.one();
            if (row != null) {
                String info = row.getString("info");
                ProductOperationImpl po = gson.fromJson(info, ProductOperationImpl.class);
                if (po != null) {
                    ProductOperationViewImpl productOperationViewImpl = new ProductOperationViewImpl(po);
                    return productOperationViewImpl;
                }
            }
        } catch (JsonSyntaxException ex) {
            LOG.log(Level.SEVERE, "Error in getProductOperation", ex);
        }
        return null;
    }

    boolean saveProductOperation(String source, ProductOperationImpl po) {
        return executeUpdate(
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
            ResultSet rs = executeQuery(
                    "select info from product_operation where source = ?"
                    + " and id >= maxTimeuuid(?)"
                    + " and id <= minTimeuuid(?)"
                    + " order by id desc limit ?",
                    source,
                    fromDate,
                    toDate,
                    limit);
            for (Row row : rs) {
                String info = row.getString("info");
                ProductOperationImpl po = gson.fromJson(info, ProductOperationImpl.class);
                if (po != null) {
                    ProductOperationViewImpl productOperationViewImpl = new ProductOperationViewImpl(po);
                    list.add(productOperationViewImpl);
                }
            }
        } catch (JsonSyntaxException ex) {
            LOG.log(Level.SEVERE, "Error in getProductOperations", ex);
        }
        return list;
    }

    public List<String> getProductOperationSources() {
        List<String> sources = new ArrayList<String>();
        try {
            ResultSet rs = executeQuery(
                    "select distinct source from product_operation");
            for (Row row : rs) {
                String source = row.getString("source");
                sources.add(source);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getProductOperationSources", ex);
        }
        return sources;
    }

}
