package org.safehaus.subutai.core.template.impl;


enum ActionType
{

    SETUP( "setup" ),
    CLONE( "clone" ),
    DESTROY( "destroy" ),
    RENAME( "rename" ),
    EXPORT( "export" ),
    IMPORT( "import" ),
    PROMOTE( "promote" ),
    INSTALL( "apt-get --force-yes --assume-yes install", true ),
    // list commands
    LIST_TEMPLATES( "list -t" ),
    LIST_CONTAINERS( "list -c" ),
    LIST_CONT_TEMP( "list" ),
    // gets generated debian package name for template. TODO: find a better way
    GET_DEB_PACKAGE_NAME(
            ". /etc/subutai/config && . /usr/share/subutai-cli/subutai/lib/deb_ops && get_debian_package_name", true ),
    GET_PACKAGE_NAME( ". /usr/share/subutai-cli/subutai/lib/deb_ops && get_package_name", true );

    private static final String PARENT_DIR = "/usr/bin/subutai ";
    private final String script;
    private boolean standAloneCommand = false;


    private ActionType( String script )
    {
        this.script = script;
    }


    private ActionType( String script, boolean standAlone )
    {
        this.script = script;
        this.standAloneCommand = standAlone;
    }


    static String wrapInBash( String command )
    {
        return String.format( "bash -c '%s'", command );
    }


    String buildCommand( String... args )
    {
        StringBuilder sb = new StringBuilder();
        if ( !standAloneCommand )
        {
            sb.append( PARENT_DIR );
        }
        sb.append( this.script );
        if ( args != null )
        {
            for ( String arg : args )
            {
                sb.append( " " ).append( arg );
            }
        }
        return sb.toString();
    }

}
