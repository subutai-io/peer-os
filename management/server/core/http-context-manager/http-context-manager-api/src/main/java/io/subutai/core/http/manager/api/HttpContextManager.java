package io.subutai.core.http.manager.api;


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