package org.safehaus.kiskis.mgmt.impl.solr.util;


import java.util.List;

import com.datastax.driver.core.ResultSet;

import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;


public class DbManagerMock implements DbManager {
    @Override
    public ResultSet executeQuery( String cql, Object... values ) {
        return null;
    }


    @Override
    public boolean executeUpdate( String cql, Object... values ) {
        return false;
    }


    @Override
    public boolean saveInfo( String source, String key, Object info ) {
        return true;
    }


    @Override
    public <T> T getInfo( String source, String key, Class<T> clazz ) {
        return null;
    }


    @Override
    public <T> List<T> getInfo( String source, Class<T> clazz ) {
        return null;
    }


    @Override
    public boolean deleteInfo( String source, String key ) {
        return false;
    }
}
