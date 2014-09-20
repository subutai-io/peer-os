package org.safehaus.subutai.core.packge.impl.info;


/**
 * Debian package states. Refer to dpkg man pages for more info.
 */
public enum PackageState implements Abbreviation
{

    NOT_INSTALLED( 'n' ),
    CONFIG_FILES( 'c' ),
    HALF_INSTALLED( 'H' ),
    UNPACKED( 'U' ),
    HALF_CONFIGURED( 'F' ),
    TRIGGERS_AWATED( 'W' ),
    TRIGGERS_PENDING( 't' ),
    INSTALLED( 'i' );

    private final char abbrev;


    private PackageState( char abbrev )
    {
        this.abbrev = abbrev;
    }


    public static PackageState getByAbbrev( char abbrev )
    {
        for ( PackageState s : values() )
        {
            if ( s.abbrev == abbrev )
            {
                return s;
            }
        }
        return NOT_INSTALLED;
    }


    @Override
    public char getAbbrev()
    {
        return abbrev;
    }
}
