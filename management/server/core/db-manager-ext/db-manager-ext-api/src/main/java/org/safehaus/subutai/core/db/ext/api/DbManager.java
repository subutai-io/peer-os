package org.safehaus.subutai.core.db.ext.api;


import java.util.List;


/**
 * Created by nurkaly on 9/29/14.
 */
public interface DbManager {


    /**
    * *******************************************************
    */
    public List getDataList(String entityName) throws Exception;

    /**
    * *******************************************************
    */
    public <T>Object  getData(Class<T> objClass,Object primaryKey) throws Exception;

    /**
     * *******************************************************
     */
    public void insertData(Object newObject) throws Exception;

    /**
     * *******************************************************
     */
    public void removeData(Object delObject) throws Exception;

    /**
     * *******************************************************
     */
    public void startUpdateMode() throws Exception;

    /**
     * *******************************************************
     */
    public void executeUpdate(String sqlScript) throws Exception;
    /**
    * *******************************************************
    */
    public List executeQuery(String sqlScript) throws Exception;

    /**
    * *******************************************************
    */
    public void commitData() throws Exception;

}
