package io.subutai.core.hubmanager.api;


import io.subutai.core.hubmanager.api.exception.HubManagerException;


public interface RestClient
{
    String CONNECTION_EXCEPTION_MARKER = "ConnectException";

    <T> RestResult<T> get( String url, Class<T> clazz );

    <T> RestResult<T> getPlain( String url, Class<T> clazz );

    <T> T getStrict( String url, Class<T> clazz ) throws HubManagerException;

    <T> RestResult<T> post( String url, Object body, Class<T> clazz );

    RestResult<Object> post( String url, Object body );

    RestResult<Object> postPlain( String url, Object body );

    <T> RestResult<T> put( String url, Object body, Class<T> clazz );

    RestResult<Object> delete( String url );
}
