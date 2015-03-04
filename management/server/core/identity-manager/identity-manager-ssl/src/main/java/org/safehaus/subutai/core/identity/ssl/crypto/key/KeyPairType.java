package org.safehaus.subutai.core.identity.ssl.crypto.key;


/**
 * Enumeration of Key Pair Types supported by the KeyPairUtil class.
 */
public enum KeyPairType
{
    RSA( "RSA", "1.2.840.113549.1.1.1", 512, 16384, 8 ), DSA( "DSA", "1.2.840.10040.4.1", 512, 1024, 64 ),
    EC( "EC", "1.2.840.10045.2.1", 160, 571, 32 );

    private String jce;
    private String oid;
    private int minSize;
    private int maxSize;
    private int stepSize;


    private KeyPairType( String jce, String oid, int minSize, int maxSize, int stepSize )
    {
        this.jce = jce;
        this.oid = oid;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.stepSize = stepSize;
    }


    /**
     * Get key pair type JCE name.
     *
     * @return JCE name
     */
    public String jce()
    {
        return jce;
    }


    /**
     * Get key pair type Object Identifier.
     *
     * @return Object Identifier
     */
    public String oid()
    {
        return oid;
    }


    /**
     * Get key pair minimum size.
     *
     * @return Minimum size
     */
    public int minSize()
    {
        return minSize;
    }


    /**
     * Get key pair maximum size.
     *
     * @return Maximum size
     */
    public int maxSize()
    {
        return maxSize;
    }


    /**
     * Get key pair step size.
     *
     * @return Step size
     */
    public int stepSize()
    {
        return stepSize;
    }


    /**
     * Resolve the supplied JCE name to a matching KeyPair type.
     *
     * @param jce JCE name
     *
     * @return KeyPair type or null if none
     */
    public static KeyPairType resolveJce( String jce )
    {
        for ( KeyPairType keyPairType : values() )
        {
            if ( jce.equals( keyPairType.jce() ) )
            {
                return keyPairType;
            }
        }

        return null;
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
