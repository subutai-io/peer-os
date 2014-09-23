/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.db.api;


import java.util.List;

import com.datastax.driver.core.ResultSet;


/**
 * Db Manager provides methods for working with database to persist and retrieve stored objects
 */
public interface DbManager
{

    /**
     * Executes a select query against db
     *
     * @param cql - sql query with placeholders for bind parameters in form of ?
     * @param values - bind parameters
     *
     * @return - resultset
     *
     * @deprecated - use executeQuery2 instead
     */
    @Deprecated
    public ResultSet executeQuery( String cql, Object... values );

    /**
     * Executes CUD (insert update delete) query against DB
     *
     * @param cql - sql query with placeholders for bind parameters in form of ?
     * @param values - bind parameters
     *
     * @return true if all went well and false if exception was raised
     *
     * @deprecated - use executeUpdate2 instead
     */
    @Deprecated
    public boolean executeUpdate( String cql, Object... values );

    /**
     * Saves POJO to DB
     *
     * @param source - source key
     * @param key - POJO key
     * @param info - custom object
     *
     * @return true if all went well and false if exception was raised
     *
     * @deprecated - use PluginDAO instead
     */
    @Deprecated
    public boolean saveInfo( String source, String key, Object info );

    /**
     * @deprecated - use DAO instead
     */
    @Deprecated
    public boolean saveEnvironmentInfo( String source, String key, Object info );

    /**
     * Returns POJO from DB
     *
     * @param source - source key
     * @param key - pojo key
     * @param clazz - class of POJO
     *
     * @return - POJO
     *
     * @deprecated - use Plugin DAO instead
     */
    @Deprecated
    public <T> T getInfo( String source, String key, Class<T> clazz );

    /**
     * @deprecated - use DAO instead
     */
    @Deprecated
    public <T> T getEnvironmentInfo( String source, String key, Class<T> clazz );

    /**
     * Returns all POJOs from DB identified by source key
     *
     * @param source - source key
     * @param clazz - class of POJO
     *
     * @return - list of POJOs
     *
     * @deprecated - use Plugin DAO instead
     */
    @Deprecated
    public <T> List<T> getInfo( String source, Class<T> clazz );

    /**
     * @deprecated - use DAO instead
     */
    @Deprecated
    public <T> List<T> getEnvironmentInfo( String source, Class<T> clazz );

    /**
     * deletes POJO from DB
     *
     * @param source - source key
     * @param key - POJO key
     *
     * @return true if all went well and false if exception was raised
     *
     * @deprecated - use Plugin DAO instead
     */
    @Deprecated
    public boolean deleteInfo( String source, String key );

    public ResultSet executeQuery2( String cql, Object... values ) throws DBException;

    public void executeUpdate2( String cql, Object... values ) throws DBException;

    /**
     * @deprecated - use Plugin DAO instead
     */
    @Deprecated
    public void saveInfo2( String source, String key, Object info ) throws DBException;

    /**
     * @deprecated - use Plugin DAO instead
     */
    @Deprecated
    public void deleteInfo2( String source, String key ) throws DBException;
}