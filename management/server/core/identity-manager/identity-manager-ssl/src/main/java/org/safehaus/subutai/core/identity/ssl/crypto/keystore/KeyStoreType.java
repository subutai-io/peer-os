package org.safehaus.subutai.core.identity.ssl.crypto.keystore;


import org.safehaus.subutai.core.identity.ssl.crypto.file.CryptoFileType;

import static org.safehaus.subutai.core.identity.ssl.crypto.file.CryptoFileType.BKS_KS;
import static org.safehaus.subutai.core.identity.ssl.crypto.file.CryptoFileType.BKS_V1_KS;
import static org.safehaus.subutai.core.identity.ssl.crypto.file.CryptoFileType.JCEKS_KS;
import static org.safehaus.subutai.core.identity.ssl.crypto.file.CryptoFileType.JKS_KS;
import static org.safehaus.subutai.core.identity.ssl.crypto.file.CryptoFileType.PKCS12_KS;
import static org.safehaus.subutai.core.identity.ssl.crypto.file.CryptoFileType.UBER_KS;


/**
 * Enumeration of KeyStore Types supported by the KeyStoreUtil class.
 */
public enum KeyStoreType
{

    JKS( "JKS", true, JKS_KS ),
    JCEKS( "JCEKS", true, JCEKS_KS ),
    PKCS12( "PKCS12", true, PKCS12_KS ),
    BKS_V1( "BKS-V1", true, BKS_V1_KS ),
    BKS( "BKS", true, BKS_KS ),
    UBER( "UBER", true, UBER_KS ),
    KEYCHAIN( "KeychainStore", false, null ),
    MS_CAPI_PERSONAL( "Windows-MY", false, null ),
    MS_CAPI_ROOT( "Windows-ROOT", false, null ),
    PKCS11( "PKCS11", false, null );


    private String jce;
    private boolean fileBased;
    private CryptoFileType cryptoFileType;


    private KeyStoreType( String jce, boolean fileBased, CryptoFileType cryptoFileType )
    {
        this.jce = jce;
        this.fileBased = fileBased;
        this.cryptoFileType = cryptoFileType;
    }


    /**
     * Get KeyStore type JCE name.
     *
     * @return JCE name
     */
    public String jce()
    {
        return jce;
    }


    /**
     * Is KeyStore type file based?
     *
     * @return True if it is, false otherwise
     */
    public boolean isFileBased()
    {
        return fileBased;
    }


    /**
     * Are key store entries password protected?
     *
     * @return True if it has, false otherwise
     */
    public boolean hasEntryPasswords()
    {
        return this != PKCS11 && this != PKCS12 && this != MS_CAPI_PERSONAL;
    }


    /**
     * Does this KeyStore type support secret key entries?
     *
     * @return True, if secret key entries are supported by this KeyStore type
     */
    public boolean supportsKeyEntries()
    {
        return this == JCEKS || this == BKS || this == UBER;
    }


    /**
     * Resolve the supplied JCE name to a matching KeyStore type.
     *
     * @param jce JCE name
     *
     * @return KeyStore type or null if none
     */
    public static KeyStoreType resolveJce( String jce )
    {
        for ( KeyStoreType keyStoreType : values() )
        {
            if ( jce.equals( keyStoreType.jce() ) )
            {
                return keyStoreType;
            }
        }

        return null;
    }


    /**
     * Get crypto file type.
     *
     * @return Crypto file type or null if KeyStore type is not file based
     */
    public CryptoFileType getCryptoFileType()
    {
        return cryptoFileType;
    }


    /**
     * Returns JCE name.
     *
     * @return JCE name
     */
    public String toString()
    {
        return jce();
    }
}
