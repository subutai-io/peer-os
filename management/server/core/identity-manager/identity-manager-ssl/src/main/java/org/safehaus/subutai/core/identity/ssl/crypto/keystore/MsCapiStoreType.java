package org.safehaus.subutai.core.identity.ssl.crypto.keystore;


/**
 * Enumeration of MS CAPI Certificate Store Types supported by the KeyStoreUtil class.
 */
public enum MsCapiStoreType
{
    PERSONAL( "Windows-MY" ), ROOT( "Windows-ROOT" );

    private String jce;


    private MsCapiStoreType( String jce )
    {
        this.jce = jce;
    }


    /**
     * Get MsCapiStoreType type JCE name.
     *
     * @return JCE name
     */
    public String jce()
    {
        return jce;
    }
}
