/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.agentmanager;

import com.datastax.driver.core.ResultSet;
import java.util.List;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;

/**
 *
 * @author dilshat
 */
public class DbManagerMock implements DbManager {

    public ResultSet executeQuery(String cql, Object... values) {
        return null;
    }

    public boolean executeUpdate(String cql, Object... values) {
        return true;
    }

    public boolean saveInfo(String source, String key, Object info) {
        return true;
    }

    public <T> T getInfo(String source, String key, Class<T> clazz) {
        return null;
    }

    public <T> List<T> getInfo(String source, Class<T> clazz) {
        return null;
    }

    public boolean deleteInfo(String source, String key) {
        return true;
    }

}
