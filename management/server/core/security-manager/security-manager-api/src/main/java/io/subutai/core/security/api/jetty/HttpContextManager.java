package io.subutai.core.security.api.jetty;


public interface HttpContextManager
{
    /* *******************************************
    *
    */
    public void reloadKeyStore();


    /* *******************************************
     *
     */
    public void reloadTrustStore();


    /* *******************************************
     *
     */
    public Object getSSLContext();
}