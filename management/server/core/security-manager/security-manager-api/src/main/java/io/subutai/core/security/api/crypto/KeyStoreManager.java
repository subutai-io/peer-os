package io.subutai.core.security.api.crypto;


/**
 * Manages JKS keystore
 */
public interface KeyStoreManager
{
    /* *****************************
     *
     */
    public void importCertAsTrusted(int port, String storeAlias, String certificateHEX);


    /* *****************************
     *
     */
    public String exportCertificate(int port,String storeAlias);


    /* *****************************
    *
    */
    public void removeCertFromTrusted(int port, String storeAlias);


}
