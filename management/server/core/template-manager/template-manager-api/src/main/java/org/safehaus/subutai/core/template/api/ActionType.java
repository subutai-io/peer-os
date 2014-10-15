package org.safehaus.subutai.core.template.api;


public enum ActionType
{

    SETUP( "setup %s" ),
    CLONE( "clone %s %s -e %s &" ),
    DESTROY( "destroy %s" ),
    RENAME( "rename %s" ),
    EXPORT( "export %s" ),
    IMPORT( "import %s" ),
    PROMOTE( "promote %s" ),
    INSTALL( "apt-get --force-yes --assume-yes install %s", true ),
    // list commands
    LIST_TEMPLATES( "list -t %s" ),
    LIST_CONTAINERS( "list -c %s" ),
    LIST_CONT_TEMP( "list %s" ),
    // gets generated debian package name for template. TODO: find a better way
    GET_DEB_PACKAGE_NAME(
            ". /etc/subutai/config && . /usr/share/subutai-cli/subutai/lib/deb_ops && get_debian_package_name  %s",
            true ),
    GET_PACKAGE_NAME( ". /usr/share/subutai-cli/subutai/lib/deb_ops && get_package_name  %s", true ),
    ADD_SOURCE( "echo \"deb http://gw.intra.lan:9999/%1$s trusty main\" > /etc/apt/sources.list.d/%1$s.list ", true ),
    IS_PACKAGE_ACCESSIBLE( "apt-cache show " + "%1$s | grep \"Package: %1$s\"", true ),
    APT_GET_UPDATE( "apt-get update", true );

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


    public static String wrapInBash( String command )
    {
        return String.format( "bash -c '%s'", command );
    }


    public String buildCommand( String... args )
    {
        //        StringBuilder sb = new StringBuilder();
        //        if ( !standAloneCommand )
        //        {
        //            sb.append( PARENT_DIR );
        //        }
        //        sb.append( this.script );
        //        if ( args != null )
        //        {
        //            for ( String arg : args )
        //            {
        //                sb.append( " " ).append( arg );
        //            }
        //        }
        String s = String.format( this.script, args );
        return ( standAloneCommand ? "" : PARENT_DIR ) + s;
    }


}
