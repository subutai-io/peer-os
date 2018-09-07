package io.subutai.core.bazaarmanager.api;


import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;


public interface RestClient
{
    String CONNECTION_EXCEPTION_MARKER = "ConnectException";

    <T> RestResult<T> get( String url, Class<T> clazz );

    <T> RestResult<T> getPlain( String url, Class<T> clazz );

    <T> T getStrict( String url, Class<T> clazz ) throws BazaarManagerException;

    <T> RestResult<T> post( String url, Object body, Class<T> clazz );

    RestResult<Object> post( String url, Object body );

    RestResult<Object> postPlain( String url, Object body );

    <T> RestResult<T> put( String url, Object body, Class<T> clazz );

    RestResult<Object> delete( String url );
}
