/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol.api;

import com.datastax.driver.core.ResultSet;

/**
 *
 * @author dilshat
 */
public interface DbManager {

    public ResultSet executeQuery(String cql, Object... values);

    public void executeUpdate(String cql, Object... values);
}
