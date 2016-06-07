package io.subutai.core.security.api.crypto;


/**
 * Manages JKS keystore
 */
public interface KeyStoreManager
{
    /* *****************************
     *
     */
    void importCertAsTrusted( int port, String storeAlias, String certificateHEX );


    /* *****************************
     *
     */
    String exportCertificate( int port, String storeAlias );


    /* *****************************
    *
    */
    void removeCertFromTrusted( int port, String storeAlias );
}
