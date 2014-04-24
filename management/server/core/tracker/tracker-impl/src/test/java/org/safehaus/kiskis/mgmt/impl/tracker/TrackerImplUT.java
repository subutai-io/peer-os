/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.tracker;

import java.util.Date;
import java.util.UUID;
import org.junit.Test;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;

/**
 *
 * @author dilshat
 */
public class TrackerImplUT {

    private final UUID poID = UUID.randomUUID();
    private final String SOURCE = "source";
    private final String DESCRIPTION = "description";

    @Test
    public void shouldCallDbManagerExecuteUpdateWhenCreatePO() {
        DbManager dbManager = mock(DbManager.class);
        TrackerImpl ti = new TrackerImpl();
        ti.setDbManager(dbManager);

        ti.createProductOperation(SOURCE, DESCRIPTION);

        verify(dbManager).executeUpdate(any(String.class), anyVararg());
    }

    @Test
    public void shouldCallDbManagerExecuteUpdateWhenSavePO() {
        DbManager dbManager = mock(DbManager.class);
        TrackerImpl ti = new TrackerImpl();
        ti.setDbManager(dbManager);
        ProductOperationImpl poi = new ProductOperationImpl(SOURCE, DESCRIPTION, ti);

        ti.saveProductOperation(SOURCE, poi);

        verify(dbManager).executeUpdate(any(String.class), anyVararg());
    }

    @Test
    public void shouldCallDbManagerExecuteQueryWhenGetPO() {
        DbManager dbManager = mock(DbManager.class);
        TrackerImpl ti = new TrackerImpl();
        ti.setDbManager(dbManager);

        ti.getProductOperation(SOURCE, poID);

        verify(dbManager).executeQuery(any(String.class), anyVararg());
    }

    @Test
    public void shouldCallDbManagerExecuteQueryWhenGetPOs() {
        DbManager dbManager = mock(DbManager.class);
        TrackerImpl ti = new TrackerImpl();
        ti.setDbManager(dbManager);

        ti.getProductOperations(SOURCE, mock(Date.class), mock(Date.class), 1);

        verify(dbManager).executeQuery(any(String.class), anyVararg());
    }

    @Test
    public void shouldCallDbManagerExecuteQueryWhenGetPOSources() {
        DbManager dbManager = mock(DbManager.class);
        TrackerImpl ti = new TrackerImpl();
        ti.setDbManager(dbManager);

        ti.getProductOperationSources();

        verify(dbManager).executeQuery(any(String.class), anyVararg());
    }
}
