/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author bahadyr
 */
public class SomeDAO {

    private static final Logger LOG = Logger.getLogger(SomeDAO.class.getName());
    DbManager dbManager;

    public SomeDAO(DbManager newDbManager) {
        dbManager = newDbManager;
    }

//    static {
//        dbManager = ServiceLocator.getService(DbManager.class);
//    }
    public void writeLog(String log) {
        saveLog(log);
    }

    public void saveLog(String log) {
        String cql = "insert into logs (id, log) values (?,?)";
        try {
            dbManager.executeUpdate(cql, System.currentTimeMillis() + "", log);
        } catch (Exception e) {
            System.out.println("can not write to cassandra " + e.getMessage());
        }
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
