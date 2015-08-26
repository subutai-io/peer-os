package io.subutai.core.http.manager.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.subutai.core.http.manager.api.HttpContextManager;

public class HttpContextManagerImpl implements HttpContextManager
{

    private static final Logger LOG = LoggerFactory.getLogger( HttpContextManagerImpl.class );


    public HttpContextManagerImpl()
    {
    }

    /* *******************************************
     *
     */
    @Override
    public void reloadKeyStore()
    {
    }


    /* *******************************************
     *
     */
    @Override
    public void reloadTrustStore()
    {
    }


    /* *******************************************
     *
     */
    @Override
    public Object getSSLContext()
    {
        return null;
    }
}
