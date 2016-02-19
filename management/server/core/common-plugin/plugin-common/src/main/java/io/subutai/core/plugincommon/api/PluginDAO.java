package io.subutai.core.plugincommon.api;


import java.util.List;


/**
 *
 */
public interface PluginDAO
{
    /* *******************************************************************
     *
     */
    public boolean saveInfo( String source, String key, Object info );


    /* *******************************************************************
     *
     */
    public boolean saveInfo( String source, String key, String info );


    /**
     * Returns all POJOs from DB identified by source key
     *
     * @param source - source key
     * @param clazz - class of POJO
     *
     * @return - list of POJOs
     */
    public <T> List<T> getInfo( String source, Class<T> clazz );


    /**
     * Returns POJO from DB
     *
     * @param source - source key
     * @param key - pojo key
     * @param clazz - class of POJO
     *
     * @return - POJO
     */
    public <T> T getInfo( String source, String key, Class<T> clazz );


    /**
     * Returns all POJOs from DB identified by source key
     *
     * @param source - source key
     *
     * @return - list of Json String
     */
    public List<String> getInfo( String source );


    /**
     * Returns POJO from DB
     *
     * @param source - source key
     * @param key - pojo key
     *
     * @return - POJO
     */
    public String getInfo( String source, String key );


    /**
     * deletes POJO from DB
     *
     * @param source - source key
     * @param key - POJO key
     */
    public boolean deleteInfo( String source, String key );

}
