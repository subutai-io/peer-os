/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.persistence;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.DbManager;

/**
 *
 * @author dilshat
 */
public class DbManagerImpl implements DbManager {

    private static final Logger LOG = Logger.getLogger(DbManagerImpl.class.getName());
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
            session.shutdown();
        } catch (Exception e) {
        }
        try {
            cluster.shutdown();
        } catch (Exception e) {
        }
        LOG.log(Level.INFO, "DbManager stopped");
    }

    public ResultSet executeQuery(String cql, Object... values) {
        try {
            PreparedStatement stmt = session.prepare(cql);
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
            PreparedStatement stmt = session.prepare(cql);
            BoundStatement boundStatement = new BoundStatement(stmt);
            if (values != null && values.length > 0) {
                boundStatement.bind(values);
            }
            session.execute(boundStatement);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in executeUpdate", ex);
        }
    }

    public void saveInfo(String source, String key, Serializable info) throws IOException {
        byte[] data = Util.serialize(info);
        executeUpdate("insert into product_info(id,source,key,info) values (?,?,?,?)",
                java.util.UUID.fromString(new com.eaio.uuid.UUID().toString()),
                source,
                key,
                ByteBuffer.wrap(data));
    }

    public <T> List<T> getInfo(String source, String key, Class<T> clazz) throws ClassNotFoundException, IOException {
        List<T> list = new ArrayList<T>();
        ResultSet rs = executeQuery("select info from product_info where source = ? and key = ? limit 100 allow filtering", source, key);
        for (Row row : rs) {
            ByteBuffer info = row.getBytes("info");
            byte[] result = new byte[info.remaining()];
            info.get(result);

            list.add(clazz.cast(Util.deserialize(clazz, result)));
        }
        return list;
    }

    public <T> List<T> getInfo(String source, Class<T> clazz) throws ClassNotFoundException, IOException {
        List<T> list = new ArrayList<T>();
        ResultSet rs = executeQuery("select info from product_info where source = ? limit 100 allow filtering", source);
        for (Row row : rs) {
            ByteBuffer info = row.getBytes("info");
            byte[] result = new byte[info.remaining()];
            info.get(result);

            list.add(clazz.cast(Util.deserialize(clazz, result)));
        }
        return list;
    }

}
