package io.subutai.core.security.api.jetty;


public interface HttpContextManager
{
    /* *******************************************
    *
    */
    public void reloadKeyStore();


    /* *******************************************
     * todo delete this
     */
    @Deprecated
    public void reloadTrustStore();


    /* *******************************************
     *  todo delete this
     */
    @Deprecated
    public Object getSSLContext();
}