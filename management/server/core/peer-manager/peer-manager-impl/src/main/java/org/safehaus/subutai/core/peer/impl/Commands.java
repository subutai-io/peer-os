package org.safehaus.subutai.core.peer.impl;


public class Commands
{
    // ManagementHost commands
    public static String getAddAptSourceCommand( String hostname, String ip )
    {
        return String.format( "sed '/^path_map.*$/ s/$/ ; %s %s/' apt-cacher.conf > apt-cacher.conf"
                        + ".new && mv apt-cacher.conf.new apt-cacher.conf && /etc/init.d/apt-cacher reload", hostname,
                ( "http://" + ip + "/ksks" ).replace( ".", "\\." ).replace( "/", "\\/" ) );
    }


    public static String getRemoveAptSourceCommand( String hostname, String ip )
    {
        return String.format( "sed -e 's,;\\s*[a-f0-9]\\{8\\}-[a-f0-9]\\{4\\}-[a-f0-9]\\{4\\}-[a-f0-9]\\{4" +
                        "\\}-[a-f0-9]\\{12\\}\\s*http:\\/\\/%s/ksks\\s*,,g' apt-cacher.conf > apt-cacher.conf"
                        + ".new && mv apt-cacher.conf.new apt-cacher.conf && /etc/init.d/apt-cacher reload",
                ip.replace( ".", "\\." ) );
    }
}
