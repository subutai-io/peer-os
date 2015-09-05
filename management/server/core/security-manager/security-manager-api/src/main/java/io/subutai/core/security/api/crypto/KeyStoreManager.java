package io.subutai.core.security.api.crypto;


/**
 * Manages JKS keystore
 */
public interface KeyStoreManager
{
    /* *****************************
     *
     */
    public void importCertAsTrusted(String port, String storeAlias, String certificateHEX);


    /* *****************************
     *
     */
    public String exportCertificate(String port,String storeAlias);


    /* *****************************
    *
    */
    public void removeCertFromTrusted(String port, String storeAlias);


}
