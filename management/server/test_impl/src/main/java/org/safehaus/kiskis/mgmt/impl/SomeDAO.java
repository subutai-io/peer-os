/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;

/**
 *
 * @author bahadyr
 */
public class SomeDAO {

    private static final Logger LOG = Logger.getLogger(SomeDAO.class.getName());

    private static final DbManager dbManager;

    static {
        dbManager = ServiceLocator.getService(DbManager.class);
    }

    public void writeLog(String log) {
        saveLog(log);
    }

    public void saveLog(String log) {
        String cql = "insert into logs (id, log) values (?,?)";
        dbManager.executeUpdate(cql, System.currentTimeMillis() + "", log);
    }

    public List<String> getLogs() {
        List<String> list = new ArrayList<String>();
        String cql = "select * from logs";
        ResultSet results = dbManager.executeQuery(cql);
        for (Row row : results) {
            String data = row.getString("log");
            list.add(data);
        }
        return list;
    }

}
