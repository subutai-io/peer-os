/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.dbmanager;

import com.datastax.driver.core.ResultSet;
import java.util.List;

/**
 * ы
 *
 * @author dilshat
 */
public interface DbManager {

    public void setCassandraKeyspace(String cassandraKeyspace);

    public void setCassandraHost(String cassandraHost);

    public void setCassandraPort(int cassandraPort);

    public ResultSet executeQuery(String cql, Object... values);

    public void executeUpdate(String cql, Object... values);

    public void saveInfo(String source, String key, Object info);

    public <T> T getInfo(String source, String key, Class<T> clazz);

    public <T> List<T> getInfo(String source, Class<T> clazz);

    public void deleteInfo(String source, String key);
    
    public void init();
    
    public void truncate(String table);
}
