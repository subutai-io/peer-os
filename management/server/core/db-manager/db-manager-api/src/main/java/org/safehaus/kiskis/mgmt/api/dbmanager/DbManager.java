/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.dbmanager;

import org.safehaus.kiskis.mgmt.shared.protocol.ProductOperationView;
import com.datastax.driver.core.ResultSet;
import java.util.List;
import java.util.UUID;

/**
 * ы
 *
 * @author dilshat
 */
public interface DbManager {

    public ResultSet executeQuery(String cql, Object... values);

    public boolean executeUpdate(String cql, Object... values);

    public boolean saveInfo(String source, String key, Object info);

    public <T> T getInfo(String source, String key, Class<T> clazz);

    public <T> List<T> getInfo(String source, Class<T> clazz);

    public boolean deleteInfo(String source, String key);

    public ProductOperationView getProductOperation(String source, UUID operationTrackId);

    public ProductOperation createProductOperation(String source, String description);

    public List<ProductOperationView> getProductOperations(String source);

}
