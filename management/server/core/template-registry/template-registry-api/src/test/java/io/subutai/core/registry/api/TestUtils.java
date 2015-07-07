package io.subutai.core.registry.api;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

import io.subutai.common.datatypes.TemplateVersion;
import io.subutai.common.protocol.Template;
import io.subutai.common.settings.Common;


/**
 * Test utils
 */
public class TestUtils
{


    public static final String CONFIG_FILE =
            "lxc.include = /usr/share/lxc/config/ubuntu.common.conf\n" + "\n" + "# Container specific configuration\n"
                    + "lxc.rootfs = /var/lib/lxc/master/rootfs\n" + "lxc.mount = /var/lib/lxc/master/fstab\n"
                    + "lxc.utsname = master\n" + "lxc.arch = amd64\n" + "\n" + "# Network configuration\n"
                    + "lxc.network.type = veth\n" + "lxc.network.flags = up\n" + "lxc.network.link = br0\n"
                    + "lxc.network.hwaddr = 00:16:3e:aa:bd:80\n" + "subutai.config.path = /etc\n"
                    + "lxc.hook.pre-start = /usr/bin/pre_start_hook\n" + "subutai.parent = master\n"
                    + "SUBUTAI_VERSION = 2.3\n"
                    + "subutai.git.branch = master\n" + "subutai.git.uuid = 76cc7a05286c25889fb611661b63d45896cf13af\n"
                    + "lxc.mount.entry = /lxc/master-opt opt none bind,rw 0 0\n"
                    + "lxc.mount.entry = /lxc-data/master-home home none bind,rw 0 0\n"
                    + "lxc.mount.entry = /lxc-data/master-var var none bind,rw 0 0";

    public static final String PACKAGES_MANIFEST =
            "ii  kmod                             15-0ubuntu6                   " +
                    "amd64        tools for managing Linux kernel modules\n"
                    + "ii  subutai-logstash                    1.0.1                         amd64        This is a " +
                    "logstash "
                    +
                    "package of subutai distribution.";

    public static final String MD_5_SUM = "ec6c39f0aed6b6a1256321d2e927a392";
    public static final String TEMPLATE_NAME = "master";
    public static final String SUBUTAI_PARENT = "master";
    public static final String UTS_NAME = "master";
    public static final String LXC_ARCH = "amd64";
    public static final String CFG_PATH = "/etc";
    public static final String GIT_BRANCH = "master";
    public static final String GIT_UUID = "76cc7a05286c25889fb611661b63d45896cf13af";


    public static Template getTemplateFromConfigFiles( String configFile, String packagesFile, String md5sum )
            throws IOException
    {
        Properties properties = new Properties();

        properties.load( new ByteArrayInputStream( configFile.getBytes( Charset.defaultCharset() ) ) );
        String lxcUtsname = properties.getProperty( "lxc.utsname" );
        String lxcArch = properties.getProperty( "lxc.arch" );
        String subutaiConfigPath = properties.getProperty( "subutai.config.path" );
        String subutaiParent = properties.getProperty( "subutai.parent" );
        String subutaiGitBranch = properties.getProperty( "subutai.git.branch" );
        String subutaiGitUuid = properties.getProperty( "subutai.git.uuid" );

        return new Template( lxcArch, lxcUtsname, subutaiConfigPath, subutaiParent, subutaiGitBranch, subutaiGitUuid,
                packagesFile, md5sum, new TemplateVersion( Common.DEFAULT_TEMPLATE_VERSION ) );
    }


    public static Template getDefaultTemplate() throws IOException
    {
        return getTemplateFromConfigFiles( CONFIG_FILE, PACKAGES_MANIFEST, MD_5_SUM );
    }
}
