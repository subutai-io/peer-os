package org.safehaus.subutai.core.packge.impl.info;


/**
 * Debian package selection states. Refer to dpkg man pages for more info.
 */
public enum SelectionState implements Abbreviation
{

    UNKNOWN( 'u' ),
    INSTALL( 'i' ),
    HOLD( 'h' ),
    DEINSTALL( 'r' ),
    PURGE( 'p' );

    private final char abbrev;


    private SelectionState( char abbrev )
    {
        this.abbrev = abbrev;
    }


    public static SelectionState getByAbbrev( char abbrev )
    {
        for ( SelectionState s : values() )
        {
            if ( s.abbrev == abbrev )
            {
                return s;
            }
        }
        return UNKNOWN;
    }


    @Override
    public char getAbbrev()
    {
        return abbrev;
    }
}
