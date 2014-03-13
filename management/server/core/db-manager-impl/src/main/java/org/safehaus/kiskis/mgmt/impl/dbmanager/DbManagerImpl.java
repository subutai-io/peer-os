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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;

/**
 *
 * @author dilshat
 */
public class DbManagerImpl implements DbManager {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOG = Logger.getLogger(DbManagerImpl.class.getName());
    private Cluster cluster;
    private Session session;
    private String cassandraHost;
    private String cassandraKeyspace;
    private int cassandraPort;
    private final Map<String, PreparedStatement> statements = new ConcurrentHashMap<String, PreparedStatement>();

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
            return null;
        }
    }

    public void executeUpdate(String cql, Object... values) {
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
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in executeUpdate", ex);
        }
    }

    public void saveInfo(String source, String key, Object info) {
        executeUpdate("insert into product_info(source,key,info) values (?,?,?)",
                source,
                key,
                gson.toJson(info));
    }

    public <T> T getInfo(String source, String key, Class<T> clazz) {
        ResultSet rs = executeQuery("select info from product_info where source = ? and key = ?", source, key);
        Row row = rs.one();
        if (row != null) {

            String info = row.getString("info");
            return gson.fromJson(info, clazz);
        }
        return null;
    }

    public <T> List<T> getInfo(String source, Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        ResultSet rs = executeQuery("select info from product_info where source = ?", source);
        for (Row row : rs) {
            String info = row.getString("info");
            list.add(gson.fromJson(info, clazz));
        }
        return list;
    }

    public void deleteInfo(String source, String key) {
        executeUpdate("delete from product_info where source = ? and key = ?", source, key);
    }

    public String executeCqlQuery(String cql) {
        try {
            session.execute(cql);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in executeQuery", ex);
            return ex.getMessage();
        }
        return "Query executed successfully";
    }

    public List<String> getLogs() {
        List<String> list = new ArrayList<String>();
        String cql = "select * from logs";
        ResultSet results = executeQuery(cql);
        for (Row row : results) {
            String data = row.getString("log");
            list.add(data);
        }
        return list;
    }

}
