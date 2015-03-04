package org.safehaus.subutai.core.jetty.fragment;


/**
 * Enumeration of Security Providers utilized by the crypto utility classes.
 */
public enum SecurityProvider
{
    /** Sun */
    SUN( "SUN" ),

    /** Bouncy Castle */
    BOUNCY_CASTLE( "BC" ),

    /** Apple */
    APPLE( "Apple" ),

    /** Microsoft CAPI */
    MS_CAPI( "SunMSCAPI" );

    private String jce;


    private SecurityProvider( String jce )
    {
        this.jce = jce;
    }


    /**
     * Get SecurityProvider type JCE name.
     *
     * @return JCE name
     */
    public String jce()
    {
        return jce;
    }
}
